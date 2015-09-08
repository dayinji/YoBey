package com.badprinter.yobey.customviews;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.badprinter.yobey.R;
import com.badprinter.yobey.commom.Constants;
import com.badprinter.yobey.utils.LyricUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by root on 15-9-8.
 */
public class WindowLyric extends TextView {
    final String TAG= "WindowLyric";
    private WindowManager wm;
    private float y;
    private String text1 = "";
    private String text2 = "";
    private float textSize;
    private LyricUtil lyricUtil;
    private List<String> lyricList;
    private List<Integer> timeList;
    private int currentTime;
    private int id;
    private float innerY;
    private Handler handler;
    private Timer timer;
    public CallBack callBack;
    private float density;
    private int displayWidth;
    private float strokeWidth;
    private Paint paintFill;
    private Paint paintStroke;
    private int hasPlayedColor = getResources().getColor(R.color.bright_lanse);
    private int noPlayedColor = getResources().getColor(R.color.qianbai);
    private int strokeColor = Color.GRAY;

    static public WindowManager.LayoutParams params;

    public WindowLyric(Context context, String path, String name, String artist) {
        super(context);
        // Init Lists
        lyricList = new ArrayList<>();
        timeList = new ArrayList<>();
        // Init LyricUtil
        lyricUtil = new LyricUtil();
        initLyric(path, name, artist);
        currentTime = 0;
        id = 0;
        // Get Density And DisplayWidth
        density = getResources().getDisplayMetrics().density;
        displayWidth = getResources().getDisplayMetrics().widthPixels;
        // Set StrokeWidth And Textsize
        strokeWidth = 0.5f*density;
        textSize = (int)(20*density);
        // Init Paint
        paintFill = new Paint();
        paintFill.setTypeface(Typeface.DEFAULT_BOLD);
        paintFill.setTextAlign(Paint.Align.CENTER);
        paintFill.setAntiAlias(true);
        paintFill.setTextSize(textSize);
        paintStroke = new Paint();
        paintStroke.setTypeface(Typeface.DEFAULT_BOLD);
        paintStroke.setTextAlign(Paint.Align.CENTER);
        paintStroke.setAntiAlias(true);
        paintStroke.setTextSize(textSize);
        paintStroke.setStyle(Paint.Style.STROKE);
        paintStroke.setStrokeWidth(strokeWidth);
        paintStroke.setColor(strokeColor);


        wm = (WindowManager) getContext().getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE);

        params = new WindowManager.LayoutParams();
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
                | WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;// 设置窗口类型为系统级
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
               // |WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;

        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        //params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = (int)(50*density);
        //params.alpha = 30;
        // Set Bg Transparent
        params.format = PixelFormat.RGBA_8888;
        params.x = 0;
        params.y = wm.getDefaultDisplay().getHeight() / 2;

        params.gravity = Gravity.LEFT | Gravity.TOP;

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    try {
                        setCurrentTime(callBack.getCurrentTime());
                        invalidate();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        // For Updating Draw
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(1);
            }
        }, 0, 60);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Text 1
        paintFill.setColor(hasPlayedColor);
        canvas.drawText(text1, displayWidth / 2, textSize, paintStroke);
        canvas.drawText(text1, displayWidth / 2, textSize, paintFill);
        // Text 2
        if (lyricList.size() > id+1) {
            paintFill.setColor(noPlayedColor);
            canvas.drawText(lyricList.get(id + 1), displayWidth / 2, params.height - 5 * density, paintStroke);
            canvas.drawText(lyricList.get(id+1), displayWidth/2, params.height-5*density, paintFill);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent me) {
        if (me.getAction() == MotionEvent.ACTION_DOWN) {
            innerY = me.getY();
        } else if (me.getAction() == MotionEvent.ACTION_MOVE) {
            float meY = me.getRawY();
            y = meY - innerY;
            updatePosition(0, y);
        } else if (me.getAction() == MotionEvent.ACTION_UP) {

        }
        return true;
    }

    private void updatePosition(float x, float y) {
        params.x = (int) x;
        params.y = (int) y;
        wm.updateViewLayout(this, params);
    }
    /*
     * Init LyricList
     */
    public void initLyric(String path, String name, String artist) {
        path = path.replace(".mp3", ".lrc");
        /*
         * When there is no lyric and download from the net
         * We need to reset the height of Lyric View.
         */
        lyricUtil.callback = new LyricUtil.OnDownLoadLyric() {
            @Override
            public void onDownLoadLyric() {
                // Do Nothing
            }
        };
        lyricUtil.init(path, name, artist);
        lyricList = lyricUtil.getLyricList();
        timeList = lyricUtil.getTimeList();
        id = 0;
        currentTime = 0;
        text1 = lyricList.get(0);
        if (lyricList.size() > 1)
            text2 = lyricList.get(1);
        else
            text2 = "";
    }
    public void setCurrentTime(int currentTime) {
        this.currentTime = currentTime;
        /*
         * Must looping to check with currentTime which lyric id is
         * Because user may drap the bar forward or aferward!
         */
        for (int i = 0; i < lyricList.size(); i++) {
            if (i == lyricList.size() - 1) {
                if (id != i &&  currentTime >= timeList.get(i)) {
                    id = i;
                    text1 = lyricList.get(id);

                }
            } else if (currentTime < timeList.get(i+1) && currentTime >= timeList.get(i) && id != i) {
                id = i;
                text1 = lyricList.get(id);
                break;
            }
        }
    }

    public String getText() {
        return text1;
    }
    public void setText(String text) {
        this.text1 = text;
    }

    public WindowManager.LayoutParams getParam() {
        return params;
    }

    public interface CallBack {
        int getCurrentTime();
    }

}
