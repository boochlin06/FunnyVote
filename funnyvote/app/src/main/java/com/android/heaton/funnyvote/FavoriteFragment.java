package com.android.heaton.funnyvote;

import android.os.Bundle;
import android.support.annotation.Nullable;
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

import com.android.heaton.funnyvote.database.DataLoader;
import com.android.heaton.funnyvote.database.VoteData;
import com.android.heaton.funnyvote.eventbus.EventBusController;
import com.android.heaton.funnyvote.ui.HidingScrollListener;
import com.android.heaton.funnyvote.ui.main.MainPageTabFragment;
import com.android.heaton.funnyvote.ui.main.VoteWallItemAdapter;
import com.getbase.floatingactionbutton.FloatingActionButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;

/**
 * Created by heaton on 16/3/30.
 */
public class FavoriteFragment extends android.support.v4.app.Fragment {

    public RecyclerView ryMain;
    private RelativeLayout RootView;
    private List<VoteData> voteDataList;
    private VoteWallItemAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RootView = (RelativeLayout) inflater.inflate(R.layout.fragment_main_page_tab, container, false);
        ryMain = (RecyclerView) RootView.findViewById(R.id.ryMainPage);
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
        voteDataList = DataLoader.getInstance(getContext()).queryFavoriteVotes(50);
        adapter = new VoteWallItemAdapter(getActivity()
                , voteDataList);
        ScaleInAnimationAdapter scaleInAnimationAdapter = new ScaleInAnimationAdapter(adapter);
        scaleInAnimationAdapter.setDuration(1000);
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
            for (int i = 0; i < voteDataList.size(); i++) {
                if (data.getVoteCode().equals(voteDataList.get(i).getVoteCode())) {
                    voteDataList.set(i, data);
                    adapter.notifyItemChanged(i);
                    break;
                }
            }
        } if(event.message.equals(EventBusController.VoteDataControlEvent.VOTE_SYNC_WALL_FOR_FAVORITE)) {
            refreshData();
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
        voteDataList = DataLoader.getInstance(getContext()).queryFavoriteVotes(50);
        adapter.setVoteList(voteDataList);
        adapter.notifyDataSetChanged();
    }
}
