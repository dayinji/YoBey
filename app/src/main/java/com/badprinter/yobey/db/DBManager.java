package com.badprinter.yobey.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.badprinter.yobey.models.Song;

import java.util.ArrayList;
import java.util.Calendar;
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
     * Common Count Table
     ****************************/

    /*
     * Update CommonCount by A Boolean "isCompleted"
     */
    public void updateCommonCountPlay(boolean isCompleted) {
        if (isCompleted) {
            Calendar calendar = Calendar.getInstance();

            // Update Day Count
            String[] days = {"SunPlay", "MonPlay", "TusePlay", "WedPlay", "ThurPlay", "FriPlay", "SatPlay"};
            String day = days[calendar.get(Calendar.DAY_OF_WEEK) - 1];
            updateCommonCountByCata(day);

            // Update Hour Count
            String hour = "clock" + Integer.toString(calendar.get(Calendar.HOUR_OF_DAY)) + "Play";
            updateCommonCountByCata(hour);

            // Update AllPlay Count
            String all = "allPlay";
            updateCommonCountByCata(all);
        } else {
            String mySwitch = "allSwitch";
            updateCommonCountByCata(mySwitch);
        }
    }

    private  void updateCommonCountByCata(String cata) {
        Cursor c = db.query("commoncount", new String[]{"count"},
                "cata=?", new String[]{cata}, null, null, null);
        int count = 0;
        while(c.moveToNext()) {
            count = c.getInt(c.getColumnIndex("count"));
        }
        c.close();
        ContentValues values = new ContentValues();
        values.put("count", count+1);
        db.update("commoncount", values, "cata=?", new String[]{cata});
        logCursor();
    }

    /*
     * Get 7 Integer Which recode the Count of Everyday's Play
     */
    public int[] getDaysCount() {
        int[] count = new int[7];
        String[] days = new String[] {
                "MonPlay", "TusePlay", "WedPlay", "ThurPlay", "FriPlay", "SatPlay", "SunPlay"
        };
        Cursor c = db.rawQuery("SELECT * FROM commoncount", null);
        while(c.moveToNext()) {
            String cata = c.getString(c.getColumnIndex("cata"));
            int position = getMeetPosition(days, cata);
            if (position != -1) {
                count[position] = c.getInt(c.getColumnIndex("count"));
            }
        }
        c.close();
        return count;
    }

    /*
     * Get the Count of All Play
     */
    public int getAllPlayCount() {
        int count = 0;
        Cursor c = db.query("commoncount", new String[]{"count"},
                "cata=?", new String[]{"allPlay"}, null, null, null);
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
        Cursor c = db.query("commoncount", new String[]{"count"},
                "cata=?", new String[]{"allSwitch"}, null, null, null);
        while(c.moveToNext()) {
            count = c.getInt(c.getColumnIndex("count"));
        }
        c.close();
        return count;
    }


    /*
     * Get 24 Integer Which recode the Count of Everyhour's Play
     */
    public int[] getHoursCount() {
        int[] count = new int[24];
        String[] hours = new String[] {
                "clock0Play", "clock1Play", "clock2Play", "clock3Play", "clock4Play",
                "clock5Play", "clock6Play", "clock7Play", "clock8Play", "clock9Play",
                "clock10Play", "clock11Play", "clock12Play", "clock13Play", "clock14Play",
                "clock15Play", "clock16Play", "clock17Play", "clock18Play", "clock19Play",
                "clock20Play", "clock21Play", "clock22Play", "clock23Play"
        };
        Cursor c = db.rawQuery("SELECT * FROM commoncount", null);
        while(c.moveToNext()) {
            String cata = c.getString(c.getColumnIndex("cata"));
            int position = getMeetPosition(hours, cata);
            if (position != -1) {
                count[position] = c.getInt(c.getColumnIndex("count"));
            }
        }
        c.close();
        return count;
    }
    private int getMeetPosition(String[] strs, String str) {
        for (int i = 0 ; i < strs.length ; i++) {
            if (strs[i].equals(str))
                return i;
        }
        return -1;
    }


    /*****************************
     * Log the Cursor for Debug
     ****************************/
    public void logCursor() {
        Cursor c = db.rawQuery("SELECT * FROM commoncount", null);
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
