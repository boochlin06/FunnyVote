package com.heaton.funnyvote.ui.mainactivity;

import com.heaton.funnyvote.data.user.UserDataRepository;
import com.heaton.funnyvote.data.user.UserDataSource;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.di.ActivityScoped;

import javax.inject.Inject;

@ActivityScoped
public class MainActivityPresenter implements MainActivityContract.Presenter {

    private MainActivityContract.View view;
    private UserDataRepository userDataRepository;

    @Inject
    public MainActivityPresenter(UserDataRepository userDataRepository) {
        this.userDataRepository = userDataRepository;
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
        userDataRepository.getUser(new UserDataSource.GetUserCallback() {
            @Override
            public void onResponse(User user) {
                view.updateUserView(user);
            }

            @Override
            public void onFailure() {
            }
        }, false);
    }

    @Override
    public void takeView(MainActivityContract.View view) {
        this.view = view;
        IntentToMainPage();
        loadUser();
    }

    @Override
    public void dropView() {
        view = null;
    }
}
