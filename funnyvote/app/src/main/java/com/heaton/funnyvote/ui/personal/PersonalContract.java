package com.heaton.funnyvote.ui.personal;

import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.database.VoteData;
import com.heaton.funnyvote.ui.main.MainPageContract;

public interface PersonalContract {
    interface Presenter extends MainPageContract.Presenter {
        void setCreateFragmentView(MainPageContract.TabPageFragment fragmentView);

        void setParticipateFragmentView(MainPageContract.TabPageFragment fragmentView);

        void setFavoriteFragmentView(MainPageContract.TabPageFragment fragmentView);

        void favoriteVote(VoteData voteData);

        void IntentToShareDialog(VoteData voteData);

        void IntentToCreateVote();

        void IntentToAuthorDetail(VoteData voteData);

        void IntentToVoteDetail(VoteData voteData);

        void pollVote(VoteData voteData, String optionCode, String password);

        void reloadCreateList(int offset);

        void reloadParticipateList(int offset);

        void reloadFavoriteList(int offset);

        void refreshCreateList();

        void refreshParticipateList();

        void refreshFavoriteList();

        void setTargetUser(User targetUser);

        void refreshAllFragment();
    }

    interface UserPageView extends MainPageContract.MainPageView {
        void setUpUserView(User user);
    }
}
