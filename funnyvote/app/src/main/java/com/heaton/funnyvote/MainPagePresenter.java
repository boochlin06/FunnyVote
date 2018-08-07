package com.heaton.funnyvote;

import com.heaton.funnyvote.data.user.UserDataRepository;
import com.heaton.funnyvote.data.user.UserDataSource;
import com.heaton.funnyvote.database.User;

public class MainPagePresenter implements MainPageContract.Presenter {

    private MainPageContract.View view;
    private UserDataRepository userDataRepository;

    public MainPagePresenter(UserDataRepository userDataRepository
            , MainPageContract.View view) {
        this.userDataRepository = userDataRepository;
        this.view = view;
        this.view.setPresenter(this);
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
    public void start() {
        IntentToMainPage();
        loadUser();
    }
}
