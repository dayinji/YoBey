package com.badprinter.yobey.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;

import com.badprinter.yobey.R;
import com.badprinter.yobey.commom.AppContext;
import com.badprinter.yobey.commom.Constants;
import com.badprinter.yobey.db.DBManager;
import com.badprinter.yobey.models.Artist;
import com.badprinter.yobey.models.Song;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by root on 15-8-12.
 */
public class SongProvider {
    private final static String TAG = "SongProvider";
    private static final Uri albumArtUri = Uri.parse("content://media/external/audio/albumart");
    /*
     * Save the songList for saving time for starting PlayerService.
     */
    private static List<Song> songList = null;
    private static List<Artist> artistList = null;
    private static List<Song> recommendList = null;
    private static Map<Long, Song> songIdMap;
    private static Map<String, Artist> artistMap;
    private static DBManager dbMgr;
    private static Context context = AppContext.getInstance();

    public static void init(Context context) {
        dbMgr = new DBManager();
    }

    public static List<Song> getSongList() {
        init(context);
        if (songList == null) {
            songList = new ArrayList<>();
            artistList = new ArrayList<>();
            songIdMap = new HashMap<>();
            artistMap = new HashMap<>();
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    null, null, null, null);
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                byte[] data = cursor.getBlob(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String fileName = new String(data, 0, data.length-1);
                mmr.setDataSource(fileName);
                String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                // test
                String pinyin = PinYinUtil.getPinYinFromHanYu(name, PinYinUtil.UPPER_CASE,
                        PinYinUtil.WITH_TONE_NUMBER, PinYinUtil.WITH_V);
                String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                if (artist == null)
                    artist = "(无名)";
                String album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
                int duration = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                int size = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
                long albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String year = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.YEAR));
                String genre = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);

                // Store the Song Info
                Song newSong = new Song(id, name, fileName, size, album, artist,
                        duration, albumId, url, pinyin, year, genre);
                songIdMap.put(id, newSong);
                songList.add(newSong);

                //Init the DB
                if (!dbMgr.inSongDetail(newSong)) {
                    dbMgr.addToSongDetail(newSong);
                }

                // Store the Artist Info
                if (!artistMap.containsKey(artist)) {
                    Artist artistItem = new Artist(artist);
                    artistItem.addSong(newSong);
                    artistList.add(artistItem);
                    artistMap.put(artist, artistItem);
                } else {
                    artistMap.get(artist).addSong(newSong);
                }
            }
            cursor.close();
            sortSongByPinyin(songList);
            sortArtistByPinyin(artistList);
        }
        return songList;
    }
    /*
     * Get SongList by Artist Name.
     */
    public static List<Song> getSongListByArtist(String artist) {
        if (songList == null) {
            getSongList();
        }
        //List<Song> songListOfArtist = new ArrayList<Song>();
        Artist temp = artistMap.get(artist);
        return temp.getSongListOfArtist();
    }
    /*
     * Get ArtistList
     */
    public static List<Artist> getArtistList() {
        if (artistList == null) {
            getSongList();
        }
        return artistList;
    }
    /*
     * Get Favorite List
     */
    public static List<Song> getFavoriteList() {
        if (songList == null) {
            getSongList();
        }
        List<Song> favoriteSongList = new ArrayList<Song>();
        for (Song song : songList) {
            if (dbMgr.isFavorite(song)) {
                favoriteSongList.add(song);
            }
        }
        return favoriteSongList;
    }
    /*
     * Get FavoriteArtist List
     */
    public static List<Artist> getFavoriteArtistList() {
        if (songList == null) {
            getSongList();
        }
        List<Artist> favoriteArtistSongList = new ArrayList<Artist>();
        for (Artist a : artistList) {
            if (dbMgr.isFavoriteArtist(a.getName())) {
                favoriteArtistSongList.add(a);
            }
        }
        return favoriteArtistSongList;
    }
    /*
     * Get SongList By SongList Name
     */
    public static List<Song> getSongListByName(String listName) {
        if (songList == null)
            getSongList();
        List<Song> listOfName = new ArrayList<>();
        if (listName.equals(Constants.ListName.LIST_FAVORITE)) {
            listOfName = getFavoriteList();
        } else if (listName.equals(Constants.ListName.LIST_ALL)) {
            listOfName = getSongList();
        } else if (listName.equals(Constants.ListName.LIST_RECENTLY)){
            listOfName = getRecentlySongs();
        } else if (listName.equals(Constants.ListName.LIST_AGO)) {
            listOfName = getAgoSongs();
        }  else if (listName.equals(Constants.ListName.LIST_RECOMMEND)) {
            listOfName = getRecommedSongs();
        } else {
            listOfName = getSongListByArtist(listName);
        }
        return listOfName;
    }
    /*
     * Get 10 Songs which are Recommed
     */
    public static List<Song> getRecommedSongs() {
        if (recommendList == null)
            updateRecommedSongs();
        return recommendList;
    }
    /*
     * Update 10 Songs which are Recommed
     */
    public static List<Song> updateRecommedSongs() {
        if (recommendList == null)
            recommendList = new ArrayList<>();
        else
            recommendList.clear();

        SharedPreferences preferences = AppContext.getInstance().getSharedPreferences(Constants.Preferences.PREFERENCES_KEY, Context.MODE_PRIVATE);
        int progressFS = preferences.getInt(Constants.Preferences.PREFERENCES_ADJUST_FAVORITE_SONG, 50);
        int progressFA = preferences.getInt(Constants.Preferences.PREFERENCES_ADJUST_FAVORITE_ARTIST, 50);
        int progressR = preferences.getInt(Constants.Preferences.PREFERENCES_ADJUST_RECENT, 50);
        int progressA = preferences.getInt(Constants.Preferences.PREFERENCES_ADJUST_AGO, 50);

        // Prevent Rate = 0
        float sum = progressA + progressFA + progressFS + progressR + 40;
        float rateFA = (progressFA + 10) /sum;
        float rateFS = (progressFS + 10) /sum;
        float rateR = (progressR + 10) /sum;
        float rateA = (progressA + 10) /sum;

        Set<Song> set = new HashSet<>();

        List<Song> listFA = new ArrayList<>();
        List<Song> listFS = getFavoriteList();
        List<Song> listR = getRecentlySongs();
        List<Song> listA = getAgoSongs();

        List<Artist>artistList = SongProvider.getFavoriteArtistList();
        for (Artist a : artistList) {
            for (Song s : a.getSongListOfArtist()) {
                listFA.add(s);
            }
        }

        // If the Count of All Songs Is Below 10
        if (listR.size() < 10) {
            Log.e(TAG, "listR.size = " + listR.size());
            return listR;
        }
        // Prevent Endless Loop
        int maxLoopCount = 100;

        while (set.size() < 10 && maxLoopCount > 0) {
            double randNum = Math.random();
            if (randNum < rateFA && listFA.size() != 0) {
                //Log.e(TAG, "cata : 喜欢的歌手" + listFA.get((int)(Math.random()*listFA.size())).getName());
                set.add(listFA.get((int)(Math.random()*listFA.size())));
            } else if (randNum >= rateFA && randNum < rateFA+rateFS && listFS.size() != 0) {
                //Log.e(TAG, "cata : 收藏的歌曲" + listFS.get((int)(Math.random()*listFS.size())).getName());
                set.add(listFS.get((int)(Math.random()*listFS.size())));
            } else if (randNum >= rateFA+rateFS && randNum < rateFA+rateFS+rateR) {
                //Log.e(TAG, "cata : 最近听过" + listR.get((int)(Math.random()*listR.size())).getName());
                set.add(listR.get((int)(Math.random()*listR.size())));
            } else if (randNum >= rateFA+rateFS+rateR && randNum < 1){
                //Log.e(TAG, "cata : 很久没听" + listA.get((int)(Math.random()*listA.size())).getName());
                set.add(listA.get((int)(Math.random()*listA.size())));
            } else {
                double randNum1 = Math.random();
                if (randNum1 < 0.5) {
                    //Log.e(TAG, "cata : 最近听过2" + listR.get((int)(Math.random()*listR.size())).getName());
                    set.add(listR.get((int)(Math.random()*listR.size())));
                } else {
                    //Log.e(TAG, "cata : 很久没听2" + listA.get((int)(Math.random()*listA.size())).getName());
                    set.add(listA.get((int)(Math.random()*listA.size())));
                }
            }
            maxLoopCount--;
            //Log.e(TAG, "maxLoopCount = " + maxLoopCount);
        }

        for (Song s : set)
            recommendList.add(s);

        sortSongByPinyin(recommendList);

        return recommendList;
    }
    /*
     * Get 10 Songs which are Listened Least
     */
    public static List<Song> getAgoSongs() {
        List<Song> list = new ArrayList<>();
        Cursor c = dbMgr.getAgoSongs();
        while(c.moveToNext()) {
            list.add(getSongById(c.getLong(c.getColumnIndex("song_id"))));
        }
        c.close();
        return list;
    }
    /*
     * Get 10 Songs which are Listened Recently
     */
    public static List<Song> getRecentlySongs() {
        List<Song> list = new ArrayList<>();
        Cursor c = dbMgr.getRecentlySongs();
        while(c.moveToNext()) {
            list.add(getSongById(c.getLong(c.getColumnIndex("song_id"))));
        }
        c.close();
        return list;
    }
    /*
     * Sort a List By Pinyin
     */
    public static void sortSongByPinyin(List<Song> list) {
        Collections.sort(list);
    }
    /*
     * Sort a ArtistList By Pinyin
     */
    public static void sortArtistByPinyin(List<Artist> list) {
        Collections.sort(list);
    }

    public static Bitmap getDefaultArtwork(Context context,boolean small) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = Bitmap.Config.RGB_565;
        if(small){
            return BitmapFactory.decodeResource(context.getResources(), R.drawable.playnext_00000);
        }
        return BitmapFactory.decodeResource(context.getResources(), R.drawable.playnext_00000);
    }

    /*
     * Get the Song with Id
     */
    public static Song getSongById(Long id) {
        if (songList == null)
            getSongList();
        return songIdMap.get(id);
    }

    /**
     * 从文件当中获取专辑封面位图
     * @param context
     * @param songid
     * @param albumid
     * @return
     */
    private static Bitmap getArtworkFromFile(Context context, long songid, long albumid){
        Bitmap bm = null;
        if(albumid < 0 && songid < 0) {
            throw new IllegalArgumentException("Must specify an album or a song id");
        }
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            FileDescriptor fd = null;
            if(albumid < 0){
                Uri uri = Uri.parse("content://media/external/audio/media/"
                        + songid + "/albumart");
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                if(pfd != null) {
                    fd = pfd.getFileDescriptor();
                }
            } else {
                Uri uri = ContentUris.withAppendedId(albumArtUri, albumid);
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                if(pfd != null) {
                    fd = pfd.getFileDescriptor();
                }
            }
            options.inSampleSize = 1;
            // 只进行大小判断
            options.inJustDecodeBounds = true;
            // 调用此方法得到options得到图片大小
            BitmapFactory.decodeFileDescriptor(fd, null, options);
            // 我们的目标是在800pixel的画面上显示
            // 所以需要调用computeSampleSize得到图片缩放的比例
            options.inSampleSize = 100;
            // 我们得到了缩放的比例，现在开始正式读入Bitmap数据
            options.inJustDecodeBounds = false;
            options.inDither = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            //根据options参数，减少所需要的内存
            bm = BitmapFactory.decodeFileDescriptor(fd, null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bm;
    }

    /**
     * 获取专辑封面位图对象
     * @param context
     * @param song_id
     * @param album_id
     * @param allowdefalut
     * @return
     */
    public static Bitmap getArtwork(Context context, long song_id, long album_id, boolean allowdefalut, boolean small){
        if(album_id < 0) {
            if(song_id < 0) {
                Bitmap bm = getArtworkFromFile(context, song_id, -1);
                if(bm != null) {
                    return bm;
                }
            }
            if(allowdefalut) {
                return getDefaultArtwork(context, small);
            }
            return null;
        }
        ContentResolver res = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(albumArtUri, album_id);
        if(uri != null) {
            InputStream in = null;
            try {
                in = res.openInputStream(uri);
                BitmapFactory.Options options = new BitmapFactory.Options();
                //先制定原始大小
                options.inSampleSize = 1;
                //只进行大小判断
                options.inJustDecodeBounds = true;
                //调用此方法得到options得到图片的大小
                BitmapFactory.decodeStream(in, null, options);
                /** 我们的目标是在你N pixel的画面上显示。 所以需要调用computeSampleSize得到图片缩放的比例 **/
                /** 这里的target为800是根据默认专辑图片大小决定的，800只是测试数字但是试验后发现完美的结合 **/
                if(small){
                    options.inSampleSize = computeSampleSize(options, 60);
                } else{
                    options.inSampleSize = computeSampleSize(options, 600);
                }
                // 我们得到了缩放比例，现在开始正式读入Bitmap数据
                options.inJustDecodeBounds = false;
                options.inDither = false;
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                in = res.openInputStream(uri);
                return BitmapFactory.decodeStream(in, null, options);
            } catch (FileNotFoundException e) {
                Bitmap bm = getArtworkFromFile(context, song_id, album_id);
                if(bm != null) {
                    if(bm.getConfig() == null) {
                        bm = bm.copy(Bitmap.Config.RGB_565, false);
                        if(bm == null && allowdefalut) {
                            return getDefaultArtwork(context, small);
                        }
                    }
                } else if(allowdefalut) {
                    bm = getDefaultArtwork(context, small);
                }
                return bm;
            } finally {
                try {
                    if(in != null) {
                        in.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 对图片进行合适的缩放
     * @param options
     * @param target
     * @return
     */
    public static int computeSampleSize(BitmapFactory.Options options, int target) {
        int w = options.outWidth;
        int h = options.outHeight;
        int candidateW = w / target;
        int candidateH = h / target;
        int candidate = Math.max(candidateW, candidateH);
        if(candidate == 0) {
            return 1;
        }
        if(candidate > 1) {
            if((w > target) && (w / candidate) < target) {
                candidate -= 1;
            }
        }
        if(candidate > 1) {
            if((h > target) && (h / candidate) < target) {
                candidate -= 1;
            }
        }
        return candidate;
    }


}
