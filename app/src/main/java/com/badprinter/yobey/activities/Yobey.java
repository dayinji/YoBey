package com.badprinter.yobey.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.badprinter.yobey.R;
import com.badprinter.yobey.commom.Constants;
import com.badprinter.yobey.customviews.DragView;
import com.badprinter.yobey.db.DBManager;
import com.badprinter.yobey.fragments.Home1;
import com.badprinter.yobey.fragments.Lists;
import com.badprinter.yobey.service.PlayerService;

public class Yobey extends ActionBarActivity {
    private String TAG = "Yobey";
    private RadioGroup tabs;
    private RadioButton tab_home;
    private RadioButton tab_list;
    private RadioButton tab_artist;
    private RadioButton tab_player;
    private FrameLayout frameLayout;
    private DragView dragView;

    private FragmentManager fragmentManager;
    private int currentFragment = 0;
    private boolean isPlay = false;
    private int current = 0;
    private int currentTime;
    private boolean isFirstTime = true;
    private YobeyReceiver yobeyReceiver;
    private DBManager dbMgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yobey);
        findViewsById();
        dbMgr = new DBManager(this);
        setOnClickListener();
        updateFragment(1);
        dragView.setAnimation(0);
        yobeyReceiver = new YobeyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.UiControl.UPDATE_UI);
        filter.addAction(Constants.UiControl.UPDATE_CURRENT);
        registerReceiver(yobeyReceiver, filter);

    }
    private void findViewsById() {
        frameLayout = (FrameLayout)findViewById(R.id.frameLayout);
        tabs = (RadioGroup)findViewById(R.id.tabs);
        tab_home = (RadioButton)findViewById(R.id.homeTab);
        tab_artist = (RadioButton)findViewById(R.id.artistTab);
        tab_list = (RadioButton)findViewById(R.id.listTab);
        tab_player = (RadioButton)findViewById(R.id.playerTab);
        dragView = (DragView)findViewById(R.id.drag);

    }
    private void setOnClickListener() {
        tabs.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                updateFragment(checkedId);
                switch (checkedId) {
                    case R.id.homeTab:
                        dragView.setAnimation(0);
                        break;
                    case R.id.listTab:
                        dragView.setAnimation(1);
                        break;
                    case R.id.artistTab:
                        dragView.setAnimation(2);
                        break;
                    case R.id.playerTab:
                        dragView.setAnimation(3);
                        break;
                    default:
                        break;
                }
            }
        });
    }
    private void updateFragment(int id) {
        fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        Fragment fragment;
        switch (id) {
            case R.id.homeTab:
                fragment  = new Home1();
                break;
            case R.id.listTab:
                fragment  = new Lists();
                break;
            case R.id.artistTab:
            case R.id.playerTab:
            default:
                fragment = new Home1();

        }
        transaction.replace(R.id.frameLayout, fragment);
        transaction.commit();
    }
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


}
