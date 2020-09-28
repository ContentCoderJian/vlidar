package com.vlidar.mobile.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.vlidar.mobile.utils.LogTrace;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public abstract class BaseActivity extends AppCompatActivity {
    protected LogTrace mLogTrace = new LogTrace("UI", getClass().getSimpleName());
    private Unbinder unbinder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLogTrace.d("onCreate");
        unbinder = ButterKnife.bind(this);
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
    }
}
