package com.heaton.funnyvote.ui.about;

import com.heaton.funnyvote.BasePresenter;
import com.heaton.funnyvote.BaseView;

public interface AboutContract {
    interface Presenter extends BasePresenter{
        void IntentToIntroduction();
        void IntentToAuthorInfo();
        void IntentToLicence();
        void IntentToProblem();
        void IntentToAppStore();
        void IntentToAbout();
        void IntentToShareApp();
    }
    interface View extends BaseView<Presenter> {
        void showIntroduction();
        void showAuthorInfo();
        void showLicence();
        void showProblem();
        void showAppStore();
        void showAbout();
        void showShareApp();
    }
}
