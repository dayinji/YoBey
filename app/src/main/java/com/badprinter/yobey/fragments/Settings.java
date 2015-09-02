package com.badprinter.yobey.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.badprinter.yobey.R;
import com.badprinter.yobey.commom.Constants;
import com.leaking.slideswitch.SlideSwitch;

import jp.wasabeef.blurry.Blurry;

public class Settings extends Fragment {
    private final String TAG="Settings";

    private ImageView[] icons = new ImageView[6];
    private View root;
    private SlideSwitch colorSwitch;

    private SharedPreferences sharedPref;

    public Settings() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root =  inflater.inflate(R.layout.fragment_settings, container, false);
        finViewsById();
        sharedPref = getActivity().getSharedPreferences(
                Constants.Preferences.PREFERENCES_KEY, Context.MODE_PRIVATE);
        ViewTreeObserver vto = icons[0].getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                icons[0].getViewTreeObserver().removeGlobalOnLayoutListener(this);
                initIcons();
            }
        });
        setListeners();
        return root;
    }

    private void finViewsById() {
        icons[0] = (ImageView)root.findViewById(R.id.nightIcon);
        icons[1] = (ImageView)root.findViewById(R.id.colorIcon);
        icons[2] = (ImageView)root.findViewById(R.id.adjustIcon);
        icons[3] = (ImageView)root.findViewById(R.id.wifiIcon);
        icons[4] = (ImageView)root.findViewById(R.id.aboutIcon);
        icons[5] = (ImageView)root.findViewById(R.id.exitIcon);
        colorSwitch = (SlideSwitch)root.findViewById(R.id.colorSwith);
    }
    private void initIcons() {
        for (int i = 0 ; i < icons.length ; i++) {
            ViewGroup.LayoutParams lp = icons[i].getLayoutParams();
            lp.width = icons[i].getMeasuredHeight();
            icons[i].setLayoutParams(lp);
        }
    }
    private void setListeners() {
        colorSwitch.setSlideListener(new SlideSwitch.SlideListener() {

            @Override
            public void open() {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt("color", 1);
                editor.commit();
                int a = sharedPref.getInt("color", -1);
                Log.e(TAG, ""+a);
            }

            @Override
            public void close() {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt("color", 0);
                editor.commit();
            }
        });
    }

}
