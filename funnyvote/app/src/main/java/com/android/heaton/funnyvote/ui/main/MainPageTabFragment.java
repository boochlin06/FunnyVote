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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.android.heaton.funnyvote.R;
import com.android.heaton.funnyvote.database.DataLoader;
import com.android.heaton.funnyvote.database.VoteData;
import com.android.heaton.funnyvote.eventbus.EventBusController;
import com.getbase.floatingactionbutton.FloatingActionButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;

public class MainPageTabFragment extends Fragment {
    public static final String TAB_HOT = "HOT";
    public static final String TAB_NEW = "NEW";
    public RecyclerView ryMain;
    private RelativeLayout RootView;
    private FloatingActionButton fabTop;
    private String tab = TAB_HOT;
    private List<VoteData> voteDataList;
    private ScaleInAnimationAdapter adapter;

    public static Fragment newInstance(String tab) {
        return new MainPageTabFragment(tab);
    }
    public MainPageTabFragment(String tab) {
        this.tab = tab;
    }
    public MainPageTabFragment() {
        this.tab = TAB_HOT;
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
        if (tab.equals(TAB_HOT)) {
            Log.d("test","wall item : hot");
            voteDataList = DataLoader.getInstance(getContext()).queryHotVotes(50);
            adapter = new ScaleInAnimationAdapter(new VoteWallItemAdapter(getActivity()
                    , voteDataList));
        } else {
            Log.d("test","wall item : new");
            voteDataList = DataLoader.getInstance(getContext()).queryNewVotes(50);
            adapter = new ScaleInAnimationAdapter(new VoteWallItemAdapter(getActivity()
                    , voteDataList));
        }
        adapter.setDuration(800);
        ryMain.setAdapter(adapter);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onVoteControl(EventBusController.VoteDataControlEvent event) {
        if (event.message.equals(EventBusController.VoteDataControlEvent.VOTE_SYNC_WALL_AND_CONTENT)) {
            VoteData data = event.data;
            for (int i = 0; i < voteDataList.size() ; i++) {
                if (data.getVoteCode().equals(voteDataList.get(i).getVoteCode())) {
                    voteDataList.set(i,data);
                    adapter.notifyItemChanged(i);
                    Log.d("test","sync i:"+i);
                    break;
                }
            }
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

}
