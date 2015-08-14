package com.badprinter.yobey.models;

import android.graphics.Bitmap;

/**
 * Created by root on 15-8-12.
 */
public class Song {
    /*
     *
     */
    private long id;
    private String name;
    private String fileName;
    private int size;
    private String album;
    private String artist;
    private int duration;
    private long albumId;
    //private Bitmap photo;

    public Song(long id, String name, String fileName, int size, String album,
                String artist, int duration, long albumId) {
        this.name = name;
        this.fileName = fileName;
        this.size = size;
        this.album = album;
        this.artist =  artist;
        this.duration = duration;
        this.id = id;
        this.albumId = albumId;
       // this.photo = photo;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

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

    public long getAlbumId() { return albumId; }
    public void setAlbumId(long albumId) { this.albumId = albumId; }

   /* public Bitmap getPhoto() { return photo; }
    public void setPhoto(Bitmap photo) { this.photo = photo; }*/
}
