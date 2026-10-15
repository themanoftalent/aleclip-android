package com.themanoftalent.aleclip.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.themanoftalent.aleclip.model.ClipType
import com.themanoftalent.aleclip.ui.theme.SpectrumColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewClipSheet(
    onSave: (ClipType, String, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(ClipType.TEXT) }
    var isPinned by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "New Clip",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Type Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(ClipType.TEXT, ClipType.LINK, ClipType.COLOR).forEach { type ->
                    val isSelected = selectedType == type
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedType = type },
                        label = { Text(type.displayName) }
                    )
                }
            }

            // Content Field
            OutlinedTextField(
                value = text,
                onValueChange = {
                    text = it
                    // Auto-detect link or color
                    val lower = it.trim().lowercase()
                    if (lower.startsWith("http://") || lower.startsWith("https://")) {
                        selectedType = ClipType.LINK
                    } else if (it.trim().startsWith("#") && (it.trim().length == 7 || it.trim().length == 9)) {
                        selectedType = ClipType.COLOR
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                placeholder = { Text(if (selectedType == ClipType.COLOR) "#00F0FF" else "Enter text or note…") },
                shape = RoundedCornerShape(12.dp)
            )

            // Pin to top toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Pin to Top")
                Switch(checked = isPinned, onCheckedChange = { isPinned = it })
            }

            // Save Button
            Button(
                onClick = {
                    val trimmed = text.trim()
                    if (trimmed.isNotEmpty()) {
                        onSave(selectedType, trimmed, isPinned)
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = text.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = SpectrumColors.Azure)
            ) {
                Text("Save Clip", fontWeight = FontWeight.Bold)
            }
        }
    }
}
