package com.badprinter.yobey.utils;

import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.util.Log;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by root on 15-8-15.
 */
public class LyricUtil {
    private final String TAG = "LyricUtil";
    private String filePath;
    private String name;
    private String artist;
    private List<String> lyricList = new ArrayList<String>();
    private List<Integer> timeList =  new ArrayList<Integer>();
    public void inti(String path, String name, String artist) {

        filePath = path;
        this.name = name;
        this.artist = artist;

        File file = new File(path);

        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String s = "";
            while ((s = bufferedReader.readLine()) != null) {
                if (Pattern.matches("^\\[\\d\\d\\:\\d\\d\\.\\d\\d\\].*", s)) {
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
            timeList.add(0);
            //loadLyricFromWeb(name);
        } catch (IOException e) {
            e.printStackTrace();
            lyricList.add("没有读取到歌词");
            timeList.add(0);
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

    private void loadLyricFromWeb(String name) {
        String url = "http://box.zhangmen.baidu.com/x?op=12&count=1&title=" + name + "$$" + artist + "$$$$";
        //String url = "http://box.zhangmen.baidu.com/bdlrc/86/8654.lrc";
        /*try {
            Log.e(TAG, "OK");
            Document doc = Jsoup.connect("http://www.badprinter.com/").get();
            //String title = doc.title();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        Log.e(TAG, "OK");

        Log.d(TAG, url);
        new DownloadTask().execute(url);

    }

    private class DownloadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                return loadFromNetwork(urls[0]);
            } catch (IOException e) {
                return "IOException";
            }
        }

        /**
         * Uses the logging framework to display the output of the fetch
         * operation in the log fragment.
         */
        @Override
        protected void onPostExecute(String result) {
            Log.i(TAG, result);
            System.out.println(result);
        }
    }


    private String loadFromNetwork(String urlString) throws IOException {
        InputStream stream = null;
        String str ="";

        try {
            stream = downloadUrl(urlString);
            str = readIt(stream, 5000);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
        return str;
    }

    private InputStream downloadUrl(String urlString) throws IOException {
        // BEGIN_INCLUDE(get_inputstream)
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Start the query
        conn.connect();
        InputStream stream = conn.getInputStream();
        return stream;
        // END_INCLUDE(get_inputstream)
    }

    private String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "ASCII");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }

}
