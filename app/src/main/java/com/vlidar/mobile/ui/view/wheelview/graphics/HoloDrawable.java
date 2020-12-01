package com.vlidar.mobile.ui.view.wheelview.graphics;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.vlidar.mobile.ui.view.wheelview.common.WheelConstants;
import com.vlidar.mobile.ui.view.wheelview.widget.WheelView;

/**
 * holo滚轮样式
 */
public class HoloDrawable extends WheelDrawable {

    private Paint mHoloBgPaint, mHoloPaint;

    private int mWheelSize, mItemH;

    HoloDrawable(int width, int height, WheelView.WheelViewStyle style, int wheelSize, int itemH) {
        super(width, height, style);
        mWheelSize = wheelSize;
        mItemH = itemH;
        init();
    }

    private void init() {
        mHoloBgPaint = new Paint();
        mHoloBgPaint.setColor(mStyle.backgroundColor != -1 ? mStyle.backgroundColor : WheelConstants
                .WHEEL_SKIN_HOLO_BG);

        mHoloPaint = new Paint();
        mHoloPaint.setStrokeWidth(mStyle.holoBorderWidth != -1 ? mStyle.holoBorderWidth : 3);
        mHoloPaint.setColor(mStyle.holoBorderColor != -1 ? mStyle.holoBorderColor : WheelConstants
                .WHEEL_SKIN_HOLO_BORDER_COLOR);
    }


    @Override
    public void draw(Canvas canvas) {
        // draw background
        canvas.drawRect(0, 0, mWidth, mHeight, mHoloBgPaint);

        // draw select border
        if (mItemH != 0) {
            int size = mWheelSize >> 1;
            canvas.drawLine(0, mItemH * size, mWidth, mItemH
                    * size, mHoloPaint);
            canvas.drawLine(0, mItemH * (size + 1), mWidth, mItemH
                    * (size + 1), mHoloPaint);
        }
    }
}
