package com.example.notepad

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    note: Note?,
    categories: List<String>,
    onSave: (String, String, String, String) -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf(note?.title ?: "") }
    var content by remember { mutableStateOf(note?.note ?: "") }
    var category by remember { mutableStateOf(note?.category ?: "") }
    var color by remember { mutableStateOf(note?.color ?: "#FFFFFF") }
    var showCategoryMenu by remember { mutableStateOf(false) }
    val isNew = note == null || note.id == -1L

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNew) stringResource(R.string.new_note) else stringResource(R.string.edit_note)) },
                navigationIcon = {
                    IconButton(onClick = {
                        onSave(title, content, category, color); onBack()
                    }) { Text("✕", fontSize = 20.sp) }
                },
                actions = {
                    if (!isNew) {
                        IconButton(onClick = { onDelete(); onBack() }) {
                            Text("🗑", fontSize = 20.sp, color = MaterialTheme.colorScheme.error)
                        }
                    }
                    IconButton(onClick = { onSave(title, content, category, color); onBack() }) {
                        Text(stringResource(R.string.save))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.hint_title)) },
                textStyle = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.category_label), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(8.dp))
                Box {
                    OutlinedTextField(
                        value = category.ifBlank { stringResource(R.string.no_category) },
                        onValueChange = { category = it },
                        readOnly = true,
                        modifier = Modifier.clickable { showCategoryMenu = true }.width(200.dp),
                        singleLine = true
                    )
                    DropdownMenu(expanded = showCategoryMenu, onDismissRequest = { showCategoryMenu = false }) {
                        DropdownMenuItem(text = { Text(stringResource(R.string.no_category)) }, onClick = { category = ""; showCategoryMenu = false })
                        categories.forEach { cat ->
                            DropdownMenuItem(text = { Text(cat) }, onClick = { category = cat; showCategoryMenu = false })
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(stringResource(R.string.note_color), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                NotePadViewModel.NOTE_COLORS.forEach { hex ->
                    val c = try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { Color.White }
                    val selected = hex == color
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { color = hex }) {
                        Box(
                            Modifier
                                .width(36.dp)
                                .height(36.dp)
                                .background(c, RoundedCornerShape(50))
                                .then(
                                    if (selected) Modifier.background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(50)) else Modifier
                                )
                        )
                        if (selected) Text("●", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text(stringResource(R.string.hint_content)) },
                modifier = Modifier.fillMaxSize(),
                textStyle = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
