package com.badprinter.yobey.customviews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

import com.badprinter.yobey.R;
import com.badprinter.yobey.utils.LyricUtil;

import java.util.List;

/**
 * Created by root on 15-8-15.
 */
public class Lyric extends TextView {
    private Paint paint;
    private LyricUtil lyricUtil = new LyricUtil();
    private String filePath = "";
    private int currentTime = 0;
    private List<String> lyricList;
    private List<Integer> timeList;



    public Lyric(Context context) {
        this(context, null);
    }

    public Lyric(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Lyric(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setFile(String url) {
        filePath = url.replace(".mp3", ".lrc");
        lyricUtil.inti(filePath);
        lyricList = lyricUtil.getLyricList();
        timeList = lyricUtil.getTimeList();
    }

    public void setCurrentTime(int currentTime) {
        this.currentTime = currentTime;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint = new Paint();
        paint.setColor(getResources().getColor(R.color.qianhui));
        paint.setTextSize(30);
        paint.setAntiAlias(true);  //消除锯齿
        paint.setTextAlign(Paint.Align.CENTER);
        int y = 0;
        int dy = 50;
        for (int i = 0; i < lyricList.size(); i++) {
            paint.setColor(Color.WHITE);
            canvas.drawText(lyricList.get(i), getWidth()/2, y, paint);
            y += dy;
        }
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), 70*50);
    }

}
