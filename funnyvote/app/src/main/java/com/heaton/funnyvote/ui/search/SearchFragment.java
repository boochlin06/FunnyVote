package com.heaton.funnyvote.ui.search;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.heaton.funnyvote.FunnyVoteApplication;
import com.heaton.funnyvote.R;
import com.heaton.funnyvote.analytics.AnalyzticsTag;
import com.heaton.funnyvote.data.Injection;
import com.heaton.funnyvote.data.VoteData.VoteDataRepository;
import com.heaton.funnyvote.database.VoteData;
import com.heaton.funnyvote.ui.main.VHVoteWallItem;

import java.util.ArrayList;
import java.util.List;

import at.grabner.circleprogress.CircleProgressView;
import at.grabner.circleprogress.TextMode;

/**
 * Created by heaton on 2017/1/22.
 */

public class SearchFragment extends Fragment implements SearchContract.View {

    private static final String TAG = SearchFragment.class.getSimpleName();
    private static final int LIMIT = VoteDataRepository.PAGE_COUNT;
    public static final String KEY_SEARCH_KEYWORD = "key_search_keyword";
    private RecyclerView ryMain;
    private RelativeLayout RootView;
    private SearchItemAdapter adapter;
    private SearchView searchView;

    private String keyword = "";
    private Tracker tracker;

    CircleProgressView circleLoad;

    private SearchContract.Presenter presenter;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.menu_search));
        if (searchView != null) {
            searchView.setQueryHint(getString(R.string.vote_detail_menu_search_hint));
            searchView.setSubmitButtonEnabled(true);
            searchView.setOnQueryTextListener(queryListener);
        }
    }

    final private SearchView.OnQueryTextListener queryListener =
            new SearchView.OnQueryTextListener() {

                @Override
                public boolean onQueryTextChange(String newText) {
                    if (newText.length() > 1) {
                        tracker.send(new HitBuilders.EventBuilder()
                                .setCategory(AnalyzticsTag.CATEGORY_SEARCH_VOTE)
                                .setAction(AnalyzticsTag.ACTION_SEARCH_VOTE)
                                .setLabel(keyword).build());

                        Log.d(TAG, "Search page onQueryTextChange:" + newText);
                        presenter.searchVote(newText);
                    }
                    return false;
                }

                @Override
                public boolean onQueryTextSubmit(String query) {

                    tracker.send(new HitBuilders.EventBuilder()
                            .setCategory(AnalyzticsTag.CATEGORY_SEARCH_VOTE)
                            .setAction(AnalyzticsTag.ACTION_SEARCH_VOTE)
                            .setLabel(query).build());
                    Log.d(TAG, "Search page onQueryTextSubmit:" + query);
                    presenter.searchVote(query);
                    return false;
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
        initRecyclerView();

        presenter = new SearchPresenter(Injection.provideVoteDataRepository(getContext())
                , Injection.provideUserRepository(getContext()), this);
        presenter.start();

    }

    private void initRecyclerView() {
        adapter = new SearchItemAdapter(getContext(), new ArrayList<VoteData>(), new VoteSearchItemListener() {
            @Override
            public void onVoteItemClick(VoteData voteData) {
                presenter.IntentToVoteDetail(voteData);
            }

            @Override
            public void onReloadVote() {
                presenter.refreshSearchList();
            }
        });
        ryMain.setAdapter(adapter);

    }

//    private void setQueryText(String queryText) {
//        this.keyword = queryText;
//        if (user != null) {
//            this.showLoadingCircle();
//            voteDataManager.getSearchVoteList(keyword, 0, user);
//        }
//    }

    @Override
    public void showLoadingCircle() {
        circleLoad.setVisibility(View.VISIBLE);
        circleLoad.setText(getString(R.string.vote_detail_circle_loading));
        circleLoad.spin();
    }

    @Override
    public void hideLoadingCircle() {
        circleLoad.stopSpinning();
        circleLoad.setVisibility(View.GONE);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        //EventBus.getDefault().register(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        //EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onRemoteEvent(EventBusManager.RemoteServiceEvent event) {
//        if (event.message.equals(EventBusManager.RemoteServiceEvent.GET_VOTE_LIST_SEARCH)) {
//            refreshData(event.voteDataList, event.offset);
//            hideLoadingCircle();
//        }
//    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onUIChange(EventBusManager.UIControlEvent event) {
//        if (event.message.equals(EventBusManager.UIControlEvent.SEARCH_KEYWORD)) {
//            setQueryText(event.keyword);
//        }
//    }

    @Override
    public void showHintToast(int res, long arg) {
        Toast.makeText(getContext(), getString(res, arg), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showVoteDetail(VoteData data) {
        VHVoteWallItem.startActivityToVoteDetail(getContext(), data.getVoteCode());
    }

    @Override
    public void setMaxCount(int max) {
        if (adapter != null) {
            adapter.setMaxCount(max);
        }
    }

    @Override
    public void refreshFragment(List<VoteData> voteDataList) {
        adapter.setVoteList(voteDataList);
        adapter.notifyDataSetChanged();
    }
//    private void refreshData(List<VoteData> voteDataList, int offset) {
//        if (voteDataList == null) {
//            voteDataList = new ArrayList<>();
//
//            voteDataList = DataLoader.getInstance(getContext()).querySearchVotes(keyword
//                    , offset, LIMIT);
//        }
//        Log.d(TAG, "Network Refresh wall item : Search");
//        int pageNumber = offset / LIMIT;
//
//        if (offset == 0) {
//            this.voteDataList = voteDataList;
//            adapter.setVoteList(this.voteDataList);
//        } else if (offset >= this.voteDataList.size()) {
//            this.voteDataList.addAll(voteDataList);
//        }
//
//        if (this.voteDataList.size() < LIMIT * (pageNumber + 1)) {
//            adapter.setMaxCount(this.voteDataList.size());
//            if (offset != 0) {
//                Toast.makeText(getContext(), R.string.wall_item_toast_no_vote_refresh, Toast.LENGTH_SHORT).show();
//            }
//        } else {
//            adapter.setMaxCount(LIMIT * (pageNumber + 2));
//        }
//        adapter.notifyDataSetChanged();
//    }

    @Override
    public void setPresenter(SearchContract.Presenter presenter) {
        this.presenter = presenter;
    }

    public interface VoteSearchItemListener {

        void onVoteItemClick(VoteData voteData);

        void onReloadVote();
    }
}
