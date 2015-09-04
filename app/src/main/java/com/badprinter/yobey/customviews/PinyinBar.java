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
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.badprinter.yobey.R;

/**
 * Created by root on 15-8-11.
 */
public class PinyinBar extends View {
    private final String TAG = "PinyinBar";

    private Paint paint;
    private int backgroundColor;
    private int selectedColor;
    private float height;
    private float fontSize;
    private float barRadius = 5;

    private ValueAnimator indicatorAnim;
   // public OnProgressChange onProgressChange;

    public PinyinBar(Context context) {
        this(context, null);
    }

    public PinyinBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PinyinBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        paint = new Paint();

        TypedArray mTypedArray = context.obtainStyledAttributes(attrs,
                R.styleable.PinyinBar);

        //获取自定义属性和默认值
        backgroundColor = mTypedArray.getColor(R.styleable.PinyinBar_pinyinBackgroundColor, 0);
        selectedColor = mTypedArray.getColor(R.styleable.PinyinBar_pinyinselectedColor, 0);

        mTypedArray.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        String[] letters = {"#", "A", "B", "C", "D", "E", "F", "G"};
        height = getHeight();
        fontSize = height/27*0.8f;

        /**
         * background, Top = 10 For Train Indicator
         */
        RectF rect = new RectF(0, 0, fontSize/0.8f, height);
        paint.setColor(backgroundColor);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);  //消除锯齿
        //canvas.drawRect(rect, paint);
        canvas.drawRoundRect(rect, barRadius, barRadius, paint);


        paint.setColor(getResources().getColor(R.color.baise));
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);  //消除锯齿
        for (int i = 0 ; i < 8 ; i++) {
            //canvas.drawRect(rect, paint);
            canvas.drawText(letters[i], 0, fontSize*i, paint);
            canvas.drawRoundRect(rect, barRadius, barRadius, paint);
        }
        /**
         * havePlayed
         */
       /* paint.setColor(havePlayedColor);
        RectF rect1 = new RectF(left, top, width*((float)progress/max)+left, bottom);
        //canvas.drawRect(rect1, paint);
        canvas.drawRoundRect(rect1, barRadius, barRadius, paint);*/

        /**
         * Indicator Bg
         */
        /*paint.setColor(havePlayedColor);
        float r1 = barHeight*indicatorSize/2;
        RectF rect2 = new RectF(width * ((float) progress / max) + left - r1,
                0, width*((float)progress/max) + left + r1, 2*r1);
        //canvas.drawRect(rect1, paint);
        canvas.drawRoundRect(rect2, barRadius, barRadius, paint);*/

        /**
         * indicator bitmap
         */
        /*Bitmap bmp = BitmapFactory.decodeResource(getResources(), indicatorId);
        float scale = barHeight*indicatorBitmapSize / bmp.getHeight();
        float r2 = barHeight*indicatorBitmapSize/2;
        //float trainLong = bmp.getWidth()*scale;
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        Bitmap bmp1 = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
        if (!bmp.isRecycled())
            bmp.recycle();

        canvas.drawBitmap(bmp1, width * ((float) progress / max) +left - r2, 0, null);*/
    }

    /*@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), 48);
    }*/

    /*@Override
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
    }*/

 /*   public interface OnProgressChange {
        void onProgressChangeCall(int toPoint);
        void onProgressAnimCall(int point);
    }*/
}

