package com.vlidar.mobile.ui.view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vlidar.mobile.R;

public class CommonButton extends RelativeLayout {
    private static final String TAG = "CommonButton";
    private Context mContext;
    private RelativeLayout mRootLayout;

    private RelativeLayout rlBg;
    private ImageView ivPic;
    private TextView tvTitle;
    private TextView tvValue;
    private TextView tvValue1;

    private AlphaAnimation mAlphaAnimation;
    private RotateAnimation mRotateAnimation;

    public CommonButton(Context context) {
        this(context, null);
    }

    public CommonButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CommonButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        LayoutInflater mLayoutInflater = LayoutInflater.from(context);
        mRootLayout = (RelativeLayout) mLayoutInflater.inflate(R.layout.common_button, null);
        addView(mRootLayout);
        initViews();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.d(TAG, "onAttachedToWindow");
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.d(TAG, "onDetachedFromWindow");
    }

    private void initViews() {
        rlBg = mRootLayout.findViewById(R.id.rl_bg);
        ivPic = mRootLayout.findViewById(R.id.iv_pic);
        tvTitle = mRootLayout.findViewById(R.id.tv_title);
        tvValue = mRootLayout.findViewById(R.id.tv_value);
        tvValue1 = mRootLayout.findViewById(R.id.tv_value1);
        tvTitle.setTypeface(Typeface.createFromAsset(mContext.getAssets(), "fonts/PingFangSC" +
                "-Regular" +
                ".ttf"));
        tvValue.setTypeface(Typeface.createFromAsset(mContext.getAssets(), "fonts/PingFangSC" +
                "-Regular" +
                ".ttf"));
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        tvTitle.setEnabled(enabled);
        tvValue.setEnabled(enabled);
        ivPic.setEnabled(enabled);
    }

    public void setBackgroundRes(int resId) {
        rlBg.setBackgroundResource(resId);
    }

    public void subViewCallOnClick() {
        tvTitle.callOnClick();
        tvValue.callOnClick();
        ivPic.callOnClick();
    }

    public void startTwinkleAnim() {
        startAlphaAnimation(ivPic);
    }

    public void stopTwinkleAnim() {
        stopAlphaAnimation(ivPic);
    }

    public void startRotateAnim() {
        startRotateAnimation(ivPic);
    }

    public void stopRotateAnim() {
        stopRotateAnimation(ivPic);
    }

    private void startRotateAnimation(ImageView imageView) {
        if (mRotateAnimation == null) {
            mRotateAnimation = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            mRotateAnimation.setDuration(1000);
            mRotateAnimation.setInterpolator(new LinearInterpolator());
//            mRotateAnimation.setFillAfter(true);
//            mRotateAnimation.setFillBefore(true);
            mRotateAnimation.setRepeatCount(AlphaAnimation.INFINITE);
        }
        imageView.startAnimation(mRotateAnimation);
    }

    private void stopRotateAnimation(ImageView imageView) {
        //清除动画
        imageView.clearAnimation();
        if (mRotateAnimation != null) {
            mRotateAnimation.cancel();
            mRotateAnimation = null;
        }
    }

    private void startAlphaAnimation(ImageView imageView) {
        if (mAlphaAnimation == null) {
            mAlphaAnimation = new AlphaAnimation(1F, 0F);
            mAlphaAnimation.setDuration(1200);
            mAlphaAnimation.setInterpolator(new LinearInterpolator());
//            mAlphaAnimation.setFillAfter(true);
//            mAlphaAnimation.setFillBefore(true);
//            mAlphaAnimation.setRepeatMode(AlphaAnimation.RESTART);
            mAlphaAnimation.setRepeatCount(AlphaAnimation.INFINITE);
        }
        imageView.startAnimation(mAlphaAnimation);
    }

    private void stopAlphaAnimation(ImageView imageView) {
        //清除动画
        imageView.clearAnimation();
        if (mAlphaAnimation != null) {
            mAlphaAnimation.cancel();
            mAlphaAnimation = null;
        }
    }

    public void setPicSrcRes(int resId) {
        ivPic.setImageResource(resId);
    }

    public void setPicBgRes(int resId) {
        ivPic.setBackgroundResource(resId);
    }

    public void setTitleRes(int resId) {
        tvTitle.setText(resId);
    }

    public void setTitleColorRes(int color) {
        tvTitle.setTextColor(color);
    }

    public void setValueText(String value) {
        tvValue.setText(value);
    }

    public void setValue1Text(String value) {
        tvValue1.setText(value);
    }
}
