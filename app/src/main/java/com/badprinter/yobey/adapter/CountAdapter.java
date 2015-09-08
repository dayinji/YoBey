package com.badprinter.yobey.adapter;

import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.badprinter.yobey.R;
import com.badprinter.yobey.commom.AppContext;
import com.badprinter.yobey.db.DBManager;
import com.db.chart.listener.OnEntryClickListener;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.LineChartView;
import com.db.chart.view.Tooltip;
import com.db.chart.view.animation.Animation;
import com.db.chart.view.animation.easing.BounceEase;
import com.db.chart.view.animation.style.DashAnimation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by root on 15-8-29.
 */
public class CountAdapter extends BaseAdapter{
    private final String TAG="CountAdapter";
    private Context context = AppContext.getInstance();
    private TextView allPlay;
    private TextView allSwitch;
    private LineChartView daysChart;
    private LinearLayout chartLayout;
    private String[] days = new String[7];
    DBManager dbMgr;
    private float[] daysCount = new float[7];
    // An Action for Updating DaysChart
    private Runnable updateDaysAction =  new Runnable() {
        @Override
        public void run() {
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    chartLayout.removeView(daysChart);
                    daysChart = new LineChartView(context);
                    initCharts();
                    initCount();
                    chartLayout.addView(daysChart);
                    daysChart.show(new Animation().setStartPoint(1, .5f));
                    /*int[] temp1 = dbMgr.getDaysCount();
                    intToFloat(temp1, daysCount);
                    LineSet set1 = new LineSet(days, daysCount);
                    styleSet(set1);
                    daysChart.addData(set1);
                    daysChart.show(new Animation().setStartPoint(1, .5f));*/
                }
            }, 500);
        }
    };


    public CountAdapter() {
        dbMgr = new DBManager();
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    // Get View
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) { {
            convertView = LayoutInflater.from(context).inflate(R.layout.count_item, null);
        }}
        allPlay = (TextView)convertView.findViewById(R.id.allPlay);
        allSwitch = (TextView)convertView.findViewById(R.id.allSwicth);
        //daysChart = (LineChartView)convertView.findViewById(R.id.chart);
        chartLayout = (LinearLayout)convertView.findViewById(R.id.chartLayout);
        daysChart = new LineChartView(context);

        //Get Dates
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format1 = new SimpleDateFormat("MM-dd");
        for (int i = 0 ; i < 7 ; i++) {
            String formattedDate = format1.format(calendar.getTime());
            days[6-i] = formattedDate;
            calendar.add(Calendar.DATE, -1);
        }

        initCharts();
        initCount();
        chartLayout.addView(daysChart);

        return convertView;
    }
    // Init Charts
    private void initCharts() {
        styleChart(daysChart);
    }

    // Update Current Chart's Count
    public void updateCount() {
        int allPlayCount = dbMgr.getAllPlayCount();
        if (allPlayCount < 1000) {
            allPlay.setText(Integer.toString(allPlayCount));
        } else {
            float largeAllPlayCount = allPlayCount/1000.0f;
            allPlay.setText(String.format("%.1f", largeAllPlayCount) + "k");
        }
        float allSwitchCount = dbMgr.getAllSwitchCount();
        if (allPlayCount == 0 && allSwitchCount == 0) {
            allSwitch.setText("0.0%");
            return;
        }
        String p = String.format("%.1f", allSwitchCount*100 / allPlayCount);
        allSwitch.setText(p + "%");
        daysChart.dismissAllTooltips();
        daysChart.dismiss(new Animation().setStartPoint(0f, 0f).setEndAction(updateDaysAction));
    }
    // Init Count
    private void initCount() {
        int allPlayCount = dbMgr.getAllPlayCount();
        if (allPlayCount < 1000) {
            allPlay.setText(Integer.toString(allPlayCount));
        } else {
            float largeAllPlayCount = allPlayCount/1000.0f;
            allPlay.setText(String.format("%.1f", largeAllPlayCount) + "k");
        }
        float allSwitchCount = dbMgr.getAllSwitchCount();
        String rate = Float.toString(allSwitchCount * 100 / allPlayCount);
        if (allPlayCount == 0 && allSwitchCount == 0) {
            allSwitch.setText("0.0%");
        } else {
            String p = String.format("%.1f", allSwitchCount*100/allPlayCount);
            allSwitch.setText(p + "%");
        }
        int[] temp1 = dbMgr.getDaysCount();
        intToFloat(temp1, daysCount);
        LineSet set1 = new LineSet(days, daysCount);
        styleSet(set1);
        daysChart.addData(set1);
        daysChart.show();
    }
    // Turn Int[] to Float[]
    private void intToFloat(int[] ints, float[] floats) {
        if (ints.length != floats.length)
            return;
        for (int i = 0 ; i < ints.length ; i++) {
            floats[i] = (float)ints[i];
        }
    }
    // Style the Chart
    private void styleChart(LineChartView chart) {
        // Get Density
        float d = context.getResources().getDisplayMetrics().density;
        // Set Height And Width
        int height = (int)(100*d);
        chart.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
        // Set Padding
        chart.setPadding((int)(15*d), (int)(10*d), (int)(5*d), 0);
        chart.setYAxis(false);
        chart.setAxisThickness(1);
        chart.setAxisColor(context.getResources().getColor(R.color.qianhui));
        chart.setYLabels(AxisController.LabelPosition.NONE);
        chart.setLabelsColor(context.getResources().getColor(R.color.qianhui));
        chart.setOnEntryClickListener(new OnEntryClickListener() {
            @Override
            public void onClick(int setIndex, int entryIndex, Rect entryRect) {
                ShowToolTip(setIndex, entryIndex, entryRect);
            }
        });
    }
    // Show ToolTip
    private void ShowToolTip(int setIndex, int entryIndex, Rect entryRect) {
        Tooltip tip = new Tooltip(context, R.layout.msg_tooltip, R.id.msg);
        tip.setEnterAnimation(PropertyValuesHolder.ofFloat(View.ALPHA, 1));
        tip.setExitAnimation(PropertyValuesHolder.ofFloat(View.ALPHA, 0));
        daysChart.dismissAllTooltips();
        tip.prepare(entryRect, daysCount[entryIndex]);
        daysChart.showTooltip(tip, true);

    }
    // Style the Set
    private void styleSet(LineSet set) {
        set.setSmooth(true);
        set.setThickness(4);
        set.setColor(context.getResources().getColor(R.color.qianhui));
        set.setDotsColor(context.getResources().getColor(R.color.qingse));
    }
    private class MyPagerAdapter extends PagerAdapter {
        private List<View> viewList;
        public MyPagerAdapter(List<View> list) {
            viewList = list;
        }
        //viewpager中的组件数量
        @Override
        public int getCount() {
            return viewList.size();
        }
        //滑动切换的时候销毁当前的组件
        @Override
        public void destroyItem(ViewGroup container, int position,
                                Object object) {
            ((ViewPager) container).removeView(viewList.get(position));
        }
        //每次滑动的时候生成的组件
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ((ViewPager) container).addView(viewList.get(position));
            return viewList.get(position);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public int getItemPosition(Object object) {
            return super.getItemPosition(object);
        }
    }
}
