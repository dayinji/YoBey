package com.badprinter.yobey.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.badprinter.yobey.R;
import com.badprinter.yobey.commom.Constants;
import com.badprinter.yobey.utils.MemoryUtil;
import com.badprinter.yobey.utils.SongProvider;

import java.util.Timer;
import java.util.TimerTask;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public class Welcome extends SwipeBackActivity {
    final private String TAG = "Welcome";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                startActivity(new Intent(Welcome.this, Yobey.class));
                finish();
            }
        }, 3200);

        Timer timer1 = new Timer();
        timer1.schedule(new TimerTask() {
            @Override
            public void run() {
                SongProvider.getSongList();
            }
        }, 800);
    }

}
