package com.badprinter.yobey.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import com.badprinter.yobey.R;
import com.badprinter.yobey.models.Song;


/**
 * Created by root on 15-8-12.
 */
public class SongListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Song> songList;

    public SongListAdapter(Context context, ArrayList<Song> songList) {
        this.context = context;
        this.songList = songList;
    }

    @Override
    public int getCount() {
        return songList.size();
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
        ListItemViewHolder holder;
        if(convertView == null)
        {
            holder = new ListItemViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.song_list_item, null);
            holder.songPhoto = (ImageView) convertView.findViewById(R.id.songPhoto);
            holder.songName = (TextView) convertView.findViewById(R.id.songName);
            holder.songArtist = (TextView) convertView.findViewById(R.id.songArtist);
            convertView.setTag(holder);			//表示给View添加一个格外的数据，
        } else {
            holder = (ListItemViewHolder)convertView.getTag();//通过getTag的方法将数据取出来
        }

        Song temp = songList.get(position);
        holder.songName.setText(temp.getName());
        holder.songArtist.setText(temp.getArtist());

        //_stackBlurManager = new StackBlurManager(getBitmapFromAsset(this, "android_platform_256.png"));
        holder.songPhoto.setImageBitmap(temp.getPhoto());

        return convertView;
    }


    /**
     * For Holder the Views of ListItem
     */
    public class ListItemViewHolder {
        public ImageView songPhoto;	// Album photo
        public TextView songName;		// Song name
        public TextView songArtist;	// Song Artist
    }
}
