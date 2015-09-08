package com.badprinter.yobey.service;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.preference.Preference;
import android.support.v7.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.badprinter.yobey.R;
import com.badprinter.yobey.activities.Player;
import com.badprinter.yobey.activities.Welcome;
import com.badprinter.yobey.activities.Yobey;
import com.badprinter.yobey.commom.Constants;
import com.badprinter.yobey.customviews.WindowLyric;
import com.badprinter.yobey.db.DBManager;
import com.badprinter.yobey.models.Song;
import com.badprinter.yobey.utils.SongProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by root on 15-8-12.
 */
public class PlayerService extends Service{
    private final String TAG = "PlayerService";
    private int duration;
    private int current = 0;
    private int currentTime = 0;
    private Long currentSongId;
    private boolean isPlay = false;
    private String listName;
    private boolean hasNotify = true;
    private NotificationManager nm;
    private WindowLyric wl;
    private WindowManager wm;
    /*
     * 0 = LoopPlaying
     * 1 = SingPlaying
     * 2 = RandomPlaying
     */
    private int mode = 0;
    private List<Integer> randIndex;
    private MediaPlayer player;
    private List<Song> songList;
    private Handler handler;
    private Timer timer;
    private boolean isShowWl = true;
    private boolean isLockWL = false;
    private SharedPreferences sharedPref;

    private DBManager dbMgr;

    // When Calling Phone, Stop Playing Music
    private boolean mResumeAfterCall = false;
    private PhoneStateListener phoneStateListener = new PhoneStateListener(){
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            if (state == TelephonyManager.CALL_STATE_RINGING) {
                if (isPlay && !mResumeAfterCall) {
                    pause();
                    mResumeAfterCall = true;
                }
            } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                if (isPlay && !mResumeAfterCall) {
                    pause();
                    mResumeAfterCall = true;
                }
            } else if (state == TelephonyManager.CALL_STATE_IDLE) {
                // start playing again
                if (mResumeAfterCall) {
                    resum();
                    mResumeAfterCall = false;
                }
            }
            boardCurrentState();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        // Init sharedPref
        sharedPref = getSharedPreferences(
                Constants.Preferences.PREFERENCES_KEY, Context.MODE_PRIVATE);

        // When Calling Phone, Stop Playing Music
        TelephonyManager tmgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        tmgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

        wm = (WindowManager) getApplicationContext().getSystemService(
                WINDOW_SERVICE);

