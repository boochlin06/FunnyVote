package com.heaton.funnyvote.data.user;

import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.retrofit.Server.UserDataQuery;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import okhttp3.ResponseBody;
import retrofit2.Callback;
import retrofit2.Response;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserDataRepositoryTest {
    private UserDataRepository userDataRepository;

    @Mock
    private UserDataSource remoteUserDataSource;
    @Mock
    private UserDataSource localUserDataSource;
    @Mock
    private UserDataSource.GetUserCallback getUserCallback;
    @Mock
    private UserDataSource.ChangeUserNameCallback changeUserNameCallback;
    @Mock
    private UserDataSource.RegisterUserCallback registerUserCallback;
    @Mock
    private UserDataSource.GetUserCodeCallback getUserCodeCallback;
    @Mock
    private UserDataSource.GetUserInfoCallback getUserInfoCallback;

    private static Response<UserDataQuery> response;


    private static User user;


    @Captor
    private ArgumentCaptor<UserDataRepository.GetUserCallback> getUserCallbackArgumentCaptor;

    @Captor
    private ArgumentCaptor<UserDataRepository.ChangeUserNameCallback> changeUserNameCallbackArgumentCaptor;
    @Captor
    private ArgumentCaptor<UserDataRepository.RegisterUserCallback> registerUserCallbackArgumentCaptor;
    @Captor
    private ArgumentCaptor<UserDataRepository.GetUserCodeCallback> getUserCodeCallbackArgumentCaptor;
    @Captor
    private ArgumentCaptor<UserDataRepository.GetUserInfoCallback> getUserInfoCallbackArgumentCaptor;
    @Captor
    private ArgumentCaptor<Callback> callbackArgumentCaptor;

    @Before
    public void setUpUserRepository() {
        MockitoAnnotations.initMocks(this);
        userDataRepository = UserDataRepository.Companion.getInstance(localUserDataSource, remoteUserDataSource);
        user = mock(User.class);
    }

    @After
    public void destroyRepositoryInstance() {
        UserDataRepository.Companion.destroyInstance();
    }

    @Test
    public void getUserWithRemoteDataSource_FirstTimeApiCall() {
        when(localUserDataSource.getUser()).thenReturn(user);
        when(user.getType()).thenReturn(User.TYPE_GUEST);
        when(user.getUserCode()).thenReturn("");

        userDataRepository.getUser(getUserCallback, true);
        verify(remoteUserDataSource).getGuestUserCode(getUserCodeCallbackArgumentCaptor.capture()
                , anyString());
        getUserCodeCallbackArgumentCaptor.getValue().onSuccess("userCode");

        verify(localUserDataSource).setUser(any(User.class));
        verify(getUserCallback).onResponse(any(User.class));
    }

    @Test
    public void getUserWithRemoteDataSourceFailure_FirstTimeApiCall() {
        when(localUserDataSource.getUser()).thenReturn(user);
        when(user.getType()).thenReturn(User.TYPE_GUEST);
        when(user.getUserCode()).thenReturn("");

        userDataRepository.getUser(getUserCallback, true);
        verify(remoteUserDataSource).getGuestUserCode(getUserCodeCallbackArgumentCaptor.capture()
                , anyString());
        getUserCodeCallbackArgumentCaptor.getValue().onFalure();

        verify(getUserCallback).onFailure();
    }

    @Test
    public void getUserWithRemoteDataSource_forceUpdateAfterFirstTimeApiCall() {
        when(localUserDataSource.getUser()).thenReturn(user);
        when(user.getType()).thenReturn(User.TYPE_GUEST);
        when(user.getUserCode()).thenReturn("UserCode");

        userDataRepository.getUser(getUserCallback, true);
        verify(remoteUserDataSource).getUserInfo(callbackArgumentCaptor.capture(), any(User.class));
        UserDataQuery userDataQuery = new UserDataQuery();
        userDataQuery.setGuestCode("guestCode");
        userDataQuery.setOtp("otp");
        response = Response.success(userDataQuery);
        callbackArgumentCaptor.getValue().onResponse(null, response);
        verify(localUserDataSource).setUser(any(User.class));
        verify(getUserCallback).onResponse(any(User.class));
    }

    @Test
    public void getUserWithRemoteDataSourceFailure_forceUpdateAfterFirstTimeApiCall() {
        when(localUserDataSource.getUser()).thenReturn(user);
        when(user.getType()).thenReturn(User.TYPE_GUEST);
        when(user.getUserCode()).thenReturn("UserCode");

        userDataRepository.getUser(getUserCallback, true);
        verify(remoteUserDataSource).getUserInfo(callbackArgumentCaptor.capture(), any(User.class));
        UserDataQuery userDataQuery = new UserDataQuery();
        userDataQuery.setGuestCode("guestCode");
        userDataQuery.setOtp("otp");
        response = Response.error(500, ResponseBody.create(null, "123"));
        callbackArgumentCaptor.getValue().onResponse(null, response);
        verify(getUserCallback).onFailure();
    }

    @Test
    public void getUser_requestUserFromLocalUserDataSource() {
        when(localUserDataSource.getUser()).thenReturn(user);
        when(user.getType()).thenReturn(User.TYPE_GUEST);
        when(user.getUserCode()).thenReturn("User Code");

        userDataRepository.getUser(getUserCallback, false);

        verify(remoteUserDataSource, never()).getGuestUserCode(
                getUserCodeCallbackArgumentCaptor.capture()
                , anyString());
        verify(getUserCallback).onResponse(any(User.class));
    }

    @Test
    public void registerUserMergeGuest_requestUserToThirdParty() {
        when(localUserDataSource.getUser()).thenReturn(user);
        when(user.getType()).thenReturn(User.TYPE_FACEBOOK);
        when(user.getUserCode()).thenReturn("User Code");

        userDataRepository.registerUser("appId", user, true, registerUserCallback);
        verify(localUserDataSource).getUser();
        verify(remoteUserDataSource).getUserCode(anyString(), anyString(), any(User.class)
                , getUserCodeCallbackArgumentCaptor.capture());
        getUserCodeCallbackArgumentCaptor.getValue().onSuccess("User Code");
        verify(remoteUserDataSource).linkGuestToLoginUser(anyString(), anyString(), callbackArgumentCaptor.capture());
        response = Response.success(null);
        callbackArgumentCaptor.getValue().onResponse(null, response);
        verify(localUserDataSource).setUser(any(User.class));
        verify(registerUserCallback).onSuccess();

    }

    @Test
    public void registerUserMergeGuestFailure_requestUserToThirdParty() {
        when(localUserDataSource.getUser()).thenReturn(user);
        when(user.getType()).thenReturn(User.TYPE_FACEBOOK);
        when(user.getUserCode()).thenReturn("User Code");

        userDataRepository.registerUser("appId", user, true, registerUserCallback);
        verify(localUserDataSource).getUser();
        verify(remoteUserDataSource).getUserCode(anyString(), anyString(), any(User.class)
                , getUserCodeCallbackArgumentCaptor.capture());
        getUserCodeCallbackArgumentCaptor.getValue().onSuccess("User Code");
        verify(remoteUserDataSource).linkGuestToLoginUser(anyString(), anyString(), callbackArgumentCaptor.capture());
        response = Response.error(500, ResponseBody.create(null, "123"));
        callbackArgumentCaptor.getValue().onResponse(null, response);
        verify(registerUserCallback).onFailure();
    }

    @Test
    public void registerUserNoMergeGuest_requestUserToThirdParty() {
        when(localUserDataSource.getUser()).thenReturn(user);
        when(user.getType()).thenReturn(User.TYPE_FACEBOOK);
        when(user.getUserCode()).thenReturn("User Code");

        userDataRepository.registerUser("appId", user, false, registerUserCallback);
        verify(localUserDataSource).getUser();
        verify(remoteUserDataSource).getUserCode(anyString(), anyString(), any(User.class)
                , getUserCodeCallbackArgumentCaptor.capture());
        getUserCodeCallbackArgumentCaptor.getValue().onSuccess("User Code");
        verify(remoteUserDataSource, never()).linkGuestToLoginUser(anyString(), anyString(), callbackArgumentCaptor.capture());

        verify(localUserDataSource).setUser(any(User.class));
        verify(registerUserCallback).onSuccess();

    }

    @Test
    public void changeCurrentUserName_requestToRemoteTest() {
        when(localUserDataSource.getUser()).thenReturn(user);
        when(user.getType()).thenReturn(User.TYPE_FACEBOOK);
        when(user.getUserCode()).thenReturn("User Code");
        userDataRepository.changeCurrentUserName("name", changeUserNameCallback);
        verify(remoteUserDataSource).getUserInfo(callbackArgumentCaptor.capture(), any(User.class));
        UserDataQuery userDataQuery = new UserDataQuery();
        userDataQuery.setGuestCode("guestCode");
        userDataQuery.setOtp("otp");
        response = Response.success(userDataQuery);
        callbackArgumentCaptor.getValue().onResponse(null, response);
        verify(localUserDataSource, times(1)).setUser(any(User.class));

        verify(remoteUserDataSource).changeUserName(callbackArgumentCaptor.capture(), anyString()
                , anyString(), anyString());
        response = Response.success(null);
        callbackArgumentCaptor.getValue().onResponse(null, response);
        verify(localUserDataSource, times(2)).setUser(any(User.class));
        verify(changeUserNameCallback).onSuccess();
    }

    @Test
    public void removeUser_onlyFromLocal() {
        userDataRepository.removeUser();
        verify(localUserDataSource).removeUser();
        verify(remoteUserDataSource, never()).removeUser();
    }


}
