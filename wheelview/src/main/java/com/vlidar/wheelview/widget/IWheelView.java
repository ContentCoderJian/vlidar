package com.vlidar.wheelview.widget;

import com.vlidar.wheelview.adapter.BaseWheelAdapter;

import java.util.HashMap;
import java.util.List;

/**
 * 滚轮抽象接口
 */
public interface IWheelView<T> {

    /**
     * 滚轮数据是否循环，默认不循环
     */
    boolean LOOP = false;

    /**
     * 滚轮默认数量
     */
    int WHEEL_SIZE = 5;

    /**
     * 滚轮选中刻度是否可点击
     */
    boolean CLICKABLE = false;

    /**
     * 设置滚轮选中刻度是否可点击
     *
     * @param clickable
     */
    void setWheelClickable(boolean clickable);

    /**
     * 设置滚轮是否循环滚动
     *
     * @param loop
     */
    void setLoop(boolean loop);

    /**
     * 设置滚轮个数
     *
     * @param wheelSize
     */
    void setWheelSize(int wheelSize);

    /**
     * 设置滚轮数据
     *
     * @param list
     */
    void setWheelData(List<T> list);

    /**
     * 设置滚轮数据源适配器
     *
     * @param adapter
     */
    void setWheelAdapter(BaseWheelAdapter<T> adapter);

    /**
     * 连接副WheelView
     *
     * @param wheelView
     */
    void join(WheelView wheelView);

    /**
     * 副WheelView数据
     *
     * @param map
     */
    void joinDatas(HashMap<String, List<T>> map);


}
