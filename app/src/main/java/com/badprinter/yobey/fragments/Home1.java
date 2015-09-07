package com.badprinter.yobey.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.badprinter.yobey.R;
import com.badprinter.yobey.activities.Player;
import com.badprinter.yobey.adapter.CountAdapter;
import com.badprinter.yobey.commom.AppContext;
import com.badprinter.yobey.commom.Constants;
import com.badprinter.yobey.customviews.WaveView;
import com.badprinter.yobey.db.DBManager;
import com.badprinter.yobey.models.Song;
import com.badprinter.yobey.utils.MyEvalucatorUtil;
import com.badprinter.yobey.utils.SongProvider;
import com.db.chart.listener.OnEntryClickListener;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.LineChartView;
import com.db.chart.view.Tooltip;
import com.db.chart.view.animation.Animation;
import com.db.chart.view.animation.easing.BounceEase;
import com.db.chart.view.animation.easing.CircEase;

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
    private ImageView bear;
    private TextView msg;


    private DBManager dbMgr;
    private MyClickListener listener = new MyClickListener();
    private boolean isPlay = false;
    private long currentSongId = 0;
    /*private AnimationDrawable animPlay;
    private AnimationDrawable animNext;
    private AnimationDrawable animPre;*/
    private int[] animId = new int[] {
            R.drawable.playtopause_00000, R.drawable.pausetoplay_00000,
            R.drawable.playnext_00000, R.drawable.playpre_00000
    };
    private HomeReceiver homeReceiver;
    private CountAdapter countAdapter;
    private String listName = Constants.ListName.LIST_ALL;
    private int current = 0;
    private int currentTime = 0;
    private ValueAnimator waveAnim;
    private ValueAnimator cdAnim;


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

        // Init Song
        SharedPreferences sharedPref = getActivity().getSharedPreferences(
                Constants.Preferences.PREFERENCES_KEY, Context.MODE_PRIVATE);
        listName = sharedPref.getString("lastListName", Constants.ListName.LIST_ALL);
        current = sharedPref.getInt("lastCurrent", 0);
        currentTime = sharedPref.getInt("lastCurrentTime", 0);



        countAdapter = new CountAdapter();
        countList.setAdapter(countAdapter);
        countList.setEnabled(false);


        ptrFrame.setPtrHandler(new PtrHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                countAdapter.updateCount();
                frame.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ptrFrame.refreshComplete();
                    }
                }, 1000);
            }

            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header);
            }
        });
        ptrFrame.addPtrUIHandler(new PtrUIHandler() {
            @Override
            public void onUIReset(PtrFrameLayout ptrFrameLayout) {
                msg.setText("下拉刷新");
                Log.e(TAG, "onUIReset");
            }

            @Override
            public void onUIRefreshPrepare(PtrFrameLayout ptrFrameLayout) {
                Log.e(TAG, "onUIRefreshPrepare");
            }

            @Override
            public void onUIRefreshBegin(PtrFrameLayout ptrFrameLayout) {
                msg.setText("正在刷新...");
                cdAnim = ValueAnimator.ofFloat(0, 1);
                cdAnim.setDuration(700);
                cdAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        cd.setRotation((float) animation.getAnimatedValue() * 360);
                    }
                });
                cdAnim.setRepeatMode(ValueAnimator.RESTART);
                cdAnim.setRepeatCount(-1);
                cdAnim.setInterpolator(new LinearInterpolator());
                cdAnim.start();
                Log.e(TAG, "onUIRefreshBegin");
            }

            @Override
            public void onUIRefreshComplete(PtrFrameLayout ptrFrameLayout) {
                msg.setText("刷新成功");
                if (cdAnim != null && cdAnim.isRunning())
                    cdAnim.cancel();
                Log.e(TAG, "onUIRefreshComplete");
            }

            @Override
            public void onUIPositionChange(PtrFrameLayout ptrFrameLayout, boolean b, byte b1, PtrIndicator ptrIndicator) {

                int curY = ptrIndicator.getCurrentPosY();
                int offsetY = ptrIndicator.getOffsetToRefresh();
                int height = ptrIndicator.getHeaderHeight();

                cd.setRotation(curY * 10);
                cd.setScaleX(1 - (float) curY / height / 3);
                cd.setScaleY(1 - (float) curY / height / 3);
                bear.setScaleX((float) curY / height / 3 + 1);
                bear.setScaleY((float) curY / height / 3 + 1);
                float k = -(50+offsetY)/(float)offsetY;
                if (ptrIndicator.getCurrentPosY() <= ptrIndicator.getOffsetToRefresh()) {
                    cd.setY(curY * k + height + 50);
                    if (b)
                        msg.setText("下拉刷新");
                } else {
                    if (b)
                        msg.setText("松开刷新");
                }
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

                // Init Notify
                SharedPreferences sharedPref = getActivity().getSharedPreferences(
                            Constants.Preferences.PREFERENCES_KEY, Context.MODE_PRIVATE);
                int hasNotify = sharedPref.getInt(Constants.Preferences.PREFERENCES_NOTIFY, Context.MODE_PRIVATE);
                if (hasNotify == 1) {
                    Intent intent1 = new Intent("com.badprinter.yobey.service.PLAYER_SERVICE");
                    intent1.putExtra("hasNotify", true);
                    intent1.putExtra("controlMsg", Constants.PlayerControl.UPDATE_NOTIFY);
                    getActivity().startService(intent1);
                } else {
                    Intent intent1 = new Intent("com.badprinter.yobey.service.PLAYER_SERVICE");
                    intent1.putExtra("hasNotify", false);
                    intent1.putExtra("controlMsg", Constants.PlayerControl.UPDATE_NOTIFY);
                    getActivity().startService(intent1);
                }

            }
        });

        return root;
    }
    @Override
    public void onDetach() {
        // Save the Last Info of Player
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
        bear = (ImageView)root.findViewById(R.id.bear);
        msg = (TextView)root.findViewById(R.id.msg);
    }

    /*
     * Set the ClickListener to Views
     */
    private void setClickListener() {
        preBt.setOnClickListener(listener);
        nextBt.setOnClickListener(listener);
        playBt.setOnClickListener(listener);
        playingPhoto.setOnClickListener(listener);
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
                    //playDrawableAnim(nextBt, 2, animNext);
                 //   playAnim(nextBt, animId[2]);
                    break;
                case R.id.preBt:
                    intent.putExtra("controlMsg", Constants.PlayerControl.PRE_SONG_MSG);
                    getActivity().startService(intent);
                    //playDrawableAnim(preBt, 3, animPre);
                   // playAnim(preBt, animId[3]);
                    break;
                case R.id.playingPhoto:
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
    /*private void playDrawableAnim(ImageView view, int id, AnimationDrawable animDrawable) {
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
    }*/
    private void playAnim(final ImageView img, final int id) {
        ValueAnimator zoomOut = ValueAnimator.ofFloat(1, 0);
        final ValueAnimator zoomIn = ValueAnimator.ofFloat(0, 1);
        zoomOut.setDuration(100);
        zoomOut.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float f = (float) animation.getAnimatedValue();
                img.setScaleX(f);
                img.setScaleY(f);
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
                img.setScaleX(f);
                img.setScaleY(f);
            }
        });
        zoomOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                img.setImageDrawable(getResources().getDrawable(id));
                zoomIn.start();
            }
        });
        zoomOut.start();
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
                    if (isPlayFromSevice && isPlay != isPlayFromSevice) {
                        //playDrawableAnim(playBt, 0, animPlay);
                        playAnim(playBt, animId[1]);
                    } else if (!isPlayFromSevice && isPlay != isPlayFromSevice) {
                        // playDrawableAnim(playBt, 1, animPlay);
                        playAnim(playBt, animId[0]);
                    }
                    isPlay = isPlayFromSevice;

                    Song temp = SongProvider.getSongById(currentSongId);
                    playingPhoto.setImageBitmap(SongProvider.getArtwork(getActivity(), temp.getId(), temp.getAlbumId(), true, false));
                    playingArtist.setText(temp.getArtist());
                    playingName.setText(temp.getName());

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
            if (waveAnim != null && waveAnim.isRunning())
                waveAnim.cancel();
            waveAnim = ValueAnimator.ofFloat(0, 1);
            waveAnim.setDuration(400);
            int duration = temp.getDuration();
            final int currentProgress = waveBar.getProgress();
            final int progress = currentTime*100/duration;
            waveAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int offset = (int)((progress - currentProgress)*(float)animation.getAnimatedValue());
                    waveBar.setProgress(currentProgress + offset);
                }
            });
            waveAnim.start();
        }
    }

}