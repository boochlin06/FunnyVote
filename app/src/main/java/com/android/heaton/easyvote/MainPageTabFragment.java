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

public class MainPageTabFragment extends Fragment {
	private RecyclerView mRootView;
    private RecyclerView ryMain;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = (RecyclerView) inflater.inflate(R.layout.fragment_main_page_tab, container, false);
        ryMain = (RecyclerView) mRootView.findViewById(R.id.ryMainPage);
		Log.d("heaton1","MainPageTabFragment oncreateview null:"+(ryMain == null));
        initRecyclerView();
		return mRootView;
	}

	@Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
        Log.d("heaton1","MainPageTabFragment onActivityCreated:");
		initRecyclerView();
	}

	private void initRecyclerView() {
        Log.d("heaton1","MainPageTabFragment initRecyclerView:");
        ryMain.setAdapter(new VoteItemAdapter(getActivity(), VoteDataLoader.getInstance(getContext()).queryHotVotes(10)));
	}

	public static Fragment newInstance() {

        Log.d("heaton1","MainPageTabFragment new main page tab fragment:");
		return new MainPageTabFragment();
	}

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("heaton1","MainPageTabFragment onDestroyView:");
    }
}