        dbMgr = new DBManager();
        isPlay = false;
        nm  = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        listName = Constants.ListName.LIST_ALL;
        songList = new ArrayList<Song>();
        songList = SongProvider.getSongList();
        currentSongId = songList.get(current).getId();
        player = new MediaPlayer();
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                updateDB(true, current);
                playNext();
                boardCurrentState();
                changeWl();
            }
        });
        init();
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    try {
                        Intent sendIntent = new Intent(Constants.UiControl.UPDATE_CURRENT);
                        currentTime = player.getCurrentPosition();
                        sendIntent.putExtra("currentTime", currentTime);
                        sendBroadcast(sendIntent);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(TAG, "Player has destory!");
                    }
                }
            }
        };
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(1);
            }
        }, 0, 500);

        // Init WindowLyric
        initWindowLyric();
    }
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags,  int startId) {

        switch (intent.getExtras().getString("controlMsg")) {
            case Constants.PlayerControl.PRE_SONG_MSG:
                if (currentTime > 20*1000 && currentTime < duration - 30*1000)
                    updateDB(false, current);
                else if (currentTime >= duration-30*1000)
                    updateDB(true, current);
                playPre();
                changeWl();
                break;
            case Constants.PlayerControl.NEXT_SONG_MSG:
                if (currentTime > 20 * 1000 && currentTime < duration - 30*1000)
                    updateDB(false, current);
                else if (currentTime >= duration-30*1000)
                    updateDB(true, current);
                playNext();
                changeWl();
                break;
            case Constants.PlayerControl.PAUSE_PLAYING_MSG:
                pause();
                break;
            case Constants.PlayerControl.CONTINUE_PLAYING_MSG:
                resum();
                break;
            case Constants.PlayerControl.PLAYING_MSG:
                current = intent.getExtras().getInt("current");
                currentSongId = songList.get(current).getId();
                currentTime = intent.getExtras().getInt("currenTime");
                play(currentTime);
                changeWl();
                break;
            case Constants.PlayerControl.UPDATE_CURRENTTIME:
                updateCurrentTime(intent.getExtras().getInt("currentTime"));
                break;
            case Constants.PlayerControl.INIT_GET_CURRENT_INFO:
                Log.e(TAG, "INIT_GET_CURRENT_INFO");
                // Only for Get Current SongId And Other Info
                break;
            case Constants.PlayerControl.CHANGE_MODE:
                mode = mode + 1 >= 3 ? 0 : mode + 1;
                break;
            case Constants.PlayerControl.UPDATE_LIST:
                //updateList(intent.getExtras().getString("listName"));
                return START_STICKY;
            case Constants.PlayerControl.CHANGE_LIST:
                changeList(intent.getExtras().getString("listName"));
                return START_STICKY;
            case Constants.PlayerControl.INIT_SERVICE:
                changeList(intent.getExtras().getString("listName"));
                current = intent.getExtras().getInt("current");
                try {
                    if (songList.size() > current) {
                        currentSongId = songList.get(current).getId();
                    } else if (songList.size() != 0) {
                        current = 0;
                        currentSongId = songList.get(current).getId();
                    } else {
                        changeList(Constants.ListName.LIST_ALL);
                        current = 0;
                        currentSongId = songList.get(current).getId();
                    }
                    init();
                    showButtonNotify();
                    if (isShowWl)
                        showLyricNotify();
                } catch (Exception e) {
                    Log.e(TAG, "Exception");
                    e.printStackTrace();
                }
                // Update WindowLyric
                changeWl();
                return START_STICKY;
            case Constants.PlayerControl.UPDATE_NOTIFY:
                boolean b = intent.getExtras().getBoolean("hasNotify");
                if (b) {
                    hasNotify = true;
                    showButtonNotify();
                } else {
                    hasNotify = false;
                    nm.cancel(950520);
                }
                return START_STICKY;
            case Constants.PlayerControl.UPDATE_WINDOWLYRIC:
                boolean isShow = intent.getExtras().getBoolean("isShow");
                if (isShow) {
                    showWl();
                } else {
                    hideWl();
                }
                return START_STICKY;
            case  Constants.PlayerControl.LOCK_LYRIC:
                WindowManager.LayoutParams params = wl.params;
                params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        |WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
                wm.updateViewLayout(wl, params);
                isLockWL = true;
                showLyricNotify();
                Log.e(TAG, "lock");
                return START_STICKY;
            case  Constants.PlayerControl.UNLOCK_LYRIC:
                WindowManager.LayoutParams params1 = wl.params;
                params1.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                wm.updateViewLayout(wl, params1);
                isLockWL = false;
                showLyricNotify();
                Log.e(TAG, "unlock");
                return START_STICKY;
            default:
                break;
        }
        boardCurrentState();
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
        timer.cancel();
        hasNotify = false;
        nm.cancel(950520);
        nm.cancel(950521);


        //Save the Preferences of WindowLyric
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(Constants.Preferences.PREFERENCES_WINDOWLYRIC_Y, wl.params.y);
        Log.e(TAG, "Y = " +  wl.params.y);
        int isLock = isLockWL ? 1 : 0;
        editor.putInt(Constants.Preferences.PREFERENCES_WINDOWLYRIC_LOCK, isLock);
        Log.e(TAG, "isLock = " + isLock);

        int isShow = isShowWl ? 1 : 0;
        editor.putInt(Constants.Preferences.PREFERENCES_WINDOWLYRIC_SHOW, isShow);
        Log.e(TAG, "isShow = " + isShow);
        editor.commit();

        if (wl != null && wl.isShown()) {
            wm.removeView(wl);
        }
        super.onDestroy();
    }

    private void init() {
        try {
            player.reset();
            player.setDataSource(songList.get(current).getFileName());
            player.prepare();
            duration = songList.get(current).getDuration();
        } catch (IOException e) {
            e.printStackTrace();
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
        return (int)(Math.random()*songList.size());
        /*int random;
        do {
            random = (int)Math.random()*songList.size();
        } while(random != current);
        return random;*/
    }

    /*
     * Play Pre Song
     */
    private void playPre() {
        //Log.d(TAG, "Play Pre!");
        if (mode == 0) {
            current--;
            current = current < 0 ? songList.size() - 1 : current;  // current trun to max position if current < 0
            currentSongId = songList.get(current).getId();
            play(0);
        } else if (mode == 1) {
            play(0);
        } else {
            current = getRandom(current);
            currentSongId = songList.get(current).getId();
            play(0);
        }
    }

    /*
     * Play Next Song
     */
    private void playNext() {
        //Log.d(TAG, "Play Next!");
        if (mode == 0) {
            current++;
            current = current > (songList.size() - 1) ? 0 : current;  // current trun to 0 position if current > max
            currentSongId = songList.get(current).getId();
            play(0);
        } else if (mode == 1) {
            play(0);
        } else {
            current = getRandom(current);
            currentSongId = songList.get(current).getId();
            play(0);
        }
    }

    /*
     * Pause Music
     */
    private void pause() {
        //Log.d(TAG, "Pause!");
        if (isPlay) {
            player.pause();
        }
        isPlay = false;
    }

    /*
     * Resum Music
     */
    private void resum() {
        //Log.d(TAG, "Resum!");
        if (!isPlay) {
            player.start();
        }
        isPlay = true;
    }

    /*
     * Update CurrentTime
     */
    private void updateCurrentTime(int currentTime) {
        this.currentTime = currentTime;
        player.seekTo(currentTime);
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
    private void updateDB(boolean isCompleted, int current) {
        Log.e(TAG, "updateDB");
        Log.e(TAG, "isCompleted = " +  isCompleted);
        Log.e(TAG, "name = " + songList.get(current).getName());
        Song temp = songList.get(current);
        if (!dbMgr.inSongDetail(temp)) {
            dbMgr.addToSongDetail(temp);
        }
        dbMgr.updatePlayCount(temp);
        if (!isCompleted) {
            dbMgr.updateSwicthCount(temp);
        }
        dbMgr.updateDateCountPlay(isCompleted);
    }
    /*
     * Update the SongList EveryTime the User Open the List
     */
    private void updateList(String listName) {
        if (listName.equals(this.listName)) {
            songList = SongProvider.getSongListByName(listName);
            this.listName = listName;
        }
    }
    /*
     * Change the SongList EveryTime the User Click the List Item to Play Music
     */
    private void changeList(String listName) {
        // Before Change List and Play a New Song
        // Save the Play Count
        if (currentTime > 20 * 1000 && currentTime < duration - 30*1000)
            updateDB(false, current);
        else if (currentTime >= duration-30*1000)
            updateDB(true, current);

        if (listName.equals(this.listName))
            return;
        else {
            songList = SongProvider.getSongListByName(listName);
            this.listName = listName;
        }
    }
    /*
     * Boardcast Current State
     */
    private void boardCurrentState() {
        Intent sendIntent = new Intent(Constants.UiControl.UPDATE_UI);
        sendIntent.putExtra("current", current);
        sendIntent.putExtra("isPlay", isPlay);
        currentTime = player.getCurrentPosition();
        sendIntent.putExtra("currentTime", currentTime);
        sendIntent.putExtra("songId", currentSongId);
        sendIntent.putExtra("mode", mode);
        sendIntent.putExtra("listName", listName);
        sendBroadcast(sendIntent);
        if (hasNotify)
            showButtonNotify();
    }

    public void showButtonNotify(){
        Song song = songList.get(current);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        RemoteViews mRemoteViews = new RemoteViews(getPackageName(), R.layout.notify_player);

        mRemoteViews.setTextViewText(R.id.playingArtist, song.getArtist());
        mRemoteViews.setTextViewText(R.id.playingName, song.getName());
        mRemoteViews.setImageViewBitmap(R.id.playingPhoto, SongProvider.getArtwork(this,
                currentSongId, songList.get(current).getAlbumId(), false, false));

        if (isPlay) {
            mRemoteViews.setImageViewResource(R.id.playBt, R.drawable.pausetoplay_00000);
        } else {
            mRemoteViews.setImageViewResource(R.id.playBt, R.drawable.playtopause_00000);
        }

        // Intents
        Intent playIntent = new Intent("com.badprinter.yobey.service.PLAYER_SERVICE");
        Intent nextIntent = new Intent("com.badprinter.yobey.service.PLAYER_SERVICE");
        Intent activityIntent = new Intent(this, Welcome.class);

        // Make Sure That Returing App Instead of New A Activity
        activityIntent.setAction(Intent.ACTION_MAIN);
        activityIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        if (isPlay)
            playIntent.putExtra("controlMsg", Constants.PlayerControl.PAUSE_PLAYING_MSG);
        else
            playIntent.putExtra("controlMsg", Constants.PlayerControl.CONTINUE_PLAYING_MSG);
        nextIntent.putExtra("controlMsg", Constants.PlayerControl.NEXT_SONG_MSG);

        PendingIntent intent_play = PendingIntent.getService(this, 1, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent intent_next = PendingIntent.getService(this, 2, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent intent_activity = PendingIntent.getActivity(this, 3, activityIntent, 0);

        mRemoteViews.setOnClickPendingIntent(R.id.playBt, intent_play);
        mRemoteViews.setOnClickPendingIntent(R.id.nextBt, intent_next);
        mRemoteViews.setOnClickPendingIntent(R.id.all, intent_activity);

        mBuilder.setTicker("YoBey")
                .setWhen(System.currentTimeMillis())
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setOngoing(true)
                .setContent(mRemoteViews)
                .setContentIntent(intent_play)
                .setContentIntent(intent_next)
                .setContentIntent(intent_activity)
                .setSmallIcon(R.drawable.ic_launcher);

        nm.notify(950520, mBuilder.build());
    }
    private void showLyricNotify() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        RemoteViews mRemoteViews = new RemoteViews(getPackageName(), R.layout.notify_lyric);

        if (!isShowWl) {
            return;
        }
        if (isLockWL) {
            mRemoteViews.setTextViewText(R.id.lockText, "点击此处解锁歌词");
        } else {
            mRemoteViews.setTextViewText(R.id.lockText, "点击此处锁定歌词");
        }

        // Intent
        Intent intent = new Intent("com.badprinter.yobey.service.PLAYER_SERVICE");

        if (isLockWL)
            intent.putExtra("controlMsg", Constants.PlayerControl.UNLOCK_LYRIC);
        else
            intent.putExtra("controlMsg", Constants.PlayerControl.LOCK_LYRIC);

        PendingIntent intent_lock = PendingIntent.getService(this, 4, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        mRemoteViews.setOnClickPendingIntent(R.id.lyricNotify, intent_lock);

        mBuilder.setTicker("YoBey")
                .setWhen(System.currentTimeMillis())
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setOngoing(true)
                .setContent(mRemoteViews)
                .setContentIntent(intent_lock)
                .setSmallIcon(R.drawable.ic_launcher);

        nm.notify(950521, mBuilder.build());
    }
    private void initWindowLyric() {
        Song song = songList.get(current);
        wl = new WindowLyric(this, song.getUrl(), song.getName(), song.getArtist());
        wl.callBack = new WindowLyric.CallBack() {
            @Override
            public int getCurrentTime() {
                return player.getCurrentPosition();
            }
        };
        // Init isLock
        int isLock = sharedPref.getInt(Constants.Preferences.PREFERENCES_WINDOWLYRIC_LOCK, 0);
        if (isLock == 1) {
            isLockWL = true;
            WindowManager.LayoutParams params = wl.params;
            params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    |WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        } else {
            isLockWL = false;
        }
        Log.e(TAG, "isLock: "+ isLock);
        // Init Y
        int y = sharedPref.getInt(Constants.Preferences.PREFERENCES_WINDOWLYRIC_Y, 100);
        wl.params.y = y;
        Log.e(TAG, "y: "+ y);
        // Init isShow
        int isShow = sharedPref.getInt(Constants.Preferences.PREFERENCES_WINDOWLYRIC_SHOW, 1);
        Log.e(TAG, "isShow: "+ isShow);
        if (isShow == 1) {
            showWl();
            showLyricNotify();
        } else {
            hideWl();
            nm.cancel(950521);
        }
    }
    private void changeWl() {
        Song song = songList.get(current);
        wl.initLyric(song.getUrl(), song.getName(), song.getArtist());
    }
    private void hideWl() {
        if (wl.isShown())
            wm.removeView(wl);
        isShowWl = false;
        nm.cancel(950521);
    }
    private void showWl() {
        wm.addView(wl, wl.params);
        isShowWl = true;
    }
}
