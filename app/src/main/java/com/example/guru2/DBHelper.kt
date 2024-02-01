package com.example.guru2

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "word_database.db"
        const val TABLE_NAME = "words"
        const val COLUMN_ID = "id"
        const val COLUMN_WORD = "word"

        private val initialWords = arrayOf("fuck", "idiot", "fool","moron","Imbecile","Stupid","Dumb")

    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = "CREATE TABLE $TABLE_NAME ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_WORD TEXT)"
        db?.execSQL(createTableQuery)

        // 초기 데이터를 데이터베이스에 저장
        initialWords.forEach { word ->
            val values = ContentValues()
            values.put(COLUMN_WORD, word)
            db?.insert(TABLE_NAME, null, values)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }
}

fun isWordInDatabase(word: String, context: Context): Boolean {
    val dbHelper = DBHelper(context)
    val db = dbHelper.readableDatabase
    val query = "SELECT * FROM ${DBHelper.TABLE_NAME} WHERE ${DBHelper.COLUMN_WORD} = ?"
    val cursor = db.rawQuery(query, arrayOf(word))
    val exists = cursor.moveToFirst()
    cursor.close()
    db.close()
    return exists
}
