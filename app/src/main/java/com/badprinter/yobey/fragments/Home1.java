package com.badprinter.yobey.fragments;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.badprinter.yobey.R;
import com.badprinter.yobey.activities.Player;
import com.badprinter.yobey.adapter.CountAdapter;
import com.badprinter.yobey.commom.Constants;
import com.badprinter.yobey.customviews.WaveView;
import com.badprinter.yobey.db.DBManager;
import com.badprinter.yobey.models.Song;
import com.badprinter.yobey.utils.SongProvider;
import com.db.chart.listener.OnEntryClickListener;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.LineChartView;
import com.db.chart.view.Tooltip;
import com.db.chart.view.animation.Animation;
import com.db.chart.view.animation.easing.BounceEase;
import com.db.chart.view.animation.easing.CircEase;
import com.yalantis.phoenix.PullToRefreshView;

import de.hdodenhof.circleimageview.CircleImageView;
import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import in.srain.cube.views.ptr.PtrUIHandler;
import in.srain.cube.views.ptr.indicator.PtrIndicator;
import jp.wasabeef.blurry.Blurry;

public class Home1 extends Fragment {
    private String TAG = "HOME1";
    private View root;
    private CircleImageView playingPhoto;
    private ImageView preBt;
    private ImageView playBt;
    private ImageView nextBt;
    private TextView playingName;
    private TextView playingArtist;
    private RelativeLayout player;
    private WaveView waveBar;
    private ListView countList;
    private PtrFrameLayout ptrFrame;
    private ImageView cd;


    private DBManager dbMgr;
    private MyClickListener listener = new MyClickListener();
    private boolean isPlay = false;
    private long currentSongId = 0;
    private AnimationDrawable animPlay;
    private AnimationDrawable animNext;
    private AnimationDrawable animPre;
    private int[] animId = new int[] {
            R.drawable.playtopause, R.drawable.pausetoplay, R.drawable.playnext, R.drawable.playpre
    };
    private HomeReceiver homeReceiver;
    private CountAdapter countAdapter;
    private String listName = Constants.ListName.LIST_ALL;
    private int current = 0;
    private int currentTime = 0;


