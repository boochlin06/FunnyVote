/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.heaton.funnyvote.ui.main;

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

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.analytics.Tracker;
import com.heaton.funnyvote.FunnyVoteApplication;
import com.heaton.funnyvote.R;
import com.heaton.funnyvote.data.VoteData.VoteDataRepository;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.database.VoteData;
import com.heaton.funnyvote.ui.HidingScrollListener;

import java.util.List;

import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;

public class MainPageTabFragment extends Fragment implements MainPageContract.TabPageFragment {
    private static final int LIMIT = VoteDataRepository.PAGE_COUNT;
    public static String TAG = MainPageTabFragment.class.getSimpleName();

    public static final String KEY_TAB = "tab";
    public static final String KEY_LOGIN_USER = "key_login_user";
    public static final String KEY_TARGET_USER = "key_target_user";

    public static final String TAB_HOT = "HOT";
    public static final String TAB_NEW = "NEW";

    public static final String TAB_CREATE = "CREATE";
    public static final String TAB_PARTICIPATE = "PARTICIPATE";
    public static final String TAB_FAVORITE = "FAVORITE";

    private RecyclerView ryMain;
    private RelativeLayout RootView;
    private String tab = TAB_HOT;
    private List<VoteData> voteDataList;
    private VoteWallItemAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton fabTop;
    private Tracker tracker;
    private MainPageContract.Presenter presenter;
    private VoteWallItemListener wallItemListener;

    public static MainPageTabFragment newInstance(String tab, User loginUser) {
        return newInstance(tab, loginUser, null);
    }

