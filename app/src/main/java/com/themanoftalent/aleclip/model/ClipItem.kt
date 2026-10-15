package com.themanoftalent.aleclip.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class ClipItem(
    val id: String = UUID.randomUUID().toString(),
    var type: ClipType = ClipType.TEXT,
    var content: String = "",
    var imagePath: String? = null,
    var timestamp: Long = System.currentTimeMillis(),
    var isPinned: Boolean = false,
    var isDeleted: Boolean = false,
    var deletedAt: Long? = null
) {
    fun matches(query: String): Boolean {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return true
        return content.contains(trimmed, ignoreCase = true) ||
                type.displayName.contains(trimmed, ignoreCase = true)
    }

    fun formattedTime(): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        return when {
            diff < 60_000 -> "Just now"
            diff < 3600_000 -> "${diff / 60_000}m ago"
            diff < 86400_000 -> "${diff / 3600_000}h ago"
            else -> {
                val sdf = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
                sdf.format(Date(timestamp))
            }
        }
    }
}
