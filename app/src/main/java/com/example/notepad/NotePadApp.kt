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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotePadApp(viewModel: NotePadViewModel) {
    val notes by viewModel.notes.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val showSearch by viewModel.showSearch.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterCategory by viewModel.filterCategory.collectAsState()

    var editingNote by remember { mutableStateOf<Note?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showBgDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.refresh() }

    if (editingNote != null) {
        NoteEditorScreen(
            note = editingNote,
            categories = categories,
            onSave = { title, content, category, color ->
                val n = editingNote!!
                if (n.id == -1L) viewModel.createNote(title, content, category, color)
                else viewModel.updateNote(n.id, title, content, category, color)
                editingNote = null
            },
            onDelete = {
                editingNote?.let { viewModel.deleteNote(it.id) }
                editingNote = null
            },
            onBack = { editingNote = null }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { viewModel.toggleSearch(!showSearch) }) {
                        Text("🔍", fontSize = 22.sp, color = Color.White)
                    }
                    IconButton(onClick = { showBgDialog = true }) {
                        Text("🎨", fontSize = 22.sp, color = Color.White)
                    }
                    IconButton(onClick = { showCategoryDialog = true }) {
                        Text("📂", fontSize = 22.sp, color = Color.White)
                    }
                    IconButton(onClick = { showSortMenu = true }) {
                        Text("↕", fontSize = 22.sp, color = Color.White)
                    }
                    DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                        DropdownMenuItem(text = { Text(stringResource(R.string.sort_modified_desc)) }, onClick = { viewModel.setSort(NotePadViewModel.SORT_MODIFIED_DESC); showSortMenu = false })
                        DropdownMenuItem(text = { Text(stringResource(R.string.sort_modified_asc)) }, onClick = { viewModel.setSort(NotePadViewModel.SORT_MODIFIED_ASC); showSortMenu = false })
                        DropdownMenuItem(text = { Text(stringResource(R.string.sort_title_asc)) }, onClick = { viewModel.setSort(NotePadViewModel.SORT_TITLE_ASC); showSortMenu = false })
                        DropdownMenuItem(text = { Text(stringResource(R.string.sort_title_desc)) }, onClick = { viewModel.setSort(NotePadViewModel.SORT_TITLE_DESC); showSortMenu = false })
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { editingNote = Note(-1, "", "", "", "#FFFFFF", 0, 0, 0) },
                containerColor = MaterialTheme.colorScheme.primary
            ) { Text("＋", fontSize = 24.sp, color = Color.White) }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (showSearch) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    label = { Text(stringResource(R.string.hint_search)) },
                    singleLine = true
                )
            }
            if (filterCategory != null) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.filter_label, filterCategory!!), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { viewModel.setCategoryFilter(null) }) { Text(stringResource(R.string.clear_filter)) }
                }
            }
            if (notes.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.no_notes), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(notes) { note ->
                        NoteCard(note = note, onClick = { editingNote = note },
                            onTogglePinned = { viewModel.togglePinned(note.id, note.pinned) },
                            onDelete = { viewModel.deleteNote(note.id) })
                    }
                }
            }
        }
    }

    if (showCategoryDialog) {
        CategoryFilterDialog(categories = categories,
            onSelect = { viewModel.setCategoryFilter(it); showCategoryDialog = false },
            onDismiss = { showCategoryDialog = false })
    }
    if (showBgDialog) {
        BgChooserDialog(viewModel = viewModel, onDismiss = { showBgDialog = false })
    }
}

@Composable
fun NoteCard(note: Note, onClick: () -> Unit, onTogglePinned: () -> Unit, onDelete: () -> Unit) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val cardColor = try { Color(android.graphics.Color.parseColor(note.color)) } catch (_: Exception) { Color.White }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(Modifier.fillMaxWidth().padding(14.dp)) {
            Column(Modifier.fillMaxWidth().padding(end = 96.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = note.title.ifBlank { stringResource(R.string.untitled) },
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth().padding(end = if (note.pinned == 1) 24.dp else 0.dp)
                    )
                    if (note.pinned == 1) {
                        Text("📌", fontSize = 14.sp, modifier = Modifier.align(Alignment.CenterEnd))
                    }
                }
                if (note.note.isNotEmpty()) {
                    Text(
                        text = note.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Text(text = NotePadViewModel.formatTime(note.modified), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (note.category.isNotEmpty()) {
                        Box(
                            modifier = Modifier.padding(start = 8.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(note.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            }
            Row(modifier = Modifier.align(Alignment.CenterEnd), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onTogglePinned) {
                    Text(if (note.pinned == 1) "★" else "☆", fontSize = 22.sp, color = if (note.pinned == 1) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Text("🗑", fontSize = 22.sp, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.confirm_delete)) },
            confirmButton = { TextButton(onClick = { onDelete(); showDeleteConfirm = false }) { Text(stringResource(R.string.delete)) } },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

@Composable
fun CategoryFilterDialog(categories: List<String>, onSelect: (String?) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.filter_by_category)) },
        text = {
            Column {
                Row(Modifier.fillMaxWidth().clickable { onSelect(null) }.padding(vertical = 12.dp)) { Text(stringResource(R.string.all_notes)) }
                categories.forEach { cat ->
                    Row(Modifier.fillMaxWidth().clickable { onSelect(cat) }.padding(vertical = 12.dp)) { Text(cat) }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

@Composable
fun BgChooserDialog(viewModel: NotePadViewModel, onDismiss: () -> Unit) {
    val current by viewModel.appBg.collectAsState()
    val options = listOf(
        NotePadViewModel.BG_DEFAULT to stringResource(R.string.bg_default),
        NotePadViewModel.BG_MILK to stringResource(R.string.bg_milk),
        NotePadViewModel.BG_LAVENDER to stringResource(R.string.bg_lavender),
        NotePadViewModel.BG_MINT to stringResource(R.string.bg_mint),
        NotePadViewModel.BG_SAND to stringResource(R.string.bg_sand)
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.bg_setting)) },
        text = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { (key, label) ->
                    val bgColor = when (key) {
                        NotePadViewModel.BG_MILK -> Color(0xFFFDF6E3)
                        NotePadViewModel.BG_LAVENDER -> Color(0xFFF3E5F5)
                        NotePadViewModel.BG_MINT -> Color(0xFFE8F5E9)
                        NotePadViewModel.BG_SAND -> Color(0xFFFBE9E7)
                        else -> Color(0xFFF5F7FA)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { viewModel.setAppBg(key); onDismiss() }) {
                        Box(Modifier.width(40.dp).height(40.dp).background(bgColor, RoundedCornerShape(12.dp)))
                        Text(label, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                        if (key == current) Text("✓", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}
