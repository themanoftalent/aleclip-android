package com.themanoftalent.aleclip.service

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.themanoftalent.aleclip.model.ClipItem
import com.themanoftalent.aleclip.model.ClipType

object ClipboardService {

    private var lastCopiedContent: String? = null

    fun ingestClipboard(context: Context): ClipItem? {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return null
        if (!cm.hasPrimaryClip()) return null

        val primaryClip = cm.primaryClip ?: return null
        if (primaryClip.itemCount == 0) return null

        val text = primaryClip.getItemAt(0)?.text?.toString()?.trim() ?: return null
        if (text.isEmpty() || text == lastCopiedContent) return null

        val storage = StorageService.getInstance(context)
        val firstClip = storage.activeClips.value.firstOrNull()
        if (firstClip?.content == text) return null

        val type = detectType(text)
        return storage.addClip(type = type, content = text)
    }

    fun copyToClipboard(context: Context, item: ClipItem) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
        lastCopiedContent = item.content
        val clip = ClipData.newPlainText("AleClip", item.content)
        cm.setPrimaryClip(clip)
    }

    private fun detectType(text: String): ClipType {
        val lower = text.lowercase()
        return when {
            lower.startsWith("http://") || lower.startsWith("https://") -> ClipType.LINK
            text.startsWith("#") && (text.length == 7 || text.length == 9) -> ClipType.COLOR
            else -> ClipType.TEXT
        }
    }
}
