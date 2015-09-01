package com.badprinter.yobey.models;

import com.badprinter.yobey.utils.PinYinUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;

/**
 * Created by root on 15-9-1.
 */
public class Artist implements Comparable<Artist>{
    private String name;
    private List<Song> songListOfArtist;
    private String pinyin;

    public Artist(String name) {
        this.name = name;
        pinyin = PinYinUtil.getPinYinFromHanYu(name, PinYinUtil.UPPER_CASE,
                PinYinUtil.WITH_TONE_NUMBER, PinYinUtil.WITH_V);
        songListOfArtist = new ArrayList<>();
    }

    public int compareTo(Artist other) {
        char[] otherPinyin = other.getPinyin().toCharArray();
        char[] thisPinyin = pinyin.toCharArray();
        int length = otherPinyin.length > thisPinyin.length ? thisPinyin.length : otherPinyin.length;
        for (int i = 0 ; i < length ; i++) {
            if (thisPinyin[i] < otherPinyin[i])
                return -1;
            else if (thisPinyin[i] > otherPinyin[i])
                return 1;
        }
        return 0;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public List<Song> getSongListOfArtist() {
        return songListOfArtist;
    }
    public void addSong(Song song) {
        songListOfArtist.add(song);
    }

    public String getPinyin() {
        return pinyin;
    }
    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }
}
