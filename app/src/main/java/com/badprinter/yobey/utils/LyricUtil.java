package com.badprinter.yobey.utils;

import android.nfc.Tag;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by root on 15-8-15.
 */
public class LyricUtil {
    private String filePath;
    private List<String> lyricList = new ArrayList<String>();
    private List<Integer> timeList =  new ArrayList<Integer>();
    public void inti(String path) {
        filePath = path;
        File file = new File(path);

        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String s = "";
            while ((s = bufferedReader.readLine()) != null) {
                if ((s.indexOf("[ar:") != -1) || (s.indexOf("[ti:") != -1)
                        || (s.indexOf("[by:") != -1)|| (s.indexOf("[al:") != -1)
                        || (s.indexOf("[offset:") != -1)|| s.equals("")) {
                } else {
                    addTimeToList(s);
                    String ss = s.substring(s.indexOf("["), s.indexOf("]") + 1);
                    s = s.replace(ss, "");
                    lyricList.add(s);
                }
            }
            bufferedReader.close();
            inputStreamReader.close();
            fileInputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            lyricList.add("没有歌词文件");
        } catch (IOException e) {
            e.printStackTrace();
            lyricList.add("没有读取到歌词");
        }
    }

    private void addTimeToList(String str) {
        int min = Integer.parseInt(str.substring(1, 3));
        int s = Integer.parseInt(str.substring(4, 6));
        int ms = Integer.parseInt(str.substring(7, 9));
        int time = (min*60 + s)*1000 + ms*10;
        timeList.add(time);
        Log.e("LyricUtil", str);
    }

    public List<String> getLyricList() {
        return lyricList;
    }
    public List<Integer> getTimeList() {
        return timeList;
    }

}