    public static MainPageTabFragment newInstance(String tab, User loginUser, User targetUser) {
        MainPageTabFragment fragment = new MainPageTabFragment();
        Bundle argument = new Bundle();
        argument.putString(MainPageTabFragment.KEY_TAB, tab);
        argument.putParcelable(MainPageTabFragment.KEY_LOGIN_USER, loginUser);
        argument.putParcelable(MainPageTabFragment.KEY_TARGET_USER, targetUser);
        fragment.setArguments(argument);
        fragment.setRetainInstance(false);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle argument = getArguments();
        this.tab = argument.getString(KEY_TAB);
        FunnyVoteApplication application = (FunnyVoteApplication) getActivity().getApplication();
        tracker = application.getDefaultTracker();
        RootView = (RelativeLayout) inflater.inflate(R.layout.fragment_main_page_tab, container, false);
        fabTop = (FloatingActionButton) RootView.findViewById(R.id.fabTop);
        fabTop.setVisibility(View.GONE);
        ryMain = (RecyclerView) RootView.findViewById(R.id.ryMainPage);
        wallItemListener = new VoteWallItemListener() {
            @Override
            public void onVoteFavoriteChange(VoteData voteData) {
                Log.d(TAG, "onVoteFavoriteChange");
                presenter.favoriteVote(voteData);
            }

            @Override
            public void onVoteItemClick(VoteData voteData) {
                presenter.IntentToVoteDetail(voteData);
            }

            @Override
            public void onVoteAuthorClick(VoteData voteData) {
                presenter.IntentToAuthorDetail(voteData);
            }

            @Override
            public void onVoteShare(VoteData voteData) {
                presenter.IntentToShareDialog(voteData);
            }

            @Override
            public void onVoteQuickPoll(VoteData voteData, String optionCode) {
                presenter.pollVote(voteData, optionCode, null);
            }

            @Override
            public void onNoVoteCreateNew() {
                presenter.IntentToCreateVote();
            }

            @Override
            public void onReloadVote() {
                if (tab.equals(TAB_HOT)) {
                    presenter.refreshHotList();
                } else if (tab.equals(TAB_NEW)) {
                    presenter.refreshNewList();
                } else if (tab.equals(TAB_CREATE)) {
                    presenter.refreshCreateList();
                } else if (tab.equals(TAB_PARTICIPATE)) {
                    presenter.refreshParticipateList();
                } else if (tab.equals(TAB_FAVORITE)) {
                    presenter.refreshFavoriteList();
                }
            }
        };
        swipeRefreshLayout = (SwipeRefreshLayout) RootView.findViewById(R.id.swipeLayout);
        swipeRefreshLayout.setOnRefreshListener(new WallItemOnRefreshListener());

        return RootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.setPresenter(presenter);
        if (presenter == null) {
            return;
        }
        if (tab.equals(TAB_HOT)) {
            presenter.setHotsFragmentView(this);
        } else if (tab.equals(TAB_NEW)) {
            presenter.setNewsFragmentView(this);
        } else if (tab.equals(TAB_CREATE)) {
            presenter.setCreateFragmentView(this);
        } else if (tab.equals(TAB_PARTICIPATE)) {
            presenter.setParticipateFragmentView(this);
        } else if (tab.equals(TAB_FAVORITE)) {
            presenter.setFavoriteFragmentView(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        LinearLayoutManager manager = (LinearLayoutManager) ryMain.getLayoutManager();
        int position = manager.findFirstVisibleItemPosition();
        if (position == 0) {
            // TODO:AUTO UPDATE .
            //refreshData();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }

    @Override
    public void setUpRecycleView(List<VoteData> voteDataList) {
        //voteDataList = voteDataList;
        adapter = new VoteWallItemAdapter(getContext(), wallItemListener
                , voteDataList);
        // if max count is -1 , the list is init.
        adapter.setMaxCount(-1);
        if (tab.equals(TAB_HOT)) {
            adapter.setNoVoteTag(VoteWallItemAdapter.TAG_NO_VOTE_REFRESH);
        } else if (tab.equals(TAB_NEW)) {
            adapter.setNoVoteTag(VoteWallItemAdapter.TAG_NO_VOTE_REFRESH);
        } else if (tab.equals(TAB_CREATE)) {
            adapter.setNoVoteTag(VoteWallItemAdapter.TAG_NO_VOTE_CREATE_NEW);
        } else if (tab.equals(TAB_PARTICIPATE)) {
            adapter.setNoVoteTag(VoteWallItemAdapter.TAG_NO_VOTE_PARTICIPATE);
        } else if (tab.equals(TAB_FAVORITE)) {
            adapter.setNoVoteTag(VoteWallItemAdapter.TAG_NO_VOTE_FAVORITE);
        }
        adapter.resetItemTypeList();
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
            }
        });
    }

    @Override
    public void refreshFragment(List<VoteData> voteDataList) {
        adapter.setVoteList(voteDataList);
        adapter.resetItemTypeList();
        adapter.notifyDataSetChanged();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void setPresenter(MainPageContract.Presenter presenter) {
        this.presenter = presenter;
    }

    private class WallItemOnRefreshListener implements SwipeRefreshLayout.OnRefreshListener {

        @Override
        public void onRefresh() {

            if (tab.equals(TAB_HOT)) {
                presenter.reloadHotList(0);
            } else if (tab.equals(TAB_NEW)) {
                presenter.reloadNewList(0);
            } else if (tab.equals(TAB_CREATE)) {
                presenter.reloadCreateList(0);
            } else if (tab.equals(TAB_PARTICIPATE)) {
                presenter.reloadParticipateList(0);
            } else if (tab.equals(TAB_FAVORITE)) {
                presenter.reloadFavoriteList(0);
            }
        }
    }


    @Override
    public void setMaxCount(int max) {
        if (adapter != null) {
            adapter.setMaxCount(max);
        }
    }

    @Override
    public void setTab(String tab) {
        this.tab = tab;
    }

    @Override
    public void hideSwipeLoadView() {
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    public interface VoteWallItemListener {
        void onVoteFavoriteChange(VoteData voteData);

        void onVoteItemClick(VoteData voteData);

        void onVoteAuthorClick(VoteData voteData);

        void onVoteShare(VoteData voteData);

        void onVoteQuickPoll(VoteData voteData, String optionCode);

        void onNoVoteCreateNew();

        void onReloadVote();
    }
}
