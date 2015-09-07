package com.badprinter.yobey.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.badprinter.yobey.commom.AppContext;
import com.badprinter.yobey.models.Song;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by root on 15-8-27.
 */
public class DBManager {
    private final String TAG = "DBManager";

    private DBHelper helper;
    private SQLiteDatabase db;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd", Locale.CHINA);

    public DBManager() {
        helper = new DBHelper(AppContext.getInstance());
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
        if (c.getCount() == 0) {
            c.close();
            return false;
        } else {
            c.close();
            return true;
        }
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
        Cursor c = db.rawQuery("SELECT * FROM favorite", null);
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
                            0, 0, getCurrentTimeString(), getCurrentTimeLong()});

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
        if (c.getCount() == 0) {
            c.close();
            return false;
        } else {
            c.close();
            return true;
        }
    }
    /*
     * Delete a Song from songdetail Table
     */
    public void deleteFromSongDetail(Song song) {
        if (!inSongDetail(song))
            return;
        deleteFromSongDetailById(song.getId());
    }
    /*
     * Delete a Song from songdetail Table by Id
     */
    public void deleteFromSongDetailById(Long songId) {
        db.delete("songdetail", "song_id=?", new String[]{Long.toString(songId)});
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
        c.close();
        ContentValues values = new ContentValues();
        values.put("switch_count", Integer.toString(count + 1));
        values.put("time_long", getCurrentTimeLong());
        values.put("time_string", getCurrentTimeString());
        db.update("songdetail", values, "song_id=?", new String[]{Long.toString(song.getId())});
        //logCursor();
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
        c.close();
        return count;
    }
    /*
     * Update Play_count(+1) && Update Time
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
        values.put("time_long", getCurrentTimeLong());
        values.put("time_string", getCurrentTimeString());
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
    /*
     * Get 10 Songs which are Listened Long Long ago
     */
    public Cursor getAgoSongs() {
        Cursor c = db.rawQuery("SELECT *  FROM songdetail ORDER BY time_long ASC LIMIT 10", null);
        return c;
    }
    /*
     * Get 10 Songs which are Listened Recently
     */
    public Cursor getRecentlySongs() {
        Cursor c = db.rawQuery("SELECT *  FROM songdetail ORDER BY time_long DESC LIMIT 10", null);
        return c;
    }

    /*****************************
     * Date & Common Count Table
     ****************************/

    /*
     * Update DateCount by A Boolean "isCompleted"
     */
    public void updateDateCountPlay(boolean isCompleted) {
        Calendar calendar = Calendar.getInstance();
        // Update Date Count
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = format1.format(calendar.getTime());
        updateDateCountByDate(formattedDate);
        // Update AllPlay Count
        String all = "allPlay";
        updateDateCountByDate(all);
        if (!isCompleted) {
            String mySwitch = "allSwitch";
            updateDateCountByDate(mySwitch);
        }
    }

    private  void updateDateCountByDate(String date) {
        Cursor c = db.query("datecount", new String[]{"count"},
                "date=?", new String[]{date}, null, null, null);
        int count = 0;
        if (c.getCount() == 0)
            addToDateCount(date);
        else
            while(c.moveToNext()) {
                count = c.getInt(c.getColumnIndex("count"));
            }
        c.close();
        ContentValues values = new ContentValues();
        values.put("count", count+1);
        db.update("datecount", values, "date=?", new String[]{date});
        logCursor();
    }

    private void addToDateCount(String date) {
        db.beginTransaction();
        try {
            db.execSQL("INSERT INTO datecount VALUES(null, ?, ?)",
                    new Object[]{date, 1});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /*
     * Get 7 Integer Which recode the Count of 7day's Play
     */
    public int[] getDaysCount() {
        int[] results = new int[7];
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        for (int i = 0 ; i < 7 ; i++) {
            String formattedDate = format1.format(calendar.getTime());
            Cursor c = db.query("datecount", new String[]{"count"},
                    "date=?", new String[]{formattedDate}, null, null, null);
            int count = 0;
            while(c.moveToNext()) {
                count = c.getInt(c.getColumnIndex("count"));
            }
            c.close();
            results[6-i] = count;
            calendar.add(Calendar.DATE, -1);
        }
        return results;
    }

    /*
     * Get the Count of All Play
     */
    public int getAllPlayCount() {
        int count = 0;
        Cursor c = db.query("datecount", new String[]{"count"},
                "date=?", new String[]{"allPlay"}, null, null, null);
        while(c.moveToNext()) {
            count = c.getInt(c.getColumnIndex("count"));
        }
        c.close();
        return count;
    }

    /*
     * Get the Count of All Switch
     */
    public int getAllSwitchCount() {
        int count = 0;
        Cursor c = db.query("datecount", new String[]{"count"},
                "date=?", new String[]{"allSwitch"}, null, null, null);
        while(c.moveToNext()) {
            count = c.getInt(c.getColumnIndex("count"));
        }
        c.close();
        return count;
    }

    /*****************************
     * Favorite Artist Table
     ****************************/
    public boolean isFavoriteArtist(String name) {
        String[] args = {name};
        Cursor c =  db.rawQuery("SELECT * " +
                "FROM favoriteartist " +
                "WHERE artist = ?",args);
        if (c.getCount() == 0) {
            c.close();
            return false;
        }
        else {
            c.close();
            return true;
        }
    }
    public void addToFavoriteArtist(String name) {
        if (isFavoriteArtist(name))
            return;
        db.beginTransaction();
        try {
            db.execSQL("INSERT INTO favoriteartist VALUES(null, ?)",
                    new Object[]{name});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void deleteFromFavoriteArtist(String name) {
        if (!isFavoriteArtist(name))
            return;
        db.delete("favoriteartist", "artist=?", new String[]{name});
    }
    public Cursor queryFromFavoriteArtist() {
        Cursor c = db.rawQuery("SELECT * FROM favoriteartist", null);
        return c;
    }
    /*****************************
     * Utils
     ****************************/
    public void logCursor() {
        Cursor c = db.rawQuery("SELECT * FROM songdetail", null);
        Log.e(TAG, "logCursor0");
        if (c.getCount() == 0) {
            c.close();
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
        c.close();
    }
    private long getCurrentTimeLong() {
        return System.currentTimeMillis();
    }
    private String getCurrentTimeString() {
        Date date = new Date();
        return dateFormat.format(date);
    }
}
