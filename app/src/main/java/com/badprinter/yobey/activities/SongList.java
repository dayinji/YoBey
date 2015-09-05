package com.badprinter.yobey.activities;

import android.animation.ObjectAnimator;
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
import com.badprinter.yobey.utils.SongProvider;
import com.twotoasters.jazzylistview.JazzyListView;
import com.twotoasters.jazzylistview.effects.CardsEffect;
import com.twotoasters.jazzylistview.effects.CurlEffect;
import com.twotoasters.jazzylistview.effects.FadeEffect;
import com.twotoasters.jazzylistview.effects.FanEffect;
import com.twotoasters.jazzylistview.effects.FlipEffect;
import com.twotoasters.jazzylistview.effects.FlyEffect;
import com.twotoasters.jazzylistview.effects.GrowEffect;
import com.twotoasters.jazzylistview.effects.HelixEffect;
import com.twotoasters.jazzylistview.effects.ReverseFlyEffect;
import com.twotoasters.jazzylistview.effects.SlideInEffect;
import com.twotoasters.jazzylistview.effects.StandardEffect;
import com.yalantis.phoenix.PullToRefreshView;

import java.util.ArrayList;
import java.util.List;

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
    private AnimationDrawable animPlay;
    private AnimationDrawable animNext;
    private AnimationDrawable animPre;
    private ObjectAnimator fadeSelectorAnim;
    private PinyinBar pinyinBar;
    private MyOnScrollListener myScrollListener;
    private int[] animId = new int[] {
            R.drawable.playtopause, R.drawable.pausetoplay, R.drawable.playnext, R.drawable.playpre
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
        // Get Anim Preference
        /*SharedPreferences pre = getSharedPreferences(
                Constants.Preferences.PREFERENCES_KEY, Context.MODE_PRIVATE);
        int animMode = pre.getInt(Constants.Preferences.PREFERENCES_LIST_ANIM, 0);
        if (animMode == 0)
            songListView.setTransitionEffect(new StandardEffect());
        else if (animMode == 1)
            songListView.setTransitionEffect(new CardsEffect());
        else if (animMode == 2)
            songListView.setTransitionEffect(new SlideInEffect());
        else if (animMode == 3)
            songListView.setTransitionEffect(new FlipEffect());
        else if (animMode == 4)
            songListView.setTransitionEffect(new FlyEffect());*/

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
                    SongList.this.isPlay = isPlay;
                    SongList.this.current = current;
                    //Song temp = songList.get(current);
                    Song temp = SongProvider.getSongById(currentSongId);
                    //bar.setMax(temp.getDuration());
                    /*
                     * Recycle the Bitmap before
                     */
                    //((BitmapDrawable)playingPhoto.getDrawable()).getBitmap().recycle();
                    playingPhoto.setImageBitmap(SongProvider.getArtwork(SongList.this, temp.getId(), temp.getAlbumId(), false, true));
                    playingArtist.setText(temp.getArtist());
                    playingName.setText(temp.getName());
                    if (isPlay) {
                        playDrawableAnim(playBt, 0, animPlay);
                    } else {
                        playDrawableAnim(playBt, 1, animPlay);
                    }
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
    /*@Override
    protected void onNewIntent(Intent intent) {

        Log.e(TAG, "listName = XXXX");
        super.onNewIntent(intent);

        setIntent(intent);//must store the new intent unless getIntent() will return the old one

        if (intent.getFlags() == Intent.FLAG_ACTIVITY_CLEAR_TOP) {
            listName = intent.getStringExtra("cata");

            songList = SongProvider.getSongListByName(listName);
            mySongListAdapter = new SongListAdapter(songList, currentSongId, listName);
            songListView.setAdapter(mySongListAdapter);
        }
    }*/
}
