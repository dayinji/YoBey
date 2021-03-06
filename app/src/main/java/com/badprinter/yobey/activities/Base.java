package com.badprinter.yobey.activities;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.badprinter.yobey.R;

public class Base extends ActionBarActivity {
    private final String TAG= "BASE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        if (getSupportActionBar() != null) {

            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            View customView = getLayoutInflater().inflate(R.layout.action_bar, null);
            actionBar.setCustomView(customView);
            actionBar.setElevation(0);
            Toolbar parent =(Toolbar) customView.getParent();
            parent.setContentInsetsAbsolute(0,0);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_base, menu);
        return true;
    }

}
