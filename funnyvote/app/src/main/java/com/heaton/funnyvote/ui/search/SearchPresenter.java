package com.heaton.funnyvote.ui.search;

import android.support.annotation.NonNull;

import com.heaton.funnyvote.R;
import com.heaton.funnyvote.data.VoteData.VoteDataRepository;
import com.heaton.funnyvote.data.user.UserDataRepository;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.database.VoteData;
import com.heaton.funnyvote.utils.schedulers.BaseSchedulerProvider;

import java.util.ArrayList;
import java.util.List;

import rx.Observer;
import rx.subscriptions.CompositeSubscription;

public class SearchPresenter implements SearchContract.Presenter {

    private static final String TAG = SearchPresenter.class.getSimpleName();
    private VoteDataRepository voteDataRepository;
    private UserDataRepository userDataRepository;

    public List<VoteData> getSearchVoteDataList() {
        return searchVoteDataList;
    }

    public void setSearchVoteDataList(List<VoteData> searchVoteDataList) {
        this.searchVoteDataList = searchVoteDataList;
    }

    private List<VoteData> searchVoteDataList;
    private User user;
    private SearchContract.View view;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    private String keyword;
    private BaseSchedulerProvider schedulerProvider;
    @NonNull
    private CompositeSubscription mSubscriptions;


    public SearchPresenter(VoteDataRepository voteDataRepository
            , UserDataRepository userDataRepository
            , SearchContract.View mainPageView
            , BaseSchedulerProvider schedulerProvider) {
        this.view = mainPageView;
        this.userDataRepository = userDataRepository;
        this.voteDataRepository = voteDataRepository;
        this.searchVoteDataList = new ArrayList<>();
        this.view.setPresenter(this);
        this.schedulerProvider = schedulerProvider;
        mSubscriptions = new CompositeSubscription();
    }

    @Override
    public void searchVote(String keyword) {
        this.keyword = keyword;
        reloadSearchList(0);
    }


    @Override
    public void reloadSearchList(final int offset) {
        mSubscriptions.add(voteDataRepository.getSearchVoteList(keyword, offset, user)
                .subscribeOn(schedulerProvider.computation())
                .observeOn(schedulerProvider.ui())
                .subscribe(new Observer<List<VoteData>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        view.showHintToast(R.string.toast_network_connect_error, 0);

                    }

                    @Override
                    public void onNext(List<VoteData> voteDataList) {
                        updateSearchList(voteDataList, offset);
                        view.refreshFragment(searchVoteDataList);
                    }
                }));
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
    public void start(final String keyword) {
        this.keyword = keyword;
        mSubscriptions.add(userDataRepository.getUser(false)
                .subscribeOn(schedulerProvider.computation())
                .observeOn(schedulerProvider.ui())
                .subscribe(new Observer<User>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(User user) {
                        SearchPresenter.this.user = user;
                        if (!(keyword == null || keyword.length() == 0)) {
                            searchVote(keyword);
                        }
                    }
                }));
    }

    @Override
    public void subscribe() {
        start("");
    }

    @Override
    public void unsubscribe() {
        mSubscriptions.clear();
    }
}
