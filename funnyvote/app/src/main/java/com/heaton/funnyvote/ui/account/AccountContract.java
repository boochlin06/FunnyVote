package com.heaton.funnyvote.ui.account;

import com.heaton.funnyvote.BasePresenter;
import com.heaton.funnyvote.BaseView;
import com.heaton.funnyvote.database.User;

public interface AccountContract {
    interface Presenter extends BasePresenter {
        void registerUser(User newUser,String appId);
        void unregisterUser();
        void updateUser();
        void changeCurrentUserName(String userName);
        void logout();
        void login(int loginType,boolean mergeGuest);
    }

    interface View extends BaseView<Presenter> {
        void showUser(User user);
        void showLoginView(String guestName);
        void showNameEditDialog();
        void showMergeOptionDialog(final int loginType);
        void facebookLogout();
        void googleSignOut();
        void twitterlogout();
        void facebookLogin();
        void twitterLogin();
        void googleSignIn();
    }
}
