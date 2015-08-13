package com.badprinter.yobey.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import com.badprinter.yobey.commom.Constants;
import com.badprinter.yobey.models.Song;
import com.badprinter.yobey.models.SongNoPhoto;
import com.badprinter.yobey.utils.SongProvider;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by root on 15-8-12.
 */
public class PlayerService extends Service {
    private final String TAG = "PlayerService";
    private int duration;
    private int current = 0;
    private boolean isPlay = false;
    /*
     * 0 = LoopPlaying
     * 1 = SingPlaying
     * 2 = RandomPlaying
     */
    private int mode = 0;
    private MediaPlayer player;
    private ArrayList<SongNoPhoto> songList;
    private SongProvider songProvider;

    @Override
    public void onCreate() {
        super.onCreate();
        player = new MediaPlayer();
        isPlay = false;
        songList = new ArrayList<SongNoPhoto>();
        songProvider = new SongProvider(PlayerService.this);
        songList = songProvider.getSongNoPhotoList();

        System.out.println("Service Create");
    }
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags,  int startId) {
        //System.out.println("onStartCommand StartID :" + Integer.toString(startId));

        switch (intent.getExtras().getString("controlMsg")) {
            case Constants.PlayerControl.PRE_SONG_MSG:
                playPre();
                break;
            case Constants.PlayerControl.NEXT_SONG_MSG:
                playNext();
                break;
            case Constants.PlayerControl.PAUSE_PLAYING_MSG:
                pause();
                break;
            case Constants.PlayerControl.CONTINUE_PLAYING_MSG:
                resum();
                break;
            case Constants.PlayerControl.PLAYING_MSG:
                play(0);
                break;
        }
        Intent sendIntent = new Intent(Constants.UiControl.UPDATE_UI);
        sendIntent.putExtra("current", current);
        sendIntent.putExtra("isPlay", isPlay);
        sendBroadcast(sendIntent);

        /*
         * THe Big Big Bug had Happened Here! I Return StartId Instead of START_STICKY Before.
         */
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
    }

    private void play(int seekPos) {
        try {
            if (player.isPlaying())
                player.stop();
            player.reset();
            player.setDataSource(songList.get(current).getFileName());
            player.prepare();
            player.setOnPreparedListener(new MyPreparedListener(seekPos));
            //player.start();
            isPlay = true;
            duration = songList.get(current).getDuration();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * Get A Random Num that Is No Equal to CurrentNum
     */
    private int getRandom(int current) {
        int random;
        do {
            random = (int)Math.random()*songList.size();
        } while(random != current);
        return random;
    }

    /*
     * Play Pre Song
     */
    private void playPre() {
        Log.d(TAG, "Play Pre!");
        if (mode == 0) {
            current--;
            current = current < 0 ? songList.size() - 1 : current;  // current trun to max position if current < 0
            play(0);
        } else if (mode == 1) {
            play(0);
        } else {
            current = getRandom(current);
            play(0);
        }
    }

    /*
     * Play Next Song
     */
    private void playNext() {
        Log.d(TAG, "Play Next!");
        if (mode == 0) {
            current++;
            current = current > (songList.size() - 1) ? 0 : current;  // current trun to 0 position if current > max
            play(0);
        } else if (mode == 1) {
            play(0);
        } else {
            current = getRandom(current);
            play(0);
        }
    }

    /*
     * Pause Music
     */
    private void pause() {
        Log.d(TAG, "Pause!");
        if (isPlay) {
            player.pause();
        }
        isPlay = false;
    }

    /*
     * Resum Music
     */
    private void resum() {
        Log.d(TAG, "Resum!");
        if (!isPlay) {
            player.start();
        }
        isPlay = true;
    }

    /*
     * the Class For Playing Music
     */
    private class MyPreparedListener implements MediaPlayer.OnPreparedListener {
        private int seekPos;
        public MyPreparedListener(int seekPos) {
            this.seekPos = seekPos;
        }
        @Override
        public void onPrepared(MediaPlayer mp) {
            player.start();
            if (seekPos > 0) {
                player.seekTo(seekPos);
            }
        }
    }
}
