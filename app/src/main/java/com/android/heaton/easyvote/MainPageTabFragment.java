/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.android.heaton.easyvote;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;

public class MainPageTabFragment extends Fragment {
    private RecyclerView mRootView;
    private RecyclerView ryMain;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = (RecyclerView) inflater.inflate(R.layout.fragment_main_page_tab, container, false);
        ryMain = (RecyclerView) mRootView.findViewById(R.id.ryMainPage);
        initRecyclerView();
        return mRootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initRecyclerView();
    }

    private void initRecyclerView() {


        ScaleInAnimationAdapter adapter = new ScaleInAnimationAdapter(new VoteItemAdapter(getActivity()
                , VoteDataLoader.getInstance(getContext()).queryHotVotes(30)));
        adapter.setDuration(200);
        adapter.setFirstOnly(false);
        ryMain.setAdapter(adapter);
        // ryMain.setAdapter(new VoteItemAdapter(getActivity(), VoteDataLoader.getInstance(getContext()).queryHotVotes(30)));
    }

    public static Fragment newInstance() {

        return new MainPageTabFragment();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("heaton1", "MainPageTabFragment onDestroyView:");
    }

}
