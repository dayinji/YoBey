package com.badprinter.yobey.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.provider.MediaStore;

import com.badprinter.yobey.models.Song;
import com.badprinter.yobey.models.SongNoPhoto;

import java.util.ArrayList;

/**
 * Created by root on 15-8-12.
 */
public class SongProvider {
    private Context context;
    private ArrayList<Song> songList;
    private ArrayList<SongNoPhoto> songNoPhotoList;
    private MediaMetadataRetriever mmr;

    public SongProvider(Context context) {

        this.context = context;
        mmr = new MediaMetadataRetriever();
        songList = new ArrayList<Song>();
        songNoPhotoList = new ArrayList<SongNoPhoto>();
    }

    /*
     * Return SongList
     */
    public ArrayList<Song> getSongList() {
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, null, null, null);

        while (cursor.moveToNext()) {
            byte[] data = cursor.getBlob(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            String fileName = new String(data, 0, data.length-1);
            mmr.setDataSource(fileName);
            String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            String author = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            String album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            int duration = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            int size = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
            Bitmap photo = null;
            byte[] photoBytes =  mmr.getEmbeddedPicture();
            if(photoBytes!=null)
            {
                photo = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.length);
            }
            songList.add(new Song(name, fileName, size, album, author, duration, photo));
        }
        cursor.close();
        return songList;
    }


    /*
     * Return List without Photo
     */
    public ArrayList<SongNoPhoto> getSongNoPhotoList() {
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, null, null, null);

        while (cursor.moveToNext()) {
            byte[] data = cursor.getBlob(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            String fileName = new String(data, 0, data.length-1);
            mmr.setDataSource(fileName);
            String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            String author = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            String album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            int duration = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            int size = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
            songNoPhotoList.add(new SongNoPhoto(name, fileName, size, album, author, duration));
        }
        cursor.close();
        return songNoPhotoList;
    }


}
