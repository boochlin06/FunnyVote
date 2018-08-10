package com.heaton.funnyvote;

import com.heaton.funnyvote.data.user.UserDataRepository;
import com.heaton.funnyvote.data.user.UserDataSource;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.ui.votedetail.OptionItemAdapter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
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
    private MainPageContract.View view;

    private MainPagePresenter presenter;
    @Captor
    private ArgumentCaptor<UserDataSource.GetUserCallback> getUserCallbackArgumentCaptor;

    @Before
    public void setupMainPresenter() {
        MockitoAnnotations.initMocks(this);

        presenter = new MainPagePresenter(userDataRepository, view);
        user = mock(User.class);
        when(user.getUserName()).thenReturn("Heaton");
    }

    @Test
    public void createPresenter_setsThePresenterToView() {
        // Get a reference to the class under test
        presenter = new MainPagePresenter(userDataRepository, view);

        // Then the presenter is set to the view
        verify(view).setPresenter(presenter);
    }

    @Test
    public void loadUserFromRepositoryAndLoadIntoView() {
        presenter.start();

        verify(userDataRepository).getUser(getUserCallbackArgumentCaptor.capture(), eq(false));
        getUserCallbackArgumentCaptor.getValue().onResponse(user);

        ArgumentCaptor<User> updateUserArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(view).updateUserView(updateUserArgumentCaptor.capture());
        assertTrue(updateUserArgumentCaptor.getValue().getUserName().length() > 0);
    }

    @Test
    public void clickOnToolbar_showCreateVoteUi() {
        presenter.IntentToCreatePage();
        verify(view).showCreatePage();
    }

    @Test
    public void clickOnToolbar_showSearchUi() {
        presenter.IntentToSearchPage("");
        verify(view).showSearchPage("");
    }

    @Test
    public void clickOnDrawer_showUserBoxUi() {
        presenter.IntentToUserPage();
        verify(view).showUserPage();
    }

    @Test
    public void clickOnDrawer_showAccountUi() {
        presenter.IntentToAccountPage();
        verify(view).showAccountPage();
    }

    @Test
    public void clickOnDrawer_showAboutUi() {
        presenter.IntentToAboutPage();
        verify(view).showAboutPage();
    }

    @Test
    public void clickOnUserIcon_showUserBoxUi() {
        presenter.IntentToUserPage();
        verify(view).showUserPage();
    }

    @Test
    public void clickOnDrawer_showMainPageUi() {
        presenter.IntentToMainPage();
        verify(view).showMainPage();
    }
}
