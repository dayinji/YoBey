package com.badprinter.yobey.customviews;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Created by root on 15-8-16.
 */
public class MyScrollView extends ScrollView {

    private String TAG = "MyScrollView";
    ValueAnimator anim = null;
    private long lastMoveTime;
    private int scrollTime = 1500;
    /*
     * The sleepTime is the time between user stop scrolling and the view scroll to current lyric
     * Notice: When user scrolls the view, the view cannot scroll to current lyric
     */
    private int sleepTime = 2000;

    public MyScrollView(Context context) {
        this(context, null);
    }

    public MyScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void slowScrollTo(final int x,final int y) {
        if (System.currentTimeMillis() - lastMoveTime <= sleepTime)
            return;
        if (anim != null && anim.isRunning()) {
            anim.end();
        }
        anim = ValueAnimator.ofFloat(0f, 1f);
        anim.setDuration(scrollTime);
        anim.setInterpolator(new DecelerateInterpolator(4f));
        final int xBefore = this.getScrollX();
        final int yBefore = this.getScrollY();

        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                MyScrollView.this.scrollTo(
                        (int)(xBefore+(float)animation.getAnimatedValue()*(x-xBefore)),
                        (int)(yBefore+(float)animation.getAnimatedValue()*(y-yBefore))
                );
            }
        });
        anim.start();
    }
    public void reset() {
        lastMoveTime = 0;
    }

    @Override
    public boolean onTouchEvent(MotionEvent me) {
        /*
         * THe statement ---- "super.onTouchEvent(me)";
         * Call onTouchEvent() of ScrollView with param [me]
         * After calling onTouchEvent() of ScrollView, execute the statements below of "super.onTouchEvent(me);"
         * Without calling "super.onTouchEvent(me);"
         * When you scroll the view, it never scroll!
         */
        super.onTouchEvent(me);
        if (me.getAction() == MotionEvent.ACTION_MOVE) {
            lastMoveTime = System.currentTimeMillis();
        }
        return true;
    }

    public int getSleepTime() {
        return sleepTime;
    }
    public void setSleepTime(int sleepTime) {
        this.sleepTime = sleepTime;
    }
    public int getScrollTime() {
        return scrollTime;
    }
    public void setScrollTime(int scrollTime) {
        this.scrollTime = scrollTime;
    }

}