    public Home1() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_home1, container, false);
        this.root = root;
        dbMgr = new DBManager();
        findViewsById();
        setClickListener();

        //test
        SharedPreferences sharedPref = getActivity().getSharedPreferences(
                Constants.Preferences.PREFERENCES_KEY, Context.MODE_PRIVATE);
        listName = sharedPref.getString("lastListName", Constants.ListName.LIST_ALL);
        current = sharedPref.getInt("lastCurrent", 0);
        currentTime = sharedPref.getInt("lastCurrentTime", 0);

        countAdapter = new CountAdapter();
        countList.setAdapter(countAdapter);
        ptrFrame.setPtrHandler(new PtrHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                frame.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ptrFrame.refreshComplete();
                    }
                }, 1200);
            }

            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header);
            }
        });
        //ptrFrame.setOffsetToRefresh(1000);
        ptrFrame.addPtrUIHandler(new PtrUIHandler() {
            @Override
            public void onUIReset(PtrFrameLayout ptrFrameLayout) {
                cd.setRotationY(0);
                cd.setY(300);
            }

            @Override
            public void onUIRefreshPrepare(PtrFrameLayout ptrFrameLayout) {
            }

            @Override
            public void onUIRefreshBegin(PtrFrameLayout ptrFrameLayout) {

                ValueAnimator anim = new ValueAnimator().ofInt(0, 360);
                anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        cd.setRotationY((int)animation.getAnimatedValue());
                    }
                });
                anim.start();
                Log.e(TAG, ""+ptrFrame.getOffsetToRefresh());
            }

            @Override
            public void onUIRefreshComplete(PtrFrameLayout ptrFrameLayout) {

            }

            @Override
            public void onUIPositionChange(PtrFrameLayout ptrFrameLayout, boolean b, byte b1, PtrIndicator ptrIndicator) {

            }
        });

        homeReceiver = new HomeReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.UiControl.UPDATE_UI);
        filter.addAction(Constants.UiControl.UPDATE_CURRENT);
        getActivity().registerReceiver(homeReceiver, filter);

        ViewTreeObserver vto2 = playingPhoto.getViewTreeObserver();
        vto2.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                playingPhoto.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                /*
                 * Init Service
                 */
                Intent intentInitService = new Intent();
                intentInitService.setAction("com.badprinter.yobey.service.PLAYER_SERVICE");
                intentInitService.putExtra("controlMsg", Constants.PlayerControl.INIT_SERVICE);
                intentInitService.putExtra("listName", listName);
                intentInitService.putExtra("current", current);
                getActivity().startService(intentInitService);

                /*
                 * Init the Bottom Control Area
                 */
                Intent intent = new Intent();
                intent.setAction("com.badprinter.yobey.service.PLAYER_SERVICE");
                intent.putExtra("controlMsg", Constants.PlayerControl.INIT_GET_CURRENT_INFO);
                getActivity().startService(intent);


                Log.e(TAG, "addOnGlobalLayoutListener");
            }
        });
        Log.e(TAG, "onCreateView");

        return root;
    }
    @Override
    public void onDetach() {
        //test
        SharedPreferences sharedPref = getActivity().getSharedPreferences(
                Constants.Preferences.PREFERENCES_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("lastListName", listName);
        editor.putInt("lastCurrent", current);
        editor.commit();

        getActivity().unregisterReceiver(homeReceiver);
        super.onDetach();
    }

    /*
     * Find All Views When Create the Home Activity
     */
    public void findViewsById() {
        playBt = (ImageView)root.findViewById(R.id.playBt);
        nextBt = (ImageView)root.findViewById(R.id.nextBt);
        preBt = (ImageView)root.findViewById(R.id.preBt);
        playingPhoto = (CircleImageView)root.findViewById(R.id.playingPhoto);
        playingName = (TextView)root.findViewById(R.id.playingName);
        playingArtist = (TextView)root.findViewById(R.id.playingArtist);
        player = (RelativeLayout)root.findViewById(R.id.player);
        waveBar = (WaveView)root.findViewById(R.id.waveBar);
        countList = (ListView)root.findViewById(R.id.countList);
        ptrFrame = (PtrFrameLayout)root.findViewById(R.id.ptrFrame);
        cd = (ImageView)root.findViewById(R.id.cd);
    }

    /*
     * Set the ClickListener to Views
     */
    private void setClickListener() {
        preBt.setOnClickListener(listener);
        nextBt.setOnClickListener(listener);
        playBt.setOnClickListener(listener);
        player.setOnClickListener(listener);
    }
    class MyClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent();
            intent.setAction("com.badprinter.yobey.service.PLAYER_SERVICE");
            switch (view.getId()) {
                case R.id.playBt:
                    if (isPlay == false) {
                        intent.putExtra("controlMsg", Constants.PlayerControl.CONTINUE_PLAYING_MSG);
                    } else {
                        intent.putExtra("controlMsg", Constants.PlayerControl.PAUSE_PLAYING_MSG);
                    }
                    getActivity().startService(intent);
                    break;
                case R.id.nextBt:
                    intent.putExtra("controlMsg", Constants.PlayerControl.NEXT_SONG_MSG);
                    getActivity().startService(intent);
                    playDrawableAnim(nextBt, 2, animNext);
                    break;
                case R.id.preBt:
                    intent.putExtra("controlMsg", Constants.PlayerControl.PRE_SONG_MSG);
                    getActivity().startService(intent);
                    playDrawableAnim(preBt, 3, animPre);
                    break;
                case R.id.player:
                    Intent trunToPlayerIntent = new Intent(getActivity(), Player.class);
                    //trunToPlayerIntent.putExtra("current", current);
                    trunToPlayerIntent.putExtra("isPlay", isPlay);
                    trunToPlayerIntent.putExtra("currentSongId", currentSongId);
                    getActivity().startActivity(trunToPlayerIntent);
                    getActivity().overridePendingTransition(R.anim.activity_slide_in, R.anim.activity_null);
                    break;
                default:
                    break;
            }
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
    private class HomeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Constants.UiControl.UPDATE_UI:
                    boolean isPlayFromSevice = intent.getBooleanExtra("isPlay", false); // Play or Pause

                    currentSongId = intent.getLongExtra("songId", 0); // Current Song Id
                    isPlay = isPlayFromSevice;

                    Song temp = SongProvider.getSongById(currentSongId);
                    Log.e(TAG, "context == null ? " + Boolean.toString(getActivity() == null));
                    playingPhoto.setImageBitmap(SongProvider.getArtwork(getActivity(), temp.getId(), temp.getAlbumId(), false, false));
                    playingArtist.setText(temp.getArtist());
                    playingName.setText(temp.getName());

                    if (isPlay) {
                        playDrawableAnim(playBt, 0, animPlay);
                    } else {
                        playDrawableAnim(playBt, 1, animPlay);
                    }
                    //test
                    listName = intent.getStringExtra("listName");
                    current = intent.getIntExtra("current", 0);

                    break;
                case Constants.UiControl.UPDATE_CURRENT:
                    currentTime = intent.getExtras().getInt("currentTime");
                    updateWaveBar(currentTime);
                    break;
                default:
                    break;
            }


        }
    }
    private void updateWaveBar(int currentTime) {
        Song temp = SongProvider.getSongById(currentSongId);
        if (temp != null) {
            int duration = temp.getDuration();
            int progress = currentTime*100/duration;
            waveBar.setProgress(progress);
        }
    }

}