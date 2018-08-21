package com.heaton.funnyvote;

import com.heaton.funnyvote.data.user.UserDataRepository;
import com.heaton.funnyvote.data.user.UserDataSource;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.ui.mainactivity.MainActivity;
import com.heaton.funnyvote.ui.mainactivity.MainActivityContract;
import com.heaton.funnyvote.ui.mainactivity.MainActivityPresenter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the implementation of {@link MainActivity}
 */
public class MainPresenterTest {
    private static User user;
    @Mock
    private UserDataRepository userDataRepository;
    @Mock
    private MainActivityContract.View view;

    MainActivityPresenter presenter;
    @Captor
    private ArgumentCaptor<UserDataSource.GetUserCallback> getUserCallbackArgumentCaptor;

    @Before
    public void setupMainPresenter() {
        MockitoAnnotations.initMocks(this);

        presenter = new MainActivityPresenter(userDataRepository);
        user = mock(User.class);
        when(user.getUserName()).thenReturn("Heaton");
    }

    @Test
    public void createPresenter_setsThePresenterToView() {
        // Get a reference to the class under test
        presenter = new MainActivityPresenter(userDataRepository);

        // Then the presenter is set to the view
        //verify(view).setPresenter(presenter);
    }

    @Test
    public void loadUserFromRepositoryAndLoadIntoView() {
        presenter.takeView(view);

        verify(userDataRepository).getUser(getUserCallbackArgumentCaptor.capture(), eq(false));
        getUserCallbackArgumentCaptor.getValue().onResponse(user);

        ArgumentCaptor<User> updateUserArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(view).updateUserView(updateUserArgumentCaptor.capture());
        assertTrue(updateUserArgumentCaptor.getValue().getUserName().length() > 0);
    }

    @Test
    public void clickOnToolbar_showCreateVoteUi() {
        presenter.takeView(view);
        presenter.IntentToCreatePage();
        verify(view).showCreatePage();
    }

    @Test
    public void clickOnToolbar_showSearchUi() {
        presenter.takeView(view);
        presenter.IntentToSearchPage("");
        verify(view).showSearchPage("");
    }

    @Test
    public void clickOnDrawer_showUserBoxUi() {
        presenter.takeView(view);
        presenter.IntentToUserPage();
        verify(view).showUserPage();
    }

    @Test
    public void clickOnDrawer_showAccountUi() {
        presenter.takeView(view);
        presenter.IntentToAccountPage();
        verify(view).showAccountPage();
    }

    @Test
    public void clickOnDrawer_showAboutUi() {
        presenter.takeView(view);
        presenter.IntentToAboutPage();
        verify(view).showAboutPage();
    }

    @Test
    public void clickOnUserIcon_showUserBoxUi() {
        presenter.takeView(view);
        presenter.IntentToUserPage();
        verify(view).showUserPage();
    }

    @Test
    public void clickOnDrawer_showMainPageUi() {
        presenter.takeView(view);
        presenter.IntentToMainPage();
        verify(view, times(2)).showMainPage();
    }
}
