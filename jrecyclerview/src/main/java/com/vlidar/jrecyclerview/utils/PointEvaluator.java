package com.vlidar.jrecyclerview.utils;

import android.animation.TypeEvaluator;
import android.graphics.PointF;

/**
 * 点的估值器
 */
public class PointEvaluator implements TypeEvaluator<PointF> {
    @Override
    public PointF evaluate(float fraction,
                           PointF startValue,
                           PointF endValue) {
        float x = startValue.x + fraction * (endValue.x - startValue.x);
        float y = startValue.y + fraction * (endValue.y - startValue.y);
        return new PointF(x, y);
    }
}
