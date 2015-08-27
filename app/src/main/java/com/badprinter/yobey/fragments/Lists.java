package com.badprinter.yobey.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.badprinter.yobey.R;
import com.badprinter.yobey.activities.Home;
import com.badprinter.yobey.activities.SongList;
import com.badprinter.yobey.adapter.ListsAdapter;
import com.badprinter.yobey.commom.Constants;
import com.twotoasters.jazzylistview.JazzyListView;
import com.twotoasters.jazzylistview.effects.CardsEffect;
import com.twotoasters.jazzylistview.effects.CurlEffect;
import com.twotoasters.jazzylistview.effects.FanEffect;
import com.twotoasters.jazzylistview.effects.FlipEffect;
import com.twotoasters.jazzylistview.effects.FlyEffect;
import com.twotoasters.jazzylistview.effects.GrowEffect;
import com.twotoasters.jazzylistview.effects.HelixEffect;
import com.twotoasters.jazzylistview.effects.ReverseFlyEffect;
import com.twotoasters.jazzylistview.effects.SlideInEffect;
import com.twotoasters.jazzylistview.effects.StandardEffect;
import com.twotoasters.jazzylistview.effects.TiltEffect;
import com.twotoasters.jazzylistview.effects.TwirlEffect;
import com.twotoasters.jazzylistview.effects.WaveEffect;
import com.twotoasters.jazzylistview.effects.ZipperEffect;

import java.lang.ref.PhantomReference;


public class Lists extends Fragment {
    private String TAG = "Lists";
    private JazzyListView lists;
    private View root;
    private final String[] listNames = {
            Constants.ListName.LIST_ALL,
            Constants.ListName.LIST_FAVORITE,
            Constants.ListName.LIST_RECOMMEND,
            Constants.ListName.LIST_MOST_LISTENED,
            Constants.ListName.LIST_LEAST_LISTENED,
    };
    //private OnFragmentInteractionListener mListener;


    public Lists() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_lists, container, false);
        root = view;
        findViewsById();
        lists.setTransitionEffect(new SlideInEffect());
        lists.setAdapter(new ListsAdapter(getActivity()));
        lists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        newCata();
                        break;
                    default:
                        startList(position);
                }
            }
        });
        return view;
    }
    private void findViewsById() {
        lists = (JazzyListView)root.findViewById(R.id.lists);
    }
    private void newCata() {

    }
    private void startList(int position) {
        Intent intent = new Intent(getActivity(), SongList.class);
        intent.putExtra("cata", listNames[position]);
        startActivity(intent);
    }

   /* @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }*/

    /*public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);
    }*/

}
