package com.badprinter.yobey.customviews;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.badprinter.yobey.R;

/**
 * Created by root on 15-8-11.
 */
public class AdjustBar extends View {
    private final String TAG = "AdjustBar";

    private Paint paint;
    private int max;
    private int progress;
    private int backgroundColor;
    private int havePlayedColor;
    private float barHeight;
    private float barWidth;
    //private int indicatorColor;
    private int indicatorId;
    //private float indicatorRadis;
    private float barRadius;
    private float indicatorSize = 2.0f;
    private float indicatorBitmapSize = 1.8f;

    private ValueAnimator indicatorAnim;
   // public OnProgressChange onProgressChange;

    public AdjustBar(Context context) {
        this(context, null);
    }

    public AdjustBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AdjustBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        paint = new Paint();

        TypedArray mTypedArray = context.obtainStyledAttributes(attrs,
                R.styleable.AdujustBar);

        //获取自定义属性和默认值
        max = mTypedArray.getInt(R.styleable.AdujustBar_adjustMax, 100);
        progress = mTypedArray.getInt(R.styleable.AdujustBar_adjustProgress, 50);
        backgroundColor = mTypedArray.getColor(R.styleable.AdujustBar_adjustBackgroundColor, 0);
        havePlayedColor = mTypedArray.getColor(R.styleable.AdujustBar_adjustHavePlayedColor, 0);
        barHeight = mTypedArray.getDimension(R.styleable.AdujustBar_adjustBarHeight, 0);
        //if(barHeight < 2*dif + 1) barHeight = 2*dif + 1;
        //indicatorRadis = mTypedArray.getDimension(R.styleable.MusicBar_indicatorRadius, 0);
        indicatorId = mTypedArray.getResourceId(R.styleable.AdujustBar_adjustIndicatorId, 0);
        barRadius = mTypedArray.getDimension(R.styleable.AdujustBar_adjustBarRadius, 0);

        mTypedArray.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        barWidth = getWidth();
        float top = barHeight*(indicatorSize-1)/2;
        float bottom = top + barHeight;
        float left = barHeight*indicatorSize/2;
        float right = barWidth - left;
        float width = barWidth - 2*left;
        /**
         * background, Top = 10 For Train Indicator
         */
        RectF rect = new RectF(left, top, right, bottom);
        paint.setColor(backgroundColor);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);  //消除锯齿
        //canvas.drawRect(rect, paint);
        canvas.drawRoundRect(rect, barRadius, barRadius, paint);

        /**
         * havePlayed
         */
        paint.setColor(havePlayedColor);
        RectF rect1 = new RectF(left, top, width*((float)progress/max)+left, bottom);
        //canvas.drawRect(rect1, paint);
        canvas.drawRoundRect(rect1, barRadius, barRadius, paint);

        /**
         * Indicator Bg
         */
        paint.setColor(havePlayedColor);
        float r1 = barHeight*indicatorSize/2;
        RectF rect2 = new RectF(width * ((float) progress / max) + left - r1,
                0, width*((float)progress/max) + left + r1, 2*r1);
        //canvas.drawRect(rect1, paint);
        canvas.drawRoundRect(rect2, barRadius, barRadius, paint);

        /**
         * indicator bitmap
         */
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), indicatorId);
        float scale = barHeight*indicatorBitmapSize / bmp.getHeight();
        float r2 = barHeight*indicatorBitmapSize/2;
        //float trainLong = bmp.getWidth()*scale;
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        Bitmap bmp1 = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
        if (!bmp.isRecycled())
            bmp.recycle();

        canvas.drawBitmap(bmp1, width * ((float) progress / max) +left - r2, 0, null);
    }

    /*@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), 48);
    }*/

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

    public int getIndicatorId() { return indicatorId; }
    public void setIndicatorId(int id) { indicatorId = id; }

    public float getbarHeight() { return barHeight; }
    public void setbarHeight(float height) { barHeight = height; }

    public float getBarRadius() { return barRadius; }
    public void setBarRadius(float radius) { barRadius = radius; }


    @Override
    public boolean onTouchEvent(MotionEvent me) {
        if (me.getAction() == MotionEvent.ACTION_MOVE) {
            float x = me.getX();
            int toPoint = (int)((x/getWidth())*max);
            setProgress(toPoint);
            //onProgressChange.onProgressChangeCall(toPoint);
        }
        else if (me.getAction() == MotionEvent.ACTION_DOWN) {
            float x = me.getX();
            int toPoint = (int)((x/getWidth())*max);
            startIndicatorAnim(toPoint);
            //onProgressChange.onProgressChangeCall(toPoint);
        }
        return true;
    }

    private void startIndicatorAnim(int endValue) {
        if (indicatorAnim != null)
            indicatorAnim.cancel();
        indicatorAnim = ValueAnimator.ofInt(progress, endValue);
        float duration = 300;
        indicatorAnim.setDuration((int)duration);
        indicatorAnim.setInterpolator(new DecelerateInterpolator(1f));
        indicatorAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int point = (int)animation.getAnimatedValue();
                setProgress(point);
               // onProgressChange.onProgressAnimCall(point);
            }
        });
        indicatorAnim.start();
    }

 /*   public interface OnProgressChange {
        void onProgressChangeCall(int toPoint);
        void onProgressAnimCall(int point);
    }*/
}

