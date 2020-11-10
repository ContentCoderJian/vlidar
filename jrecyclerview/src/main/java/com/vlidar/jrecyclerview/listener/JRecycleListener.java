package com.vlidar.jrecyclerview.listener;

import android.view.MotionEvent;

/**
 * JRecycleListener
 */
public interface JRecycleListener {

    boolean onTouch(MotionEvent event, float deltaY);

    boolean onUp(MotionEvent event);

}
