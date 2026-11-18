package com.example.notepad

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
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
    private val prefs: SharedPreferences = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

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

    private val _appBg = MutableStateFlow(prefs.getString(KEY_APP_BG, BG_DEFAULT) ?: BG_DEFAULT)
    val appBg: StateFlow<String> = _appBg.asStateFlow()

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

    fun createNote(title: String, content: String, category: String, color: String): Long {
        val id = db.createNote(title, content, category, color)
        refresh()
        return id
    }

    fun updateNote(id: Long, title: String, content: String, category: String, color: String) {
        db.updateNote(id, title, content, category, color)
        refresh()
    }

    fun deleteNote(id: Long) { db.deleteNote(id); refresh() }

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

    fun setAppBg(bg: String) {
        _appBg.value = bg
        prefs.edit().putString(KEY_APP_BG, bg).apply()
    }

    override fun onCleared() { db.close(); super.onCleared() }

    companion object {
        const val SORT_MODIFIED_DESC = "modified_desc"
        const val SORT_MODIFIED_ASC = "modified_asc"
        const val SORT_TITLE_ASC = "title_asc"
        const val SORT_TITLE_DESC = "title_desc"

        private const val PREFS_NAME = "notepad_prefs"
        private const val KEY_APP_BG = "app_bg"
        const val BG_DEFAULT = "default"
        const val BG_MILK = "milk"
        const val BG_LAVENDER = "lavender"
        const val BG_MINT = "mint"
        const val BG_SAND = "sand"

        val NOTE_COLORS = listOf(
            "#FFFFFF",   // 默认白
            "#FFF9C4",   // 黄
            "#FFCDD2",   // 粉
            "#BBDEFB",   // 蓝
            "#C8E6C9",   // 绿
            "#E1BEE7",   // 紫
            "#FFE0B2"    // 橙
        )

        fun formatTime(ts: Long): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            return sdf.format(Date(ts))
        }
    }
}
