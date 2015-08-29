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
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.badprinter.yobey.R;
import com.badprinter.yobey.utils.MyEvalucatorUtil;

/**
 * Created by root on 15-8-28.
 */
public class DragView extends View {
    private final String TAG = "DragView";
    private Paint paint;
    private int delayTime;
    private int borderRadius;
    private int background;
    private int color;
    private int start = 0;
    private int end = 0;
    private ValueAnimator anim0;
    private ValueAnimator anim1;
    int index = 0;
    boolean isDrag = false;
    float dragX = 0;

    public DragView(Context context) {
        this(context, null);
    }

    public DragView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        paint = new Paint();

        TypedArray mTypedArray = context.obtainStyledAttributes(attrs,
                R.styleable.DragView);

        //获取自定义属性和默认值
        delayTime = mTypedArray.getInt(R.styleable.DragView_delayTime, 200);
        borderRadius = mTypedArray.getInt(R.styleable.DragView_borderRadius, 20);
        background = mTypedArray.getColor(R.styleable.DragView_myBackgroundColor, R.color.qianhui);
        color = mTypedArray.getInt(R.styleable.DragView_dragColor, R.color.qingse);

        mTypedArray.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        /**
         * background, Top = 10 For Train Indicator
         */
        RectF rect = new RectF(0, 0, width, height);
        paint.setColor(background);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);  //消除锯齿
        canvas.drawRect(rect, paint);


        /**
         * havePlayed
         */
        paint.setColor(color);
        RectF rect1 = new RectF(start, 0, end, height);
        canvas.drawRoundRect(rect1, borderRadius, borderRadius, paint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec),MeasureSpec.getSize(heightMeasureSpec));
    }

    public void setAnimation(int i) {
        if (anim0 != null && anim0.isRunning())
            anim0.cancel();
        final int goalStart = i*getWidth()/4;
        final int originalStart = start;
        index = i;
        anim0 = ValueAnimator.ofFloat(0f, 1f);
        anim0.setDuration(2000);
        MyEvalucatorUtil.JellyFloatAnim jelly = new MyEvalucatorUtil.JellyFloatAnim();
        jelly.setDuration(2000);
        jelly.setFirstTime(150);
        jelly.setFreq(1.2);
        jelly.setAmp(0.025);
        jelly.setDecay(3.0);
        anim0.setEvaluator(jelly);
        anim0.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float f = (float)animation.getAnimatedValue();
                start = (int) ((goalStart - originalStart) * f + originalStart);
                end = start + getWidth() / 4;
                invalidate();
            }
        });
        anim0.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent me) {
        if (me.getAction() == MotionEvent.ACTION_MOVE) {
            float x = me.getX();
            if (isDrag) {
                start = getWidth()*index/4 + (int)(x - dragX)*2/3;
                end = start + getWidth()/4;
                invalidate();
            }
        } else if (me.getAction() == MotionEvent.ACTION_DOWN) {
            float x = me.getX();
            if (x >= getWidth()*index/4 && x < getWidth()*(index+1)/4 && isDrag == false) {
                if (anim0 != null && anim0.isRunning())
                    anim0.cancel();
                isDrag = true;
                dragX = x;
            }
        } else if (me.getAction() == MotionEvent.ACTION_UP) {
            isDrag = false;
            setAnimation(index);
        }
        return true;
    }
}
