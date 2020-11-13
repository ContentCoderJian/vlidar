package com.vlidar.wheelview.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

import com.vlidar.wheelview.common.WheelConstants;

/**
 * 嵌套ScrollView
 */
public class NestedScrollView extends ScrollView {

    public NestedScrollView(Context context) {
        super(context);
        init();
    }

    public NestedScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NestedScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void init() {
        setOnTouchListener((v, event) -> {
            WheelView wv = findViewWithTag(WheelConstants.TAG);
            if (wv != null) {
                wv.getParent().requestDisallowInterceptTouchEvent(false);
            }
            return false;
        });
    }


}
