package com.badprinter.yobey.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;

import com.badprinter.yobey.R;
import com.badprinter.yobey.commom.Constants;
import com.badprinter.yobey.db.DBManager;
import com.badprinter.yobey.models.Song;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

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
    private static List<String> artistList = null;
    private static Map<Long, Song> songIdMap;
    private static DBManager dbMgr;

    public static void init(Context context) {
        dbMgr = new DBManager(context);
    }

    public static List<Song> getSongList(Context context) {
        init(context);
        if (songList == null) {
            songList = new ArrayList<>();
            artistList = new ArrayList<>();
            songIdMap = new Hashtable<>();
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
                String album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
                int duration = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                int size = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
                long albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String year = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.YEAR));
                String genre = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);
                Song newSong = new Song(id, name, fileName, size, album, artist,
                        duration, albumId, url, pinyin, year, genre);
                songIdMap.put(id, newSong);
                songList.add(newSong);

                if (!artistList.contains(artist)) {
                    artistList.add(artist);
                }
            }
            cursor.close();
            sortByPinyin(songList);
        }
        return songList;
    }
    /*
     * Get SongList by Artist Name.
     */
    public static List<Song> getSongListByArtist(Context context, String artist) {
        if (songList == null) {
            getSongList(context);
        }
        List<Song> songListOfArtist = new ArrayList<Song>();
        for (Song song : songList) {
            if (song.getArtist() == artist) {
                songListOfArtist.add(song);
            }
        }
        return songListOfArtist;
    }
    /*
     * Get ArtistList
     */
    public static List<String> getArtistList(Context context) {
        if (artistList == null) {
            getSongList(context);
        }
        return artistList;
    }
    /*
     * Get Favorite List
     */
    public static List<Song> getFavoriteList(Context context) {
        if (songList == null) {
            getSongList(context);
        }
        List<Song> favoriteSongList = new ArrayList<Song>();
        for (Song song : songList) {
            if (dbMgr.isFavorite(song)) {
                favoriteSongList.add(song);
            }
        }
        return favoriteSongList;
    }
    public static List<Song> getSongListByName(Context context, String listName) {
        if (songList == null)
            getSongList(context);
        List<Song> listOfName = new ArrayList<>();
        if (listName.equals(Constants.ListName.LIST_FAVORITE)) {
            listOfName = SongProvider.getFavoriteList(context);
        } else if (listName.equals(Constants.ListName.LIST_ALL)) {
            listOfName = SongProvider.getSongList(context);
        } else {
            listOfName = SongProvider.getSongList(context);
        }
        return listOfName;
    }
    public static void sortByPinyin(List<Song> list) {
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
    public static Song getSongById(Long id, Context context) {
        if (songList == null)
            getSongList(context);
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
