package com.themanoftalent.aleclip.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.themanoftalent.aleclip.model.ClipItem
import com.themanoftalent.aleclip.model.ClipType
import com.themanoftalent.aleclip.service.ClipboardService
import com.themanoftalent.aleclip.service.StorageService
import kotlinx.coroutines.flow.*

enum class FilterTab(val label: String) {
    ALL("All"),
    PINNED("Pinned"),
    TEXT("Text"),
    LINKS("Links"),
    IMAGES("Images"),
    FILES("Files"),
    COLORS("Colors"),
    TRASH("Trash")
}

class ClipViewModel(application: Application) : AndroidViewModel(application) {

    private val storage = StorageService.getInstance(application)

    val searchQuery = MutableStateFlow("")
    val selectedTab = MutableStateFlow(FilterTab.ALL)

    val activeClips = storage.activeClips
    val deletedClips = storage.deletedClips
    val maxClips = storage.maxClips

    val filteredClips: StateFlow<List<ClipItem>> = combine(
        storage.activeClips,
        storage.deletedClips,
        searchQuery,
        selectedTab
    ) { active, deleted, query, tab ->
        val source = if (tab == FilterTab.TRASH) deleted else active
        source.filter { item ->
            // Search query filter
            if (query.isNotBlank() && !item.matches(query)) {
                return@filter false
            }

            // Tab filter
            when (tab) {
                FilterTab.ALL, FilterTab.TRASH -> true
                FilterTab.PINNED -> item.isPinned
                FilterTab.TEXT -> item.type == ClipType.TEXT
                FilterTab.LINKS -> item.type == ClipType.LINK
                FilterTab.IMAGES -> item.type == ClipType.IMAGE
                FilterTab.FILES -> item.type == ClipType.FILE
                FilterTab.COLORS -> item.type == ClipType.COLOR
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun copyClip(item: ClipItem) {
        ClipboardService.copyToClipboard(getApplication(), item)
    }

    fun pasteFromClipboard(): ClipItem? {
        return ClipboardService.ingestClipboard(getApplication())
    }

    fun togglePin(id: String) {
        storage.togglePin(id)
    }

    fun deleteClip(id: String): ClipItem? {
        return storage.deleteClip(id)
    }

    fun undoDelete(): ClipItem? {
        return storage.undoLastDelete()
    }

    fun restoreClip(id: String): ClipItem? {
        return storage.restoreClip(id)
    }

    fun permanentlyDeleteClip(id: String): ClipItem? {
        return storage.permanentlyDeleteClip(id)
    }

    fun emptyTrash() {
        storage.emptyTrash()
    }

    fun addClip(type: ClipType, content: String, isPinned: Boolean): ClipItem {
        return storage.addClip(type, content, isPinned)
    }

    fun updateClip(item: ClipItem) {
        storage.updateClip(item)
    }

    fun setMaxClips(limit: Int) {
        storage.setMaxClips(limit)
    }

    fun exportBackup(): String {
        return storage.exportBackupJson()
    }

    fun importBackup(json: String): Boolean {
        return storage.importBackupJson(json)
    }
}
