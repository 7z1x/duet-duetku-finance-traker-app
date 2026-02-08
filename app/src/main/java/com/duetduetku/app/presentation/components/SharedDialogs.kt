package com.duetduetku.app.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun EditLimitDialog(currentLimit: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    val numberFormat = remember { java.text.NumberFormat.getNumberInstance(java.util.Locale("id", "ID")) }
    var text by remember { 
        mutableStateOf(
            try {
                // Initialize with formatted value
                val clean = currentLimit.filter { it.isDigit() }
                if (clean.isNotEmpty()) {
                    numberFormat.format(clean.toLong())
                } else {
                    currentLimit
                }
            } catch (e: Exception) {
                currentLimit
            }
        ) 
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Daily Budget Limit") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { newText ->
                    // Filter only digits
                    val clean = newText.filter { it.isDigit() }
                    if (clean.isEmpty()) {
                        text = ""
                    } else {
                        // Limit length to avoid Long overflow if needed, e.g. 15 digits
                        if (clean.length <= 15) {
                            try {
                                val parsed = clean.toLong()
                                text = numberFormat.format(parsed)
                            } catch (e: Exception) {
                                // Fallback
                            }
                        }
                    }
                },
                label = { Text("Limit (Rp)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        },
        confirmButton = {
            TextButton(onClick = { 
                // Return raw number string to caller
                onConfirm(text.filter { it.isDigit() }) 
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun EditNameDialog(currentName: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf(currentName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Name") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Name") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ThemeSelectionDialog(currentTheme: Int, onDismiss: () -> Unit, onThemeSelected: (Int) -> Unit) {
    val options = listOf("System Default", "Light Mode", "Dark Mode")
    // Map index to theme value (0, 1, 2) which happens to match list index here
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Theme") },
        text = {
            Column {
                options.forEachIndexed { index, text ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (index == currentTheme),
                                onClick = { onThemeSelected(index) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (index == currentTheme),
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = text, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}


