package com.badprinter.yobey.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.badprinter.yobey.R;
import com.badprinter.yobey.adapter.SongListAdapter;
import com.badprinter.yobey.commom.Constants;
import com.badprinter.yobey.customviews.MusicBar;
import com.badprinter.yobey.models.Song;
import com.badprinter.yobey.service.PlayerService;
import com.badprinter.yobey.utils.SongProvider;

import org.w3c.dom.Text;

import java.util.ArrayList;


public class Home extends ActionBarActivity implements View.OnClickListener {

    private final String TAG = "HomeActivity";
    private ListView songListView;
    private MusicBar bar;
    private TextView musicInfo;
    private Button preBt;
    private Button playBt;
    private Button nextBt;
    private boolean isPlay = false;
    private int current = 0;
    private int currentTime;
    private boolean isFirstTime = true;
    private HomeReceiver homeReceiver;

    //private SongListAdapter songListAdapter;
    private ArrayList<Song> songList = new ArrayList<Song>();
    private SongProvider songProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        /*
         * Find All Views
         */

        findViewsById();
        setClickListener();
        songProvider = new SongProvider(Home.this);
        songList = songProvider.getSongList();
        songListView.setAdapter(new SongListAdapter(Home.this, songList));
        songListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent("com.badprinter.yobey.service.PLAYER_SERVICE");
                intent.putExtra("controlMsg", Constants.PlayerControl.PLAYING_MSG);
                intent.putExtra("current", position);
                isFirstTime = false;
                startService(intent);
            }
        });
        bar.setMax(songList.get(current).getDuration());
        /*
         * A Callback for Chaneging CurrentTime
         */
        bar.onProgessChange = new MusicBar.OnProgessChange() {
            public void OnProgessChangeCall(int toPoint) {
                Intent intent = new Intent("com.badprinter.yobey.service.PLAYER_SERVICE");
                intent.putExtra("controlMsg", Constants.PlayerControl.UPDATE_CURRENTTIME);
                intent.putExtra("currentTime", toPoint);
                startService(intent);
            }
        };
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
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
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

    /*
     * Find All Views When Create the Home Activity
     */
    private void findViewsById() {
        songListView = (ListView)findViewById(R.id.songList);
        preBt = (Button)findViewById(R.id.preBt);
        nextBt = (Button)findViewById(R.id.nextBt);
        playBt = (Button)findViewById(R.id.playBt);
        bar = (MusicBar)findViewById(R.id.musicBar);
        musicInfo = (TextView)findViewById(R.id.musicInfo);
    }

    /*
     * Set the ClickListener to Views
     */
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
        switch (view.getId()) {
            case R.id.preBt:
                intent.putExtra("controlMsg", Constants.PlayerControl.PRE_SONG_MSG);
                startService(intent);
                isFirstTime = false;
                break;
            case R.id.playBt:
                if (isPlay == false) {
                    if (isFirstTime) {
                        intent.putExtra("controlMsg", Constants.PlayerControl.PLAYING_MSG);
                        intent.putExtra("current", current);
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
                isFirstTime = false;
                break;
            case R.id.musicBar:

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
                    Home.this.isPlay = isPlay;
                    Home.this.current = current;
                    bar.setMax(songList.get(current).getDuration());
                    musicInfo.setText(songList.get(current).getName() + " " + songList.get(current).getArtist());
                    if (isPlay) {
                        playBt.setText("Pause");
                    } else {
                        playBt.setText("Play");
                    }
                    break;
                case Constants.UiControl.UPDATE_CURRENT:
                    updateBar(intent.getExtras().getInt("currentTime"));
            }


        }
    }

    private void updateBar(int currentTime) {
        bar.setProgress(currentTime);
    }
}
