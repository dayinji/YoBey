package com.badprinter.yobey.models;

import android.graphics.Bitmap;

/**
 * Created by root on 15-8-12.
 */
public class Song implements Comparable<Song>{
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
    private String url;
    private String pinyin;
    private String date;

    public Song(long id, String name, String fileName, int size, String album,
                String artist, int duration, long albumId, String url,
                String pinyin) {
        this.name = name;
        this.fileName = fileName;
        this.size = size;
        this.album = album;
        this.artist =  artist;
        this.duration = duration;
        this.id = id;
        this.albumId = albumId;
        this.url = url;
        this.pinyin = pinyin;
    }

    public int compareTo(Song other) {
        char[] otherPinyin = other.getPinyin().toCharArray();
        char[] thisPinyin = pinyin.toCharArray();
        int length = otherPinyin.length > thisPinyin.length ? thisPinyin.length : otherPinyin.length;
        for (int i = 0 ; i < length ; i++) {
            if (thisPinyin[i] < otherPinyin[i])
                return -1;
            else if (thisPinyin[i] > otherPinyin[i])
                return 1;
        }
        if (thisPinyin.length < otherPinyin.length)
            return -1;
        else if (thisPinyin.length > otherPinyin.length)
            return 1;
        return 0;
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

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getPinyin() { return pinyin; }
    public void setPinyin(String pinyin) { this.pinyin = pinyin; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

}
