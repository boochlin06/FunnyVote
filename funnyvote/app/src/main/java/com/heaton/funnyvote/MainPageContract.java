package com.heaton.funnyvote;

import com.heaton.funnyvote.database.User;

public interface MainPageContract {
    interface Presenter extends BasePresenter {
        void IntentToSearchPage();
        void IntentToCreatePage();
        void IntentToUserPage();
        void IntentToMainPage();
        void IntentToAboutPage();
        void IntentToAccountPage();
    }
    interface View extends BaseView<Presenter> {
        void showSearchPage();
        void showCreatePage();
        void showUserPage();
        void showMainPage();
        void showAboutPage();
        void showAccountPage();
        void updateUserView(User user);
    }
}
