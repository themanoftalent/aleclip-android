package com.themanoftalent.aleclip.service

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.themanoftalent.aleclip.model.ClipType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

/**
 * Syncs clips with AleClip for Mac/iPhone through Google Drive: "My Drive/AleClip/.sync".
 *
 * Android's Google Drive app does not let other apps open a Drive folder, so this uses the
 * Drive REST API with Google Sign-In (AuthorizationClient). It requires an Android OAuth
 * client in Google Cloud Console for this package name + signing SHA-1 (see README).
 *
 * Protocol (same as Mac/iOS): .sync/sync_payload_<device>.json =
 *   { version, deviceName, timestamp, collections, clips, deletedHashes }
 */
object DriveSyncService {

    private const val DRIVE_SCOPE = "https://www.googleapis.com/auth/drive"
    private const val API = "https://www.googleapis.com/drive/v3"
    private const val UPLOAD = "https://www.googleapis.com/upload/drive/v3"
    private const val FOLDER_MIME = "application/vnd.google-apps.folder"
    private const val PREFS = "aleclip_sync"

    sealed class State {
        data object NotConnected : State()
        data object Syncing : State()
        data class Synced(val at: Long, val message: String) : State()
        data class Error(val message: String) : State()
    }

    private val _state = MutableStateFlow<State>(State.NotConnected)
    val state: StateFlow<State> = _state.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var debounceJob: Job? = null

    fun isEnabled(context: Context) = prefs(context).getBoolean("enabled", false)

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /** Hook storage changes to a debounced sync; restore the last status. */
    fun init(context: Context) {
        val app = context.applicationContext
        if (isEnabled(app)) {
            val last = prefs(app).getLong("lastSync", 0L)
            _state.value = if (last > 0) State.Synced(last, "Connected") else State.Synced(0, "Connected")
        }
        StorageService.getInstance(app).onLocalChange = { scheduleSync(app) }
    }

    private fun authRequest() = AuthorizationRequest.builder()
        .setRequestedScopes(listOf(Scope(DRIVE_SCOPE)))
        .build()

    /** Interactive connect: shows Google's consent screen if needed. */
    fun connect(activity: Activity, launcher: ActivityResultLauncher<IntentSenderRequest>) {
        Identity.getAuthorizationClient(activity)
            .authorize(authRequest())
            .addOnSuccessListener { result ->
                val pending = result.pendingIntent
                if (result.hasResolution() && pending != null) {
                    launcher.launch(IntentSenderRequest.Builder(pending.intentSender).build())
                } else {
                    onAuthorized(activity.applicationContext, result)
                }
            }
            .addOnFailureListener { e ->
                _state.value = State.Error(friendlyAuthError(e))
            }
    }

    /** Result of the consent screen launched by [connect]. */
    fun handleAuthResult(context: Context, data: Intent?) {
        try {
            val result = Identity.getAuthorizationClient(context).getAuthorizationResultFromIntent(data)
            onAuthorized(context.applicationContext, result)
        } catch (e: Exception) {
            _state.value = State.Error(friendlyAuthError(e))
        }
    }

    fun disconnect(context: Context) {
        prefs(context).edit().putBoolean("enabled", false).apply()
        _state.value = State.NotConnected
    }

    private fun onAuthorized(context: Context, result: AuthorizationResult) {
        val token = result.accessToken
        if (token == null) {
            _state.value = State.Error("Google didn't return Drive access. Try again.")
            return
        }
        prefs(context).edit().putBoolean("enabled", true).apply()
        scope.launch { runSync(context, token) }
    }

    fun scheduleSync(context: Context, delayMs: Long = 3_000) {
        if (!isEnabled(context)) return
        debounceJob?.cancel()
        debounceJob = scope.launch {
            delay(delayMs)
            syncNow(context)
        }
    }

    /** Silent sync: gets a token without UI (works once the user has connected). */
    fun syncNow(context: Context) {
        if (!isEnabled(context)) return
        val app = context.applicationContext
        Identity.getAuthorizationClient(app)
            .authorize(authRequest())
            .addOnSuccessListener { result ->
                val token = result.accessToken
                if (result.hasResolution() || token == null) {
                    _state.value = State.Error("Google access expired. Tap Connect Google Drive again.")
                } else {
                    scope.launch { runSync(app, token) }
                }
            }
            .addOnFailureListener { e -> _state.value = State.Error(friendlyAuthError(e)) }
    }

    private fun friendlyAuthError(e: Exception): String {
        val msg = e.message ?: e.javaClass.simpleName
        return if (msg.contains("10:") || msg.contains("DEVELOPER_ERROR")) {
            "Google Drive sync isn't configured for this build yet (OAuth client missing). See README › Google Drive sync."
        } else {
            "Couldn't connect to Google Drive: $msg"
        }
    }

