package com.badprinter.yobey.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.renderscript.Element;
import android.telephony.TelephonyManager;
import android.text.BoringLayout;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;


import com.badprinter.yobey.commom.AppContext;
import com.badprinter.yobey.commom.Constants;

import org.json.JSONArray;
import org.json.JSONObject;
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
import java.net.URLDecoder;
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
    public OnDownLoadLyric callback;

    public void init(String path, String name, String artist) {

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
            if (canDownLoad())
                loadLyricFromWeb(name);
            else if (is2G()) {
                lyricList.add("当前为2G网络,如需下载歌词请到设置页设置");
                timeList.add(0);
            } else {
                lyricList.add("下载不到歌词");
                timeList.add(0);
            }
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
        //String url = "http://box.zhangmen.baidu.com/x?op=12&count=1&title=" + name + "$$" + artist + "$$$$";
        //new DownloadTask().execute(url);
        myTask = new DownloadTask();
        myTask.execute();
    }

    private class DownloadTask extends AsyncTask<Void, Void, byte[]> {
        @Override
        protected byte[] doInBackground(Void... urls) {
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
            Log.e(TAG, "I got it(Lyric)!");
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(filePath);
                OutputStreamWriter writer = new OutputStreamWriter(out, "GBK");
                String outStr = new String(result, 0, result.length-1, "UTF-8");
                writer.write(outStr, 0, outStr.length()-1);
                writer.flush();
                writer.close();
                out.close();
                // Reset the View Hight
                callback.onDownLoadLyric();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
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
     * Download lyric
     */
    private byte[] downLoadLryic() throws IOException{
        String searchDownLoadUrl1 = "";
        String searchDownLoadUrl2 = "";
        String lrcUrl = "http://music.baidu.com";

        searchDownLoadUrl1 = "http://sug.music.baidu.com/info/suggestion?format=json&version=2&from=0&word=" +
                URLEncoder.encode(name, "utf-8") + "&_=1405404358299";
        boolean hasfound = false;

        Document doc1 = Jsoup.connect(searchDownLoadUrl1).timeout(5000).get();
        Elements bodys = doc1.getElementsByTag("body");
        org.jsoup.nodes.Element body = bodys.get(0);
        Log.e(TAG, "body1 = " + body.text());
        try {
            JSONObject jsonObject1 = new JSONObject(body.text());
            JSONArray jsonArray = jsonObject1.getJSONObject("data").getJSONArray("song");
            for (int i = 0 ; i < jsonArray.length() ; i++) {
                JSONObject jsonObject2 = (JSONObject)jsonArray.get(i);
                String songName = (String)jsonObject2.get("songname");
                String songArtist = (String)jsonObject2.get("artistname");
                if (URLDecoder.decode(songName, "utf-8").equals(name) &&
                        URLDecoder.decode(songArtist, "utf-8").equals(artist)) {
                    String songid = (String)jsonObject2.get("songid");
                    searchDownLoadUrl2 = "http://music.baidu.com/data/music/links?songIds=" +
                            songid + "&format=json";
                    Log.e(TAG, "searchDownLoadUrl2 = " + searchDownLoadUrl2);
                    break;
                }
                Log.e(TAG, "match failed! : songName = " + URLDecoder.decode(songName, "utf-8") +
                ". songArtist = " + URLDecoder.decode(songArtist, "utf-8"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!searchDownLoadUrl2.equals("")) {
            String body2 = Jsoup.connect(searchDownLoadUrl2).ignoreContentType(true).execute().body();
            try {
                JSONObject jsonObject3 = new JSONObject(body2);
                JSONArray jsonArray = jsonObject3.getJSONObject("data").getJSONArray("songList");
                JSONObject jsonObject4 = (JSONObject)jsonArray.get(0);
                String url = (String)jsonObject4.get("lrcLink");
                if (!url.equals("")) {
                    lrcUrl += url.replace("\\", "");
                    Log.e(TAG, "lrcUrl = " + lrcUrl);
                    hasfound = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        /*
         * If no found lyric, return null
         */
        if (!hasfound) {
            byte[] nullBytes = {};
            return nullBytes;
        }

        Connection.Response resultImageResponse = Jsoup.connect(lrcUrl).ignoreContentType(true).execute();
        byte[] bytes = resultImageResponse.bodyAsBytes();
        return bytes;
    }
    public interface OnDownLoadLyric {
        void onDownLoadLyric();
    }
    private boolean canDownLoad() {
        Context context = AppContext.getInstance();
        SharedPreferences sharedPref = context.getSharedPreferences(Constants.Preferences.PREFERENCES_KEY, Context.MODE_PRIVATE);
        boolean isOnly34Wifi = sharedPref.getInt(Constants.Preferences.PREFERENCES_WIFI, 1) == 1 ? true : false;
        int netWorkMode = 3;
        final int NETWORKTYPE_WIFI = 0;
        final int NETWORKTYPE_4G = 1;
        final int NETWORKTYPE_3G = 2;
        final int NETWORKTYPE_2G = 3;
        final int NETWORKTYPE_WAP = 4;
        final int NETWORKTYPE_INVALID = 5;
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            String type = networkInfo.getTypeName();
            if (type.equalsIgnoreCase("WIFI")) {
                netWorkMode = NETWORKTYPE_WIFI;
            } else if (type.equalsIgnoreCase("MOBILE")) {
                String proxyHost = android.net.Proxy.getDefaultHost();
                netWorkMode = TextUtils.isEmpty(proxyHost)
                        ? (isFastMobileNetwork() ? NETWORKTYPE_3G : NETWORKTYPE_2G)
                        : NETWORKTYPE_WAP;
            }
        } else {
            netWorkMode = NETWORKTYPE_INVALID;
        }
        Log.e(TAG, "netWorkMode = " + netWorkMode);
        Log.e(TAG, "isOnly34Wifi = " + isOnly34Wifi);
        if (!isOnly34Wifi && netWorkMode != NETWORKTYPE_INVALID)
            return true;
        else if (isOnly34Wifi && (netWorkMode == 0 || netWorkMode == 1 || netWorkMode == 2))
            return true;
        else
            return false;
    }
    private boolean is2G() {
        Context context = AppContext.getInstance();
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            String type = networkInfo.getTypeName();
            if (type.equalsIgnoreCase("MOBILE")) {
                String proxyHost = android.net.Proxy.getDefaultHost();
                return !isFastMobileNetwork();
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    private boolean isFastMobileNetwork() {
        Context context = AppContext.getInstance();
        TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        switch (telephonyManager.getNetworkType()) {
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                return false; // ~ 50-100 kbps
            case TelephonyManager.NETWORK_TYPE_CDMA:
                return false; // ~ 14-64 kbps
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return false; // ~ 50-100 kbps
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                return true; // ~ 400-1000 kbps
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                return true; // ~ 600-1400 kbps
            case TelephonyManager.NETWORK_TYPE_GPRS:
                return false; // ~ 100 kbps
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                return true; // ~ 2-14 Mbps
            case TelephonyManager.NETWORK_TYPE_HSPA:
                return true; // ~ 700-1700 kbps
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                return true; // ~ 1-23 Mbps
            case TelephonyManager.NETWORK_TYPE_UMTS:
                return true; // ~ 400-7000 kbps
            case TelephonyManager.NETWORK_TYPE_EHRPD:
                return true; // ~ 1-2 Mbps
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                return true; // ~ 5 Mbps
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return true; // ~ 10-20 Mbps
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return false; // ~25 kbps
            case TelephonyManager.NETWORK_TYPE_LTE:
                return true; // ~ 10+ Mbps
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                return false;
            default:
                return false;
        }
    }

}
