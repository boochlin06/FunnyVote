package com.heaton.funnyvote.ui.account;

import com.heaton.funnyvote.data.user.UserDataRepository;
import com.heaton.funnyvote.data.user.UserDataSource;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.database.VoteData;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AccountPresenterTest {

    private static User user;
    private static final String voteCode = "CODE_123";
    private static final VoteData voteData = new VoteData();
    private static List<VoteData> voteDataList = new ArrayList<>();

    private AccountPresenter presenter;

    @Mock
    private AccountContract.View view;

    @Mock
    private UserDataRepository userDataRepository;
    @Captor
    private ArgumentCaptor<UserDataSource.GetUserCallback> getUserCallbackArgumentCaptor;
    @Captor
    private ArgumentCaptor<UserDataSource.RegisterUserCallback> registerUserCallbackArgumentCaptor;
    @Captor
    private ArgumentCaptor<UserDataSource.ChangeUserNameCallback> changeUserNameCallbackArgumentCaptor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        user = mock(User.class);
        when(user.getUserName()).thenReturn("Heaton");
    }

    @Test
    public void createPresenter_setsThePresenterToView() {
        // Get a reference to the class under test
        presenter = new AccountPresenter(userDataRepository
                , view);

        // Then the presenter is set to the userPageView
        verify(view).setPresenter(presenter);
    }

    @Test
    public void getUserFromRepositoryAndUpdateToView() {
        presenter = new AccountPresenter(userDataRepository
                , view);
        presenter.start();
        verify(userDataRepository).getUser(getUserCallbackArgumentCaptor.capture(), eq(false));
        getUserCallbackArgumentCaptor.getValue().onResponse(user);
        verify(view).showUser(any(User.class));
    }

    @Test
    public void logoutUserAndUpdateToView() {
        presenter = new AccountPresenter(userDataRepository
                , view);

        when(user.getType()).thenReturn(User.TYPE_FACEBOOK);
        presenter.setUser(user);
        presenter.logout();
        verify(view).facebookLogout();

        when(user.getType()).thenReturn(User.TYPE_GOOGLE);
        presenter.setUser(user);
        presenter.logout();
        verify(view).googleSignOut();

        when(user.getType()).thenReturn(User.TYPE_TWITTER);
        presenter.setUser(user);
        presenter.logout();
        verify(view).twitterlogout();
    }

    @Test
    public void loginUserAndUpdateToView() {
        presenter = new AccountPresenter(userDataRepository
                , view);
        presenter.login(AccountPresenter.LOGIN_FB, true);
        verify(view).facebookLogin();

        presenter.login(AccountPresenter.LOGIN_GOOGLE, true);
        verify(view).googleSignIn();

        presenter.login(AccountPresenter.LOGIN_TWITTER, true);
        verify(view).twitterLogin();
    }

    @Test
    public void registerUserAndUpdateToView() {
        presenter = new AccountPresenter(userDataRepository
                , view);
        presenter.registerUser(user, "appId");
        verify(userDataRepository).registerUser(anyString(), any(User.class)
                , anyBoolean(), registerUserCallbackArgumentCaptor.capture());
        registerUserCallbackArgumentCaptor.getValue().onSuccess();
        verify(userDataRepository).getUser(getUserCallbackArgumentCaptor.capture(), anyBoolean());
        getUserCallbackArgumentCaptor.getValue().onResponse(user);
        verify(view).showUser(any(User.class));
    }

    @Test
    public void registerUserFailureAndUpdateToView() {
        presenter = new AccountPresenter(userDataRepository
                , view);
        presenter.registerUser(user, "appId");
        verify(userDataRepository).registerUser(anyString(), any(User.class)
                , anyBoolean(), registerUserCallbackArgumentCaptor.capture());
        registerUserCallbackArgumentCaptor.getValue().onFailure();
        verify(userDataRepository).getUser(getUserCallbackArgumentCaptor.capture(), anyBoolean());
        getUserCallbackArgumentCaptor.getValue().onFailure();
        verify(view).showLoginView(anyString());
    }

    @Test
    public void changeUserNameFailureAndUpdateToView() {
        presenter = new AccountPresenter(userDataRepository
                , view);
        presenter.changeCurrentUserName("new name");
        verify(userDataRepository).changeCurrentUserName(anyString(), changeUserNameCallbackArgumentCaptor.capture());
        changeUserNameCallbackArgumentCaptor.getValue().onSuccess();
        verify(userDataRepository).getUser(getUserCallbackArgumentCaptor.capture(), anyBoolean());
        getUserCallbackArgumentCaptor.getValue().onFailure();
        verify(view).showLoginView(anyString());
    }

    @Test
    public void unregisterUser() {
        presenter = new AccountPresenter(userDataRepository
                , view);
        presenter.unregisterUser();
        verify(userDataRepository).unregisterUser();
    }

}
