package com.heaton.funnyvote.ui.account;

import android.util.Log;

import com.heaton.funnyvote.data.user.UserDataRepository;
import com.heaton.funnyvote.data.user.UserDataSource;
import com.heaton.funnyvote.database.User;

public class AccountPresenter implements AccountContract.Presenter {

    private static final int RC_GOOGLE_SIGN_IN = 101;
    private static final int LOGIN_FB = 111;
    private static final int LOGIN_GOOGLE = 112;
    private static final int LOGIN_TWITTER = 113;
    private static final String TAG = AccountPresenter.class.getSimpleName();
    private UserDataRepository userDataRepository;
    private AccountContract.View view;
    private User user;
    private boolean mergeGuest;

    public AccountPresenter(UserDataRepository userDataRepository
            , AccountContract.View view) {
        this.userDataRepository = userDataRepository;
        this.view = view;
    }

    @Override
    public void start() {
        updateUser();
    }

    @Override
    public void registerUser(User newUser) {
        userDataRepository.registerUser(newUser, mergeGuest, new UserDataSource.RegisterUserCallback() {
            @Override
            public void onSuccess() {
                updateUser();
            }

            @Override
            public void onFailure() {
                updateUser();
            }
        });
    }

    @Override
    public void unregisterUser() {
        userDataRepository.unregisterUser();
    }

    @Override
    public void updateUser() {
        userDataRepository.getUser(new UserDataSource.GetUserCallback() {
            @Override
            public void onResponse(User user) {
                AccountPresenter.this.user = user;
                if (user.getType() != User.TYPE_GUEST) {
                    view.showUser(user);
                } else {
                    view.showLoginView(user.getUserName());
                }
            }

            @Override
            public void onFailure() {
                view.showLoginView("Heaton");
            }
        }, false);
    }

    @Override
    public void changeCurrentUserName(String userName) {
        userDataRepository.changeCurrentUserName(userName, new UserDataSource.ChangeUserNameCallback() {
            @Override
            public void onSuccess() {
                updateUser();
            }

            @Override
            public void onFailure() {
                Log.d(TAG, "ChangeUserNameCallback onFailure");
            }
        });
    }

    @Override
    public void logout() {
        switch (user.getType()) {
            case User.TYPE_FACEBOOK:
                view.facebookLogout();
                break;
            case User.TYPE_GOOGLE:
                view.googleSignOut();
                break;
            case User.TYPE_TWITTER:
                view.twitterlogout();
                break;
        }
    }

    @Override
    public void login(int loginType, boolean mergeGuest) {
        this.mergeGuest = mergeGuest;
        switch (loginType) {
            case LOGIN_FB:
                view.facebookLogin();
                break;
            case LOGIN_GOOGLE:
                view.googleSignIn();
                break;
            case LOGIN_TWITTER:
                view.twitterLogin();
                break;
        }
    }
}
