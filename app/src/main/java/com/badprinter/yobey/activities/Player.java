package com.badprinter.yobey.activities;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.BoringLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.badprinter.yobey.R;
import com.badprinter.yobey.commom.Constants;
import com.badprinter.yobey.customviews.Lyric;
import com.badprinter.yobey.customviews.MusicBar;
import com.badprinter.yobey.customviews.MyScrollView;
import com.badprinter.yobey.db.DBManager;
import com.badprinter.yobey.models.Song;
import com.badprinter.yobey.utils.SongProvider;

import org.w3c.dom.Text;

import java.util.List;

import jp.wasabeef.blurry.Blurry;
import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;


public class Player extends SwipeBackActivity implements View.OnClickListener {

    private String TAG = "Player";
    private SwipeBackLayout mSwipeBackLayout;
    private ImageView blurBg;
    private TextView songName;
    private TextView songArtist;
    //private List<Song> songList;
    private ImageView preBt;
    private ImageView playBt;
    private ImageView nextBt;
    private ImageView likeBt;
    private ImageView modeBt;
    private MusicBar bar;
    private ImageView smoke;

    //private int current;
    private long currentSongId;
    private boolean isPlay = false;
    private int mode = 0;
    private int currentTime;
    //private String listName;
    private AnimationDrawable animPlay;
    private AnimationDrawable animNext;
    private AnimationDrawable animPre;
    private AnimationDrawable animlike;
    private AnimationDrawable smokeAnimDrawable;
    private Lyric lyricView;
    private MyScrollView scrollLyric;
    private int lyricId = -1;
    private PlayerReceiver playerReceiver;
    private DBManager dbMgr;

    private Toast modeToast;
    private Toast likeToast;


    private ValueAnimator changeBlurBg = null;
    private int[] animId = new int[] {
            R.drawable.playtopause, R.drawable.pausetoplay,
            R.drawable.playnext, R.drawable.playpre,
            R.drawable.like, R.drawable.unlike
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_player);

        findViewsById();
        setClickListener();

