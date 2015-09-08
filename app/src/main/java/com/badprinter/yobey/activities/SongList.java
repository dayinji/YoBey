package com.badprinter.yobey.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.nfc.Tag;
import android.renderscript.Long4;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.badprinter.yobey.R;
import com.badprinter.yobey.adapter.SongListAdapter;
import com.badprinter.yobey.commom.Constants;
import com.badprinter.yobey.customviews.PinyinBar;
import com.badprinter.yobey.db.DBManager;
import com.badprinter.yobey.models.Song;
import com.badprinter.yobey.service.PlayerService;
import com.badprinter.yobey.utils.MyEvalucatorUtil;
import com.badprinter.yobey.utils.SongProvider;

import java.util.ArrayList;
import java.util.List;

import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import in.srain.cube.views.ptr.PtrUIHandler;
import in.srain.cube.views.ptr.indicator.PtrIndicator;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public class SongList extends SwipeBackActivity implements View.OnClickListener{

    private final String TAG = "SongListActivity";

    private ListView songListView;
    private ImageView preBt;
    private ImageView playBt;
    private ImageView nextBt;
    private ImageView playingPhoto;
    private TextView playingName;
    private TextView playingArtist;
    private RelativeLayout bottomLayout;
    private TextView selectorText;

    private boolean isPlay = false;
    private int current = 0;
    private long currentSongId = 0;
    private int currentTime;
    private boolean isFirstTime = true;
    private String listName;
    private ListReceiver listReceiver;
    private SongListAdapter mySongListAdapter;
   /* private AnimationDrawable animPlay;
    private AnimationDrawable animNext;
    private AnimationDrawable animPre;*/
    private ObjectAnimator fadeSelectorAnim;
    private PinyinBar pinyinBar;
    private MyOnScrollListener myScrollListener;
    private int[] animId = new int[] {
            R.drawable.pausetoplay_00000, R.drawable.playtopause_00000, R.drawable.playnext_00000, R.drawable.playpre_00000
    };
    /*
     * 0 = LoopPlaying
     * 1 = SingPlaying
     * 2 = RandomPlaying
     */
    private int mode = 0;

    //private SongListAdapter songListAdapter;
    private List<Song> songList = new ArrayList<Song>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "oncreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_list);

        /*
         * Find All Views
         */
        findViewsById();
        setClickListener();

        // Init fadeSelectorAnim
        fadeSelectorAnim = ObjectAnimator.ofFloat(selectorText, "alpha", 1f, 0f).setDuration(300);
        fadeSelectorAnim.setStartDelay(300);

        listName = getIntent().getStringExtra("cata");
        songList = SongProvider.getSongListByName(listName);
        /*
         * Update the SongList in Service
         */
        Intent updateListIntent = new Intent();
        updateListIntent.putExtra("listName", listName);
        updateListIntent.setAction("com.badprinter.yobey.service.PLAYER_SERVICE");
        updateListIntent.putExtra("controlMsg", Constants.PlayerControl.UPDATE_LIST);
        startService(updateListIntent);

        mySongListAdapter = new SongListAdapter(songList, currentSongId, listName);
        songListView.setAdapter(mySongListAdapter);
        songListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
        songListView.setVerticalScrollBarEnabled(false);

        listReceiver = new ListReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.UiControl.UPDATE_UI);
        filter.addAction(Constants.UiControl.UPDATE_CURRENT);
        registerReceiver(listReceiver, filter);

        myScrollListener = new MyOnScrollListener();
        songListView.setOnScrollListener(myScrollListener);
        // Init pinyinBar
        pinyinBar.callback = new PinyinBar.PinyinBarCallBack() {
            @Override
            public void onBarChange(int current) {
                if (songList.size() != 0) {
                    char[] letters = {'#', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
                            'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
                            'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
                            'Y', 'Z'
                    };
                    int position = mySongListAdapter.getPositionByLetter(letters[current]);
                    Log.e(TAG, "position = " + position);
                    songListView.setSelection(position);
                    if (fadeSelectorAnim != null && fadeSelectorAnim.isRunning())
                        fadeSelectorAnim.cancel();
                    selectorText.setAlpha(1);
                    String name = songList.get(position).getName();
                    selectorText.setText(name.substring(0, 1));
                    fadeSelectorAnim.start();
                }
            }
        };

        /*
         * Init the Bottom Control Area
         */
        Intent intent = new Intent();
        intent.setAction("com.badprinter.yobey.service.PLAYER_SERVICE");
        intent.putExtra("controlMsg", Constants.PlayerControl.INIT_GET_CURRENT_INFO);
        startService(intent);

    }

    private class MyOnScrollListener implements AbsListView.OnScrollListener{
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        }
    }


    @Override
    public void onDestroy() {
        unregisterReceiver(listReceiver);
        super.onDestroy();
    }



    /*
     * Find All Views When Create the Home Activity
     */
    private void findViewsById() {
        songListView = (ListView)findViewById(R.id.songList);
        preBt = (ImageView)findViewById(R.id.preBt);
        nextBt = (ImageView)findViewById(R.id.nextBt);
        playBt = (ImageView)findViewById(R.id.playBt);
        //bar = (MusicBar)findViewById(R.id.musicBar);
        playingPhoto = (ImageView)findViewById(R.id.playingPhoto);
        playingName = (TextView)findViewById(R.id.playingName);
        playingArtist = (TextView)findViewById(R.id.playingArtist);
        bottomLayout = (RelativeLayout)findViewById(R.id.bottomLayout);
        pinyinBar = (PinyinBar)findViewById(R.id.pinyinBar);
        selectorText = (TextView)findViewById(R.id.selectorText);

    }

    /*
     * Set the ClickListener to Views
     */
    private void setClickListener() {
        preBt.setOnClickListener(this);
        nextBt.setOnClickListener(this);
        playBt.setOnClickListener(this);
        //bar.setOnClickListener(this);
        bottomLayout.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        Intent intent = new Intent();
        intent.setAction("com.badprinter.yobey.service.PLAYER_SERVICE");
        switch (view.getId()) {
            case R.id.preBt:
                intent.putExtra("controlMsg", Constants.PlayerControl.PRE_SONG_MSG);
                startService(intent);
                //playDrawableAnim(preBt, 3, animPre);
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
                //playDrawableAnim(nextBt, 2, animNext);
                isFirstTime = false;
                break;
            case R.id.bottomLayout:
                Intent trunToPlayerIntent = new Intent(SongList.this, Player.class);
                trunToPlayerIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                //trunToPlayerIntent.putExtra("current", current);
                trunToPlayerIntent.putExtra("isPlay", isPlay);
                trunToPlayerIntent.putExtra("isFirstTime", isFirstTime);
                trunToPlayerIntent.putExtra("currentTime", currentTime);
                trunToPlayerIntent.putExtra("currentSongId", currentSongId);
                startActivity(trunToPlayerIntent);
                overridePendingTransition(R.anim.activity_slide_in,R.anim.activity_null);
                break;
            default:
                break;

        }

    }

    /*
     * Receive the Broad from Sevice for Updating UI
     */
    private class ListReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // If This SongList Is Not the Current SongList, Return;
            switch (intent.getAction()) {
                case Constants.UiControl.UPDATE_UI:
                    boolean isPlay = intent.getBooleanExtra("isPlay", false); // Play or Pause
                    int current = intent.getIntExtra("current", 0);
                    SongList.this.currentSongId = intent.getLongExtra("songId", 0); // Current Song Id
                    mySongListAdapter.updateItem(currentSongId);
                    // Play Animation
                    if (isPlay && SongList.this.isPlay != isPlay) {
                        // playDrawableAnim(playBt, 0, animPlay);
                        playAnim(playBt, animId[0], 0f, true);
                    } else if (!isPlay && SongList.this.isPlay){
                        // playDrawableAnim(playBt, 1, animPlay);
                        playAnim(playBt, animId[1], 0f, true);
                    }
                    SongList.this.isPlay = isPlay;
                    SongList.this.current = current;
                    //Song temp = songList.get(current);
                    Song temp = SongProvider.getSongById(currentSongId);
                    //bar.setMax(temp.getDuration());
                    /*
                     * Recycle the Bitmap before
                     */
                    //((BitmapDrawable)playingPhoto.getDrawable()).getBitmap().recycle();
                    playingPhoto.setImageBitmap(SongProvider.getArtwork(SongList.this, temp.getId(), temp.getAlbumId(), true, true));
                    playingArtist.setText(temp.getArtist());
                    playingName.setText(temp.getName());
                    break;
                case Constants.UiControl.UPDATE_CURRENT:
                    updateBar(intent.getExtras().getInt("currentTime"));
                    break;
                default:
                    break;
            }


        }
    }

    private void updateBar(int currentTime) {
        this.currentTime = currentTime;
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

    private void playAnim(final ImageView view, final int id, float middlePoint, boolean isZoomOut) {
        ValueAnimator zoomOut = ValueAnimator.ofFloat(1, middlePoint);
        final ValueAnimator zoomIn = ValueAnimator.ofFloat(middlePoint, 1);
        zoomOut.setDuration(100);
        zoomOut.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float f = (float) animation.getAnimatedValue();
                view.setScaleX(f);
                view.setScaleY(f);
            }
        });
        zoomIn.setDuration(2000);
        MyEvalucatorUtil.JellyFloatAnim jelly = new MyEvalucatorUtil.JellyFloatAnim();
        jelly.setDuration(2000);
        jelly.setFirstTime(100);
        jelly.setAmp(0.03);
        zoomIn.setEvaluator(jelly);
        zoomIn.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float f = (float) animation.getAnimatedValue();
                view.setScaleX(f);
                view.setScaleY(f);
            }
        });
        zoomOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setImageDrawable(getResources().getDrawable(id));
                zoomIn.start();
            }
        });
        if (isZoomOut) {
            zoomOut.start();
        } else {
            zoomIn.start();
        }
    }

}
