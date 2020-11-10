package com.vlidar.jrecyclerview.anim;

import android.animation.ObjectAnimator;
import android.view.View;

/**
 从下往上动画
 */

public class SlideInBottomAnimation extends IBaseAnimation {
    @Override
    protected void init(View view) {
        addAnimTogether(ObjectAnimator.ofFloat(view, "translationY",
                view.getRootView().getHeight() >> 2, 0));
        addAnimTogether(ObjectAnimator.ofFloat(view, "alpha", 0.5f, 1f));
    }
}

