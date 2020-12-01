package com.vlidar.mobile.ui.view.wheelview.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vlidar.mobile.R;
import com.vlidar.mobile.ui.view.wheelview.common.WheelConstants;
import com.vlidar.mobile.ui.view.wheelview.widget.ListFontTextView;

/**
 * 滚轮数组适配器
 */
public class ArrayWheelAdapter<T> extends BaseWheelAdapter<T> {

    private Context mContext;

    public ArrayWheelAdapter(Context context) {
        mContext = context;
    }

    @Override
    public View bindView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
//            convertView = new WheelItem(mContext);
            convertView = LayoutInflater.from(mContext).inflate(R.layout.wheel_item, parent,
                    false);
            holder = new ViewHolder();
            holder.tvValue = convertView.findViewById(R.id.tv_value);
            holder.tvValue.setTag(WheelConstants.WHEEL_ITEM_TEXT_TAG);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
//        WheelItem wheelItem = (WheelItem) convertView;
        T item = getItem(position);
        if (item != null) {
//            if (wheelItem instanceof CharSequence) {
//                wheelItem.setText((CharSequence) item);
//            } else {
//                wheelItem.setText(item.toString());
//            }
            holder.tvValue.setText(item.toString());
        }
        return convertView;
    }

    static class ViewHolder {
        ListFontTextView tvValue;
    }

}
