package com.heaton.funnyvote.ui.account;

import com.heaton.funnyvote.data.user.UserDataRepository;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.database.VoteData;
import com.heaton.funnyvote.utils.schedulers.BaseSchedulerProvider;
import com.heaton.funnyvote.utils.schedulers.ImmediateSchedulerProvider;

import org.junit.Before;
import org.junit.Test;
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
    private BaseSchedulerProvider schedulerProvider;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        schedulerProvider = new ImmediateSchedulerProvider();
        user = mock(User.class);
        when(user.getUserName()).thenReturn("Heaton");
    }

    @Test
    public void createPresenter_setsThePresenterToView() {
        // Get a reference to the class under test
        presenter = new AccountPresenter(userDataRepository
                , view, schedulerProvider);

        // Then the presenter is set to the userPageView
        verify(view).setPresenter(presenter);
    }

    @Test
    public void getUserFromRepositoryAndUpdateToView() {
        when(userDataRepository.getUser(eq(false))).thenReturn(rx.Observable.just(user));
        presenter = new AccountPresenter(userDataRepository
                , view, schedulerProvider);
        presenter.subscribe();
        verify(userDataRepository).getUser(eq(false));
        verify(view).showUser(any(User.class));
    }

    @Test
    public void logoutUserAndUpdateToView() {
        when(userDataRepository.getUser(eq(false))).thenReturn(rx.Observable.just(user));
        presenter = new AccountPresenter(userDataRepository
                , view, schedulerProvider);

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
                , view, schedulerProvider);
        presenter.login(AccountPresenter.LOGIN_FB, true);
        verify(view).facebookLogin();

        presenter.login(AccountPresenter.LOGIN_GOOGLE, true);
        verify(view).googleSignIn();

        presenter.login(AccountPresenter.LOGIN_TWITTER, true);
        verify(view).twitterLogin();
    }

    @Test
    public void registerUserAndUpdateToView() {
        when(userDataRepository.registerUser(anyString(), any(User.class)
                , anyBoolean())).thenReturn(rx.Observable.just(true));
        when(userDataRepository.getUser(anyBoolean()))
                .thenReturn(rx.Observable.just(user));
        presenter = new AccountPresenter(userDataRepository
                , view, schedulerProvider);
        presenter.registerUser(user, "appId");
        verify(userDataRepository).registerUser(anyString(), any(User.class)
                , anyBoolean());
        //registerUserCallbackArgumentCaptor.getValue().onSuccess();
        verify(userDataRepository).getUser(anyBoolean());
        //getUserCallbackArgumentCaptor.getValue().onResponse(user);
        verify(view).showUser(any(User.class));
    }

    @Test
    public void registerUserFailureAndUpdateToView() {
        when(userDataRepository.registerUser(anyString(), any(User.class)
                , anyBoolean())).thenReturn(rx.Observable.error(new Exception("test error")));
        when(userDataRepository.getUser(anyBoolean()))
                .thenReturn(rx.Observable.error(new Exception("test error")));
        presenter = new AccountPresenter(userDataRepository
                , view, schedulerProvider);
        presenter.registerUser(user, "appId");
        verify(userDataRepository).registerUser(anyString(), any(User.class), anyBoolean());
        verify(userDataRepository).getUser(anyBoolean());
        verify(view).showLoginView(anyString());
    }

    @Test
    public void changeUserNameFailureAndUpdateToView() {
        when(userDataRepository.changeCurrentUserName(anyString()))
                .thenReturn(rx.Observable.just(user));
        when(userDataRepository.getUser(anyBoolean()))
                .thenReturn(rx.Observable.error(new Exception("test error")));
        presenter = new AccountPresenter(userDataRepository
                , view, schedulerProvider);
        presenter.changeCurrentUserName("new name");
        verify(userDataRepository).changeCurrentUserName(anyString());
        verify(userDataRepository).getUser(anyBoolean());
        verify(view).showLoginView(anyString());
    }

    @Test
    public void unregisterUser() {
        presenter = new AccountPresenter(userDataRepository
                , view, schedulerProvider);
        presenter.unregisterUser();
        verify(userDataRepository).unregisterUser();
    }

}
