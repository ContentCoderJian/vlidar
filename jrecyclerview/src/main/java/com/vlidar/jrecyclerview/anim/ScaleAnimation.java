package com.vlidar.jrecyclerview.anim;

import android.animation.ObjectAnimator;
import android.view.View;

/**
 * 缩放动画
 */

public class ScaleAnimation extends IBaseAnimation {

    @Override
    protected void init(View view) {
        addAnimTogether(ObjectAnimator.ofFloat(view, "scaleX", 0.5f, 1f));
        addAnimTogether(ObjectAnimator.ofFloat(view, "scaleY", 0.5f, 1f));
    }

}
