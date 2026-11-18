package com.example.notepad

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class NotesDbAdapter(context: Context) {

    private val dbHelper = DatabaseHelper(context.applicationContext)
    private var db: SQLiteDatabase? = null

    fun open(): NotesDbAdapter {
        db = dbHelper.writableDatabase
        return this
    }

    fun close() {
        dbHelper.close()
    }

    fun createNote(title: String, note: String, category: String, color: String = "#FFFFFF"): Long {
        val values = ContentValues().apply {
            put(KEY_TITLE, title)
            put(KEY_NOTE, note)
            put(KEY_CATEGORY, category)
            put(KEY_COLOR, color)
            val now = System.currentTimeMillis()
            put(KEY_CREATED, now)
            put(KEY_MODIFIED, now)
            put(KEY_PINNED, 0)
        }
        return db?.insert(TABLE, null, values) ?: -1
    }

    fun updateNote(rowId: Long, title: String, note: String, category: String, color: String): Boolean {
        val values = ContentValues().apply {
            put(KEY_TITLE, title)
            put(KEY_NOTE, note)
            put(KEY_CATEGORY, category)
            put(KEY_COLOR, color)
            put(KEY_MODIFIED, System.currentTimeMillis())
        }
        return db?.update(TABLE, values, "$KEY_ID=$rowId", null) == 1
    }

    fun deleteNote(rowId: Long): Boolean {
        return db?.delete(TABLE, "$KEY_ID=$rowId", null) == 1
    }

    fun fetchAllNotes(sortOrder: String = "$KEY_PINNED DESC, $KEY_MODIFIED DESC"): List<Note> {
        return query(null, null, sortOrder)
    }

    fun fetchNote(rowId: Long): Note? {
        val list = query("$KEY_ID=$rowId", null, null)
        return list.firstOrNull()
    }

    fun searchNotes(query: String, sortOrder: String = "$KEY_PINNED DESC, $KEY_MODIFIED DESC"): List<Note> {
        val sel = "$KEY_TITLE LIKE ? OR $KEY_NOTE LIKE ?"
        val args = arrayOf("%$query%", "%$query%")
        return query(sel, args, sortOrder)
    }

    fun fetchByCategory(category: String, sortOrder: String = "$KEY_PINNED DESC, $KEY_MODIFIED DESC"): List<Note> {
        return query("$KEY_CATEGORY=?", arrayOf(category), sortOrder)
    }

    fun togglePinned(rowId: Long, pinned: Int): Boolean {
        val values = ContentValues().apply { put(KEY_PINNED, pinned) }
        return db?.update(TABLE, values, "$KEY_ID=$rowId", null) == 1
    }

    fun fetchCategories(): List<String> {
        val cursor = db?.query(true, TABLE, arrayOf(KEY_CATEGORY), null, null, null, null, null, null)
        val cats = mutableListOf<String>()
        cursor?.use {
            while (it.moveToNext()) {
                val c = it.getString(it.getColumnIndexOrThrow(KEY_CATEGORY))
                if (c.isNotEmpty()) cats.add(c)
            }
        }
        return cats
    }

    private fun query(selection: String?, args: Array<String>?, sortOrder: String?): List<Note> {
        val cursor = db?.query(TABLE, ALL_KEYS, selection, args, null, null, sortOrder)
        val list = mutableListOf<Note>()
        cursor?.use {
            val idI = it.getColumnIndexOrThrow(KEY_ID)
            val tI = it.getColumnIndexOrThrow(KEY_TITLE)
            val nI = it.getColumnIndexOrThrow(KEY_NOTE)
            val cI = it.getColumnIndexOrThrow(KEY_CATEGORY)
            val colorI = it.getColumnIndexOrThrow(KEY_COLOR)
            val crI = it.getColumnIndexOrThrow(KEY_CREATED)
            val mI = it.getColumnIndexOrThrow(KEY_MODIFIED)
            val pI = it.getColumnIndexOrThrow(KEY_PINNED)
            while (it.moveToNext()) {
                list.add(
                    Note(
                        id = it.getLong(idI),
                        title = it.getString(tI),
                        note = it.getString(nI) ?: "",
                        category = it.getString(cI) ?: "",
                        color = it.getString(colorI) ?: "#FFFFFF",
                        created = it.getLong(crI),
                        modified = it.getLong(mI),
                        pinned = it.getInt(pI)
                    )
                )
            }
        }
        return list
    }

    companion object {
        const val KEY_ID = "_id"
        const val KEY_TITLE = "title"
        const val KEY_NOTE = "note"
        const val KEY_CATEGORY = "category"
        const val KEY_COLOR = "color"
        const val KEY_CREATED = "created"
        const val KEY_MODIFIED = "modified"
        const val KEY_PINNED = "pinned"

        private const val DB_NAME = "notepad.db"
        private const val TABLE = "notes"
        private const val DB_VER = 3
        private val ALL_KEYS = arrayOf(KEY_ID, KEY_TITLE, KEY_NOTE, KEY_CATEGORY, KEY_COLOR, KEY_CREATED, KEY_MODIFIED, KEY_PINNED)
    }

    private class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VER) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE $TABLE (
                    $KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $KEY_TITLE TEXT NOT NULL,
                    $KEY_NOTE TEXT,
                    $KEY_CATEGORY TEXT DEFAULT '',
                    $KEY_COLOR TEXT DEFAULT '#FFFFFF',
                    $KEY_CREATED INTEGER,
                    $KEY_MODIFIED INTEGER,
                    $KEY_PINNED INTEGER DEFAULT 0
                )
            """.trimIndent())
        }

        override fun onUpgrade(db: SQLiteDatabase, oldV: Int, newV: Int) {
            if (oldV < 2) {
                db.execSQL("ALTER TABLE $TABLE ADD COLUMN $KEY_CATEGORY TEXT DEFAULT ''")
                db.execSQL("ALTER TABLE $TABLE ADD COLUMN $KEY_CREATED INTEGER")
                db.execSQL("ALTER TABLE $TABLE ADD COLUMN $KEY_MODIFIED INTEGER")
                db.execSQL("ALTER TABLE $TABLE ADD COLUMN $KEY_PINNED INTEGER DEFAULT 0")
            }
            if (oldV < 3) {
                db.execSQL("ALTER TABLE $TABLE ADD COLUMN $KEY_COLOR TEXT DEFAULT '#FFFFFF'")
            }
        }
    }
}

data class Note(
    val id: Long,
    val title: String,
    val note: String,
    val category: String,
    val color: String,
    val created: Long,
    val modified: Long,
    val pinned: Int
)
