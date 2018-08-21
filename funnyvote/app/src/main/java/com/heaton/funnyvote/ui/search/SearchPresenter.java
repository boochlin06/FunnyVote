package com.heaton.funnyvote.ui.search;

import com.heaton.funnyvote.R;
import com.heaton.funnyvote.data.VoteData.VoteDataRepository;
import com.heaton.funnyvote.data.VoteData.VoteDataSource;
import com.heaton.funnyvote.data.user.UserDataRepository;
import com.heaton.funnyvote.data.user.UserDataSource;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.database.VoteData;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class SearchPresenter implements SearchContract.Presenter {

    private static final String TAG = SearchPresenter.class.getSimpleName();
    String keyword;
    private VoteDataRepository voteDataRepository;
    private UserDataRepository userDataRepository;
    private List<VoteData> searchVoteDataList;
    private User user;
    private SearchContract.View view;
    @Inject
    public SearchPresenter(VoteDataRepository voteDataRepository
            , UserDataRepository userDataRepository) {
        this.userDataRepository = userDataRepository;
        this.voteDataRepository = voteDataRepository;
        this.searchVoteDataList = new ArrayList<>();
    }

    public List<VoteData> getSearchVoteDataList() {
        return searchVoteDataList;
    }

    public void setSearchVoteDataList(List<VoteData> searchVoteDataList) {
        this.searchVoteDataList = searchVoteDataList;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    @Override
    public void searchVote(String keyword) {
        this.keyword = keyword;
        reloadSearchList(0);
    }


    @Override
    public void reloadSearchList(final int offset) {
        voteDataRepository.getSearchVoteList(keyword, offset, user, new VoteDataSource.GetVoteListCallback() {
            @Override
            public void onVoteListLoaded(List<VoteData> voteDataList) {
                updateSearchList(voteDataList, offset);
                view.refreshFragment(searchVoteDataList);
            }

            @Override
            public void onVoteListNotAvailable() {
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
        //Log.d(TAG, "searchVoteDataList:" + searchVoteDataList.size() + ",offset :" + offset);
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
    public void startwithSearch(final String keyword) {
        //this.keyword = keyword;
        userDataRepository.getUser(new UserDataSource.GetUserCallback() {
            @Override
            public void onResponse(User user) {
                SearchPresenter.this.user = user;
                if (!(keyword == null || keyword.length() == 0)) {
                    System.out.println("1keyword:" + keyword);
                    searchVote(keyword);
                }
            }

            @Override
            public void onFailure() {

            }
        }, false);
    }

    @Override
    public void takeView(SearchContract.View view) {
        this.view = view;
        startwithSearch(this.keyword);
    }


    @Override
    public void dropView() {
        this.view = null;
    }
}
