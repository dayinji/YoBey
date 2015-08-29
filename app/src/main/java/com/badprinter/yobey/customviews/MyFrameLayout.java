package com.badprinter.yobey.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.badprinter.yobey.R;

/**
 * Created by root on 15-8-29.
 */
public class MyFrameLayout extends FrameLayout {
    private final String TAG="MyFrameLayout";
    public MyFrameLayout(Context context) {
        this(context, null);
    }

    public MyFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // Pass the ev to DragView
        this.getChildAt(0).onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }
}
