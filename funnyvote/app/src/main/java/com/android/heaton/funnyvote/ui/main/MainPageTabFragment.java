/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.android.heaton.funnyvote.ui.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;

import com.android.heaton.funnyvote.R;
import com.android.heaton.funnyvote.database.DataLoader;
import com.android.heaton.funnyvote.database.VoteData;
import com.android.heaton.funnyvote.eventbus.EventBusController;
import com.android.heaton.funnyvote.ui.HidingScrollListener;
import com.android.heaton.funnyvote.ui.UserSharepreferenceController;
import com.getbase.floatingactionbutton.FloatingActionButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;

public class MainPageTabFragment extends Fragment {
    public static final String TAB_HOT = "HOT";
    public static final String TAB_NEW = "NEW";

    public static final String TAB_CREATE = "CREATE";
    public static final String TAB_PARTICIPATE = "PARTICIPATE";

    public RecyclerView ryMain;
    private RelativeLayout RootView;
    private String tab = TAB_HOT;
    private List<VoteData> voteDataList;
    private VoteWallItemAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton fabTop;

    public static MainPageTabFragment newInstance(String tab) {
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
        swipeRefreshLayout = (SwipeRefreshLayout) RootView.findViewById(R.id.swipeLayout);
        swipeRefreshLayout.setOnRefreshListener(new WallItemOnRefreshListener());
        initRecyclerView();
        return RootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        LinearLayoutManager manager = (LinearLayoutManager) ryMain.getLayoutManager();
        int position = manager.findFirstVisibleItemPosition();
        if (position == 0) {
            refreshData();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initRecyclerView();
    }

    private void initRecyclerView() {
        if (tab.equals(TAB_HOT)) {
            voteDataList = DataLoader.getInstance(getContext()).queryHotVotes(50);
            adapter = new VoteWallItemAdapter(getActivity()
                    , voteDataList);
        } else if (tab.equals(TAB_NEW)) {
            voteDataList = DataLoader.getInstance(getContext()).queryNewVotes(50);
            adapter = new VoteWallItemAdapter(getActivity()
                    , voteDataList);
        } else if (tab.equals(TAB_CREATE)) {
            voteDataList = DataLoader.getInstance(getContext()).queryVoteDataByAuthor(
                    UserSharepreferenceController.getUser(getContext()).getUserCode(), 50);
            adapter = new VoteWallItemAdapter(getActivity()
                    , voteDataList);
        } else if (tab.equals(TAB_PARTICIPATE)) {
            voteDataList = DataLoader.getInstance(getContext()).queryVoteDataByAuthor(
                    UserSharepreferenceController.getUser(getContext()).getUserCode(), 50);
            adapter = new VoteWallItemAdapter(getActivity()
                    , voteDataList);
        }
        ScaleInAnimationAdapter scaleInAnimationAdapter = new ScaleInAnimationAdapter(adapter);
        scaleInAnimationAdapter.setDuration(1000);
        ryMain.setAdapter(adapter);
        ryMain.addOnScrollListener(new HidingScrollListener() {
            @Override
            public void onHide() {
                fabTop.animate().translationY(
                        fabTop.getHeight() + 50)
                        .setInterpolator(new AccelerateInterpolator(2));
            }

            @Override
            public void onShow() {
                this.resetScrollDistance();
                fabTop.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
            }
        });
        fabTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayoutManager manager = (LinearLayoutManager) ryMain.getLayoutManager();
                int position = manager.findFirstVisibleItemPosition();
                if (position > 5) {
                    ryMain.scrollToPosition(5);
                }
                ryMain.smoothScrollToPosition(0);
                ryMain.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        EventBus.getDefault().post(new EventBusController.UIControlEvent(
                                EventBusController.UIControlEvent.SCROLL_TO_TOP));
                    }
                }, 200);
            }
        });
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
            for (int i = 0; i < voteDataList.size(); i++) {
                if (data.getVoteCode().equals(voteDataList.get(i).getVoteCode())) {
                    voteDataList.set(i, data);
                    adapter.notifyItemChanged(i);
                    break;
                }
            }
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private class WallItemOnRefreshListener implements SwipeRefreshLayout.OnRefreshListener {

        @Override
        public void onRefresh() {
            refreshData();
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void refreshData() {
        if (tab.equals(TAB_HOT)) {
            voteDataList = DataLoader.getInstance(getContext()).queryHotVotes(50);
        } else if (tab.equals(TAB_NEW)) {
            voteDataList = DataLoader.getInstance(getContext()).queryNewVotes(50);
        } else if (tab.equals(TAB_CREATE)) {
            voteDataList = DataLoader.getInstance(getContext()).queryVoteDataByAuthor(
                    UserSharepreferenceController.getUser(getContext()).getUserCode(), 50);
        } else if (tab.equals(TAB_PARTICIPATE)) {
            voteDataList = DataLoader.getInstance(getContext()).queryVoteDataByAuthor(
                    UserSharepreferenceController.getUser(getContext()).getUserCode(), 50);
        }
        Log.d("test", "Refresh wall item :" + tab);
        adapter.setVoteList(voteDataList);
        adapter.notifyDataSetChanged();
    }
}
