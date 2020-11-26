package com.vlidar.mobile.ui.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.hjq.xtoast.OnToastListener;
import com.hjq.xtoast.XToast;
import com.vlidar.mobile.R;
import com.vlidar.mobile.ui.VlidarApplication;
import com.vlidar.mobile.utils.AppTools;
import com.vlidar.mobile.utils.LogTrace;
import com.vlidar.mobile.utils.ScreenUtils;

import java.lang.ref.WeakReference;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public abstract class BaseActivity extends AppCompatActivity {
    protected LogTrace mLogTrace = new LogTrace("UI", getClass().getSimpleName());
    protected MyHandler myHandler;
    private Unbinder unbinder;

    protected static final int DEFAULT_TOAST_DURATION = 5000;
    protected XToast xToast;

    private String toastMessage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLogTrace.d("onCreate");
        AppTools.addActivity(this);
        unbinder = ButterKnife.bind(this);
        myHandler = new MyHandler(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLogTrace.d("onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLogTrace.d("onResume");
    }

    @Override
    protected void onStop() {
        super.onStop();
        mLogTrace.d("onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLogTrace.d("onDestroy");
        unbinder.unbind();
        AppTools.removeActivity(this);
    }

    @SuppressLint("HandlerLeak")
    protected class MyHandler extends Handler {
        private final WeakReference<BaseActivity> mActivity;

        public MyHandler(BaseActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            mLogTrace.d("handlerMessage", "msg.what: " + msg.what);
            handleMessages(msg);
        }
    }

    protected void requestPermission() {
        XXPermissions.with(this)
                .permission(Permission.SYSTEM_ALERT_WINDOW)
                .request(new OnPermission() {

                    @Override
                    public void hasPermission(List<String> granted, boolean all) {
                        // 传入 Application 表示这个是一个全局的 Toast
                    }

                    @Override
                    public void noPermission(List<String> denied, boolean never) {
                        showToast("请授予悬浮窗权限", 2000);
                    }
                });
    }

    protected void showToast(String text, int duration) {
        if (xToast != null && xToast.isShow()) {
            xToast.setText(text).setDuration(duration).update();
        } else {
            if (XXPermissions.hasPermission(this, Permission.SYSTEM_ALERT_WINDOW)) {
                xToast = new XToast(getApplication());
            } else {
                xToast = new XToast(this);
            }
            xToast.setView(R.layout.toast_customer)
                    .setDraggable()
                    .setDuration(duration)
                    .setAnimStyle(android.R.style.Animation_Dialog)
                    .setGravity(Gravity.BOTTOM)
                    .setYOffset(ScreenUtils.dip2px(this, 140))
                    .setOutsideTouchable(true)
                    .setText(text)
                    .setOnTouchListener((toast, view1, event) -> {
                        toast.cancel();
                        return false;
                    })
                    .setOnToastListener(new OnToastListener() {
                        @Override
                        public void onShow(XToast toast) {

                        }

                        @Override
                        public void onDismiss(XToast toast) {
                            xToast = null;
                        }
                    })
                    .show();
        }
        this.toastMessage = text;
    }

    protected boolean isShowNoTFToast() {
        return xToast != null && xToast.isShow() && TextUtils.equals("未插卡，请插入TF卡", toastMessage);
    }

    protected abstract void handleMessages(Message msg);

}
