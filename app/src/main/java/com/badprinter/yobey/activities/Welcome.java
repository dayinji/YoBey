package com.badprinter.yobey.activities;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.badprinter.yobey.R;
import com.badprinter.yobey.utils.SongProvider;

import java.util.Timer;
import java.util.TimerTask;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public class Welcome extends SwipeBackActivity {

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
        }, 2000);
    }

    @Override
    public void onWindowFocusChanged (boolean hasFocus) {
        SongProvider.getSongList();
    }
}
