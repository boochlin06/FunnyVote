package com.heaton.funnyvote.ui.search;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.heaton.funnyvote.FunnyVoteApplication;
import com.heaton.funnyvote.R;
import com.heaton.funnyvote.analytics.AnalyzticsTag;
import com.heaton.funnyvote.data.VoteData.VoteDataManager;
import com.heaton.funnyvote.data.user.UserManager;
import com.heaton.funnyvote.database.DataLoader;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.database.VoteData;
import com.heaton.funnyvote.eventbus.EventBusManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import at.grabner.circleprogress.CircleProgressView;
import at.grabner.circleprogress.TextMode;

/**
 * Created by heaton on 2017/1/22.
 */

public class SearchFragment extends Fragment implements SearchItemAdapter.OnReloadClickListener {

    private static final String TAG = SearchFragment.class.getSimpleName();
    private static final int LIMIT = VoteDataManager.PAGE_COUNT;
    public static final String KEY_SEARCH_KEYWORD = "key_search_keyword";
    private RecyclerView ryMain;
    private RelativeLayout RootView;
    private SearchItemAdapter adapter;

    private VoteDataManager voteDataManager;
    private UserManager userManager;
    private User user;
    private String keyword = "";
    private Tracker tracker;

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

        FunnyVoteApplication application = (FunnyVoteApplication) getActivity().getApplication();
        tracker = application.getDefaultTracker();
        RootView = (RelativeLayout) inflater.inflate(R.layout.fragment_search, container, false);
        ryMain = (RecyclerView) RootView.findViewById(R.id.rySearchResult);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(ryMain.getContext(),
                DividerItemDecoration.VERTICAL);
        ryMain.addItemDecoration(dividerItemDecoration);
        circleLoad = (CircleProgressView) RootView.findViewById(R.id.circleLoad);
        circleLoad.setTextMode(TextMode.TEXT);
        circleLoad.setShowTextWhileSpinning(true);
        circleLoad.setFillCircleColor(getResources().getColor(R.color.md_amber_50));
        return RootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        hideLoadingCircle();
        voteDataManager = VoteDataManager.getInstance(getContext().getApplicationContext());

        if (user == null) {
            userManager = UserManager.getInstance(getContext().getApplicationContext());
            userManager.getUser(getUserCallback, false);
        } else {
            initRecyclerView();
        }
        if (getArguments().getString(KEY_SEARCH_KEYWORD) != null) {
            setQueryText(getArguments().getString(KEY_SEARCH_KEYWORD));
        }
    }

    private void initRecyclerView() {
        voteDataList = DataLoader.getInstance(getContext()).querySearchVotes(keyword, 0, LIMIT);
        adapter = new SearchItemAdapter(getContext(), voteDataList);
        adapter.setOnReloadClickListener(this);
        ryMain.setAdapter(adapter);
        if (!TextUtils.isEmpty(keyword)) {
            showLoadingCircle(this.getString(R.string.vote_detail_circle_loading));
            voteDataManager.getSearchVoteList(keyword, 0, user);
        }
    }

    private void setQueryText(String queryText) {
        this.keyword = queryText;
        if (user != null) {
            this.showLoadingCircle(getString(R.string.vote_detail_circle_loading));
            voteDataManager.getSearchVoteList(keyword, 0, user);
            tracker.send(new HitBuilders.EventBuilder()
                    .setCategory(AnalyzticsTag.CATEGORY_SEARCH_VOTE)
                    .setAction(AnalyzticsTag.ACTION_SEARCH_VOTE)
                    .setLabel(keyword).build());
        }
    }

    private void showLoadingCircle(String content) {
        circleLoad.setVisibility(View.VISIBLE);
        circleLoad.setText(content);
        circleLoad.spin();
    }

    private void hideLoadingCircle() {
        circleLoad.stopSpinning();
        circleLoad.setVisibility(View.INVISIBLE);
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
    public void onRemoteEvent(EventBusManager.RemoteServiceEvent event) {
        if (event.message.equals(EventBusManager.RemoteServiceEvent.GET_VOTE_LIST_SEARCH)) {
            refreshData(event.voteDataList, event.offset);
            hideLoadingCircle();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUIChange(EventBusManager.UIControlEvent event) {
        if (event.message.equals(EventBusManager.UIControlEvent.SEARCH_KEYWORD)) {
            setQueryText(event.keyword);
        }
    }

    private void refreshData(List<VoteData> voteDataList, int offset) {
        if (voteDataList == null) {
            voteDataList = new ArrayList<>();

            voteDataList = DataLoader.getInstance(getContext()).querySearchVotes(keyword
                    , offset, LIMIT);
        }
        Log.d(TAG, "Network Refresh wall item : Search");
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
                Toast.makeText(getContext(), R.string.wall_item_toast_no_vote_refresh, Toast.LENGTH_SHORT).show();
            }
        } else {
            adapter.setMaxCount(LIMIT * (pageNumber + 2));
        }
        adapter.notifyDataSetChanged();
    }
}
