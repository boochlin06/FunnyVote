package com.heaton.funnyvote.ui.search;

import com.heaton.funnyvote.BasePresenter;
import com.heaton.funnyvote.BaseView;
import com.heaton.funnyvote.database.VoteData;

import java.util.List;

public interface SearchContract {
    interface Presenter extends BasePresenter {
        void searchVote(String keyword);

        void reloadSearchList(int offset);

        void refreshSearchList();

        void IntentToVoteDetail(VoteData voteData);

        void start(String keyword);
    }

    interface View extends BaseView<Presenter> {
        void showLoadingCircle();

        void hideLoadingCircle();

        void showHintToast(int res, long arg);

        void showVoteDetail(VoteData data);

        void setMaxCount(int max);

        void refreshFragment(List<VoteData> voteDataList);
    }
}
