package com.badprinter.yobey.db;

import android.content.ContentValues;
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
    private static final int DATABASE_VERSION = 10;

    /*
     * My Favorite List Table
     */
    final String SQL_CREATE_TABLE_FAVORITE = "CREATE TABLE IF NOT EXISTS favorite (" +
            "_id integer primary key autoincrement, " +
            "song_id varchar(100), " +
            "name varchar(100), " +
            "artist varchar(100)) ";
    /*
     * My Favorite Artist Table
     */
    final String SQL_CREATE_TABLE_FAVORITE_ARTIST = "CREATE TABLE IF NOT EXISTS favoriteartist (" +
            "_id integer primary key autoincrement, " +
            "artist varchar(100)) ";
    /*
     * Song Detail Table
     */
    final String SQL_CREATE_TABLE_SONG_DETAIL = "CREATE TABLE IF NOT EXISTS songdetail (" +
            "_id integer primary key autoincrement, " +
            "song_id varchar(100), " +
            "name varchar(100), " +
            "artist varchar(100), " +
            "play_count integer, " +
            "switch_count integer, " +
            "time_string varchar(50), " +
            "time_long bigint)";
    /*
     * DateCount & CommonCount Table
     */
    final String SQL_CREATE_TABLE_DATE_COUNT = "CREATE TABLE IF NOT EXISTS datecount (" +
            "_id integer primary key autoincrement, " +
            "date varchar(100), " +
            "count integer)";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE_FAVORITE);
        db.execSQL(SQL_CREATE_TABLE_SONG_DETAIL);
        db.execSQL(SQL_CREATE_TABLE_DATE_COUNT);
        db.execSQL(SQL_CREATE_TABLE_FAVORITE_ARTIST);
        initCommonCount(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "onUpgrade");
        db.execSQL("DROP TABLE IF EXISTS "+"songdetail");
        db.execSQL("DROP TABLE IF EXISTS "+"favorite");
        db.execSQL("DROP TABLE IF EXISTS "+"commoncount");
        onCreate(db);
    }
    private void initCommonCount(SQLiteDatabase db) {
        String[] catas = new String[] {
                "allPlay", "allSwitch"
        };
        for (int i = 0 ; i < catas.length ; i++) {
            ContentValues values = new ContentValues();
            values.put("date", catas[i]);
            values.put("count", 0);
            db.insert("datecount", null, values);
        }

    }
}
