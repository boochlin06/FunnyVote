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
import rx.Observable;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserDataRepositoryTest {
//    private UserDataRepository userDataRepository;
//
//    @Mock
//    private UserDataSource remoteUserDataSource;
//    @Mock
//    private UserDataSource localUserDataSource;
//
//    private static Response<UserDataQuery> response;
//
//
//    private static User user;
//
//    @Before
//    public void setUpUserRepository() {
//        MockitoAnnotations.initMocks(this);
//        userDataRepository = UserDataRepository.getInstance(localUserDataSource, remoteUserDataSource);
//        user = mock(User.class);
//    }
//
//    @After
//    public void destroyRepositoryInstance() {
//        UserDataRepository.destroyInstance();
//    }
//
//    @Test
//    public void getUserWithRemoteDataSource_FirstTimeApiCall() {
//        when(userDataRepository.getUser(anyBoolean())).thenReturn(rx.Observable.just(user));
//        when(localUserDataSource.getUser()).thenReturn(user);
//        when(user.getType()).thenReturn(User.TYPE_GUEST);
//        when(user.getUserCode()).thenReturn("");
//
//        userDataRepository.getUser(true);
//        verify(remoteUserDataSource).getGuestUserCode(anyString());
//        //getUserCodeCallbackArgumentCaptor.getValue().onSuccess("userCode");
//
//        verify(localUserDataSource).setUser(any(User.class));
//        //verify(getUserCallback).onResponse(any(User.class));
//    }
//
//    @Test
//    public void getUserWithRemoteDataSourceFailure_FirstTimeApiCall() {
//        when(userDataRepository.getUser(anyBoolean())).thenReturn(rx.Observable.error(new Exception("test error")));
//        when(localUserDataSource.getUser()).thenReturn(user);
//        when(user.getType()).thenReturn(User.TYPE_GUEST);
//        when(user.getUserCode()).thenReturn("");
//
//        userDataRepository.getUser(true);
//        verify(remoteUserDataSource).getGuestUserCode(anyString());
//        //getUserCodeCallbackArgumentCaptor.getValue().onFalure();
//
//        //verify(getUserCallback).onFailure();
//    }
//
//    @Test
//    public void getUserWithRemoteDataSource_forceUpdateAfterFirstTimeApiCall() {
//        when(userDataRepository.getUser(anyBoolean())).thenReturn(rx.Observable.just(user));
//        when(localUserDataSource.getUser()).thenReturn(user);
//        when(user.getType()).thenReturn(User.TYPE_GUEST);
//        when(user.getUserCode()).thenReturn("UserCode");
//
//        userDataRepository.getUser(true);
//        verify(remoteUserDataSource).getUserInfo(any(User.class));
//        UserDataQuery userDataQuery = new UserDataQuery();
//        userDataQuery.guestCode = "guestCode";
//        userDataQuery.otp = "otp";
//        response = Response.success(userDataQuery);
//        //callbackArgumentCaptor.getValue().onResponse(null, response);
//        verify(localUserDataSource).setUser(any(User.class));
//        //verify(getUserCallback).onResponse(any(User.class));
//    }
//
//    @Test
//    public void getUserWithRemoteDataSourceFailure_forceUpdateAfterFirstTimeApiCall() {
//
//        when(userDataRepository.getUser(anyBoolean())).thenReturn(rx.Observable.just(user));
//        when(localUserDataSource.getUser()).thenReturn(user);
//        when(user.getType()).thenReturn(User.TYPE_GUEST);
//        when(user.getUserCode()).thenReturn("UserCode");
//
//        userDataRepository.getUser(true);
//        verify(remoteUserDataSource).getUserInfo( any(User.class));
//        UserDataQuery userDataQuery = new UserDataQuery();
//        userDataQuery.guestCode = "guestCode";
//        userDataQuery.otp = "otp";
//        response = Response.error(500, ResponseBody.create(null, "123"));
//        //callbackArgumentCaptor.getValue().onResponse(null, response);
//        //verify(getUserCallback).onFailure();
//    }
//
//    @Test
//    public void getUser_requestUserFromLocalUserDataSource() {
//        when(userDataRepository.registerUser(anyString(), any(), anyBoolean())).thenReturn(rx.Observable.just(user));
//        when(localUserDataSource.getUser()).thenReturn(user);
//        when(user.getType()).thenReturn(User.TYPE_GUEST);
//        when(user.getUserCode()).thenReturn("User Code");
//
//        userDataRepository.getUser(false);
//
//        verify(remoteUserDataSource, never()).getGuestUserCode(anyString());
//        //verify(getUserCallback).onResponse(any(User.class));
//    }
//
//    @Test
//    public void registerUserMergeGuest_requestUserToThirdParty() {
//        when(userDataRepository.registerUser(anyString(), any(), anyBoolean())).thenReturn(rx.Observable.just(user));
//        when(localUserDataSource.getUser()).thenReturn(user);
//        when(user.getType()).thenReturn(User.TYPE_FACEBOOK);
//        when(user.getUserCode()).thenReturn("User Code");
//
//        userDataRepository.registerUser("appId", user, true);
//        verify(localUserDataSource).getUser();
//        verify(remoteUserDataSource).getUserCode(anyString(), anyString(), any(User.class));
//        //getUserCodeCallbackArgumentCaptor.getValue().onSuccess("User Code");
//        verify(remoteUserDataSource).linkGuestToLoginUser(anyString(), anyString());
//        response = Response.success(null);
//        //callbackArgumentCaptor.getValue().onResponse(null, response);
//        verify(localUserDataSource).setUser(any(User.class));
//        //verify(registerUserCallback).onSuccess();
//
//    }
//
//    @Test
//    public void registerUserMergeGuestFailure_requestUserToThirdParty() {
//        when(userDataRepository.registerUser(anyString(), any(), anyBoolean())).thenReturn(rx.Observable.just(user));
//        when(localUserDataSource.getUser()).thenReturn(user);
//        when(user.getType()).thenReturn(User.TYPE_FACEBOOK);
//        when(user.getUserCode()).thenReturn("User Code");
//
//        userDataRepository.registerUser("appId", user, true);
//        verify(localUserDataSource).getUser();
//        verify(remoteUserDataSource).getUserCode(anyString(), anyString(), any(User.class));
//        //getUserCodeCallbackArgumentCaptor.getValue().onSuccess("User Code");
//        verify(remoteUserDataSource).linkGuestToLoginUser(anyString(), anyString());
//        response = Response.error(500, ResponseBody.create(null, "123"));
//        //callbackArgumentCaptor.getValue().onResponse(null, response);
//        //verify(registerUserCallback).onFailure();
//    }
//
//    @Test
//    public void registerUserNoMergeGuest_requestUserToThirdParty() {
//        when(userDataRepository.registerUser(anyString(), any(), anyBoolean())).thenReturn(rx.Observable.just(user));
//        when(localUserDataSource.getUser()).thenReturn(user);
//        when(user.getType()).thenReturn(User.TYPE_FACEBOOK);
//        when(user.getUserCode()).thenReturn("User Code");
//
//        userDataRepository.registerUser("appId", user, false);
//        verify(localUserDataSource).getUser();
//        verify(remoteUserDataSource).getUserCode(anyString(), anyString(), any(User.class));
//        //getUserCodeCallbackArgumentCaptor.getValue().onSuccess("User Code");
//        verify(remoteUserDataSource, never()).linkGuestToLoginUser(anyString(), anyString());
//
//        verify(localUserDataSource).setUser(any(User.class));
//        //verify(registerUserCallback).onSuccess();
//
//    }
//
//    @Test
//    public void changeCurrentUserName_requestToRemoteTest() {
//        when(userDataRepository.changeCurrentUserName(anyString())).thenReturn(Observable.just(true));
//        when(localUserDataSource.getUser()).thenReturn(user);
//        when(user.getType()).thenReturn(User.TYPE_FACEBOOK);
//        when(user.getUserCode()).thenReturn("User Code");
//        userDataRepository.changeCurrentUserName("name");
//        verify(remoteUserDataSource).getUserInfo(any(User.class));
//        UserDataQuery userDataQuery = new UserDataQuery();
//        userDataQuery.guestCode = "guestCode";
//        userDataQuery.otp = "otp";
//        response = Response.success(userDataQuery);
//        //callbackArgumentCaptor.getValue().onResponse(null, response);
//        verify(localUserDataSource, times(1)).setUser(any(User.class));
//
//        verify(remoteUserDataSource).changeUserName( anyString()
//                , anyString(), anyString());
//        response = Response.success(null);
//        //callbackArgumentCaptor.getValue().onResponse(null, response);
//        verify(localUserDataSource, times(2)).setUser(any(User.class));
//        //verify(changeUserNameCallback).onSuccess();
//    }
//
//    @Test
//    public void removeUser_onlyFromLocal() {
//        userDataRepository.removeUser();
//        verify(localUserDataSource).removeUser();
//        verify(remoteUserDataSource, never()).removeUser();
//    }


}
