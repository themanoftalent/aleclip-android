package com.themanoftalent.aleclip.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.themanoftalent.aleclip.model.ClipItem
import com.themanoftalent.aleclip.ui.components.ClipRow
import com.themanoftalent.aleclip.ui.components.WatermarkLogo
import com.themanoftalent.aleclip.ui.theme.SpectrumColors
import com.themanoftalent.aleclip.ui.viewmodel.ClipViewModel
import com.themanoftalent.aleclip.ui.viewmodel.FilterTab
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: ClipViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val filteredClips by viewModel.filteredClips.collectAsState()
    val activeClips by viewModel.activeClips.collectAsState()
    val deletedClips by viewModel.deletedClips.collectAsState()
    val maxClips by viewModel.maxClips.collectAsState()

    var selectedClipForDetail by remember { mutableStateOf<ClipItem?>(null) }
    var showNewClipSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { WatermarkLogo(compact = true) },
                actions = {
                    // Paste Button
                    FilledTonalButton(
                        onClick = {
                            val pasted = viewModel.pasteFromClipboard()
                            coroutineScope.launch {
                                if (pasted != null) {
                                    snackbarHostState.showSnackbar("Pasted from clipboard")
                                } else {
                                    snackbarHostState.showSnackbar("Clipboard is empty or duplicate")
                                }
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Paste", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Settings Button
                    IconButton(onClick = { showSettingsSheet = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedTab != FilterTab.TRASH) {
                FloatingActionButton(
                    onClick = { showNewClipSheet = true },
                    containerColor = SpectrumColors.Azure,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New Clip")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                placeholder = { Text("Search clips, links, colors…") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true
            )

            // Category Filter Pills Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectedTab.value = tab },
                        label = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(tab.label)
                                if (tab == FilterTab.TRASH && deletedClips.isNotEmpty()) {
                                    Badge(
                                        containerColor = Color.Red,
                                        contentColor = Color.White
                                    ) {
                                        Text("${deletedClips.size}")
                                    }
                                }
                            }
                        }
                    )
                }
            }

            // Clips List or Empty State
            if (filteredClips.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        WatermarkLogo(compact = false)
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No matching clips found"
                            else if (selectedTab == FilterTab.TRASH) "Trash is empty"
                            else "No clips yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (searchQuery.isNotEmpty()) "Try a different search term."
                            else if (selectedTab == FilterTab.TRASH) "Deleted clips will appear here."
                            else "Copy text, links, or colors from any app, or tap + to create one.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredClips, key = { it.id }) { clip ->
                        ClipRow(
                            item = clip,
                            isTrashMode = selectedTab == FilterTab.TRASH,
                            onCopy = {
                                viewModel.copyClip(clip)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Copied to clipboard!")
                                }
                            },
                            onTogglePin = { viewModel.togglePin(clip.id) },
                            onEdit = { selectedClipForDetail = clip },
                            onDelete = {
                                viewModel.deleteClip(clip.id)
                                coroutineScope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Clip moved to Trash",
                                        actionLabel = "Undo",
                                        duration = SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        viewModel.undoDelete()
                                    }
                                }
                            },
                            onRestore = {
                                viewModel.restoreClip(clip.id)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Clip restored!")
                                }
                            },
                            onPermanentDelete = {
                                viewModel.permanentlyDeleteClip(clip.id)
                            }
                        )
                    }
                }
            }
        }
    }

    // Modal Bottom Sheets
    selectedClipForDetail?.let { clip ->
        ClipDetailSheet(
            item = clip,
            isTrashMode = selectedTab == FilterTab.TRASH,
            onSave = { updated -> viewModel.updateClip(updated) },
            onCopy = {
                viewModel.copyClip(clip)
                coroutineScope.launch { snackbarHostState.showSnackbar("Copied to clipboard!") }
            },
            onDelete = {
                viewModel.deleteClip(clip.id)
                coroutineScope.launch {
                    val result = snackbarHostState.showSnackbar("Clip moved to Trash", "Undo")
                    if (result == SnackbarResult.ActionPerformed) viewModel.undoDelete()
                }
            },
            onRestore = {
                viewModel.restoreClip(clip.id)
                coroutineScope.launch { snackbarHostState.showSnackbar("Clip restored!") }
            },
            onDismiss = { selectedClipForDetail = null }
        )
    }

    if (showNewClipSheet) {
        NewClipSheet(
            onSave = { type, content, isPinned ->
                viewModel.addClip(type, content, isPinned)
                coroutineScope.launch { snackbarHostState.showSnackbar("Clip saved!") }
            },
            onDismiss = { showNewClipSheet = false }
        )
    }

    if (showSettingsSheet) {
        SettingsSheet(
            maxClips = maxClips,
            activeClipsCount = activeClips.size,
            trashClipsCount = deletedClips.size,
            onSetMaxClips = { viewModel.setMaxClips(it) },
            onEmptyTrash = { viewModel.emptyTrash() },
            onExportBackup = { viewModel.exportBackup() },
            onDismiss = { showSettingsSheet = false }
        )
    }
}
