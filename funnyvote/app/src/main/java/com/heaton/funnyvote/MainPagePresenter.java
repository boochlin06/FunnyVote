package com.heaton.funnyvote;

import android.support.annotation.NonNull;

import com.heaton.funnyvote.data.user.UserDataRepository;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.utils.schedulers.BaseSchedulerProvider;

import rx.Observer;
import rx.subscriptions.CompositeSubscription;

public class MainPagePresenter implements MainPageContract.Presenter {

    private MainPageContract.View view;
    private UserDataRepository userDataRepository;
    private BaseSchedulerProvider schedulerProvider;
    @NonNull
    private CompositeSubscription mSubscriptions;

    public MainPagePresenter(UserDataRepository userDataRepository
            , MainPageContract.View view
            , BaseSchedulerProvider schedulerProvider) {
        this.userDataRepository = userDataRepository;
        this.view = view;
        this.view.setPresenter(this);
        this.schedulerProvider = schedulerProvider;
        this.mSubscriptions = new CompositeSubscription();
    }

    @Override
    public void IntentToSearchPage(String searchKeyword) {
        view.showSearchPage(searchKeyword);
    }

    @Override
    public void IntentToCreatePage() {
        view.showCreatePage();
    }

    @Override
    public void IntentToUserPage() {
        view.showUserPage();
    }

    @Override
    public void IntentToMainPage() {
        view.showMainPage();
    }

    @Override
    public void IntentToAboutPage() {
        view.showAboutPage();
    }

    @Override
    public void IntentToAccountPage() {
        view.showAccountPage();
    }

    @Override
    public void loadUser() {
        mSubscriptions.add(userDataRepository.getUser(false)
                .subscribeOn(schedulerProvider.computation())
                .observeOn(schedulerProvider.ui())
                .subscribe(new Observer<User>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(User user) {
                        view.updateUserView(user);
                    }
                }));
    }

    @Override
    public void subscribe() {
        IntentToMainPage();
        loadUser();
    }

    @Override
    public void unsubscribe() {
        mSubscriptions.clear();
    }
}
