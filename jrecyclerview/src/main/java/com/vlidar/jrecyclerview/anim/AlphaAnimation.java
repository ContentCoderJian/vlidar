package com.vlidar.jrecyclerview.anim;

import android.animation.ObjectAnimator;
import android.view.View;

/**
 * 透明度动画
 */

public class AlphaAnimation extends IBaseAnimation {

    @Override
    protected void init(View view) {
        addAnimTogether(ObjectAnimator.ofFloat(view, "alpha", 0f, 1f));
    }

}
