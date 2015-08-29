package com.badprinter.yobey.adapter;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.badprinter.yobey.R;
import com.badprinter.yobey.db.DBManager;
import com.db.chart.listener.OnEntryClickListener;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.LineChartView;
import com.db.chart.view.Tooltip;
import com.db.chart.view.animation.Animation;
import com.db.chart.view.animation.easing.BounceEase;
import com.db.chart.view.animation.style.DashAnimation;

/**
 * Created by root on 15-8-29.
 */
public class CountAdapter extends BaseAdapter{
    private final String TAG="CountAdapter";
    private Context context;
    private TextView allPlay;
    private TextView allSwitch;
    private LineChartView daysChart;
    private LineChartView hoursChart;
    String[] days = {"Mon", "Tuse", "Wed", "Thur", "Fri", "Sat", "Sun"};
    String[] hours = {"00", "01", "02", "03", "04", "05", "06", "07", "08", "09",
            "10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
            "20", "21", "22", "23"};
    DBManager dbMgr;
    private float[] daysCount = new float[7];
    private float[] hoursCount = new float[24];


    public CountAdapter(Context context) {
        this.context = context;
        dbMgr = new DBManager(context);
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) { {
            convertView = LayoutInflater.from(context).inflate(R.layout.count_item, null);
        }}
        allPlay = (TextView)convertView.findViewById(R.id.allPlay);
        allSwitch = (TextView)convertView.findViewById(R.id.allSwicth);
        daysChart = (LineChartView)convertView.findViewById(R.id.dayschart);
        hoursChart = (LineChartView)convertView.findViewById(R.id.hourschart);

        Log.e(TAG, "f=getView ");
        styleChart(daysChart);
        styleChart(hoursChart);
        initCount();

        return convertView;
    }
    public void updateCount() {
        Runnable action1 =  new Runnable() {
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
        Runnable action2 =  new Runnable() {
            @Override
            public void run() {
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        int[] temp2 = dbMgr.getHoursCount();
                        intToFloat(temp2, hoursCount);
                        LineSet set2 = new LineSet(hours, hoursCount);
                        styleSet(set2);
                        hoursChart.addData(set2);
                        hoursChart.show(new Animation().setStartPoint(1, .5f));
                    }
                }, 500);
            }
        };
        // Clear data
        daysChart.dismiss(new Animation().setStartPoint(0f, 0f).setEndAction(action1));
        hoursChart.dismiss(new Animation().setStartPoint(0f, 0f).setEndAction(action2));
    }
    private void initCount() {
        int[] temp1 = dbMgr.getDaysCount();
        int[] temp2 = dbMgr.getHoursCount();
        intToFloat(temp1, daysCount);
        intToFloat(temp2, hoursCount);
        LineSet set1 = new LineSet(days, daysCount);
        LineSet set2 = new LineSet(hours, hoursCount);
        styleSet(set1);
        styleSet(set2);
        daysChart.addData(set1);
        hoursChart.addData(set2);
        daysChart.show();
        hoursChart.show();
    }
    private void intToFloat(int[] ints, float[] floats) {
        if (ints.length != floats.length)
            return;
        for (int i = 0 ; i < ints.length ; i++) {
            floats[i] = (float)ints[i];
        }
    }
    private void styleChart(LineChartView chart) {
        chart.setYAxis(false);
        chart.setAxisThickness(1);
        chart.setAxisColor(context.getResources().getColor(R.color.qianhui));
        chart.setYLabels(AxisController.LabelPosition.NONE);
        chart.setLabelsColor(context.getResources().getColor(R.color.qianhui));
        chart.setOnEntryClickListener(new OnEntryClickListener() {
            @Override
            public void onClick(int setIndex, int entryIndex, Rect entryRect) {
                //Do things
            }
        });
        chart.setOnEntryClickListener(new OnEntryClickListener() {
            @Override
            public void onClick(int i, int i1, Rect rect) {
                //Tooltip tip = new Tooltip(context);
            }
        });
    }
    private void styleSet(LineSet set) {
        set.setSmooth(true);
        set.setThickness(4);
        set.setColor(context.getResources().getColor(R.color.qianhui));
        set.setDotsColor(context.getResources().getColor(R.color.qingse));
    }
}