    // MARK: - Sync run

    @Volatile private var running = false

    private suspend fun runSync(context: Context, token: String) {
        if (running) return
        running = true
        _state.value = State.Syncing
        try {
            val storage = StorageService.getInstance(context)
            val drive = DriveApi(token)

            val aleClipId = drive.findOrCreateFolder("AleClip", "root")
            val syncId = drive.findOrCreateFolder(".sync", aleClipId)
            val ownName = "sync_payload_${deviceLabel(context)}.json"

            // 1. Merge every other device's payload.
            var added = 0
            var removed = 0
            var devices = 0
            for ((fileId, name) in drive.listPayloads(syncId)) {
                if (name == ownName) continue
                val payload = runCatching { JSONObject(drive.download(fileId)) }.getOrNull() ?: continue
                devices++
                val (a, r) = storage.applyRemote(parseClips(payload), parseTombstones(payload))
                added += a
                removed += r
            }

            // 2. Upload this device's payload.
            val (clips, tombstones) = storage.snapshotForSync()
            val body = buildPayload(clips, tombstones).toString(2)
            drive.upsertFile(syncId, ownName, body)

            val now = System.currentTimeMillis()
            prefs(context).edit().putLong("lastSync", now).apply()
            val parts = mutableListOf("Synced with $devices other device${if (devices == 1) "" else "s"}")
            if (added > 0) parts += "$added new"
            if (removed > 0) parts += "$removed removed"
            _state.value = State.Synced(now, parts.joinToString(" · "))
        } catch (e: Exception) {
            _state.value = State.Error("Sync failed: ${e.message ?: e.javaClass.simpleName}")
        } finally {
            running = false
        }
    }

    private fun deviceLabel(context: Context): String {
        val p = prefs(context)
        val existing = p.getString("deviceSuffix", null)
        val suffix = existing ?: UUID.randomUUID().toString().take(4).uppercase().also {
            p.edit().putString("deviceSuffix", it).apply()
        }
        val model = (Build.MODEL ?: "Android").replace(Regex("[^A-Za-z0-9]+"), "")
        return "Android-$model-$suffix"
    }

    // MARK: - Payload JSON (matches the Mac's Codable format; ISO-8601 dates without fractions)

    private fun isoFormat() = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    private fun parseIso(value: String?): Long? {
        if (value.isNullOrBlank()) return null
        val cleaned = value.replace(Regex("\\.\\d+"), "") // drop fractional seconds if any
        return runCatching { isoFormat().parse(cleaned)?.time }.getOrNull()
    }

    private fun mapType(raw: String?): ClipType = when (raw) {
        "link" -> ClipType.LINK
        "color" -> ClipType.COLOR
        "file" -> ClipType.FILE
        "image" -> ClipType.IMAGE
        else -> ClipType.TEXT // text, richText, html, code
    }

