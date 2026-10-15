package com.themanoftalent.aleclip.service

import android.content.Context
import com.themanoftalent.aleclip.model.ClipItem
import com.themanoftalent.aleclip.model.ClipType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

class StorageService private constructor(private val context: Context) {

    private val _activeClips = MutableStateFlow<List<ClipItem>>(emptyList())
    val activeClips: StateFlow<List<ClipItem>> = _activeClips.asStateFlow()

    private val _deletedClips = MutableStateFlow<List<ClipItem>>(emptyList())
    val deletedClips: StateFlow<List<ClipItem>> = _deletedClips.asStateFlow()

    private val _maxClips = MutableStateFlow(200)
    val maxClips: StateFlow<Int> = _maxClips.asStateFlow()

    private val undoStack = mutableListOf<Pair<ClipItem, Int>>()
    private val dataFile = File(context.filesDir, "clips.json")

    init {
        loadData()
    }

    @Synchronized
    fun addClip(type: ClipType, content: String, isPinned: Boolean = false): ClipItem {
        val trimmed = content.trim()
        val current = _activeClips.value.toMutableList()

        // Check duplicate with top clip
        val existingIndex = current.indexOfFirst { it.content == trimmed && it.type == type }
        if (existingIndex >= 0) {
            val existing = current.removeAt(existingIndex)
            existing.timestamp = System.currentTimeMillis()
            if (isPinned) existing.isPinned = true
            current.add(0, existing)
            _activeClips.value = current
            saveData()
            return existing
        }

        val newItem = ClipItem(
            id = UUID.randomUUID().toString(),
            type = type,
            content = trimmed,
            timestamp = System.currentTimeMillis(),
            isPinned = isPinned,
            isDeleted = false
        )
        current.add(0, newItem)

        // Prune if exceeding limit
        if (current.size > _maxClips.value) {
            val unpinned = current.filter { !it.isPinned }
            if (unpinned.isNotEmpty()) {
                val toRemove = unpinned.last()
                current.remove(toRemove)
            }
        }

        _activeClips.value = current
        saveData()
        return newItem
    }

    @Synchronized
    fun updateClip(item: ClipItem) {
        val current = _activeClips.value.toMutableList()
        val index = current.indexOfFirst { it.id == item.id }
        if (index >= 0) {
            current[index] = item
            _activeClips.value = current
            saveData()
        }
    }

