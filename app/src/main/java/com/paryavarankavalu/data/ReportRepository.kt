package com.paryavarankavalu.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ReportRepository(context: Context) : SQLiteOpenHelper(context, "paryavaran.db", null, 3) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE reports(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                reporter_name TEXT NOT NULL,
                category TEXT NOT NULL,
                notes TEXT NOT NULL,
                photo_path TEXT NOT NULL,
                latitude REAL NOT NULL,
                longitude REAL NOT NULL,
                is_cleaned INTEGER NOT NULL DEFAULT 0,
                created_at INTEGER NOT NULL,
                cleaned_at INTEGER,
                cleaned_by TEXT,
                cleaned_photo_path TEXT
            )
            """.trimIndent()
        )
        createUsersTable(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE reports ADD COLUMN cleaned_at INTEGER")
            createUsersTable(db)
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE reports ADD COLUMN cleaned_by TEXT")
            db.execSQL("ALTER TABLE reports ADD COLUMN cleaned_photo_path TEXT")
        }
    }

    private fun createUsersTable(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS users(
                name TEXT PRIMARY KEY,
                password TEXT NOT NULL,
                role TEXT NOT NULL,
                karma INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
    }

    fun createUser(name: String, password: String, role: String): Boolean {
        val values = ContentValues().apply {
            put("name", name)
            put("password", password)
            put("role", role)
            put("karma", 0)
        }
        return writableDatabase.insert("users", null, values) != -1L
    }

    fun login(name: String, password: String, role: String): User? {
        val cursor = readableDatabase.rawQuery(
            "SELECT * FROM users WHERE name = ? AND password = ? AND role = ?",
            arrayOf(name, password, role)
        )
        cursor.use {
            if (!it.moveToFirst()) return null
            return User(
                name = it.getString(it.getColumnIndexOrThrow("name")),
                password = it.getString(it.getColumnIndexOrThrow("password")),
                role = it.getString(it.getColumnIndexOrThrow("role")),
                karma = it.getInt(it.getColumnIndexOrThrow("karma"))
            )
        }
    }

    fun user(name: String): User? {
        val cursor = readableDatabase.rawQuery("SELECT * FROM users WHERE name = ?", arrayOf(name))
        cursor.use {
            if (!it.moveToFirst()) return null
            return User(
                name = it.getString(it.getColumnIndexOrThrow("name")),
                password = it.getString(it.getColumnIndexOrThrow("password")),
                role = it.getString(it.getColumnIndexOrThrow("role")),
                karma = it.getInt(it.getColumnIndexOrThrow("karma"))
            )
        }
    }

    fun addKarma(name: String, points: Int) {
        writableDatabase.execSQL(
            "UPDATE users SET karma = karma + ? WHERE name = ?",
            arrayOf(points, name)
        )
    }

    fun add(report: Report): Long {
        val values = ContentValues().apply {
            put("reporter_name", report.reporterName)
            put("category", report.category)
            put("notes", report.notes)
            put("photo_path", report.photoPath)
            put("latitude", report.latitude)
            put("longitude", report.longitude)
            put("is_cleaned", if (report.isCleaned) 1 else 0)
            put("created_at", report.createdAt)
        }
        return writableDatabase.insert("reports", null, values)
    }

    fun all(): List<Report> = query("SELECT * FROM reports ORDER BY created_at DESC")

    fun pending(): List<Report> = query("SELECT * FROM reports WHERE is_cleaned = 0 ORDER BY created_at DESC")

    fun cleaned(): List<Report> = query("SELECT * FROM reports WHERE is_cleaned = 1 ORDER BY cleaned_at DESC, created_at DESC")

    fun cleanedBy(workerName: String): List<Report> =
        query("SELECT * FROM reports WHERE cleaned_by = ? ORDER BY cleaned_at DESC, created_at DESC", arrayOf(workerName))

    fun byId(id: Long): Report? = query("SELECT * FROM reports WHERE id = ?", arrayOf(id.toString())).firstOrNull()

    fun byReporter(name: String): List<Report> =
        query("SELECT * FROM reports WHERE reporter_name = ? ORDER BY created_at DESC", arrayOf(name))

    fun markCleaned(id: Long, workerName: String, cleanedPhotoPath: String) {
        val values = ContentValues().apply {
            put("is_cleaned", 1)
            put("cleaned_at", System.currentTimeMillis())
            put("cleaned_by", workerName)
            put("cleaned_photo_path", cleanedPhotoPath)
        }
        writableDatabase.update("reports", values, "id = ?", arrayOf(id.toString()))
    }

    private fun query(sql: String, args: Array<String>? = null): List<Report> {
        val cursor = readableDatabase.rawQuery(sql, args)
        val reports = mutableListOf<Report>()
        cursor.use {
            while (it.moveToNext()) {
                reports += Report(
                    id = it.getLong(it.getColumnIndexOrThrow("id")),
                    reporterName = it.getString(it.getColumnIndexOrThrow("reporter_name")),
                    category = it.getString(it.getColumnIndexOrThrow("category")),
                    notes = it.getString(it.getColumnIndexOrThrow("notes")),
                    photoPath = it.getString(it.getColumnIndexOrThrow("photo_path")),
                    latitude = it.getDouble(it.getColumnIndexOrThrow("latitude")),
                    longitude = it.getDouble(it.getColumnIndexOrThrow("longitude")),
                    isCleaned = it.getInt(it.getColumnIndexOrThrow("is_cleaned")) == 1,
                    createdAt = it.getLong(it.getColumnIndexOrThrow("created_at")),
                    cleanedAt = it.columnLongOrNull("cleaned_at"),
                    cleanedBy = it.columnStringOrNull("cleaned_by"),
                    cleanedPhotoPath = it.columnStringOrNull("cleaned_photo_path")
                )
            }
        }
        return reports
    }

    private fun android.database.Cursor.columnLongOrNull(name: String): Long? {
        val index = getColumnIndex(name)
        if (index == -1 || isNull(index)) return null
        return getLong(index)
    }

    private fun android.database.Cursor.columnStringOrNull(name: String): String? {
        val index = getColumnIndex(name)
        if (index == -1 || isNull(index)) return null
        return getString(index)
    }
}
