package com.heaton.funnyvote.ui.search;

import android.text.TextUtils;
import android.util.Log;

import com.heaton.funnyvote.R;
import com.heaton.funnyvote.data.VoteData.VoteDataRepository;
import com.heaton.funnyvote.data.VoteData.VoteDataSource;
import com.heaton.funnyvote.data.user.UserDataRepository;
import com.heaton.funnyvote.data.user.UserDataSource;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.database.VoteData;

import java.util.ArrayList;
import java.util.List;

public class SearchPresenter implements SearchContract.Presenter {

    private static final String TAG = SearchPresenter.class.getSimpleName();
    private VoteDataRepository voteDataRepository;
    private UserDataRepository userDataRepository;
    private List<VoteData> searchVoteDataList;
    private User user;
    private SearchContract.View view;
    private String keyword;


    public SearchPresenter(VoteDataRepository voteDataRepository
            , UserDataRepository userDataRepository
            , SearchContract.View mainPageView) {
        this.view = mainPageView;
        this.userDataRepository = userDataRepository;
        this.voteDataRepository = voteDataRepository;
        this.searchVoteDataList = new ArrayList<>();
    }

    @Override
    public void searchVote(String keyword) {
        this.keyword = keyword;
        reloadSearchList(9);
    }


    @Override
    public void reloadSearchList(final int offset) {
        voteDataRepository.getSearchVoteList(keyword, offset, user, new VoteDataSource.GetVoteListCallback() {
            @Override
            public void onVoteListLoaded(List<VoteData> voteDataList) {
                updateSearchList(voteDataList, offset);
                view.refreshFragment(searchVoteDataList);
                view.hideLoadingCircle();
            }

            @Override
            public void onVoteListNotAvailable() {
                view.hideLoadingCircle();
                view.showHintToast(R.string.toast_network_connect_error, 0);
            }
        });
    }

    @Override
    public void refreshSearchList() {
        reloadSearchList(searchVoteDataList.size());
    }

    @Override
    public void IntentToVoteDetail(VoteData voteData) {
        view.showVoteDetail(voteData);
    }

    private void updateSearchList(List<VoteData> voteDataList, int offset) {
        int pageNumber = offset / VoteDataRepository.PAGE_COUNT;
        if (offset == 0) {
            this.searchVoteDataList = voteDataList;
        } else if (offset >= this.searchVoteDataList.size()) {
            this.searchVoteDataList.addAll(voteDataList);
        }
        Log.d(TAG, "searchVoteDataList:" + searchVoteDataList.size() + ",offset :" + offset);
        if (this.searchVoteDataList.size() < VoteDataRepository.PAGE_COUNT * (pageNumber + 1)) {
            view.setMaxCount(this.searchVoteDataList.size());
            if (offset != 0) {
                view.showHintToast(R.string.wall_item_toast_no_vote_refresh, 0);
            }
        } else {
            view.setMaxCount(VoteDataRepository.PAGE_COUNT * (pageNumber + 2));
        }
    }

    @Override
    public void start() {
        keyword = "";
        userDataRepository.getUser(new UserDataSource.GetUserCallback() {
            @Override
            public void onResponse(User user) {
                SearchPresenter.this.user = user;
                if (!TextUtils.isEmpty(keyword)) {
                    searchVote(keyword);
                }
            }

            @Override
            public void onFailure() {

            }
        }, false);
    }
}
