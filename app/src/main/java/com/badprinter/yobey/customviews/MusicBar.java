package com.badprinter.yobey.customviews;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.SeekBar;

import com.badprinter.yobey.R;

/**
 * Created by root on 15-8-11.
 */
public class MusicBar extends View {
    private final String TAG = "MusicBar";
    private Paint paint;
    private int max;
    private int progress;
    private int backgroundColor;
    private int havePlayedColor;
    private float barHeight;
    private float barWidth;
    private int indicatorColor;
    private float indicatorRadis;
    private float barRadius;
    private ValueAnimator indicatorAnim;
    public OnProgessChange onProgessChange;

    public MusicBar(Context context) {
        this(context, null);
    }

    public MusicBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MusicBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        paint = new Paint();

        TypedArray mTypedArray = context.obtainStyledAttributes(attrs,
                R.styleable.MusicBar);

        //获取自定义属性和默认值
        max = mTypedArray.getInt(R.styleable.MusicBar_max, 300);
        progress = mTypedArray.getInt(R.styleable.MusicBar_progress, 0);
        backgroundColor = mTypedArray.getColor(R.styleable.MusicBar_backgroundColor, 0);
        havePlayedColor = mTypedArray.getColor(R.styleable.MusicBar_havePlayedColor, 0);
        barHeight = mTypedArray.getDimension(R.styleable.MusicBar_barHeight, 0);
        //if(barHeight < 2*dif + 1) barHeight = 2*dif + 1;
        indicatorRadis = mTypedArray.getDimension(R.styleable.MusicBar_indicatorRadius, 0);
        indicatorColor = mTypedArray.getColor(R.styleable.MusicBar_indicatorColor, 0);
        barRadius = (float)barHeight/2;

        mTypedArray.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        barWidth = getWidth();

        /**
         * background, Top = 10 For Train Indicator
         */
        RectF rect = new RectF(0, 40, barWidth, barHeight+40);
        paint.setColor(backgroundColor);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);  //消除锯齿
        canvas.drawRect(rect, paint);


        /**
         * havePlayed
         */
        paint.setColor(havePlayedColor);
        RectF rect1 = new RectF(0, 40, barWidth*((float)progress/max), barHeight+40);
        canvas.drawRect(rect1, paint);

        /**
         * indicator (Train)
         */
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.train);
        float scale = 40.0f / bmp.getHeight();
        float trainLong = bmp.getWidth()*scale;
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        Bitmap bmp1 = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
        if (!bmp.isRecycled())
            bmp.recycle();
        canvas.drawBitmap(bmp1, barWidth * ((float) progress / max) - trainLong, 0, null);
        /*paint.setColor(indicatorColor);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        float indicatorCentre = barWidth*((float)progress/max);
        canvas.drawCircle(indicatorCentre, indicatorRadis, indicatorRadis, paint);*/
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), 48);
        //this.setLayoutParams(new ParentLayoutType.LayoutParams(parentWidth/2,parentHeight));
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public synchronized int getMax() { return max; }
    public synchronized void setMax(int max) {
        if (max < 0)
            this.max = 0;
        else
            this.max = max;
    }

    public synchronized int getProgress() { return progress; }
    public synchronized void setProgress(int progress) {
        if (progress > max) {
            this.progress = max;
        } else if (progress < 0) {
            this.progress = 0;
        } else {
            this.progress = progress;
        }
        //invalidate();
        postInvalidate();
    }
    public void setAnimProgress(int progress) {
        startIndicatorAnim(progress);
    }

    public int getBackgroundColor() { return backgroundColor; }
    public void setBackgroundColor(int color) { backgroundColor = color; }

    public int getHavePlayedColor() { return havePlayedColor; }
    public void setHavePlayedColor(int color) { havePlayedColor = color; }

    public int getIndicatorColor() { return indicatorColor; }
    public void setIndicatorColor(int color) { indicatorColor = color; }

    public float getbarHeight() { return barHeight; }
    public void setbarHeight(float height) { barHeight = height; }

    public float getIndicatorRadis() { return indicatorRadis; }
    public void setIndicatorRadis(float radius) { indicatorRadis = radius; }


    @Override
    public boolean onTouchEvent(MotionEvent me) {
        if (me.getAction() == MotionEvent.ACTION_MOVE) {
            float x = me.getX();
            int toPoint = (int)((x/getWidth())*max);
            setProgress(toPoint);
            onProgessChange.OnProgessChangeCall(toPoint);
        }
        else if (me.getAction() == MotionEvent.ACTION_DOWN) {
            float x = me.getX();
            int toPoint = (int)((x/getWidth())*max);
            startIndicatorAnim(toPoint);
            onProgessChange.OnProgessChangeCall(toPoint);
        }
        return true;
    }

    private void startIndicatorAnim(int endValue) {
        if (indicatorAnim != null)
            indicatorAnim.cancel();
        indicatorAnim = ValueAnimator.ofInt(progress, endValue);
        float duration = Math.abs((float)progress - (float)endValue)/max*600;
        indicatorAnim.setDuration((int)duration);
        indicatorAnim.setInterpolator(new DecelerateInterpolator());
        indicatorAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setProgress((int)animation.getAnimatedValue());
            }
        });
        indicatorAnim.start();
    }

    public interface OnProgessChange {
        void OnProgessChangeCall(int toPoint);
    }
}