    @Synchronized
    fun togglePin(id: String) {
        val current = _activeClips.value.toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index >= 0) {
            val item = current[index]
            item.isPinned = !item.isPinned
            // Move pinned to top if pinned
            current.removeAt(index)
            if (item.isPinned) {
                current.add(0, item)
            } else {
                current.add(index, item)
            }
            _activeClips.value = current
            saveData()
        }
    }

    @Synchronized
    fun deleteClip(id: String): ClipItem? {
        val current = _activeClips.value.toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index >= 0) {
            val removed = current.removeAt(index)
            removed.isDeleted = true
            removed.deletedAt = System.currentTimeMillis()

            undoStack.add(Pair(removed, index))
            if (undoStack.size > 30) undoStack.removeAt(0)

            val trash = _deletedClips.value.toMutableList()
            trash.add(0, removed)

            _activeClips.value = current
            _deletedClips.value = trash
            saveData()
            return removed
        }
        return null
    }

    @Synchronized
    fun undoLastDelete(): ClipItem? {
        if (undoStack.isEmpty()) return null
        val (clip, originalIndex) = undoStack.removeAt(undoStack.lastIndex)
        clip.isDeleted = false
        clip.deletedAt = null

        val trash = _deletedClips.value.toMutableList()
        trash.removeAll { it.id == clip.id }
        _deletedClips.value = trash

        val current = _activeClips.value.toMutableList()
        current.removeAll { it.id == clip.id }
        val targetIndex = originalIndex.coerceIn(0, current.size)
        current.add(targetIndex, clip)
        _activeClips.value = current

        saveData()
        return clip
    }

    @Synchronized
    fun restoreClip(id: String): ClipItem? {
        val trash = _deletedClips.value.toMutableList()
        val index = trash.indexOfFirst { it.id == id }
        if (index >= 0) {
            val item = trash.removeAt(index)
            item.isDeleted = false
            item.deletedAt = null

            val current = _activeClips.value.toMutableList()
            current.removeAll { it.id == item.id }
            current.add(0, item)

            _deletedClips.value = trash
            _activeClips.value = current
            saveData()
            return item
        }
        return null
    }

    @Synchronized
    fun permanentlyDeleteClip(id: String): ClipItem? {
        val trash = _deletedClips.value.toMutableList()
        val index = trash.indexOfFirst { it.id == id }
        if (index >= 0) {
            val removed = trash.removeAt(index)
            undoStack.removeAll { it.first.id == id }
            _deletedClips.value = trash
            saveData()
            return removed
        }
        return null
    }

    @Synchronized
    fun emptyTrash() {
        undoStack.clear()
        _deletedClips.value = emptyList()
        saveData()
    }

    @Synchronized
    fun setMaxClips(limit: Int) {
        _maxClips.value = limit
        saveData()
    }

    // MARK: - JSON Persistence

    private fun saveData() {
        try {
            val root = JSONObject()
            root.put("maxClips", _maxClips.value)

            val activeArr = JSONArray()
            for (c in _activeClips.value) {
                activeArr.put(clipToJson(c))
            }
            root.put("active", activeArr)

            val deletedArr = JSONArray()
            for (c in _deletedClips.value) {
                deletedArr.put(clipToJson(c))
            }
            root.put("deleted", deletedArr)

            dataFile.writeText(root.toString(2))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadData() {
        if (!dataFile.exists()) return
        try {
            val root = JSONObject(dataFile.readText())
            _maxClips.value = root.optInt("maxClips", 200)

            val activeList = mutableListOf<ClipItem>()
            val activeArr = root.optJSONArray("active")
            if (activeArr != null) {
                for (i in 0 until activeArr.length()) {
                    jsonToClip(activeArr.getJSONObject(i))?.let { activeList.add(it) }
                }
            }
            _activeClips.value = activeList

            val deletedList = mutableListOf<ClipItem>()
            val deletedArr = root.optJSONArray("deleted")
            if (deletedArr != null) {
                for (i in 0 until deletedArr.length()) {
                    jsonToClip(deletedArr.getJSONObject(i))?.let { deletedList.add(it) }
                }
            }
            _deletedClips.value = deletedList
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun clipToJson(c: ClipItem): JSONObject {
        val o = JSONObject()
        o.put("id", c.id)
        o.put("type", c.type.idName)
        o.put("content", c.content)
        o.put("timestamp", c.timestamp)
        o.put("isPinned", c.isPinned)
        o.put("isDeleted", c.isDeleted)
        if (c.deletedAt != null) o.put("deletedAt", c.deletedAt)
        return o
    }

    private fun jsonToClip(o: JSONObject): ClipItem? {
        val content = o.optString("content", "")
        if (content.isEmpty()) return null
        return ClipItem(
            id = o.optString("id", UUID.randomUUID().toString()),
            type = ClipType.fromString(o.optString("type", "text")),
            content = content,
            timestamp = o.optLong("timestamp", System.currentTimeMillis()),
            isPinned = o.optBoolean("isPinned", false),
            isDeleted = o.optBoolean("isDeleted", false),
            deletedAt = if (o.has("deletedAt")) o.optLong("deletedAt") else null
        )
    }

    fun exportBackupJson(): String {
        val arr = JSONArray()
        for (c in _activeClips.value) {
            arr.put(clipToJson(c))
        }
        return arr.toString(2)
    }

    fun importBackupJson(jsonString: String): Boolean {
        return try {
            val arr = JSONArray(jsonString)
            val current = _activeClips.value.toMutableList()
            val existingContents = current.map { it.content }.toSet()

            var added = 0
            for (i in 0 until arr.length()) {
                val clip = jsonToClip(arr.getJSONObject(i))
                if (clip != null && !existingContents.contains(clip.content)) {
                    current.add(clip)
                    added++
                }
            }
            _activeClips.value = current
            saveData()
            true
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: StorageService? = null

        fun getInstance(context: Context): StorageService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: StorageService(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
