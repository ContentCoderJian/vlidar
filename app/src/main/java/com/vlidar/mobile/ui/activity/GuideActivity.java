package com.vlidar.mobile.ui.activity;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Message;
import android.widget.TextView;

import com.vlidar.mobile.R;
import com.vlidar.mobile.utils.AppTools;
import com.vlidar.mobile.logic.BLESPPUtils;
import com.vlidar.mobile.utils.StatusBarUtils;

import butterknife.BindView;

public class GuideActivity extends BaseActivity {
    private static final int MSG_GOTO_MAIN = 1;
    private static final int DELAY_2S = 2000;
    @BindView(R.id.tv_guide_company)
    TextView tvGuideCompany;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StatusBarUtils.transparencyBar(this);
        StatusBarUtils.changeStatusBar(this, true);
        setContentView(R.layout.activity_guide);
        super.onCreate(savedInstanceState);
        tvGuideCompany.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/PingFangSC-Thin" +
                ".ttf"));
        BLESPPUtils.getInstance(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        myHandler.removeMessages(MSG_GOTO_MAIN);
    }

    @Override
    protected void onResume() {
        super.onResume();
        myHandler.sendEmptyMessageDelayed(MSG_GOTO_MAIN, DELAY_2S);
    }

    @Override
    protected void onStop() {
        super.onStop();
        myHandler.removeMessages(MSG_GOTO_MAIN);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myHandler.removeMessages(MSG_GOTO_MAIN);
    }

    @Override
    protected void handleMessages(Message msg) {
        if (msg.what == MSG_GOTO_MAIN) {
            AppTools.startForwardActivity(this, MainActivity.class, true);
        }
    }
}
