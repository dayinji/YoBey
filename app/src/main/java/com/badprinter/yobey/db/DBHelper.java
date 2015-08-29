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
    private static final int DATABASE_VERSION = 4;

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
    /*
     * CommonCount Table
     */
    final String SQL_CREATE_TABLE_COMMON_COUNT = "CREATE TABLE IF NOT EXISTS commoncount (" +
            "_id integer primary key autoincrement, " +
            "cata varchar(100), " +
            "count integer)";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE_FAVORITE);
        db.execSQL(SQL_CREATE_TABLE_SONG_DETAIL);
        db.execSQL(SQL_CREATE_TABLE_COMMON_COUNT);
        initCommonCount(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "onUpgrade");
        db.execSQL("DROP TABLE IF EXISTS "+"songdetail");
        db.execSQL("DROP TABLE IF EXISTS "+"favorite");
        onCreate(db);
    }
    private void initCommonCount(SQLiteDatabase db) {
        String[] catas = new String[] {
                "allPlay", "allSwitch", "MonPlay", "TusePlay",
                "WedPlay", "ThurPlay", "FriPlay", "SatPlay",
                "SunPlay", "clock1Play", "clock2Play", "clock3Play",
                "clock4Play", "clock5Play", "clock6Play", "clock7Play",
                "clock8Play", "clock9Play", "clock10Play", "clock11Play",
                "clock12Play", "clock13Play", "clock14Play", "clock15Play",
                "clock16Play", "clock17Play", "clock18Play", "clock19Play",
                "clock20Play", "clock21Play", "clock22Play", "clock23Play",
                "clock0Play"
        };
        for (int i = 0 ; i < catas.length ; i++) {
            ContentValues values = new ContentValues();
            values.put("cata", catas[i]);
            values.put("count", 0);
            db.insert("commoncount", null, values);
        }

    }
}