        init(getIntent());

    }

    private void init(Intent intent) {
        currentSongId = intent.getLongExtra("currentSongId", 0);
        dbMgr = new DBManager();
        /*
         * Init the playerReceiver
         */
        playerReceiver = new PlayerReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.UiControl.UPDATE_UI);
        filter.addAction(Constants.UiControl.UPDATE_CURRENT);
        registerReceiver(playerReceiver, filter);

        Song temp = SongProvider.getSongById(currentSongId);
        /*
         * Init Artist and Name
         */
        songArtist.setText(temp.getArtist());
        songName.setText(temp.getName());

        blurBg.setImageBitmap(SongProvider.getArtwork(Player.this, temp.getId(),
                temp.getAlbumId(), false, true));
        ViewTreeObserver vto2 = blurBg.getViewTreeObserver();
        vto2.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                blurBg.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                Blurry.with(Player.this)
                        .radius(20)
                        .sampling(5)//.async()
                        .capture(findViewById(R.id.blurBg))
                        .into((ImageView) findViewById(R.id.blurBg));
                Log.e(TAG, "addOnGlobalLayoutListener");
            }
        });
        if (dbMgr.isFavorite(SongProvider.getSongById(currentSongId))) {
            likeBt.setBackgroundResource(R.drawable.like_00029);
        }
        lyricView.inti(temp.getUrl(), temp.getName(), temp.getArtist());

        bar.setMax(temp.getDuration());
        bar.onProgressChange = new MusicBar.OnProgressChange() {
            public void onProgressChangeCall(int toPoint) {
                Intent intent = new Intent("com.badprinter.yobey.service.PLAYER_SERVICE");
                intent.putExtra("controlMsg", Constants.PlayerControl.UPDATE_CURRENTTIME);
                intent.putExtra("currentTime", toPoint);
                startService(intent);
            }
            public void onProgressAnimCall(int point) {
                smoke.setX(((float)bar.getProgress()/SongProvider.getSongById(currentSongId).getDuration())*bar.getMeasuredWidth()
                        - smoke.getMeasuredWidth() - 4);
            }
        };
        ViewTreeObserver vto1 = smoke.getViewTreeObserver();
        vto1.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                updateBar(currentTime);
            }
        });
        /*
         * Init the scrollLyric View
         */
        scrollLyric.setOverScrollMode(ScrollView.OVER_SCROLL_NEVER);
        scrollLyric.setHorizontalScrollBarEnabled(false);
        /*
         * Init the SwipeBack effect
         */
        mSwipeBackLayout = getSwipeBackLayout();
        mSwipeBackLayout.setEdgeSize(200);
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
        /*
         * Init the smoke anim
         */
        smokeAnimDrawable = (AnimationDrawable) smoke.getBackground();
        smokeAnimDrawable.setOneShot(false);
        smokeAnimDrawable.start();

        /*
         * Init From Service
         */
        Intent initIntent = new Intent();
        initIntent.setAction("com.badprinter.yobey.service.PLAYER_SERVICE");
        initIntent.putExtra("controlMsg", Constants.PlayerControl.INIT_GET_CURRENT_INFO);
        startService(initIntent);

    }
    @Override
    public void onDestroy() {
        unregisterReceiver(playerReceiver);
        super.onDestroy();
    }

    private void findViewsById() {
        blurBg = (ImageView)findViewById(R.id.blurBg);
        songName = (TextView)findViewById(R.id.songName);
        songArtist = (TextView)findViewById(R.id.songArtist);
        preBt = (ImageView)findViewById(R.id.preBt);
        nextBt = (ImageView)findViewById(R.id.nextBt);
        playBt = (ImageView)findViewById(R.id.playBt);
        likeBt = (ImageView)findViewById(R.id.likeBt);
        modeBt = (ImageView)findViewById(R.id.modeBt);
        bar = (MusicBar)findViewById(R.id.musicBar);
        smoke = (ImageView)findViewById(R.id.smoke);
        lyricView = (Lyric)findViewById(R.id.lyricView);
        scrollLyric = (MyScrollView)findViewById(R.id.scrollLyric);
    }

    private void setClickListener() {
        preBt.setOnClickListener(this);
        nextBt.setOnClickListener(this);
        playBt.setOnClickListener(this);
        likeBt.setOnClickListener(this);
        modeBt.setOnClickListener(this);
        bar.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();
        intent.setAction("com.badprinter.yobey.service.PLAYER_SERVICE");
        switch(view.getId()) {
            case R.id.preBt:
                intent.putExtra("controlMsg", Constants.PlayerControl.PRE_SONG_MSG);
                startService(intent);
                playDrawableAnim(preBt, 3, animPre);
                break;
            case R.id.playBt:
                if (isPlay == false) {
                    intent.putExtra("controlMsg", Constants.PlayerControl.PLAYING_MSG);
                    intent.putExtra("currenTime", currentTime);
                    intent.putExtra("controlMsg", Constants.PlayerControl.CONTINUE_PLAYING_MSG);
                } else {
                    intent.putExtra("controlMsg", Constants.PlayerControl.PAUSE_PLAYING_MSG);
                }
                startService(intent);
                break;
            case R.id.nextBt:
                intent.putExtra("controlMsg", Constants.PlayerControl.NEXT_SONG_MSG);
                startService(intent);
                playDrawableAnim(nextBt, 2, animNext);
                break;
            case R.id.likeBt:
                Song currentSong = SongProvider.getSongById(currentSongId);
                if (dbMgr.isFavorite(currentSong)) {
                    dbMgr.deleteFromFavorite(currentSong);
                    playDrawableAnim(likeBt, 5, animlike);
                    if (likeToast != null)
                        likeToast.cancel();
                    likeToast = Toast.makeText(Player.this, "取消收藏", Toast.LENGTH_SHORT);
                    likeToast.show();
                } else {
                    dbMgr.addToFavorite(currentSong);
                    playDrawableAnim(likeBt, 4, animlike);
                    if (likeToast != null)
                        likeToast.cancel();
                    likeToast = Toast.makeText(Player.this, "收藏成功", Toast.LENGTH_SHORT);
                    likeToast.show();
                }
                break;
            case R.id.modeBt:
                //mode = mode + 1 >= 3 ? 0 : mode + 1;
                intent.putExtra("controlMsg", Constants.PlayerControl.CHANGE_MODE);
                startService(intent);


        }
    }

    private void playDrawableAnim(ImageView view, int id, AnimationDrawable animDrawable) {
        if (animDrawable != null && animDrawable.isRunning())
            animDrawable.stop();
        if (id == 2)
            view.setBackgroundResource(R.drawable.playnext_00000);
        else if (id == 3)
            view.setBackgroundResource(R.drawable.playpre_00000);
        view.setBackgroundResource(animId[id]);
        animDrawable = (AnimationDrawable) view.getBackground();
        animDrawable.setOneShot(true);
        animDrawable.start();
    }

    /*
     * Receive the Broad from Sevice for Updating UI
     */
    private class PlayerReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Constants.UiControl.UPDATE_UI:
                    boolean isPlay = intent.getBooleanExtra("isPlay", false); // Play or Pause
                    //int current = intent.getIntExtra("current", 0); // Current Song Id
                    long currentSongId = intent.getLongExtra("songId", 0);

                    if (currentSongId != Player.this.currentSongId) {
                        Player.this.currentSongId = currentSongId;
                        Song temp = SongProvider.getSongById(currentSongId);
                        bar.setMax(temp.getDuration());
                        changeBlurBg(temp.getId(), temp.getAlbumId());
                        lyricView.inti(temp.getUrl(), temp.getName(), temp.getArtist());
                        Player.this.currentTime = intent.getExtras().getInt("currentTime");
                        scrollLyric.reset();
                        updateBar(currentTime);
                        songArtist.setText(temp.getArtist());
                        songName.setText(temp.getName());
                        if (dbMgr.isFavorite(temp))
                            likeBt.setBackgroundResource(R.drawable.like_00029);
                        else
                            likeBt.setBackgroundResource(R.drawable.like_00000);

                    }
                    if (Player.this.isPlay != isPlay) {
                        int animId = isPlay ? 0 : 1;
                        playDrawableAnim(playBt, animId, animPlay);
                    }
                    if (intent.getIntExtra("mode", 0) != mode) {
                        mode = intent.getIntExtra("mode", 0);
                        int[] modeDrawables = new int[] {
                                R.drawable.mode_normal, R.drawable.mode_single, R.drawable.mode_random};
                        String[] modeText = new String[] {
                                "列表循环", "单曲循环", "随机播放"
                        };
                        modeBt.setBackgroundResource(modeDrawables[mode]);
                        if (modeToast != null)
                            modeToast.cancel();
                        modeToast = Toast.makeText(Player.this, modeText[mode], Toast.LENGTH_SHORT);
                        modeToast.show();

                    }
                    Player.this.isPlay = isPlay;
                    break;
                case Constants.UiControl.UPDATE_CURRENT:
                    Player.this.currentTime = intent.getExtras().getInt("currentTime");
                    updateBar(Player.this.currentTime);
                    break;
            }


        }
    }

    private void updateBar(int currentTime) {
        bar.setProgress(currentTime);
        smoke.setX(((float) currentTime / SongProvider.getSongById(currentSongId).getDuration()) * bar.getMeasuredWidth()
                - smoke.getMeasuredWidth() - 4);

        lyricView.setCurrentTime(currentTime);
        if (lyricId != lyricView.getId()) {
            lyricId = lyricView.getId();
            scrollLyric.slowScrollTo(0, lyricView.getId()*lyricView.getDy());
        }

    }

    private void changeBlurBg(long songId, long albumId) {
        /*
         * Recycle the Bitmap before
         */
        Bitmap before = ((BitmapDrawable) blurBg.getDrawable()).getBitmap();
        blurBg.setAlpha(0f);
        blurBg.setImageBitmap(SongProvider.getArtwork(Player.this, songId, albumId, false, true));
        Blurry.with(Player.this)
                .radius(20)
                .sampling(5)
                        //.async()
                .capture(findViewById(R.id.blurBg))
                .into((ImageView) findViewById(R.id.blurBg));
        if (before != null && !before.isRecycled())
            before.recycle();
        if (changeBlurBg != null && changeBlurBg.isRunning()) {
            changeBlurBg.end();
        }
        changeBlurBg = ValueAnimator.ofFloat(0f, 1f);
        changeBlurBg.setDuration(1000);
        changeBlurBg.setInterpolator(new AccelerateInterpolator());
        //changeBlurBg.setStartDelay(1000);
        changeBlurBg.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                blurBg.setAlpha((float) animation.getAnimatedValue());
            }
        });
        changeBlurBg.start();
    }
}
