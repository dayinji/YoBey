package com.badprinter.yobey.fragments;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.badprinter.yobey.R;
import com.badprinter.yobey.activities.SongList;
import com.badprinter.yobey.commom.Constants;
import com.indris.material.RippleView;
import com.leaking.slideswitch.SlideSwitch;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import jp.wasabeef.blurry.Blurry;

public class Settings extends Fragment implements View.OnClickListener{
    private final String TAG="Settings";

    private ImageView[] icons = new ImageView[6];
    private View root;
    private SlideSwitch colorSwitch;
    private RippleView exitRipple;
    private AlertDialog exitAlert;
    private RippleView nightRipple;
    private AlertDialog nightAlert;
    private Timer timer;
    private int nightMode = 0;
    private int nightTime = -1;
    private Handler handler;
    private TextView countdown;

    private SharedPreferences sharedPref;
    public static final String ACTION_REBOOT =
            "android.intent.action.REBOOT";
    public static final String ACTION_REQUEST_SHUTDOWN = "android.intent.action.ACTION_REQUEST_SHUTDOWN";
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
        // Init sharedPref
        sharedPref = getActivity().getSharedPreferences(
                Constants.Preferences.PREFERENCES_KEY, Context.MODE_PRIVATE);
        // Init Icons for Modifying Their Width
        ViewTreeObserver vto = icons[0].getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                icons[0].getViewTreeObserver().removeGlobalOnLayoutListener(this);
                initIcons();
            }
        });
        setListeners();

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    try {
                        if (nightTime == 0)
                            getActivity().finish();
                        nightTime--;
                        countdown.setText(getTimeString(nightTime));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
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
        exitRipple = (RippleView)root.findViewById(R.id.exitRipple);
        nightRipple = (RippleView)root.findViewById(R.id.nightRipple);
        countdown = (TextView)root.findViewById(R.id.countdown);
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
        exitRipple.setOnClickListener(this);
        nightRipple.setOnClickListener(this);
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.exitRipple:
                exitRipple.setClickable(false);
                Runnable exitRun = new Runnable() {
                    public void run() {
                        showExitDialog();
                        exitRipple.setClickable(true);
                    }
                };
                delayRun(exitRun, 500);
                break;
            case R.id.nightRipple:
                nightRipple.setClickable(false);
                Runnable nightRun = new Runnable() {
                    public void run() {
                        showNightDialog();
                        nightRipple.setClickable(true);
                    }
                };
                delayRun(nightRun, 500);
                break;
            case R.id.exitYesBt:
                Button exitYesBt = (Button)exitAlert.findViewById(R.id.exitYesBt);
                exitYesBt.setBackgroundColor(getResources().getColor(R.color.qingse));
                exitYesBt.setTextColor(getResources().getColor(R.color.baise));
                Runnable exitYesRun = new Runnable() {
                    public void run() {
                        getActivity().finish();
                    }
                };
                delayRun(exitYesRun, 50);
                break;
            case R.id.exitNoBt:
                Button exitNoBt = (Button)exitAlert.findViewById(R.id.exitNoBt);
                exitNoBt.setBackgroundColor(getResources().getColor(R.color.qingse));
                exitNoBt.setTextColor(getResources().getColor(R.color.baise));
                Runnable exitNoRun = new Runnable() {
                    public void run() {
                        exitAlert.dismiss();
                    }
                };
                delayRun(exitNoRun, 50);
                break;
            case R.id.nightCloseBt:
            case R.id.nightMins10Bt:
            case R.id.nightMins20Bt:
            case R.id.nightMins30Bt:
            case R.id.nightMins45Bt:
            case R.id.nightMins60Bt:
            case R.id.nightMins90Bt:
                ArrayList<Integer> btIds = new ArrayList<>();
                btIds.add(R.id.nightCloseBt);
                btIds.add(R.id.nightMins10Bt);
                btIds.add(R.id.nightMins20Bt);
                btIds.add(R.id.nightMins30Bt);
                btIds.add(R.id.nightMins45Bt);
                btIds.add(R.id.nightMins60Bt);
                btIds.add(R.id.nightMins90Bt);

                Button lastBt = (Button)nightAlert.findViewById(btIds.get(nightMode));
                Button selectedBt = (Button)nightAlert.findViewById(view.getId());
                // Change Color
                selectedBt.setBackgroundColor(getResources().getColor(R.color.qingse));
                selectedBt.setTextColor(getResources().getColor(R.color.baise));
                lastBt.setBackgroundColor(getResources().getColor(R.color.baise));
                lastBt.setTextColor(getResources().getColor(R.color.qingse));
                // Reset nightTime And nightMode
                int[] nightTimes = {-1, 10*60, 20*60, 30*60, 45*60, 60*60, 90*60};
                nightMode = btIds.indexOf(view.getId());
                nightTime = nightTimes[nightMode];
                setAlarmShutdown();
                Runnable nightDismissRun = new Runnable() {
                    public void run() {
                        nightAlert.dismiss();
                    }
                };
                delayRun(nightDismissRun, 50);
                break;
        }
    }
    /*
     * Show ExitDialog
     */
    private void showExitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_exit, null);

        builder.setView(view);

        final Button exitYesBt = (Button)view.findViewById(R.id.exitYesBt);
        final Button exitNoBt = (Button)view.findViewById(R.id.exitNoBt);

        exitYesBt.setOnClickListener(this);
        exitNoBt.setOnClickListener(this);

        exitAlert = builder.create();
        exitAlert.show();
    }
    /*
     * Show NightDialog
     */
    private void showNightDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_night, null);

        final Button nightCloseBt = (Button)view.findViewById(R.id.nightCloseBt);
        final Button nightMins10Bt = (Button)view.findViewById(R.id.nightMins10Bt);
        final Button nightMins20Bt = (Button)view.findViewById(R.id.nightMins20Bt);
        final Button nightMins30Bt = (Button)view.findViewById(R.id.nightMins30Bt);
        final Button nightMins45Bt = (Button)view.findViewById(R.id.nightMins45Bt);
        final Button nightMins60Bt = (Button)view.findViewById(R.id.nightMins60Bt);
        final Button nightMins90Bt = (Button)view.findViewById(R.id.nightMins90Bt);

        nightCloseBt.setOnClickListener(this);
        nightMins10Bt.setOnClickListener(this);
        nightMins20Bt.setOnClickListener(this);
        nightMins30Bt.setOnClickListener(this);
        nightMins45Bt.setOnClickListener(this);
        nightMins60Bt.setOnClickListener(this);
        nightMins90Bt.setOnClickListener(this);

        Button[] bts = {nightCloseBt, nightMins10Bt, nightMins20Bt, nightMins30Bt,
                nightMins45Bt, nightMins60Bt, nightMins90Bt};
        bts[nightMode].setBackgroundColor(getResources().getColor(R.color.qingse));
        bts[nightMode].setTextColor(getResources().getColor(R.color.baise));

        builder.setView(view);

        nightAlert = builder.create();
        nightAlert.show();
    }
    /*
     * Run A Runable after DelayTime
     */
    private void delayRun(Runnable run, int delayTime) {
        Handler handlerTimer = new Handler();
        handlerTimer.postDelayed(run, delayTime);
    }
    /*
     * Set CountDown Timer
     */
    private void setAlarmShutdown() {
        if (timer != null)
            timer.cancel();
        if (nightMode == 0) {
            countdown.setText("");
            return;
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(1);
            }
        }, 0, 1000);
    }
    /*
     * Convert a Int(second) to mm:ss Format
     */
    private String getTimeString(int time) {
        int min = time / 60;
        int sec = time % 60;
        String result = String.format("%02d", min) + ":" + String.format("%02d", sec);
        return result;
    }
}
