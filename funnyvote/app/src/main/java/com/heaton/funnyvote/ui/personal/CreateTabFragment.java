package com.heaton.funnyvote.ui.personal;

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

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.heaton.funnyvote.R;
import com.heaton.funnyvote.data.VoteData.VoteDataRepository;
import com.heaton.funnyvote.database.VoteData;
import com.heaton.funnyvote.di.ActivityScoped;
import com.heaton.funnyvote.ui.HidingScrollListener;
import com.heaton.funnyvote.ui.main.MainPageContract;
import com.heaton.funnyvote.ui.main.VoteWallItemAdapter;

import java.util.List;

import javax.inject.Inject;

import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;

@ActivityScoped
public class CreateTabFragment extends dagger.android.support.DaggerFragment implements MainPageContract.TabPageFragment {
    private static final int LIMIT = VoteDataRepository.PAGE_COUNT;
    public static String TAG = CreateTabFragment.class.getSimpleName();
    @Inject
    public PersonalContract.Presenter presenter;
    private RecyclerView ryMain;
    private RelativeLayout RootView;
    private VoteWallItemAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton fabTop;
    private VoteWallItemAdapter.VoteWallItemListener wallItemListener;

    @Inject
    public CreateTabFragment() {
        Log.d("test", "CreateTabFragment init");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RootView = (RelativeLayout) inflater.inflate(R.layout.fragment_main_page_tab, container, false);
        fabTop = (FloatingActionButton) RootView.findViewById(R.id.fabTop);
        fabTop.setVisibility(View.GONE);
        ryMain = (RecyclerView) RootView.findViewById(R.id.ryMainPage);
        Log.d(TAG, "onCreateView");
        wallItemListener = new VoteWallItemAdapter.VoteWallItemListener() {
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
                presenter.refreshCreateList();
            }
        };
        swipeRefreshLayout = (SwipeRefreshLayout) RootView.findViewById(R.id.swipeLayout);
        swipeRefreshLayout.setOnRefreshListener(new WallItemOnRefreshListener());

        return RootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (presenter == null) {
            return;
        }
        presenter.setCreateFragmentView(this);
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
        if (adapter != null)
            adapter.notifyDataSetChanged();
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
        adapter.setNoVoteTag(VoteWallItemAdapter.TAG_NO_VOTE_REFRESH);
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
        if (adapter != null) {
            adapter.setVoteList(voteDataList);
            adapter.resetItemTypeList();
            adapter.notifyDataSetChanged();
        }
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
    public void setMaxCount(int max) {
        if (adapter != null) {
            adapter.setMaxCount(max);
        }
    }

    @Override
    public void setTab(String tab) {
        //this.tab = tab;
    }

    @Override
    public void hideSwipeLoadView() {
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private class WallItemOnRefreshListener implements SwipeRefreshLayout.OnRefreshListener {

        @Override
        public void onRefresh() {
            presenter.reloadCreateList(0);
        }
    }


}
