package com.vlidar.jrecyclerview.anim;

import android.animation.ObjectAnimator;
import android.view.View;

/**
 * 从左进入
 */

public class SlideInLeftAnimation extends IBaseAnimation {
    @Override
    protected void init(View view) {
        addAnimTogether(ObjectAnimator.ofFloat(view, "translationX",
                -view.getRootView().getWidth(), 0));
    }
}
