package com.badprinter.yobey.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badprinter.yobey.R;
import com.badprinter.yobey.activities.SongList;
import com.badprinter.yobey.commom.AppContext;
import com.badprinter.yobey.commom.Constants;
import com.badprinter.yobey.db.DBManager;
import com.badprinter.yobey.models.Song;
import com.badprinter.yobey.utils.MyEvalucatorUtil;
import com.badprinter.yobey.utils.SongProvider;
import com.indris.material.RippleView;

import org.w3c.dom.Text;


/**
 * Created by root on 15-8-12.
 */
public class SongListAdapter extends BaseAdapter {

    private final String TAG = "SongListAdapter";
    private List<Song> songList;
    private int current = 0;
    private long currentSongId = 0;
    private Drawable greyDrawable;
    private Drawable qingseDrawable;
    private Context context = AppContext.getInstance();
    private DBManager dbMgr;
    private String listName;

    public SongListAdapter(List<Song> songList, Long currentSongId, String listName) {
        this.songList = songList;
        this.currentSongId = currentSongId;
        this.dbMgr = new DBManager();
        this.listName = listName;
        this.greyDrawable = context.getResources().getDrawable(R.drawable.like_grey);
        this.qingseDrawable = context.getResources().getDrawable(R.drawable.like_qingse);
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ListItemViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.song_list_item, null);
            holder = new ListItemViewHolder();

            //.songPhoto = (ImageView) convertView.findViewById(R.id.songPhoto);
            holder.songName = (TextView) convertView.findViewById(R.id.songName);
            holder.songArtist = (TextView) convertView.findViewById(R.id.songArtist);
            holder.songCata = (ImageView)convertView.findViewById(R.id.songCata);
            holder.ripple = (RippleView)convertView.findViewById(R.id.rippleView);

            convertView.setTag(holder);

        } else {
            holder = (ListItemViewHolder) convertView.getTag();
        }

        Log.e(TAG, "bug position = " + position + ", songlist'size = " + songList.size());
        final Song temp = songList.get(position);
        Log.e(TAG, "bug temp == null : " + (temp == null));
        Log.e(TAG, "bug songName = " + temp.getName());
        holder.songName.setText(temp.getName());
        holder.songArtist.setText(temp.getArtist());
        if (dbMgr.isFavorite(temp)) {
            holder.songCata.setImageDrawable(qingseDrawable);
        } else {
            holder.songCata.setImageDrawable(greyDrawable);
        }
        holder.songCata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dbMgr.isFavorite(temp)) {
                    dbMgr.deleteFromFavorite(temp);
                    clearAnim(holder);
                    startAnim(holder, greyDrawable);
                } else {
                    dbMgr.addToFavorite(temp);
                    clearAnim(holder);
                    startAnim(holder, qingseDrawable);
                }
            }
        });
        holder.ripple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPlayMusic(position);
            }
        });

        if (songList.get(position).getId() == currentSongId) {
            // do something
        } else {
            // do something
        }

        return convertView;
    }

    public char getLetterByPosition(int position) {
        char[] pinyin = songList.get(position).getPinyin().toCharArray();
        if (pinyin[0] <= 'Z' &&  pinyin[0] >= 'A')
            return pinyin[0];
        else
            return '#';
    }
    public int getPositionByLetter(char letter) {

        int index = findByBinarySearch(0, songList.size() - 1, letter);
        // When Find Nothing
        if (index == -1 && letter != 'A') {
            return getPositionByLetter((char)(letter-1));
        }
        else  if (index == -1 && letter == 'A') {
            return getPositionByLetter('#');
        }
        // When found
        for (int i = index ; i > 0 ; i --) {
            char[] pinyin = songList.get(i-1).getPinyin().toCharArray();
            if (pinyin[0] != letter) {
                return i;
            }
            if (i == 1) {
                return 0;
            }
        }
        return 0;
    }
    private int findByBinarySearch(int start, int end, char letter) {
        if (letter == '#')
            return 0;
        if (end < start)
            return -1;
        int middle = (end + start)/2;
        char[] pinyin = songList.get(middle).getPinyin().toCharArray();
        if (pinyin[0] > letter) {
            return findByBinarySearch(start, middle - 1, letter);
        } else if (pinyin[0] < letter) {
            return findByBinarySearch(middle + 1, end, letter);
        } else {
            return middle;
        }
    }


    /**
     * For Holder the Views of ListItem
     */
    public class ListItemViewHolder {
        public TextView songName;
        public TextView songArtist;
        public ImageView songCata;
        private RippleView ripple;
        public ValueAnimator zoomOutAnim;
        public ValueAnimator zoomInAnim;
    }

    public void updateItem(Long currentSongId) {
        long last = this.currentSongId;
        this.currentSongId = currentSongId;
        if (currentSongId != last) {

        }
    }


    private void clearAnim(ListItemViewHolder holder) {
        if (holder.zoomOutAnim != null && holder.zoomOutAnim.isRunning())
            holder.zoomOutAnim.end();
        if (holder.zoomInAnim != null && holder.zoomInAnim.isRunning())
            holder.zoomInAnim.end();
    }

    private void startAnim(final ListItemViewHolder holder, final Drawable drawable) {
        holder.zoomOutAnim = ValueAnimator.ofFloat(1, 0).setDuration(100);
        holder.zoomOutAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float f = (float)animation.getAnimatedValue();
                holder.songCata.setScaleX(f);
                holder.songCata.setScaleY(f);
            }
        });
        holder.zoomInAnim = ValueAnimator.ofFloat(0, 1).setDuration(2000);
        holder.zoomInAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float f = (float) animation.getAnimatedValue();
                holder.songCata.setScaleX(f);
                holder.songCata.setScaleY(f);
            }
        });
        MyEvalucatorUtil.JellyFloatAnim jelly = new MyEvalucatorUtil.JellyFloatAnim();
        jelly.setDuration(2000);
        jelly.setFirstTime(100);
        jelly.setAmp(0.03);
        holder.zoomInAnim.setEvaluator(jelly);
        holder.zoomOutAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                holder.songCata.setImageDrawable(drawable);
                holder.zoomInAnim.start();
            }
        });
        holder.zoomOutAnim.start();
    }

    private void startPlayMusic(final int position) {
        Handler handlerTimer = new Handler();
        handlerTimer.postDelayed(new Runnable() {
            public void run() {
                // Intent for Changing SongList
                Intent changeListIntent = new Intent("com.badprinter.yobey.service.PLAYER_SERVICE");
                changeListIntent.putExtra("controlMsg", Constants.PlayerControl.CHANGE_LIST);
                changeListIntent.putExtra("listName", listName);
                context.startService(changeListIntent);
                // Intent for Playing Music
                Intent intent = new Intent("com.badprinter.yobey.service.PLAYER_SERVICE");
                intent.putExtra("controlMsg", Constants.PlayerControl.PLAYING_MSG);
                intent.putExtra("current", position);
                intent.putExtra("currenTime", 0);
                context.startService(intent);
            }
        }, 500);
    }


}