    private fun parseClips(payload: JSONObject): List<RemoteClip> {
        val arr = payload.optJSONArray("clips") ?: return emptyList()
        val out = ArrayList<RemoteClip>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            val type = mapType(o.optString("type", "text"))
            val content = o.optString("plainText", "")
            if (content.isBlank() || type == ClipType.IMAGE) continue
            val created = parseIso(o.optString("createdAt")) ?: System.currentTimeMillis()
            val lastUsed = parseIso(o.optString("lastUsedAt")) ?: created
            out += RemoteClip(
                id = o.optString("id", UUID.randomUUID().toString()),
                type = type,
                content = content,
                createdAt = created,
                lastUsedAt = lastUsed,
                isPinned = o.optBoolean("isPinned", false),
                contentHash = o.optString("contentHash").ifBlank { aleClipContentHash(type, content) }
            )
        }
        return out
    }

    private fun parseTombstones(payload: JSONObject): Map<String, Long> {
        val obj = payload.optJSONObject("deletedHashes") ?: return emptyMap()
        val out = HashMap<String, Long>()
        for (key in obj.keys()) parseIso(obj.optString(key))?.let { out[key] = it }
        return out
    }

    private fun buildPayload(clips: List<com.themanoftalent.aleclip.model.ClipItem>, tombstones: Map<String, Long>): JSONObject {
        val fmt = isoFormat()
        val clipArr = JSONArray()
        for (c in clips) {
            val macType = when (c.type) {
                ClipType.TEXT -> "text"
                ClipType.LINK -> "link"
                ClipType.COLOR -> "color"
                ClipType.FILE -> "file"
                ClipType.IMAGE -> "image"
            }
            val o = JSONObject()
            o.put("id", c.id.uppercase())
            o.put("type", macType)
            o.put("plainText", c.content)
            if (c.type == ClipType.LINK) o.put("url", c.content)
            if (c.type == ClipType.COLOR) o.put("colorHex", c.content)
            o.put("createdAt", fmt.format(Date(c.timestamp)))
            o.put("lastUsedAt", fmt.format(Date(c.timestamp)))
            o.put("isPinned", c.isPinned)
            o.put("contentHash", aleClipContentHash(c.type, c.content))
            o.put("isSensitive", false)
            o.put("sourceDevice", "Android")
            clipArr.put(o)
        }
        val tomb = JSONObject()
        for ((hash, at) in tombstones) tomb.put(hash, fmt.format(Date(at)))
        return JSONObject()
            .put("version", "2.2.0")
            .put("deviceName", "Android")
            .put("timestamp", fmt.format(Date()))
            .put("collections", JSONArray())
            .put("clips", clipArr)
            .put("deletedHashes", tomb)
    }

    // MARK: - Minimal Drive v3 REST client

    private class DriveApi(private val token: String) {

        private fun q(s: String) = URLEncoder.encode(s, "UTF-8")

        private fun request(method: String, url: String, body: ByteArray? = null, contentType: String? = null): String {
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.requestMethod = if (method == "PATCH") "POST" else method
            if (method == "PATCH") conn.setRequestProperty("X-HTTP-Method-Override", "PATCH")
            conn.setRequestProperty("Authorization", "Bearer $token")
            conn.connectTimeout = 15_000
            conn.readTimeout = 60_000
            if (body != null) {
                conn.doOutput = true
                contentType?.let { conn.setRequestProperty("Content-Type", it) }
                conn.outputStream.use { it.write(body) }
            }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() } ?: ""
            conn.disconnect()
            if (code !in 200..299) {
                val reason = runCatching { JSONObject(text).getJSONObject("error").getString("message") }.getOrNull()
                throw IllegalStateException("Drive $code${reason?.let { ": $it" } ?: ""}")
            }
            return text
        }

        fun findOrCreateFolder(name: String, parentId: String): String {
            val query = "name = '$name' and mimeType = '$FOLDER_MIME' and '$parentId' in parents and trashed = false"
            val res = JSONObject(request("GET", "$API/files?q=${q(query)}&fields=files(id,name)&spaces=drive"))
            val files = res.optJSONArray("files")
            if (files != null && files.length() > 0) return files.getJSONObject(0).getString("id")
            val meta = JSONObject().put("name", name).put("mimeType", FOLDER_MIME).put("parents", JSONArray().put(parentId))
            val created = JSONObject(request("POST", "$API/files?fields=id", meta.toString().toByteArray(), "application/json; charset=UTF-8"))
            return created.getString("id")
        }

        /** (fileId, name) of every sync payload in the .sync folder. */
        fun listPayloads(syncId: String): List<Pair<String, String>> {
            val query = "'$syncId' in parents and name contains 'sync_payload_' and trashed = false"
            val res = JSONObject(request("GET", "$API/files?q=${q(query)}&fields=files(id,name)&pageSize=100"))
            val files = res.optJSONArray("files") ?: return emptyList()
            return (0 until files.length()).map {
                val f = files.getJSONObject(it)
                f.getString("id") to f.getString("name")
            }
        }

        fun download(fileId: String): String = request("GET", "$API/files/$fileId?alt=media")

        fun upsertFile(parentId: String, name: String, content: String) {
            val query = "name = '$name' and '$parentId' in parents and trashed = false"
            val res = JSONObject(request("GET", "$API/files?q=${q(query)}&fields=files(id)"))
            val files = res.optJSONArray("files")
            val bytes = content.toByteArray(Charsets.UTF_8)
            if (files != null && files.length() > 0) {
                val id = files.getJSONObject(0).getString("id")
                request("PATCH", "$UPLOAD/files/$id?uploadType=media", bytes, "application/json")
            } else {
                val boundary = "aleclip${System.currentTimeMillis()}"
                val meta = JSONObject().put("name", name).put("parents", JSONArray().put(parentId)).put("mimeType", "application/json")
                val multipart = buildString {
                    append("--$boundary\r\nContent-Type: application/json; charset=UTF-8\r\n\r\n")
                    append(meta.toString()).append("\r\n")
                    append("--$boundary\r\nContent-Type: application/json\r\n\r\n")
                    append(content).append("\r\n--$boundary--\r\n")
                }
                request("POST", "$UPLOAD/files?uploadType=multipart", multipart.toByteArray(Charsets.UTF_8), "multipart/related; boundary=$boundary")
            }
        }
    }
}
