package com.badprinter.yobey.customviews;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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
public class PinyinBar extends View {
    private final String TAG = "PinyinBar";

    private Paint paint;
    private int backgroundColor;
    private int selectedColor;
    private float height;
    private float width;
    private float cellHeight;
    private float fontSize;
    private float barRadius = 5;
    private int current = -1;
    private float selectorY = 0;
    private float selectorX = 99999;
    private boolean isTouch = false;
    private int defaultBackgroundColor = getResources().getColor(R.color.qianbai);

    public PinyinBarCallBack callback;

    private ValueAnimator seletorAnim;
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
        String[] letters = {"#", "A", "B", "C", "D", "E", "F", "G",
                "H", "I", "J", "K", "L", "M", "N", "O",
                "P", "Q", "R", "S", "T", "U", "V", "W",
                "X", "Y", "Z"};
        height = getHeight();
        width = getWidth();
        cellHeight = height/27;
        fontSize = height/27*0.6f;
        float yOffset = (height/27 - fontSize)/2;

        /**
         * background
         */
        RectF rect = new RectF(0, 0, width, height);

        paint.setColor(backgroundColor);

        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);  //消除锯齿
        //canvas.drawRect(rect, paint);
        canvas.drawRect(rect, paint);


        /*
         * Selected
         */
        RectF rect1 = new RectF(selectorX, selectorY, width, selectorY + cellHeight);
        paint.setColor(selectedColor);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);  //消除锯齿
        //canvas.drawRect(rect, paint);
        canvas.drawRect(rect1, paint);

        /*
         * Letters
         */
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(fontSize);
        paint.setAntiAlias(true);  //消除锯齿
        for (int i = 0 ; i < letters.length ; i++) {
            if (i != current)
                paint.setColor(getResources().getColor(R.color.qianhui));
            else
                paint.setColor(getResources().getColor(R.color.baise));
            canvas.drawText(letters[i], (width-fontSize)/2, fontSize*(i+1)/0.6f - yOffset, paint);
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent me) {
        if (me.getAction() == MotionEvent.ACTION_MOVE) {
            if (seletorAnim != null && seletorAnim.isRunning())
                seletorAnim.cancel();
            float y = me.getY();
            setSelectorY(y);
        }
        else if (me.getAction() == MotionEvent.ACTION_DOWN) {
            isTouch = true;
            selectorX = 0;
            float y = me.getY();
            setSelectorY(y);
        } else if (me.getAction() == MotionEvent.ACTION_UP) {
            isTouch = false;
            float y = (int)(me.getY()/cellHeight)*cellHeight;
            setSelectorY(y);
            startSelectorAnim();
        }
        return true;
    }

    private void startSelectorAnim() {
        if (seletorAnim != null)
            seletorAnim.cancel();
        seletorAnim = ValueAnimator.ofFloat(0, 1);
        seletorAnim.setDuration(300);
        seletorAnim.setInterpolator(new DecelerateInterpolator(1f));
        seletorAnim.setStartDelay(200);
        seletorAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (!isTouch)
                    selectorX = animation.getAnimatedFraction()*width;
                else
                    selectorX = 0;
                invalidate();
            }
        });
        current = -1;
        seletorAnim.start();
    }

    private void setSelectorY(float y) {
        if (y > height)
            y = height-cellHeight;
        else if (y < 0)
            y = 0;
        selectorY = y;
        Log.e(TAG, "selectorY/cellHeight = " + selectorY/cellHeight );
        int c =Math.round(selectorY / cellHeight);
        c = c >= 27 ? 26 : c;
        if (c != current) {
            this.current = c;
           // Log.e(TAG, "selectorY = " + selectorY + " ; CURRENT = " + c);
            callback.onBarChange(current);
        }
        invalidate();
    }

    public interface PinyinBarCallBack {
        void onBarChange(int current);
    }
}

