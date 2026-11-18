package com.example.notepad

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotePadViewModel(application: Application) : AndroidViewModel(application) {

    private val db = NotesDbAdapter(application).apply { open() }

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private val _sortOrder = MutableStateFlow("modified_desc")
    val sortOrder: StateFlow<String> = _sortOrder.asStateFlow()

    private val _filterCategory = MutableStateFlow<String?>(null)
    val filterCategory: StateFlow<String?> = _filterCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _showSearch = MutableStateFlow(false)
    val showSearch: StateFlow<Boolean> = _showSearch.asStateFlow()

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            val sqlSort = when (_sortOrder.value) {
                SORT_MODIFIED_ASC -> "pinned DESC, modified ASC"
                SORT_TITLE_ASC -> "pinned DESC, title ASC"
                SORT_TITLE_DESC -> "pinned DESC, title DESC"
                else -> "pinned DESC, modified DESC"
            }
            val list = when {
                _searchQuery.value.isNotBlank() -> db.searchNotes(_searchQuery.value, sqlSort)
                _filterCategory.value != null -> db.fetchByCategory(_filterCategory.value!!, sqlSort)
                else -> db.fetchAllNotes(sqlSort)
            }
            _notes.value = list
            _categories.value = db.fetchCategories()
        }
    }

    fun createNote(title: String, content: String, category: String): Long {
        val id = db.createNote(title, content, category)
        refresh()
        return id
    }

    fun updateNote(id: Long, title: String, content: String, category: String) {
        db.updateNote(id, title, content, category)
        refresh()
    }

    fun deleteNote(id: Long) {
        db.deleteNote(id)
        refresh()
    }

    fun togglePinned(id: Long, pinned: Int) {
        db.togglePinned(id, if (pinned == 1) 0 else 1)
        refresh()
    }

    fun setSort(order: String) { _sortOrder.value = order; refresh() }
    fun setCategoryFilter(cat: String?) { _filterCategory.value = cat; refresh() }
    fun setSearchQuery(q: String) { _searchQuery.value = q; refresh() }
    fun toggleSearch(show: Boolean) {
        _showSearch.value = show
        if (!show) { _searchQuery.value = ""; refresh() }
    }

    override fun onCleared() { db.close(); super.onCleared() }

    companion object {
        const val SORT_MODIFIED_DESC = "modified_desc"
        const val SORT_MODIFIED_ASC = "modified_asc"
        const val SORT_TITLE_ASC = "title_asc"
        const val SORT_TITLE_DESC = "title_desc"

        fun formatTime(ts: Long): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            return sdf.format(Date(ts))
        }
    }
}
