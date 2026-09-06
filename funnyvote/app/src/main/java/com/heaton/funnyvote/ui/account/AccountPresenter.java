package com.heaton.funnyvote.ui.account;

import android.util.Log;

import com.heaton.funnyvote.data.user.UserDataRepository;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.utils.schedulers.BaseSchedulerProvider;

import io.reactivex.disposables.CompositeDisposable;

public class AccountPresenter implements AccountContract.Presenter {

    public static final int LOGIN_FB = 111;
    public static final int LOGIN_GOOGLE = 112;
    public static final int LOGIN_TWITTER = 113;
    private static final String TAG = AccountPresenter.class.getSimpleName();
    private UserDataRepository userDataRepository;
    private AccountContract.View view;
    private BaseSchedulerProvider schedulerProvider;
    private CompositeDisposable subscription;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    private User user;
    private boolean mergeGuest;

    public AccountPresenter(UserDataRepository userDataRepository
            , AccountContract.View view
            , BaseSchedulerProvider schedulerProvider) {
        this.userDataRepository = userDataRepository;
        this.view = view;
        this.view.setPresenter(this);
        this.schedulerProvider = schedulerProvider;
        this.subscription = new CompositeDisposable();
    }

    @Override
    public void subscribe() {
        updateUser();
    }

    @Override
    public void unsubscribe() {
        subscription.clear();
    }

    @Override
    public void registerUser(User newUser, String appId) {
        subscription.add(userDataRepository.registerUser(appId, newUser, mergeGuest)
                .subscribeOn(schedulerProvider.computation())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                        o -> updateUser(),
                        e -> updateUser()
                ));
    }

    @Override
    public void unregisterUser() {
        userDataRepository.unregisterUser();
    }

    @Override
    public void updateUser() {
        subscription.add(userDataRepository.getUser(false)
                .subscribeOn(schedulerProvider.computation())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                        user -> {
                            AccountPresenter.this.user = user;
                            if (AccountPresenter.this.user.getType() != User.TYPE_GUEST) {
                                view.showUser(user);
                            } else {
                                view.showLoginView(user.getUserName());
                            }
                        },
                        throwable -> view.showLoginView("Heaton")
                ));
    }

    @Override
    public void changeCurrentUserName(String userName) {
        subscription.add(userDataRepository.changeCurrentUserName(userName)
                .subscribeOn(schedulerProvider.computation())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                        o -> updateUser(),
                        e -> Log.d(TAG, "ChangeUserNameCallback onFailure")
                ));
    }

    @Override
    public void logout() {
        if (user == null) return;
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
