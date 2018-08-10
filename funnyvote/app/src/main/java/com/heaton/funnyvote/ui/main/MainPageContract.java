package com.heaton.funnyvote.ui.main;

import com.heaton.funnyvote.BasePresenter;
import com.heaton.funnyvote.BaseView;
import com.heaton.funnyvote.database.Promotion;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.database.VoteData;

import java.util.List;

public interface MainPageContract {
    interface Presenter extends BasePresenter {
        void resetPromotion();

        void setHotsFragmentView(TabPageFragment hotsFragmentView);

        void setNewsFragmentView(TabPageFragment newsFragmentView);

        void setCreateFragmentView(MainPageContract.TabPageFragment fragmentView);

        void setParticipateFragmentView(MainPageContract.TabPageFragment fragmentView);

        void setFavoriteFragmentView(MainPageContract.TabPageFragment fragmentView);

        void favoriteVote(VoteData voteData);

        void IntentToShareDialog(VoteData voteData);

        void IntentToCreateVote();

        void IntentToAuthorDetail(VoteData voteData);

        void IntentToVoteDetail(VoteData voteData);

        void pollVote(VoteData voteData, String optionCode, String password);

        void reloadHotList(int offset);

        void reloadNewList(int offset);

        void refreshNewList();

        void refreshHotList();

        void reloadCreateList(int offset);

        void reloadParticipateList(int offset);

        void reloadFavoriteList(int offset);

        void refreshCreateList();

        void refreshParticipateList();

        void refreshFavoriteList();

        void setTargetUser(User targetUser);

        void refreshAllFragment();
    }

    interface MainPageView extends BaseView<Presenter> {
        void showShareDialog(VoteData data);

        void showAuthorDetail(VoteData data);

        void showCreateVote();

        void showVoteDetail(VoteData data);

        void showIntroductionDialog();

        void showLoadingCircle();

        void hideLoadingCircle();

        void setupPromotionAdmob(List<Promotion> promotionList, User user);

        void setUpTabsAdapter(User user);

        void setUpTabsAdapter(User user,User targetUser);

        void showHintToast(int res, long arg);

        void showPollPasswordDialog(VoteData data, String optionCode);

        void hidePollPasswordDialog();

        void shakePollPasswordDialog();

        boolean isPasswordDialogShowing();

    }

    interface TabPageFragment extends BaseView<Presenter> {
        void setUpRecycleView(List<VoteData> voteDataList);

        void refreshFragment(List<VoteData> voteDataList);

        void setTab(String tab);

        void hideSwipeLoadView();

        void setMaxCount(int max);
    }
}
