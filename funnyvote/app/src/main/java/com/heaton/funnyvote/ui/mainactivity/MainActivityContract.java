package com.heaton.funnyvote.ui.mainactivity;

import com.heaton.funnyvote.BasePresenter;
import com.heaton.funnyvote.BaseView;
import com.heaton.funnyvote.database.User;

public interface MainActivityContract {
    interface Presenter extends BasePresenter<View> {
        void IntentToSearchPage(String searchKeyword);

        void IntentToCreatePage();

        void IntentToUserPage();

        void IntentToMainPage();

        void IntentToAboutPage();

        void IntentToAccountPage();

        void loadUser();

        void takeView(View view);
    }

    interface View extends BaseView<Presenter> {
        void showSearchPage(String searchKeyword);

        void showCreatePage();

        void showUserPage();

        void showMainPage();

        void showAboutPage();

        void showAccountPage();

        void updateUserView(User user);
    }
}
