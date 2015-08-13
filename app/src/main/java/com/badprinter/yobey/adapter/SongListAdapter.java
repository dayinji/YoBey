package com.badprinter.yobey.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import com.badprinter.yobey.R;
import com.badprinter.yobey.models.Song;
import com.badprinter.yobey.utils.MyEvalucator;


/**
 * Created by root on 15-8-12.
 */
public class SongListAdapter extends BaseAdapter {

    private final String TAG = "SongListAdapter";
    private Context context;
    private ArrayList<Song> songList;
    private int current = 0;
    private HashMap<View, Integer> viewsPosition = new HashMap<>();
    private ArrayList<View> views;

    public SongListAdapter(Context context, ArrayList<Song> songList) {
        this.context = context;
        this.songList = songList;
        views = new ArrayList<View>();
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
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.song_list_item, null);
            holder = new ListItemViewHolder();

            holder.songPhoto = (ImageView) convertView.findViewById(R.id.songPhoto);
            holder.songName = (TextView) convertView.findViewById(R.id.songName);
            holder.songArtist = (TextView) convertView.findViewById(R.id.songArtist);
            holder.playingLayout = (FrameLayout) convertView.findViewById(R.id.playingLayout);
            holder.playingPhoto = (ImageView) convertView.findViewById(R.id.playingPhoto);
            holder.playingName = (TextView) convertView.findViewById(R.id.playingName);
            holder.playingArtist = (TextView) convertView.findViewById(R.id.playingArtist);
            holder.normalLayout = (LinearLayout) convertView.findViewById(R.id.normalLayout);
            holder.blackBar = (ImageView) convertView.findViewById(R.id.blackBar);

            convertView.setTag(holder);

            views.add(convertView);

        } else {
            holder = (ListItemViewHolder) convertView.getTag();
        }

        Song temp = songList.get(position);
        holder.songName.setText(temp.getName());
        holder.songArtist.setText(temp.getArtist());
        holder.songPhoto.setImageBitmap(temp.getPhoto());
        holder.playingName.setText(temp.getName());
        holder.playingArtist.setText(temp.getArtist());
        holder.playingPhoto.setImageBitmap(temp.getPhoto());

        if (position == current) {
            LinearLayout normal = (LinearLayout) convertView.findViewById(R.id.normalLayout);
            normal.setAlpha(0);
            FrameLayout playing = (FrameLayout) convertView.findViewById(R.id.playingLayout);
            playing.setAlpha(1);
        } else {
            LinearLayout normal = (LinearLayout) convertView.findViewById(R.id.normalLayout);
            normal.setAlpha(1);
            normal.setRotationY(0f);
            FrameLayout playing = (FrameLayout) convertView.findViewById(R.id.playingLayout);
            playing.setAlpha(0);
        }

        // Save the convertView and Position
        viewsPosition.put(convertView, position);

        return convertView;
    }


    /**
     * For Holder the Views of ListItem
     */
    public class ListItemViewHolder {
        public ImageView songPhoto;    // Album photo
        public TextView songName;        // Song name
        public TextView songArtist;    // Song Artist
        public FrameLayout playingLayout;
        public ImageView playingPhoto;    // Album photo
        public TextView playingName;        // Song name
        public TextView playingArtist;    // Song Artist
        public LinearLayout normalLayout;
        public ImageView blackBar;
        public ValueAnimator normalAnim = null;
        public ValueAnimator playingAnim1 = null;
        public ValueAnimator playingAnim2 = null;

    }

    public void updateItem(int current) {
        if (current != this.current) {
            for (int i = 0; i < views.size(); i++) {
                View temp = views.get(i);
                int position = viewsPosition.get(temp);
                if (position == this.current) {
                    ListItemViewHolder holder = (ListItemViewHolder) temp.getTag();
                    turnToNormal(holder);
                }
                if (position == current) {
                    ListItemViewHolder holder = (ListItemViewHolder) temp.getTag();
                    turnToPlaying(holder);
                }
            }
            this.current = current;
        }
    }

    private void turnToNormal(final ListItemViewHolder holder) {
        clearAnim(holder);
        holder.normalLayout.setRotationY(0f);
        holder.normalAnim = ValueAnimator.ofFloat(0f, 1f);
        holder.normalAnim.setDuration(500);
        holder.normalAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                holder.normalLayout.setAlpha((float) animation.getAnimatedValue());
                holder.playingLayout.setAlpha(1 - (float) animation.getAnimatedValue());
            }
        });
        holder.normalAnim.start();
    }

    private void turnToPlaying(final ListItemViewHolder holder) {
        Log.d(TAG, "y = " + holder.normalLayout.getRotationY());
        holder.playingLayout.setRotationY(-90f);
        clearAnim(holder);
        holder.playingAnim1 = ValueAnimator.ofFloat(0f, 1f);
        holder.playingAnim1.setDuration(350);
        holder.playingAnim1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                holder.normalLayout.setRotationY(90 * (float) animation.getAnimatedValue());
            }
        });
        holder.playingAnim1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                holder.playingLayout.setAlpha(1);
            }
        });
        holder.playingAnim2 = ValueAnimator.ofFloat(1f, 0f);
        holder.playingAnim2.setDuration(2000);
        MyEvalucator.JellyFloatAnim jelly = new MyEvalucator.JellyFloatAnim();
        jelly.setDuration(2000);
        jelly.setFirstTime(180);
        holder.playingAnim2.setEvaluator(jelly);
        holder.playingAnim2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                holder.playingLayout.setRotationY(-90 * (float) animation.getAnimatedValue());
            }
        });
        holder.playingAnim2.setStartDelay(350);
        holder.playingAnim2.start();
        holder.playingAnim1.start();

        //Init BlackBar Xpos
        holder.blackBar.setX(0);
    }

    private void clearAnim(final ListItemViewHolder holder) {
        if (holder.normalAnim != null && holder.normalAnim.isRunning())
            holder.normalAnim.end();
        if (holder.playingAnim1 != null && holder.playingAnim1.isRunning())
            holder.playingAnim1.end();
        if (holder.playingAnim2 != null && holder.playingAnim2.isRunning())
            holder.playingAnim2.end();
    }

    public void updateBar(int progress) {
        for (int i = 0; i < views.size(); i++) {
            View temp = views.get(i);
            int position = viewsPosition.get(temp);
            if (position == this.current) {
                ListItemViewHolder holder = (ListItemViewHolder) temp.getTag();
                float x = holder.blackBar.getMeasuredWidth() * progress / songList.get(current).getDuration();
                holder.blackBar.setX(x);
            }
        }
    }
}
