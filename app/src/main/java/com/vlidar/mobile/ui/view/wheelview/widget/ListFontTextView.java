package com.vlidar.mobile.ui.view.wheelview.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import com.vlidar.mobile.ui.VlidarApplication;

public class ListFontTextView extends androidx.appcompat.widget.AppCompatTextView {
    private Typeface mTypeface;

    public ListFontTextView(Context context) {
        super(context);
        init(context);
    }

    public ListFontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ListFontTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        if (mTypeface == null) {
            mTypeface = VlidarApplication.PingFangSC_Regular;
        }
        if (mTypeface != null) {
            this.setTypeface(mTypeface);
        }
    }

}
