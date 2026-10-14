package com.themanoftalent.aleclip.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.themanoftalent.aleclip.ui.components.SpectrumLogoBadge
import com.themanoftalent.aleclip.ui.theme.SpectrumColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    maxClips: Int,
    activeClipsCount: Int,
    trashClipsCount: Int,
    onSetMaxClips: (Int) -> Unit,
    onEmptyTrash: () -> Unit,
    onExportBackup: () -> String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var showEmptyTrashConfirm by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header Logo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SpectrumLogoBadge(size = 56)
                    Text("AleClip", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    Text("Version 2.0.1 (2026)", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }

            HorizontalDivider()

            // General Section
            Text("General", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Max History Limit")
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(50, 100, 200, 500).forEach { limit ->
                        FilterChip(
                            selected = maxClips == limit,
                            onClick = { onSetMaxClips(limit) },
                            label = { Text("$limit") }
                        )
                    }
                }
            }

            HorizontalDivider()

            // Storage & Trash
            Text("Storage & Trash", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Active Clips")
                Text("$activeClipsCount", fontWeight = FontWeight.Bold)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Trash Items")
                Text("$trashClipsCount", fontWeight = FontWeight.Bold)
            }

            if (trashClipsCount > 0) {
                OutlinedButton(
                    onClick = { showEmptyTrashConfirm = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Empty Trash")
                }
            }

            HorizontalDivider()

            // Backup & Migration
            Text("Backup & Migration", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Button(
                onClick = {
                    val json = onExportBackup()
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/json"
                        putExtra(Intent.EXTRA_SUBJECT, "AleClip Backup")
                        putExtra(Intent.EXTRA_TEXT, json)
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Export Backup JSON"))
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = SpectrumColors.Azure)
            ) {
                Icon(Icons.Default.Upload, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export Backup (JSON)")
            }

            HorizontalDivider()

            // About Section
            Text("About", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = SpectrumColors.Emerald)
                        Text("100% On-Device & Private", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Text(
                        "All clipboard clips and notes remain securely on your device. Zero analytics, zero ad tracking, zero cloud transmission.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Developer: themanoftalent", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }

    if (showEmptyTrashConfirm) {
        AlertDialog(
            onDismissRequest = { showEmptyTrashConfirm = false },
            title = { Text("Empty Trash?") },
            text = { Text("All items in Trash will be permanently deleted. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onEmptyTrash()
                        showEmptyTrashConfirm = false
                    }
                ) {
                    Text("Empty Trash", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmptyTrashConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
