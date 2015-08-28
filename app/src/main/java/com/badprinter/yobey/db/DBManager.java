package com.badprinter.yobey.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.badprinter.yobey.models.Song;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 15-8-27.
 */
public class DBManager {
    private final String TAG = "DBManager";

    private DBHelper helper;
    private SQLiteDatabase db;

    public DBManager(Context context) {
        helper = new DBHelper(context);
        db = helper.getWritableDatabase();
    }

    /*****************************
     * Favorite Table
     ****************************/

    /*
     * Add a Song to Favorite Table
     */
    public void addToFavorite(Song song) {
        if (isFavorite(song))
            return;
        db.beginTransaction();
        try {
            db.execSQL("INSERT INTO favorite VALUES(null, ?, ?, ?)",
                    new Object[]{song.getId(), song.getName(), song.getArtist()});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
    /*
     * Judge Whether a Song Is in Favorite Table or Not
     */
    public boolean isFavorite(Song song) {
        String[] args = {Long.toString(song.getId())};
        Cursor c =  db.rawQuery("SELECT * " +
                "FROM favorite " +
                "WHERE song_id = ?",args);
        if (c.getCount() == 0)
            return false;
        else
            return true;
    }
    /*
     * Delete a Song from Favorite Table
     */
    public void deleteFromFavorite(Song song) {
        if (!isFavorite(song))
            return;
        db.delete("favorite", "song_id=?", new String[]{Long.toString(song.getId())});
    }
    /*
     * Query A Cursor from Favorite Table
     */
    public Cursor queryFromFavorite() {
        Cursor c = db.rawQuery("SELECT song_id FROM favorite", null);
        return c;
    }

    /*****************************
     * Songdetail Table
     ****************************/
    /*
     * Add a New Song to songdetail Table
     */
    public void addToSongDetail(Song song) {
        if (inSongDetail(song))
            return;
        db.beginTransaction();
        try {
            db.execSQL("INSERT INTO songdetail VALUES(null, ?, ?, ?, ?, ?, ?, ?)",
                    new Object[]{song.getId(), song.getName(), song.getArtist(),
                            song.getYear(), song.getGenre(), 0, 0});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
    /*
     * Judge Whether a Song Is in songdetail Table or Not
     */
    public boolean inSongDetail(Song song) {
        String[] args = {Long.toString(song.getId())};
        Cursor c =  db.rawQuery("SELECT * " +
                "FROM songdetail " +
                "WHERE song_id = ?",args);
        if (c.getCount() == 0)
            return false;
        else
            return true;
    }
    /*
     * Delete a Song from songdetail Table
     */
    public void deleteFromSongDetail(Song song) {
        if (!inSongDetail(song))
            return;
        db.delete("songdetail", "song_id=?", new String[]{Long.toString(song.getId())});
    }
    /*
     * Query A Cursor from songdetail Table
     */
    public Cursor queryFromSongDetail() {
        Cursor c = db.rawQuery("SELECT * FROM songdetail", null);
        return c;
    }
    /*
     * Update Switch_count(+1)
     */
    public void updateSwicthCount(Song song) {
        Cursor c = db.rawQuery("SELECT switch_count FROM songdetail WHERE song_id = ?",
                new String[]{Long.toString(song.getId())});
        int count = 0;
        while(c.moveToNext()) {
            count = Integer.parseInt(c.getString(c.getColumnIndex("switch_count")));
        }
        logCursor();
        c.close();
        ContentValues values = new ContentValues();
        values.put("switch_count", Integer.toString(count + 1));
        db.update("songdetail", values, "song_id=?", new String[]{Long.toString(song.getId())});
    }
    /*
     * Get Switch_count
     */
    public int getSwicthCount(Song song) {
        Cursor c = db.rawQuery("SELECT switch_count FROM songdetail WHERE id = ?",
                new String[]{Long.toString(song.getId())});
        int count = 0;
        while(c.moveToNext()) {
            count = Integer.parseInt(c.getString(c.getColumnIndex("switch_count")));
        }
        return count;
    }
    /*
     * Update Play_count(+1)
     */
    public void updatePlayCount(Song song) {
        Cursor c = db.rawQuery("SELECT play_count FROM songdetail WHERE song_id = ?",
                new String[]{Long.toString(song.getId())});
        int count = 0;
        while(c.moveToNext()) {
            count = Integer.parseInt(c.getString(c.getColumnIndex("play_count")));
        }
        c.close();
        ContentValues values = new ContentValues();
        values.put("play_count", Integer.toString(count + 1));
        db.update("songdetail", values, "song_id=?", new String[]{Long.toString(song.getId())});
    }
    /*
     * Get Play_count
     */
    public int getPlayCount(Song song) {
        Cursor c = db.rawQuery("SELECT play_count FROM songdetail WHERE id = ?",
                new String[]{Long.toString(song.getId())});
        int count = 0;
        while(c.moveToNext()) {
            count = Integer.parseInt(c.getString(c.getColumnIndex("play_count")));
        }
        c.close();
        return count;
    }



    /*****************************
     * Log the Cursor for Debug
     ****************************/
    public void logCursor() {
        Cursor c = db.rawQuery("SELECT * FROM songdetail", null);
        Log.e(TAG, "logCursor0");
        if (c == null) {
            return;
        }
        for (int i = 0 ; i < c.getColumnCount(); i++)
            System.out.print(c.getColumnName(i) + '\t');
        System.out.println();
        while(c.moveToNext()) {
            //Log.e(TAG, "logCursor1");
            for (int i = 0 ; i < c.getColumnCount(); i++) {
                System.out.print(c.getString(i) + '\t');
            }
            System.out.println();
        }
    }
}
