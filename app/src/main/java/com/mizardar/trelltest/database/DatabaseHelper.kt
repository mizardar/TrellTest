package com.mizardar.trelltest.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import com.mizardar.trelltest.model.ModelVideo

const val DB_NAME = "TrellVideos"
const val DB_VERSION = 1
const val TABLE_VIDEOS = "bookmarked_videos"
const val KEY_ID = "_id"
const val KEY_PATH = "path"


class DatabaseHelper(private val context: Context) : SQLiteOpenHelper(
    context, DB_NAME, null,
    DB_VERSION
) {


    private val CREATE_VIDEOS_TABLE = "CREATE TABLE $TABLE_VIDEOS ($KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT, $KEY_PATH  TEXT)"


    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(CREATE_VIDEOS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_VIDEOS")
        onCreate(db)
    }

    fun addBookmarkedVideo(modelVideo: ModelVideo):Long{
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(KEY_PATH, modelVideo.videoPath)
        val success = db.insert(TABLE_VIDEOS, null, contentValues)
        db.close()
        return success
    }

    fun deleteBookmarkedVideo(modelVideo: ModelVideo):Int{
        val db = this.writableDatabase
        val success = db.delete(TABLE_VIDEOS,"$KEY_PATH='${modelVideo.videoPath}'",null)
        db.close()
        return success
    }


    fun getAllBookmarkedVideos():List<String>{
        val bookmarkedVideos:ArrayList<String> = ArrayList<String>()
        val selectQuery = "SELECT  * FROM $TABLE_VIDEOS"
        val db = this.readableDatabase
        var cursor: Cursor? = null
        try{
            cursor = db.rawQuery(selectQuery, null)
        }catch (e: SQLiteException) {
            db.execSQL(selectQuery)
            return ArrayList()
        }
        if (cursor.moveToFirst()) {
            do {
                val videoPath = cursor.getString(cursor.getColumnIndex(KEY_PATH))
                bookmarkedVideos.add(videoPath)
            } while (cursor.moveToNext())
        }
        return bookmarkedVideos
    }
}