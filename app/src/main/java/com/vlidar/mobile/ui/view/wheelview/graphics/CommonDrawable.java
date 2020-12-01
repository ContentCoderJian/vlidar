package com.vlidar.mobile.ui.view.wheelview.graphics;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;

import com.vlidar.mobile.ui.view.wheelview.common.WheelConstants;
import com.vlidar.mobile.ui.view.wheelview.widget.WheelView;

/**
 * common滚轮样式
 */
public class CommonDrawable extends WheelDrawable {

    private static final int[] SHADOWS_COLORS =
            {
                    0xFF111111,
                    0x00AAAAAA,
                    0x00AAAAAA
            };  // 阴影色值

    private GradientDrawable mTopShadow =
            new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
            SHADOWS_COLORS);    // 顶部阴影

    private GradientDrawable mBottomShadow =
            new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
            SHADOWS_COLORS);    // 底部阴影

    private Paint mCommonBgPaint, mCommonPaint, mCommonDividerPaint, mCommonBorderPaint;

    private int mWheelSize, mItemH;

    CommonDrawable(int width, int height, WheelView.WheelViewStyle style, int wheelSize,
                   int itemH) {
        super(width, height, style);
        mWheelSize = wheelSize;
        mItemH = itemH;
        init();
    }

    private void init() {
        mCommonBgPaint = new Paint();
        mCommonBgPaint.setColor(mStyle.backgroundColor != -1 ? mStyle.backgroundColor :
                WheelConstants
                .WHEEL_SKIN_COMMON_BG);

        mCommonPaint = new Paint();
        mCommonPaint.setColor(WheelConstants.WHEEL_SKIN_COMMON_COLOR);

        mCommonDividerPaint = new Paint();
        mCommonDividerPaint.setColor(WheelConstants.WHEEL_SKIN_COMMON_DIVIDER_COLOR);
        mCommonDividerPaint.setStrokeWidth(2);

        mCommonBorderPaint = new Paint();
        mCommonBorderPaint.setStrokeWidth(6);
        mCommonBorderPaint.setColor(WheelConstants.WHEEL_SKIN_COMMON_BORDER_COLOR);

    }

    @Override
    public void draw(Canvas canvas) {
        // draw background
        canvas.drawRect(0, 0, mWidth, mHeight, mCommonBgPaint);

        // draw select border
        if (mItemH != 0) {
            int size = mWheelSize >> 1;
            canvas.drawRect(0, mItemH * size, mWidth, mItemH
                    * (size + 1), mCommonPaint);
            canvas.drawLine(0, mItemH * size, mWidth, mItemH
                    * size, mCommonDividerPaint);
            canvas.drawLine(0, mItemH * (size + 1), mWidth, mItemH
                    * (size + 1), mCommonDividerPaint);

            // top, bottom
            mTopShadow.setBounds(0, 0, mWidth, mItemH);
            mTopShadow.draw(canvas);

            mBottomShadow.setBounds(0, mHeight - mItemH, mWidth, mHeight);
            mBottomShadow.draw(canvas);

            // left,right
            canvas.drawLine(0, 0, 0, mHeight, mCommonBorderPaint);
            canvas.drawLine(mWidth, 0, mWidth, mHeight, mCommonBorderPaint);
        }
    }
}
