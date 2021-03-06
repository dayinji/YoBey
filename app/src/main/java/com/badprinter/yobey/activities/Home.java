package com.badprinter.yobey.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.badprinter.yobey.R;
import com.badprinter.yobey.adapter.SongListAdapter;
import com.badprinter.yobey.commom.Constants;
import com.badprinter.yobey.customviews.MusicBar;
import com.badprinter.yobey.models.Song;
import com.badprinter.yobey.service.PlayerService;
import com.badprinter.yobey.utils.Animation_3D;
import com.badprinter.yobey.utils.SongProvider;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;


public class Home extends ActionBarActivity implements View.OnClickListener {

    private final String TAG = "HomeActivity";
    private ListView songListView;
    private ImageView preBt;
    private ImageView playBt;
    private ImageView nextBt;
    private ImageView playingPhoto;
    private TextView playingName;
    private TextView playingArtist;
    private RelativeLayout bottomLayout;
    private boolean isPlay = false;
    private int current = 0;
    private int currentTime;
    private String listName;
    private boolean isFirstTime = true;
    private HomeReceiver homeReceiver;
    private SongListAdapter mySongListAdapter;
    private AnimationDrawable animPlay;
    private AnimationDrawable animNext;
    private AnimationDrawable animPre;
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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        /*
         * Find All Views
         */
        findViewsById();
        setClickListener();

        songList = SongProvider.getSongList();
        mySongListAdapter = new SongListAdapter(songList, (long)1, Constants.ListName.LIST_ALL);
        songListView.setAdapter(mySongListAdapter);
        songListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent("com.badprinter.yobey.service.PLAYER_SERVICE");
                intent.putExtra("controlMsg", Constants.PlayerControl.PLAYING_MSG);
                intent.putExtra("current", position);
                intent.putExtra("currenTime", 0);
                isFirstTime = false;
                startService(intent);
            }
        });

        Song temp = songList.get(current);
        playingPhoto.setImageBitmap(SongProvider.getArtwork(Home.this, temp.getId(), temp.getAlbumId(), false, true));

        homeReceiver = new HomeReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.UiControl.UPDATE_UI);
        filter.addAction(Constants.UiControl.UPDATE_CURRENT);
        registerReceiver(homeReceiver, filter);

    }


    @Override
    public void onDestroy() {
        Intent intent = new Intent(Home.this, PlayerService.class);
        stopService(intent);
        unregisterReceiver(homeReceiver);
        //test
        SharedPreferences sharedPref = getSharedPreferences(
                Constants.Preferences.PREFERENCES_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("lastId", 1);
        editor.commit();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
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
                Intent trunToPlayerIntent = new Intent(Home.this, Player.class);
                trunToPlayerIntent.putExtra("current", current);
                trunToPlayerIntent.putExtra("isPlay", isPlay);
                trunToPlayerIntent.putExtra("isFirstTime", isFirstTime);
                trunToPlayerIntent.putExtra("currentTime", currentTime);
                trunToPlayerIntent.putExtra("listName", listName);
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
    private class HomeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Constants.UiControl.UPDATE_UI:
                    boolean isPlay = intent.getBooleanExtra("isPlay", false); // Play or Pause
                    int current = intent.getIntExtra("current", -1); // Current Song Id
                    String listName = intent.getStringExtra("listName");
                    mySongListAdapter.updateItem((long)current);
                    Home.this.isPlay = isPlay;
                    Home.this.current = current;
                    Home.this.listName = listName;
                    Song temp = songList.get(current);
                    //bar.setMax(temp.getDuration());
                    /*
                     * Recycle the Bitmap before
                     */
                    //((BitmapDrawable)playingPhoto.getDrawable()).getBitmap().recycle();
                    playingPhoto.setImageBitmap(SongProvider.getArtwork(Home.this, temp.getId(), temp.getAlbumId(), false, true));
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
            }


        }
    }

    private void updateBar(int currentTime) {
        this.currentTime = currentTime;
        //bar.setProgress(currentTime);
        //mySongListAdapter.updateBar(currentTime);
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
}
