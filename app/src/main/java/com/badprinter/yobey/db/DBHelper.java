package com.badprinter.yobey.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by root on 15-8-27.
 */
public class DBHelper extends SQLiteOpenHelper {
    private final String TAG = "DBHelper";

    private static final String DATABASE_NAME = "yobey.db";
    private static final int DATABASE_VERSION = 3;

    /*
     * My Favorite List Table
     */
    final String SQL_CREATE_TABLE_FAVORITE = "CREATE TABLE IF NOT EXISTS favorite (" +
            "_id integer primary key autoincrement, " +
            "song_id varchar(100), " +
            "name varchar(100), " +
            "artist varchar(100)) ";
    /*
     * MUSIC Table
     */
    final String SQL_CREATE_TABLE_SONG_DETAIL = "CREATE TABLE IF NOT EXISTS songdetail (" +
            "_id integer primary key autoincrement, " +
            "song_id varchar(100), " +
            "name varchar(100), " +
            "artist varchar(100), " +
            "year integer, " +
            "genre String, " +
            "play_count integer, " +
            "switch_count integer)";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE_FAVORITE);
        db.execSQL(SQL_CREATE_TABLE_SONG_DETAIL);
        Log.e(TAG, "create songdetail");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "onUpgrade");
        db.execSQL("DROP TABLE IF EXISTS "+"songdetail");
        db.execSQL("DROP TABLE IF EXISTS "+"favorite");
        onCreate(db);
    }
}
