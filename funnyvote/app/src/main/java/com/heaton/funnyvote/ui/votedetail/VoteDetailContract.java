package com.heaton.funnyvote.ui.votedetail;

import com.heaton.funnyvote.BasePresenter;
import com.heaton.funnyvote.BaseView;
import com.heaton.funnyvote.database.Option;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.database.VoteData;

import java.util.List;

public interface VoteDetailContract {
    interface View extends BaseView<Presenter> {
        void showLoadingCircle();

        void hideLoadingCircle();

        void showSortOptionDialog(VoteData data);

        void showPollPasswordDialog();

        void hidePollPasswordDialog();

        boolean isPasswordDialogShowing();

        void shakePollPasswordDialog();

        void shakeAddNewOptionPasswordDialog();

        void showAddNewOptionPasswordDialog(final String newOptionText);

        void hideAddNewOptionPasswordDialog();

        void showResultOption(int optionType);

        void showUnPollOption(int optionType);

        void showExitCheckDialog();

        void showVoteInfoDialog(VoteData data);

        void showTitleDetailDialog(VoteData data);

        void showCaseView();

        void updateFavoriteView(boolean isFavorite);

        void setUpAdMob(User user);

        void setUpViews(VoteData voteData, int optionType);

        void setUpSubmit(int optionType);

        void setUpOptionAdapter(VoteData data, int optionType, List<Option> optionList);

        void showHintToast(int res);

        void showMultiChoiceToast(int max, int min);

        void showMultiChoiceAtLeast(int min);

        void showMultiChoiceOverMaxToast(int max);

        void refreshOptions();

        void updateChoiceOptions(List<Long> choiceList);

        void updateExpandOptions(List<String> expandList);

        void showShareDialog(VoteData data);

        void showAuthorDetail(VoteData data);

        void moveToTop();

        void updateSearchView(List<Option> searchList, boolean isSearchMode);

    }

    interface Presenter extends BasePresenter {
        void searchOption(String newText);

        void favoriteVote();

        void pollVote(String password);

        void resetOptionChoiceStatus(long optionId, String optionCode);

        void resetOptionExpandStatus(String optionCode);

        void addNewOptionStart();

        void addNewOptionContentRevise(long optionId, String inputText);

        void addNewOptionCompleted(String password, String newOptionText);

        void removeOption(long optionId);

        void showVoteInfo();

        void showTitleDetail();

        void IntentToShareDialog();

        void IntentToAuthorDetail();

        void changeOptionType();

        void sortOptions(int sortType);

        void CheckSortOptionType();

    }
}
