/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.android.heaton.funnyvote.ui.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.android.heaton.funnyvote.R;
import com.android.heaton.funnyvote.database.VoteDataLoader;
import com.getbase.floatingactionbutton.FloatingActionButton;

import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;

public class MainPageTabFragment extends Fragment {
    public RecyclerView ryMain;
    private RelativeLayout RootView;
    private FloatingActionButton fabTop;

    public static Fragment newInstance() {

        return new MainPageTabFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RootView = (RelativeLayout) inflater.inflate(R.layout.fragment_main_page_tab, container, false);
        ryMain = (RecyclerView) RootView.findViewById(R.id.ryMainPage);
        fabTop = (FloatingActionButton) RootView.findViewById(R.id.fabTop);
        fabTop.setVisibility(View.GONE);
        fabTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ryMain.scrollToPosition(0);
            }
        });
        initRecyclerView();
        return RootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initRecyclerView();
    }

    private void initRecyclerView() {
        ScaleInAnimationAdapter adapter = new ScaleInAnimationAdapter(new VoteWallItemAdapter(getActivity()
                , VoteDataLoader.getInstance(getContext()).queryHotVotes(30)));
        adapter.setDuration(500);
        ryMain.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

}
