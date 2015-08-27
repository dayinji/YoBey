package com.badprinter.yobey.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

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
     * Get a SongLIst of SongId from Favorite Table
     */
    public List<Long> queryFromFavorite() {
        List<Long> list = new ArrayList<Long>();
        Cursor c = db.rawQuery("SELECT song_id FROM favorite", null);
        while(c.moveToNext()) {
            Long id = Long.parseLong(c.getString(c.getColumnIndex("song_id")));
            list.add(id);
        }
        c.close();
        return list;
    }
}
