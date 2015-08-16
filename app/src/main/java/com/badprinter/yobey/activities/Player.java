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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.badprinter.yobey.R;
import com.badprinter.yobey.commom.Constants;
import com.badprinter.yobey.customviews.Lyric;
import com.badprinter.yobey.customviews.MusicBar;
import com.badprinter.yobey.models.Song;
import com.badprinter.yobey.utils.SongProvider;

import org.w3c.dom.Text;

import java.util.List;

import jp.wasabeef.blurry.Blurry;
import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;


public class Player extends SwipeBackActivity implements View.OnClickListener {

    private SwipeBackLayout mSwipeBackLayout;
    private ImageView blurBg;
    private TextView songName;
    private TextView songArtist;
    private List<Song> songList;
    private ImageView preBt;
    private ImageView playBt;
    private ImageView nextBt;
    private MusicBar bar;
    private ImageView smoke;
    private int current;
    private boolean isPlay = false;
    private int mode = 0;
    private int currentTime;
    private boolean isFirstTime = true;
    //private HomeReceiver homeReceiver;
    private AnimationDrawable animPlay;
    private AnimationDrawable animNext;
    private AnimationDrawable animPre;
    private AnimationDrawable smakeAnimDrawable;
    private Lyric lyricView;

    private ValueAnimator changeBlurBg = null;
    private int[] animId = new int[] {
            R.drawable.playtopause, R.drawable.pausetoplay, R.drawable.playnext, R.drawable.playpre
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_player);

        findViewsById();
        setClickListener();

        songList = SongProvider.getSongList(Player.this);

        /*
         * Set something accroding to intent
         */
        Intent intent = getIntent();
        if (intent != null) {
            current = intent.getIntExtra("current", 0);
            isPlay = intent.getBooleanExtra("isPlay", false);
            isFirstTime = intent.getBooleanExtra("isFirstTime", false);
            currentTime = intent.getIntExtra("currentTime", 0);


            if (isPlay) {
                playBt.setBackgroundResource(R.drawable.pausetoplay_00000);
            }

            Song temp = songList.get(current);

            lyricView.setFile(temp.getUrl(), temp.getName(), temp.getArtist());

            bar.setMax(temp.getDuration());
            /*
             * A Callback for Chaneging CurrentTime
             */
            bar.onProgessChange = new MusicBar.OnProgessChange() {
                public void OnProgessChangeCall(int toPoint) {
                    Intent intent = new Intent("com.badprinter.yobey.service.PLAYER_SERVICE");
                    intent.putExtra("controlMsg", Constants.PlayerControl.UPDATE_CURRENTTIME);
                    intent.putExtra("currentTime", toPoint);
                    startService(intent);
                    smoke.setX(((float)bar.getProgress()/songList.get(current).getDuration())*bar.getMeasuredWidth()
                            - smoke.getMeasuredWidth() - 4);

                }
            };
            blurBg.setImageBitmap(SongProvider.getArtwork(Player.this, temp.getId(), temp.getAlbumId(), false, true));
            songName.setText(temp.getName());
            songArtist.setText(temp.getArtist());

            ViewTreeObserver vto2 = blurBg.getViewTreeObserver();
            vto2.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    blurBg.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    Blurry.with(Player.this)
                            .radius(25)
                            .sampling(6)
                            .async()
                            .capture(findViewById(R.id.blurBg))
                            .into((ImageView) findViewById(R.id.blurBg));
                }
            });
            ViewTreeObserver vto1 = smoke.getViewTreeObserver();
            vto1.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    updateBar(currentTime);
                }
            });
        }

        PlayerReceiver playerReceiver = new PlayerReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.UiControl.UPDATE_UI);
        filter.addAction(Constants.UiControl.UPDATE_CURRENT);
        registerReceiver(playerReceiver, filter);

        mSwipeBackLayout = getSwipeBackLayout();
        mSwipeBackLayout.setEdgeSize(200);
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);

        smakeAnimDrawable = (AnimationDrawable) smoke.getBackground();
        smakeAnimDrawable.setOneShot(false);
        smakeAnimDrawable.start();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_player, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void findViewsById() {
        blurBg = (ImageView)findViewById(R.id.blurBg);
        songName = (TextView)findViewById(R.id.songName);
        songArtist = (TextView)findViewById(R.id.songArtist);
        preBt = (ImageView)findViewById(R.id.preBt);
        nextBt = (ImageView)findViewById(R.id.nextBt);
        playBt = (ImageView)findViewById(R.id.playBt);
        bar = (MusicBar)findViewById(R.id.musicBar);
        smoke = (ImageView)findViewById(R.id.smoke);
        lyricView = (Lyric)findViewById(R.id.lyricView);
    }

    private void setClickListener() {
        preBt.setOnClickListener(this);
        nextBt.setOnClickListener(this);
        playBt.setOnClickListener(this);
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
                isFirstTime = false;
                break;
            case R.id.playBt:
                if (isPlay == false) {
                    if (isFirstTime) {
                        intent.putExtra("controlMsg", Constants.PlayerControl.PLAYING_MSG);
                        intent.putExtra("current", current);
                        intent.putExtra("currenTime", currentTime);
                        isFirstTime = false;
                    } else {
                        intent.putExtra("controlMsg", Constants.PlayerControl.CONTINUE_PLAYING_MSG);
                    }
                } else {
                    intent.putExtra("controlMsg", Constants.PlayerControl.PAUSE_PLAYING_MSG);
                }
                startService(intent);
                break;
            case R.id.nextBt:
                intent.putExtra("controlMsg", Constants.PlayerControl.NEXT_SONG_MSG);
                startService(intent);
                playDrawableAnim(nextBt, 2, animNext);
                isFirstTime = false;
                break;

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
                    int current = intent.getIntExtra("current", -1); // Current Song Id
                    if (current == Player.this.current) {
                        Player.this.isPlay = isPlay;
                        if (isPlay) {
                            playDrawableAnim(playBt, 0, animPlay);
                        } else {
                            playDrawableAnim(playBt, 1, animPlay);
                        }

                    } else {
                        Player.this.isPlay = isPlay;
                        Player.this.current = current;
                        Song temp = songList.get(current);
                        bar.setMax(temp.getDuration());
                        changeBlurBg(temp.getId(), temp.getAlbumId());
                        songArtist.setText(temp.getArtist());
                        songName.setText(temp.getName());
                        if (isPlay) {
                            playDrawableAnim(playBt, 0, animPlay);
                        } else {
                            playDrawableAnim(playBt, 1, animPlay);
                        }
                    }

                    break;
                case Constants.UiControl.UPDATE_CURRENT:
                    updateBar(intent.getExtras().getInt("currentTime"));
            }


        }
    }

    private void updateBar(int currentTime) {
        this.currentTime = currentTime;
        bar.setProgress(currentTime);
        smoke.setX(((float)currentTime/songList.get(current).getDuration())*bar.getMeasuredWidth()
                - smoke.getMeasuredWidth() - 4);
    }

    private void changeBlurBg(long songId, long albumId) {
        /*
         * Recycle the Bitmap before
         */
        Bitmap before = ((BitmapDrawable) blurBg.getDrawable()).getBitmap();
        blurBg.setAlpha(0f);
        blurBg.setImageBitmap(SongProvider.getArtwork(Player.this, songId, albumId, false, true));
        Blurry.with(Player.this)
                .radius(25)
                .sampling(6)
                .async()
                .capture(findViewById(R.id.blurBg))
                .into((ImageView) findViewById(R.id.blurBg));
        if (!before.isRecycled())
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
