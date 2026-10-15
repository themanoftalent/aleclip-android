package com.themanoftalent.aleclip.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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

            // Card 1: Open Source | Free to Fork & Use
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(
                                    Color(0xFF007AFF),
                                    shape = RoundedCornerShape(6.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "{}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        Text(
                            text = "Open Source | Free to Fork & Use",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "Feel free to fork, modify, use, and contribute!",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Developed by:  ",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "SoftAI Yazılım",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Developer:  ",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Assoc. Prof. Dr. Mehmet Akif Cifci",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "GitHub:  ",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "@themanoftalent",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = Color(0xFF007AFF),
                                modifier = Modifier.clickable {
                                    val browserIntent = Intent(
                                        Intent.ACTION_VIEW,
                                        android.net.Uri.parse("https://github.com/themanoftalent")
                                    )
                                    context.startActivity(browserIntent)
                                }
                            )
                        }
                    }
                }
            }

            // Card 2: Follow & Support
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("❤️", fontSize = 16.sp)
                        Text(
                            text = "Follow & Support",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "If you find this project useful, star the repository, follow me, or buy me a coffee! Your support helps keep future projects free and open-source.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Button 1: GitHub
                        Surface(
                            onClick = {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    android.net.Uri.parse("https://github.com/themanoftalent")
                                )
                                context.startActivity(intent)
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF007AFF).copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, Color(0xFF007AFF).copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "</>",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF007AFF)
                                )
                                Text(
                                    text = "GitHub",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Button 2: Sponsor Me
                        Surface(
                            onClick = {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    android.net.Uri.parse("https://github.com/sponsors/themanoftalent")
                                )
                                context.startActivity(intent)
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFF3B30).copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, Color(0xFFFF3B30).copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("❤️", fontSize = 12.sp)
                                Text(
                                    text = "Sponsor Me",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Button 3: Buy Me a Coffee
                        Surface(
                            onClick = {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    android.net.Uri.parse("https://buymeacoffee.com/themanoftalent")
                                )
                                context.startActivity(intent)
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFF9500).copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, Color(0xFFFF9500).copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("☕", fontSize = 12.sp)
                                Text(
                                    text = "Buy Me a Coffee",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Privacy Policy & Terms Link (Required for Play Store / App Store compliance)
            OutlinedButton(
                onClick = {
                    val browserIntent = Intent(
                        Intent.ACTION_VIEW,
                        android.net.Uri.parse("https://themanoftalent.github.io/aleclip-android/")
                    )
                    context.startActivity(browserIntent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp), tint = SpectrumColors.Emerald)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Privacy Policy & Terms", fontSize = 13.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
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
