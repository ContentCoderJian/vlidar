package com.vlidar.mobile.ui.activity;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Message;
import android.widget.TextView;

import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.vlidar.mobile.R;
import com.vlidar.mobile.logic.BLESPPUtils;
import com.vlidar.mobile.utils.AppTools;
import com.vlidar.mobile.utils.StatusBarUtils;

import java.util.List;

import butterknife.BindView;

public class SplashActivity extends BaseActivity {
    private static final int MSG_GOTO_MAIN = 1;
    private static final int DELAY_GOTO = 1500;
    @BindView(R.id.tv_guide_company)
    TextView tvGuideCompany;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StatusBarUtils.transparencyBar(this);
        StatusBarUtils.changeStatusBar(this, true);
        setContentView(R.layout.activity_guide);
        super.onCreate(savedInstanceState);
        tvGuideCompany.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/PingFangSC" +
                "-Thin" +
                ".ttf"));
        BLESPPUtils.getInstance(this);
        if (!XXPermissions.hasPermission(this, Permission.SYSTEM_ALERT_WINDOW)) {
            XXPermissions.with(this)
                    .permission(Permission.SYSTEM_ALERT_WINDOW)
                    .request(new OnPermission() {

                        @Override
                        public void hasPermission(List<String> granted, boolean all) {
                        }

                        @Override
                        public void noPermission(List<String> denied, boolean never) {
                            showToast("请授予悬浮窗权限, 否则当应用处于后台时无法显示弹窗", 5000);
                        }
                    });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        myHandler.removeMessages(MSG_GOTO_MAIN);
    }

    @Override
    protected void onResume() {
        super.onResume();
        myHandler.sendEmptyMessageDelayed(MSG_GOTO_MAIN, DELAY_GOTO);
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
