package com.badprinter.yobey.adapter;

import android.content.Context;
import android.graphics.Color;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.badprinter.yobey.R;
import com.badprinter.yobey.models.Song;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 15-8-27.
 */
public class ListsAdapter extends BaseAdapter {
    private Context context;
    private String[] catas = {"All Music", "My Favorite", "Recommend", "Most Listened", "Least Listened"};
    private int[] colors;
    private String[] cataWords = {"全", "藏", "佳", "多", "少"};

    public ListsAdapter(Context context) {
        this.context = context;
        colors = new int[5];
        colors[0] = context.getResources().getColor(R.color.fense);
        colors[1] = context.getResources().getColor(R.color.qingse);
        colors[2] = context.getResources().getColor(R.color.lanse);
        colors[3] = context.getResources().getColor(R.color.zise);
        colors[4] = context.getResources().getColor(R.color.huangse);
    }

    @Override
    public int getCount() {
        return catas.length;
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
        Holder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.lists_item, null);
            holder = new Holder();
            holder.cataText = (TextView) convertView.findViewById(R.id.cataText);
            holder.cataImg = (ImageView) convertView.findViewById(R.id.cataImg);

            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }
        TextDrawable drawable = TextDrawable.builder()
                .buildRect(cataWords[position], colors[position]);
        holder.cataImg.setImageDrawable(drawable);
        holder.cataText.setText(catas[position]);
        return convertView;
    }

    private class Holder {
        ImageView cataImg;
        TextView cataText;
    }
}
