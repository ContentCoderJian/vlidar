package com.vlidar.mobile.ui.adapter;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kyleduo.switchbutton.SwitchButton;
import com.vlidar.jrecyclerview.config.JRecycleConfig;
import com.vlidar.jrecyclerview.swipe.JSwipeViewHolder;
import com.vlidar.mobile.R;
import com.vlidar.mobile.ui.entity.ScanTimer;
import com.vlidar.mobile.utils.LogTrace;

import java.util.List;

public class TimerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private LogTrace mLogTrace = new LogTrace("UI", "TimerAdapter");

    private final List<ScanTimer> mData;
    private final LayoutInflater mLayoutInflater;
    private final Activity mActivity;

    private OnDeleteListener onDeleteListener;

    public TimerAdapter(Activity activity, List<ScanTimer> data) {
        mData = data;
        mLayoutInflater = LayoutInflater.from(activity);
        mActivity = activity;
    }

    public void setOnDeleteListener(OnDeleteListener onDeleteListener) {
        this.onDeleteListener = onDeleteListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TimerHolder(mLayoutInflater.inflate(JRecycleConfig.SWIPE_LAYOUT,
                parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder,
                                 int position) {
        ScanTimer scanTimer = mData.get(position);
        TimerHolder viewHolder = (TimerHolder) holder;
        StringBuilder timeBuilder = new StringBuilder();
        if (scanTimer.getBeginHour() < 10) {
            timeBuilder.append("0");
        }
        timeBuilder.append(scanTimer.getBeginHour()).append(":");
        if (scanTimer.getBeginMinute() < 10) {
            timeBuilder.append("0");
        }
        timeBuilder.append(scanTimer.getBeginMinute());
        viewHolder.tvTime.setText(timeBuilder.toString());
        viewHolder.rlTimerBg.setOnClickListener(v -> {
            mLogTrace.d("onBindViewHolder, rlTimerBg-->onClick", "position=" + position);
            viewHolder.switchButton.toggle();
        });
        viewHolder.switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mLogTrace.d("onBindViewHolder, switchButton-->onCheckedChanged",
                        "position=" + position + ", isChecked=" + isChecked);
            }
        });
        viewHolder.ibSwipeDelete.setOnClickListener(v -> {
            if (onDeleteListener != null) {
                onDeleteListener.onDeleteClick(position);
            }
            viewHolder.getSwipeItemLayout().close();
        });
    }

    @Override
    public int getItemCount() {
        if (mData == null) {
            return 0;
        }
        return mData.size();
    }

    class TimerHolder extends JSwipeViewHolder {
        private RelativeLayout rlTimerBg;
        private TextView tvTime;
        private SwitchButton switchButton;
        private ImageButton ibSwipeDelete;

        TimerHolder(View itemView) {
            super(itemView);
        }

        @Override
        public int getRightMenuLayout() {
            return R.layout.swipe_only_right;
        }

        @Override
        public int getContentLayout() {
            return R.layout.item_timer;
        }

        @Override
        public void initItem(FrameLayout frameLayout) {
            rlTimerBg = frameLayout.findViewById(R.id.rl_timer_background);
            tvTime = frameLayout.findViewById(R.id.tv_time);
            tvTime.setTypeface(Typeface.createFromAsset(mActivity.getAssets(), "fonts" +
                    "/PingFangSC" +
                    "-Medium.ttf"));
            switchButton = frameLayout.findViewById(R.id.switch_button);
            ibSwipeDelete = frameLayout.findViewById(R.id.ib_swipe_delete);
        }
    }

    public interface OnDeleteListener {
        void onDeleteClick(int position);
    }
}
