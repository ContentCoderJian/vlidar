package com.vlidar.mobile.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.vlidar.mobile.utils.AppTools;
import com.vlidar.mobile.utils.LogTrace;

import java.lang.ref.WeakReference;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public abstract class BaseActivity extends AppCompatActivity {
    protected LogTrace mLogTrace = new LogTrace("UI", getClass().getSimpleName());
    protected MyHandler myHandler;
    private Unbinder unbinder;

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

    protected class MyHandler extends Handler {
        private final WeakReference<BaseActivity> mActivty;

        public MyHandler(BaseActivity activity) {
            mActivty = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            mLogTrace.d("handlerMessage", "msg.what: " + msg.what);
            handleMessages(msg);
        }
    }

    protected abstract void handleMessages(Message msg);

}
