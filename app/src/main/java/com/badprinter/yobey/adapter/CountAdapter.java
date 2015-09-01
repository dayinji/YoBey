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

import java.util.ArrayList;
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
    private LineChartView hoursChart;
    private ViewPager pager;
    private ImageView dayHourBt;
    private String[] days = {"Mon", "Tuse", "Wed", "Thur", "Fri", "Sat", "Sun"};
    private String[] hours = {"00", "01", "02", "03", "04", "05", "06", "07", "08", "09",
            "10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
            "20", "21", "22", "23"};
    DBManager dbMgr;
    private AnimationDrawable dayHourAnim;
    private float[] daysCount = new float[7];
    private float[] hoursCount = new float[24];
    private final String HOURS_CHART = "CountAdapter_hours_chart";
    private final String DAYS_CHART = "CountAdapter_days_chart";
    private String currentChart;
    // An Action for Updating DaysChart
    private Runnable updateDaysAction =  new Runnable() {
        @Override
        public void run() {
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    int[] temp1 = dbMgr.getDaysCount();
                    intToFloat(temp1, daysCount);
                    LineSet set1 = new LineSet(days, daysCount);
                    styleSet(set1);
                    daysChart.addData(set1);
                    daysChart.show(new Animation().setStartPoint(1, .5f));
                }
            }, 500);
        }
    };
    // An Action for Updating HoursChart
    private Runnable updateHoursAction =  new Runnable() {
        @Override
        public void run() {
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    int[] temp1 = dbMgr.getHoursCount();
                    intToFloat(temp1, hoursCount);
                    LineSet set1 = new LineSet(hours, hoursCount);
                    styleSet(set1);
                    hoursChart.addData(set1);
                    hoursChart.show(new Animation().setStartPoint(1, .5f));
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
        pager = (ViewPager)convertView.findViewById(R.id.chartPager);
        dayHourBt = (ImageView)convertView.findViewById(R.id.dayHourBt);
        dayHourBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentChart.equals(HOURS_CHART)) {
                    if (dayHourAnim != null && dayHourAnim.isRunning())
                        dayHourAnim.stop();
                    dayHourBt.setBackgroundResource(R.drawable.hour_to_day);
                    dayHourAnim = (AnimationDrawable) dayHourBt.getBackground();
                    dayHourAnim.setOneShot(true);
                    dayHourAnim.start();
                    pager.setCurrentItem(0, true);
                } else if (currentChart.equals(DAYS_CHART)) {
                    if (dayHourAnim != null && dayHourAnim.isRunning())
                        dayHourAnim.stop();
                    dayHourBt.setBackgroundResource(R.drawable.day_to_hour);
                    dayHourAnim = (AnimationDrawable) dayHourBt.getBackground();
                    dayHourAnim.setOneShot(true);
                    dayHourAnim.start();
                    pager.setCurrentItem(1, true);
                }
            }
        });
        initCharts();
        initCount();
        initPager();


        return convertView;
    }
    // Init Charts
    private void initCharts() {
        daysChart = new LineChartView(context);
        daysChart.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100));
        daysChart.setPadding(10, 10, 10, 10);
        hoursChart = new LineChartView(context);
        hoursChart.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100));
        hoursChart.setPadding(10, 10, 10, 10);

        styleChart(daysChart);
        styleChart(hoursChart);
        currentChart = DAYS_CHART;
    }
    // Init Pager
    private void initPager() {
        List<View> list = new ArrayList<>();
        list.add(daysChart);
        list.add(hoursChart);
        pager.setAdapter(new MyPagerAdapter(list));
        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0)
                    currentChart = DAYS_CHART;
                else
                    currentChart = HOURS_CHART;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        pager.setHorizontalFadingEdgeEnabled(false);

    }
    // Update Current Chart's Count
    public void updateCount() {
        int allPlayCount = dbMgr.getAllPlayCount();
        allPlay.setText(Integer.toString(allPlayCount));
        float allSwitchCount = dbMgr.getAllSwitchCount();
        if (allPlayCount == 0 && allSwitchCount == 0) {
            allSwitch.setText("0.0%");
            return;
        }
        String p = String.format("%.1f", allSwitchCount*100/(allPlayCount+allSwitchCount));
        allSwitch.setText(p + "%");
        hoursChart.dismiss(new Animation().setStartPoint(0f, 0f).setEndAction(updateHoursAction));
        daysChart.dismiss(new Animation().setStartPoint(0f, 0f).setEndAction(updateDaysAction));
    }
    // Init Count
    private void initCount() {
        int allPlayCount = dbMgr.getAllPlayCount();
        allPlay.setText(Integer.toString(allPlayCount));
        float allSwitchCount = dbMgr.getAllSwitchCount();
        String rate = Float.toString(allSwitchCount * 100 / (allPlayCount + allSwitchCount));
        if (allPlayCount == 0 && allSwitchCount == 0) {
            allSwitch.setText("0.0%");
        } else {
            String p = String.format("%.1f", allSwitchCount*100/(allPlayCount+allSwitchCount));
            allSwitch.setText(p + "%");
        }
        int[] temp1 = dbMgr.getDaysCount();
        int[] temp2 = dbMgr.getHoursCount();
        intToFloat(temp1, daysCount);
        intToFloat(temp2, hoursCount);
        LineSet set1 = new LineSet(days, daysCount);
        LineSet set2 = new LineSet(hours, hoursCount);
        styleSet(set1);
        styleSet(set2);
        daysChart.addData(set1);
        daysChart.show();
        hoursChart.addData(set2);
        hoursChart.show();
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
        if (currentChart.equals(DAYS_CHART)) {
            daysChart.dismissAllTooltips();
            tip.prepare(entryRect, daysCount[entryIndex]);
            daysChart.showTooltip(tip, true);
        } else if (currentChart.equals(HOURS_CHART)) {
            hoursChart.dismissAllTooltips();
            tip.prepare(entryRect, hoursCount[entryIndex]);
            hoursChart.showTooltip(tip, true);
        }
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
