package com.badprinter.yobey.fragments;

import android.app.Activity;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.badprinter.yobey.R;
import com.badprinter.yobey.adapter.ArtistAdapter;

import java.lang.ref.PhantomReference;

public class Artist extends Fragment {
    private View root;
    private ListView artistList;
    private ArtistAdapter adapter;

    public Artist() {
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
        root =  inflater.inflate(R.layout.fragment_artist, container, false);
        findViewsById();
        adapter = new ArtistAdapter(getActivity());
        artistList.setAdapter(adapter);

        return root;
    }
    private void findViewsById() {
        artistList = (ListView)root.findViewById(R.id.artistList);
    }



}
