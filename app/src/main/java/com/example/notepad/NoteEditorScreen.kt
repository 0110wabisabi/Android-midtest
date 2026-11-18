package com.example.notepad

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    note: Note?,
    categories: List<String>,
    onSave: (String, String, String) -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf(note?.title ?: "") }
    var content by remember { mutableStateOf(note?.note ?: "") }
    var category by remember { mutableStateOf(note?.category ?: "") }
    var showCategoryMenu by remember { mutableStateOf(false) }
    val isNew = note == null || note.id == -1L

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNew) stringResource(R.string.new_note) else stringResource(R.string.edit_note)) },
                navigationIcon = {
                    IconButton(onClick = {
                        onSave(title, content, category)
                        onBack()
                    }) {
                        Text("✕", fontSize = 20.sp)
                    }
                },
                actions = {
                    if (!isNew) {
                        IconButton(onClick = { onDelete(); onBack() }) {
                            Text("🗑", fontSize = 20.sp, color = MaterialTheme.colorScheme.error)
                        }
                    }
                    IconButton(onClick = {
                        onSave(title, content, category)
                        onBack()
                    }) {
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

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    stringResource(R.string.category_label),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                androidx.compose.foundation.layout.Box {
                    OutlinedTextField(
                        value = category.ifBlank { stringResource(R.string.no_category) },
                        onValueChange = { category = it },
                        readOnly = true,
                        modifier = Modifier.padding(start = 8.dp),
                        singleLine = true,
                        trailingIcon = {
                            Text(
                                if (showCategoryMenu) "▲" else "▼",
                                fontSize = 14.sp,
                                modifier = Modifier
                                    .clickable { showCategoryMenu = !showCategoryMenu }
                                    .padding(8.dp)
                            )
                        }
                    )
                    androidx.compose.foundation.layout.Spacer(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showCategoryMenu = !showCategoryMenu }
                    )
                    DropdownMenu(
                        expanded = showCategoryMenu,
                        onDismissRequest = { showCategoryMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.no_category)) },
                            onClick = { category = ""; showCategoryMenu = false }
                        )
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = { category = cat; showCategoryMenu = false }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text(stringResource(R.string.hint_content)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                textStyle = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
