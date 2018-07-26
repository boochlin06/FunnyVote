package com.heaton.funnyvote.ui.createvote;

import android.net.Uri;

import com.heaton.funnyvote.BasePresenter;
import com.heaton.funnyvote.BaseView;
import com.heaton.funnyvote.database.Option;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.database.VoteData;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface CreateVoteContract {
    interface Presenter extends BasePresenter {
        void submitCreateVote();

        void addNewOption();

        void removeOption(long optionId);

        void reviseOption(long optionId, String optionText);

        void setActivityView(ActivityView view);

        void setOptionFragmentView(OptionFragmentView view);

        void setSettingFragmentView(SettingFragmentView view);

        void updateVoteSecurity(String security);

        void updateVoteEndTime(long timeInMill);

        void updateVoteImage(File image);

        void updateVoteTitle(String title);

    }

    interface ActivityView extends BaseView<Presenter> {
        void showExitCheckDialog();

        void showLoadingCircle();

        void hideLoadingCircle();

        void showCreateVoteError(Map<String, Boolean> errorMap);

        void showHintToast(int res);

        void showHintToast(int res, long arg);

        void IntentToVoteDetail(VoteData voteData);
    }

    interface SettingFragmentView extends BaseView<Presenter> {
        void setUpVoteSettings(VoteData voteSettings);

        void updateUserSetting(User user);

        VoteData getFinalVoteSettings(VoteData oldVoteData);

        void updateSwtNeedPwd(boolean isChecked);
    }

    interface OptionFragmentView extends BaseView<Presenter> {
        void setUpOptionAdapter(List<Option> optionList);

        void refreshOptions();

        void setVoteImage(Uri imageUri);

    }
}
