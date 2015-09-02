package com.badprinter.yobey.activities;

import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.badprinter.yobey.R;
import com.badprinter.yobey.commom.Constants;
import com.badprinter.yobey.customviews.DragView;
import com.badprinter.yobey.db.DBManager;
import com.badprinter.yobey.fragments.Home1;
import com.badprinter.yobey.fragments.Lists;
import com.badprinter.yobey.fragments.Settings;
import com.badprinter.yobey.models.Artist;
import com.badprinter.yobey.service.PlayerService;

import java.util.ArrayList;
import java.util.List;

public class Yobey extends Base {
    private String TAG = "Yobey";
    private RadioGroup tabs;
    private RadioButton tab_home;
    private RadioButton tab_list;
    private RadioButton tab_artist;
    private RadioButton tab_player;
    private DragView dragView;
    private ViewPager pager;

    private int currentFragment = 0;
    private boolean isPlay = false;
    private int current = 0;
    private int currentTime;
    private boolean isFirstTime = true;
    private YobeyReceiver yobeyReceiver;
    private DBManager dbMgr;

    private Home1 home;
    private Lists lists;
    private com.badprinter.yobey.fragments.Artist artist;
    private Settings settings;

    private Drawable homeDrawableWhite;
    private Drawable listDrawableWhite;
    private Drawable artistDrawableWhite;
    private Drawable settingsDrawableWhite;
    private Drawable homeDrawableGrey;
    private Drawable listDrawableGrey;
    private Drawable artistDrawableGrey;
    private Drawable settingsDrawableGrey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yobey);

        findViewsById();
        dbMgr = new DBManager();
        setOnClickListener();
        initPager();
        dragView.setAnimation(0);

        initTabs();

        yobeyReceiver = new YobeyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.UiControl.UPDATE_UI);
        filter.addAction(Constants.UiControl.UPDATE_CURRENT);
        registerReceiver(yobeyReceiver, filter);

    }
    private void findViewsById() {
        tabs = (RadioGroup)findViewById(R.id.tabs);
        tab_home = (RadioButton)findViewById(R.id.homeTab);
        tab_artist = (RadioButton)findViewById(R.id.artistTab);
        tab_list = (RadioButton)findViewById(R.id.listTab);
        tab_player = (RadioButton)findViewById(R.id.playerTab);
        dragView = (DragView)findViewById(R.id.drag);
        pager = (ViewPager)findViewById(R.id.pager);

    }
    private void setOnClickListener() {
        tabs.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.homeTab:
                        dragView.setAnimation(0);
                        pager.setCurrentItem(0, true);
                        setWhiteText(tab_home, 0);
                        break;
                    case R.id.listTab:
                        dragView.setAnimation(1);
                        pager.setCurrentItem(1, true);
                        setWhiteText(tab_list, 1);
                        break;
                    case R.id.artistTab:
                        dragView.setAnimation(2);
                        pager.setCurrentItem(2, true);
                        setWhiteText(tab_artist, 2);
                        break;
                    case R.id.playerTab:
                        dragView.setAnimation(3);
                        pager.setCurrentItem(3, true);
                        setWhiteText(tab_player, 3);
                        break;
                    default:
                        break;
                }
                //updateFragment(checkedId);
            }
        });
    }
    private void initTabs() {
        homeDrawableWhite = getResources().getDrawable(R.drawable.home_white);
        homeDrawableWhite.setBounds(0, 0, 50, 50);
        homeDrawableGrey = getResources().getDrawable(R.drawable.home_grey);
        homeDrawableGrey.setBounds(0, 0, 50, 50);

        listDrawableGrey = getResources().getDrawable(R.drawable.list_grey);
        listDrawableGrey.setBounds(0, 0, 50, 50);
        listDrawableWhite = getResources().getDrawable(R.drawable.list_white);
        listDrawableWhite.setBounds(0, 0, 50, 50);

        artistDrawableGrey = getResources().getDrawable(R.drawable.artist_grey);
        artistDrawableGrey.setBounds(0, 0, 50, 50);
        artistDrawableWhite = getResources().getDrawable(R.drawable.artist_white);
        artistDrawableWhite.setBounds(0, 0, 50, 50);

        settingsDrawableGrey = getResources().getDrawable(R.drawable.settings_grey);
        settingsDrawableGrey.setBounds(0, 0, 50, 50);
        settingsDrawableWhite = getResources().getDrawable(R.drawable.settings_white);
        settingsDrawableWhite.setBounds(0, 0, 50, 50);


        tab_player.setCompoundDrawables(null, settingsDrawableGrey, null, null);
        tab_home.setCompoundDrawables(null, homeDrawableWhite, null, null);
        tab_artist.setCompoundDrawables(null, artistDrawableGrey, null, null);
        tab_list.setCompoundDrawables(null, listDrawableGrey, null, null);
    }
    private void initPager() {
        home = new Home1();
        lists = new Lists();
        artist = new com.badprinter.yobey.fragments.Artist();
        settings = new Settings();

        List<Fragment> list = new ArrayList<>();
        list.add(home);
        list.add(lists);
        list.add(artist);
        list.add(settings);
        pager.setAdapter(new MyPagerAdapter(getSupportFragmentManager(), list));
        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                dragView.setAnimation(position);
                RadioButton[] tabs = {tab_home, tab_list, tab_artist, tab_player};
                setWhiteText(tabs[position], position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        pager.setAlwaysDrawnWithCacheEnabled(true);
        pager.setHorizontalFadingEdgeEnabled(false);
    }
    private void setWhiteText(RadioButton tab, int position) {
        tab_player.setTextColor(getResources().getColor(R.color.qianhui));
        tab_list.setTextColor(getResources().getColor(R.color.qianhui));
        tab_artist.setTextColor(getResources().getColor(R.color.qianhui));
        tab_home.setTextColor(getResources().getColor(R.color.qianhui));

        tab_player.setCompoundDrawables(null, settingsDrawableGrey, null, null);
        tab_list.setCompoundDrawables(null, listDrawableGrey, null, null);
        tab_artist.setCompoundDrawables(null, artistDrawableGrey, null, null);
        tab_home.setCompoundDrawables(null, homeDrawableGrey, null, null);

        Drawable[] temps = {homeDrawableWhite, listDrawableWhite, artistDrawableWhite, settingsDrawableWhite};
        tab.setTextColor(getResources().getColor(R.color.baise));
        tab.setCompoundDrawables(null, temps[position], null, null);

    }
    /*private void updateFragment(int id) {
        fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        Fragment fragment;
        switch (id) {
            case R.id.homeTab:
                if (home == null) {
                    home = new Home1();
                    Log.e(TAG, "new Home");
                }
                Log.e(TAG, "old Home");
                fragment  = home;
                break;
            case R.id.listTab:
                if (lists == null)
                    lists = new Lists();
                fragment  = lists;
                break;
            case R.id.artistTab:
            case R.id.playerTab:
            default:
                if (home == null)
                    home = new Home1();
                fragment  = home;
                break;
        }
        transaction.setCustomAnimations(R.anim.enter, R.anim.exit);
        transaction.replace(R.id.frameLayout, fragment);
        transaction.commit();
    }*/
    @Override
    public void onDestroy() {
        Intent intent = new Intent(Yobey.this, PlayerService.class);
        stopService(intent);
        unregisterReceiver(yobeyReceiver);
        super.onDestroy();
    }
    private class YobeyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Constants.UiControl.UPDATE_UI:
                    boolean isPlay = intent.getBooleanExtra("isPlay", false); // Play or Pause
                    int current = intent.getIntExtra("current", -1); // Current Song Id
                    Yobey.this.isPlay = isPlay;
                    Yobey.this.current = current;
                    break;
                case Constants.UiControl.UPDATE_CURRENT:
                   // updateBar(intent.getExtras().getInt("currentTime"));
            }


        }
    }
    public DBManager getDBMgr() {
        return dbMgr;
    }
    private class MyPagerAdapter extends FragmentPagerAdapter {
        private List<Fragment> fragmentList;
        public MyPagerAdapter(FragmentManager fm, List<Fragment> list) {
            super(fm);
            this.fragmentList = list;
        }
        @Override
        public int getCount() {
            return fragmentList.size();
        }
        @Override
        public Fragment getItem(int arg0) {
            return fragmentList.get(arg0);
        }
        @Override
        public void destroyItem (ViewGroup container, int position, Object object) {
            // Never Destroy Fragment for Preventing from Getting stuck!
            return;
        }
    }


}
