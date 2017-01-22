package com.android.heaton.funnyvote.ui.search;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.heaton.funnyvote.R;
import com.android.heaton.funnyvote.data.VoteData.VoteDataManager;
import com.android.heaton.funnyvote.data.user.UserManager;
import com.android.heaton.funnyvote.database.DataLoader;
import com.android.heaton.funnyvote.database.User;
import com.android.heaton.funnyvote.database.VoteData;
import com.android.heaton.funnyvote.eventbus.EventBusController;
import com.android.heaton.funnyvote.ui.main.VoteWallItemAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import at.grabner.circleprogress.CircleProgressView;

/**
 * Created by heaton on 2017/1/22.
 */

public class SearchFragment extends Fragment implements SearchItemAdapter.OnReloadClickListener {

    private static final int LIMIT = VoteDataManager.PAGE_COUNT;
    private RecyclerView ryMain;
    private RelativeLayout RootView;
    private SearchItemAdapter adapter;

    private VoteDataManager voteDataManager;
    private UserManager userManager;
    private User user;
    private String keyword = "";

    CircleProgressView circleLoad;

    private List<VoteData> voteDataList;

    private UserManager.GetUserCallback getUserCallback = new UserManager.GetUserCallback() {
        @Override
        public void onResponse(User user) {
            SearchFragment.this.user = user;
            initRecyclerView();
        }

        @Override
        public void onFailure() {
            initRecyclerView();
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        RootView = (RelativeLayout) inflater.inflate(R.layout.fragment_search, container, false);
        ryMain = (RecyclerView) RootView.findViewById(R.id.rySearchResult);
        circleLoad = (CircleProgressView) RootView.findViewById(R.id.circleLoad);
        voteDataManager = VoteDataManager.getInstance(getContext().getApplicationContext());

        if (user == null) {
            userManager = UserManager.getInstance(getContext().getApplicationContext());
            userManager.getUser(getUserCallback);
        } else {
            initRecyclerView();
        }
        return RootView;
    }

    private void initRecyclerView() {
        voteDataList = DataLoader.getInstance(getContext()).querySearchVotes(keyword, 0, LIMIT);
        adapter = new SearchItemAdapter(getContext(), voteDataList);
        adapter.setOnReloadClickListener(this);
        adapter.setNoVoteTag(VoteWallItemAdapter.TAG_NO_VOTE_REFRESH);
        ryMain.setAdapter(adapter);
        if (!TextUtils.isEmpty(keyword)) {
            voteDataManager.getSearchVoteList(keyword, 0, user);
            showLoadingCircle(getString(R.string.vote_detail_circle_loading));
        }
    }

    public void setQueryText(String queryText) {
        this.keyword = queryText;
        if (user != null) {
            showLoadingCircle(getString(R.string.vote_detail_circle_loading));
            voteDataManager.getSearchVoteList(keyword, 0, user);
        }
    }

    private void showLoadingCircle(String content) {
        circleLoad.setVisibility(View.VISIBLE);
        circleLoad.setText(content);
        circleLoad.spin();
    }

    private void hideLoadingCircle() {
        circleLoad.stopSpinning();
        circleLoad.setVisibility(View.GONE);
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

    @Override
    public void onReloadClicked() {
        final int offset = voteDataList.size();
        voteDataManager.getSearchVoteList(keyword, offset, user);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRemoteEvent(EventBusController.RemoteServiceEvent event) {
        if (event.message.equals(EventBusController.RemoteServiceEvent.GET_VOTE_LIST_SEARCH)) {
            refreshData(event.voteDataList, event.offset);
            hideLoadingCircle();
        }
    }

    private void refreshData(List<VoteData> voteDataList, int offset) {
        if (voteDataList == null) {
            voteDataList = new ArrayList<>();

            voteDataList = DataLoader.getInstance(getContext()).querySearchVotes(keyword
                    , offset, LIMIT);
        }
        Log.d("test", "Network Refresh wall item : Search");
        int pageNumber = offset / LIMIT;

        if (offset == 0) {
            this.voteDataList = voteDataList;
            adapter.setVoteList(this.voteDataList);
        } else if (offset >= this.voteDataList.size()) {
            this.voteDataList.addAll(voteDataList);
        }

        if (this.voteDataList.size() < LIMIT * (pageNumber + 1)) {
            adapter.setMaxCount(this.voteDataList.size());
            if (offset != 0) {
                Toast.makeText(getContext(), R.string.Wall_item_toast_no_vote_refresh, Toast.LENGTH_SHORT).show();
            }
        } else {
            adapter.setMaxCount(LIMIT * (pageNumber + 2));
        }
        adapter.notifyDataSetChanged();
    }
}
