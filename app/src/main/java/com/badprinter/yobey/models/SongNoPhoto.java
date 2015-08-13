package com.badprinter.yobey.models;

import android.graphics.Bitmap;

/**
 * Created by root on 15-8-12.
 */
public class SongNoPhoto {
    /*
 *
 */
    private String name;
    private String fileName;
    private int size;
    private String album;
    private String artist;
    private int duration;

    public SongNoPhoto(String name, String fileName, int size, String album,
                String artist, int duration) {
        this.name = name;
        this.fileName = fileName;
        this.size = size;
        this.album = album;
        this.artist =  artist;
        this.duration = duration;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public String getAlbum() { return album; }
    public void setAlbum(String name) { this.album = album; }

    public String getArtist() { return artist; }
    public void setArtist(String fileName) { this.artist = artist; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

}
