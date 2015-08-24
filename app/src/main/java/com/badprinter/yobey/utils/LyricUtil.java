package com.badprinter.yobey.utils;

import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.renderscript.Element;
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

    public void inti(String path, String name, String artist) {

        filePath = path;
        this.name = name;
        this.artist = artist;

        File file = new File(path);

        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "GBK");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String s = "";
            List<String> lyricFile = new ArrayList<String>();
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
            Log.d(TAG, "no found lyric file, I am trying to connect the net fro it!");
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
    private String getLyricFromString(String str) {
        String ss = str.substring(str.indexOf("["), str.indexOf("]") + 1);
        str = str.replace(ss, "");
        return str;
    }

    public List<String> getLyricList() {
        return lyricList;
    }
    public List<Integer> getTimeList() {
        return timeList;
    }

    private void loadLyricFromWeb(String name) {
        String url = "http://box.zhangmen.baidu.com/x?op=12&count=1&title=" + name + "$$" + artist + "$$$$";
        new DownloadTask().execute(url);
    }

    private class DownloadTask extends AsyncTask<String, Void, byte[]> {
        @Override
        protected byte[] doInBackground(String... urls) {
            try {
                /*
                 * Get songId for getting lyric
                 */
                String name = BytesUtil.bytesToHex(LyricUtil.this.name.getBytes("utf-8"));
                String artist = BytesUtil.bytesToHex(LyricUtil.this.artist.getBytes("utf-8"));
                String url =
                        "http://box.zhangmen.baidu.com/x?op=12&count=1&title="+name+"$$"+artist+"$$$$";
                /*Document doc = Jsoup.connect(url).get();
                Elements eles = doc.getElementsByTag("lrcid");
                if (eles.size() == 0) {
                    byte[] nullBytes = {};
                    return nullBytes;
                }
                String id = eles.get(0).text();*/
                /*
                 * Get lyric and save as file for next time to fetch
                 */
                /*String url1 = "http://box.zhangmen.baidu.com/bdlrc/" + Integer.toString(Integer.parseInt(id)/100) +
                        "/" + id + ".lrc";
                Connection.Response resultImageResponse = Jsoup.connect(url1).ignoreContentType(true).execute();
                byte[] bytes = resultImageResponse.bodyAsBytes();
                return bytes;*/
                return test();
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
                Log.d(TAG, "connct failed! Cannot find lyric file in the net");
                lyricList.add("没有找到可下载的歌词文件");
                timeList.add(0);
                return;
            }
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(filePath);
                OutputStreamWriter writer = new OutputStreamWriter(out, "GBK");
                /*out.write(result, 0, result.length-1);
                out.flush();
                out.close();*/
                String outStr = new String(result, 0, result.length-1, "GBK");
                writer.write(outStr, 0, outStr.length()-1);
                writer.flush();
                Log.d(TAG, outStr);
               /* writer.close();
                out.close();*/
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                Log.d(TAG, "download lyric of " + name + " successful!");
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
    private byte[] test() throws IOException{
        Log.e("geciceshi!!! : " , "has test!!");
        String searchDownLoadUrl = "http://www.lrc123.com/?keyword="+ URLEncoder.encode(name,"utf-8")+"+"+
                URLEncoder.encode(artist,"utf-8")+"&field=all";
        String url = "http://www.lrc123.com";
        boolean hasfound = false;
        Document doc = Jsoup.connect(searchDownLoadUrl).get();
        Elements eles = doc.getElementsByTag("a");
        if (eles.size() == 0) {
            byte[] nullBytes = {};
            return nullBytes;
        }
        //String id = eles.get(0).text();
        for (int i = 0 ; i < eles.size() ; i++) {
            Pattern pattern = Pattern.compile("^/download/lrc/\\d+-\\d+\\.aspx$");
            //Elements hrefs = eles.get(i).getElementsByAttribute("href");
            if (eles.get(i).attr("href") != "") {
                String str = eles.get(i).attr("href");
                Matcher matcher = pattern.matcher(str);
                if (matcher.matches()) {
                    Log.e("geciceshi!!! : " , "has match!");
                    url += str;
                    hasfound = true;
                    break;
                }
            }
        }
        if (!hasfound) {
            Log.e("geciceshi!!! : " , "has not found!");
            byte[] nullBytes = {};
            return nullBytes;
        }
        Connection.Response resultImageResponse = Jsoup.connect(url).ignoreContentType(true).execute();
        byte[] bytes = resultImageResponse.bodyAsBytes();
        return bytes;
    }


}
