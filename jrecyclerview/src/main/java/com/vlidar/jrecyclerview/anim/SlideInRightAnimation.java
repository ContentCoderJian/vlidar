package com.vlidar.jrecyclerview.anim;

import android.animation.ObjectAnimator;
import android.view.View;

/**
 * 从右进
 */

public class SlideInRightAnimation extends IBaseAnimation {
    @Override
    protected void init(View view) {
        addAnimTogether(ObjectAnimator.ofFloat(view, "translationX",
                view.getRootView().getWidth(), 0));
    }
}
