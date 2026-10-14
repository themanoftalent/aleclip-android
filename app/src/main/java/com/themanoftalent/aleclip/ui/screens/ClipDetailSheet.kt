package com.themanoftalent.aleclip.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.themanoftalent.aleclip.model.ClipItem
import com.themanoftalent.aleclip.model.ClipType
import com.themanoftalent.aleclip.ui.components.parseHexColor
import com.themanoftalent.aleclip.ui.theme.SpectrumColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClipDetailSheet(
    item: ClipItem,
    isTrashMode: Boolean,
    onSave: (ClipItem) -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    onRestore: (() -> Unit)?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var textContent by remember { mutableStateOf(item.content) }
    var isPinned by remember { mutableStateOf(item.isPinned) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Type Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = item.type.accentColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = item.type.displayName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = item.type.accentColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                Text(
                    text = item.formattedTime(),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // Editable or Viewable Content
            when (item.type) {
                ClipType.COLOR -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .background(parseHexColor(textContent), CircleShape)
                                .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = textContent,
                            onValueChange = { textContent = it },
                            label = { Text("Color HEX") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                else -> {
                    OutlinedTextField(
                        value = textContent,
                        onValueChange = { textContent = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 140.dp, max = 280.dp),
                        shape = RoundedCornerShape(12.dp),
                        readOnly = isTrashMode
                    )
                }
            }

            // Link opener if URL
            if (item.type == ClipType.LINK || textContent.startsWith("http://") || textContent.startsWith("https://")) {
                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(textContent))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SpectrumColors.Azure)
                ) {
                    Icon(Icons.Default.OpenInBrowser, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open in Browser")
                }
            }

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Copy Button
                Button(
                    onClick = {
                        onCopy()
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = SpectrumColors.Azure)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy")
                }

                // Share Button
                OutlinedButton(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, textContent)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Clip"))
                    }
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(18.dp))
                }

                // Delete / Restore Button
                if (isTrashMode) {
                    if (onRestore != null) {
                        Button(
                            onClick = {
                                onRestore()
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SpectrumColors.Mint)
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = "Restore", modifier = Modifier.size(18.dp))
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            onDelete()
                            onDismiss()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Save Changes button
            if (!isTrashMode && textContent != item.content) {
                Button(
                    onClick = {
                        item.content = textContent
                        item.isPinned = isPinned
                        onSave(item)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SpectrumColors.Emerald)
                ) {
                    Text("Save Changes", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
