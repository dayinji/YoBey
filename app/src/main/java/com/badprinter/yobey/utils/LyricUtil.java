package com.badprinter.yobey.utils;

import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.renderscript.Element;
import android.text.BoringLayout;
import android.util.Log;
import android.widget.Toast;


import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
    private DownloadTask myTask = new DownloadTask();

    public void inti(String path, String name, String artist) {

        this.filePath = path;
        this.name = name;
        this.artist = artist;
        if (!myTask.isCancelled()) {
            myTask.cancel(true);
        }
        lyricList.clear();
        timeList.clear();


        File file = new File(path);

        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "GBK");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String s = "";
            /*
             * Save every lyric begin with [min:s.ms]
             * Every item only can exist a [min:s.ms]
             */
            while ((s = bufferedReader.readLine()) != null) {
                Pattern p=Pattern.compile("\\[\\d\\d\\:\\d\\d\\.\\d\\d\\]");
                Matcher m=p.matcher(s);
                int count = 0;
                int endPos = 0;
                while(m.find()) {
                    count++;
                    timeList.add(getTimeFromString(s.substring(m.start(), m.end())));
                    endPos = m.end();
                }
                for (int i = 0 ; i < count ; i++) {
                    lyricList.add(s.substring(endPos, s.length()));
                }
            }

            bufferedReader.close();
            inputStreamReader.close();
            fileInputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            loadLyricFromWeb(name);
        } catch (IOException e) {
            e.printStackTrace();
            lyricList.add("没有读取到歌词");
            timeList.add(0);
        }
        sortList();
    }

    private int getTimeFromString(String str) {
        int min = Integer.parseInt(str.substring(1, 3));
        int s = Integer.parseInt(str.substring(4, 6));
        int ms = Integer.parseInt(str.substring(7, 9));
        int time = (min*60 + s)*1000 + ms*10;
        return time;
    }

    public List<String> getLyricList() {
        return lyricList;
    }
    public List<Integer> getTimeList() {
        return timeList;
    }

    private void loadLyricFromWeb(String name) {
        String url = "http://box.zhangmen.baidu.com/x?op=12&count=1&title=" + name + "$$" + artist + "$$$$";
        //new DownloadTask().execute(url);
        myTask = new DownloadTask();
        myTask.execute(url);
    }

    private class DownloadTask extends AsyncTask<String, Void, byte[]> {
        @Override
        protected byte[] doInBackground(String... urls) {
            try {
                return downLoadLryic();
            } catch (IOException e) {
                e.printStackTrace();
            }
            byte[] nullBytes = {};
            return nullBytes;
        }

        /**
         * Uses the logging framework to display the output of the fetch
         * operation in the log fragment.
         */
        @Override
        protected void onPostExecute(byte[] result) {
            if (result.length == 0) {
                lyricList.add("没有找到可下载的歌词文件");
                timeList.add(0);
                return;
            }
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(filePath);
                OutputStreamWriter writer = new OutputStreamWriter(out, "GBK");
                String outStr = new String(result, 0, result.length-1, "GBK");
                writer.write(outStr, 0, outStr.length()-1);
                writer.flush();
                writer.close();
                out.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                inti(filePath, name, artist);
            }
        }
    }

    private void sortList() {
        for (int i = 1 ; i < timeList.size() ; i++) {
            for (int j = 0 ; j < i ; j++) {
                if (timeList.get(i) < timeList.get(j)) {
                    int temp = timeList.get(i);
                    String str = lyricList.get(i);
                    timeList.set(i, timeList.get(j));
                    lyricList.set(i, lyricList.get(j));
                    timeList.set(j, temp);
                    lyricList.set(j, str);

                }
            }
        }
    }

    /*
     * Download lyric from lrc123.com
     */
    private byte[] downLoadLryic() throws IOException{
        String searchDownLoadUrl;
        if (!name.equals("") && artist == null) {
            searchDownLoadUrl = "http://www.lrc123.com/?keyword=" + URLEncoder.encode(name, "utf-8") + "&field=all";
        } else if (!name.equals("") && !artist.equals("")) {
            searchDownLoadUrl = "http://www.lrc123.com/?keyword=" + URLEncoder.encode(name, "utf-8") + "+" +
                    URLEncoder.encode(artist, "utf-8") + "&field=all";
        } else {
            byte[] nullBytes = {};
            return nullBytes;
        }
        String url = "http://www.lrc123.com";
        boolean hasfound = false;

        Document doc = Jsoup.connect(searchDownLoadUrl).timeout(5000).get();
        Elements eles = doc.getElementsByTag("a");
        for (int i = 0 ; i < eles.size() ; i++) {
            Pattern pattern = Pattern.compile("^/download/lrc/\\d+-\\d+\\.aspx$");
            if (eles.get(i).attr("href") != "") {
                String str = eles.get(i).attr("href");
                Matcher matcher = pattern.matcher(str);
                if (matcher.matches()) {
                    url += str;
                    hasfound = true;
                    break;
                }

            }
        }
        /*
         * If no found lyric, return null
         */
        if (!hasfound) {
            byte[] nullBytes = {};
            return nullBytes;
        }

        Connection.Response resultImageResponse = Jsoup.connect(url).ignoreContentType(true).execute();
        byte[] bytes = resultImageResponse.bodyAsBytes();
        return bytes;
    }


}
