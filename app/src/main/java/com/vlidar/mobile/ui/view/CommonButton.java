package com.vlidar.mobile.ui.view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
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
