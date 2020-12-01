package com.vlidar.mobile.ui.view.wheelview.graphics;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import com.vlidar.mobile.ui.view.wheelview.common.WheelConstants;
import com.vlidar.mobile.ui.view.wheelview.widget.WheelView;

/**
 * 滚轮样式
 */
public class WheelDrawable extends Drawable {

    int mWidth;   // 控件宽

    int mHeight;  // 控件高

    WheelView.WheelViewStyle mStyle;

    private Paint mBgPaint;

    WheelDrawable(int width, int height, WheelView.WheelViewStyle style) {
        mWidth = width;
        mHeight = height;
        mStyle = style;
        init();
    }

    private void init() {
        mBgPaint = new Paint();
        mBgPaint.setColor(mStyle.backgroundColor != -1 ? mStyle.backgroundColor :
                WheelConstants.WHEEL_BG);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawRect(0, 0, mWidth, mHeight, mBgPaint);
    }

    @Override
    public void setAlpha(int alpha) {
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
    }

    @Override
    public int getOpacity() {
        return PixelFormat.UNKNOWN;
    }
}
