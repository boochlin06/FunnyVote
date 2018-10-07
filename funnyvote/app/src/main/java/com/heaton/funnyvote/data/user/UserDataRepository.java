package com.heaton.funnyvote.data.user;

import android.util.Log;

import com.heaton.funnyvote.data.RemoteServiceApi;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.retrofit.Server;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Callback;
import rx.Observable;
import rx.functions.Func1;

public class UserDataRepository implements UserDataSource {

    private static final String TAG = UserDataRepository.class.getSimpleName();
    private static UserDataRepository INSTANCE = null;

    private UserDataSource localUserDataSource;
    private UserDataSource remoteUserSource;

    public static UserDataRepository getInstance(UserDataSource localuserDataSource
            , UserDataSource remoteUserSource) {
        if (INSTANCE == null) {
            synchronized (UserDataRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new UserDataRepository(localuserDataSource
                            , remoteUserSource);
                }
            }
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

    public UserDataRepository(UserDataSource userDataSource, UserDataSource remoteUserSource) {
        this.localUserDataSource = userDataSource;
        this.remoteUserSource = remoteUserSource;
    }


    @Override
    public Observable<User> getUser(boolean forceUpdateUserCode) {
        final User user = localUserDataSource.getUser();
        if (user.getType() == User.TYPE_GUEST && user.getUserCode().isEmpty()) {
            final String guestName = "Guest" + (int) (Math.random() * 1000);//Util.randomUserName(context);
            Log.d(TAG, "Guest!" + user.getUserCode() + " name:" + guestName);
            return remoteUserSource.getGuestUserCode(guestName)
                    .flatMap((Func1<String, Observable<User>>) userCode -> {
                        user.setUserName(guestName);
                        user.setUserCode(userCode);
                        localUserDataSource.setUser(user);
                        return Observable.just(localUserDataSource.getUser());
                    });
        } else {
            if (forceUpdateUserCode) {
                return remoteUserSource.getUserInfo(user)
                        .flatMap((Func1<Server.UserDataQuery, Observable<User>>) userDataQuery -> {
                            String userCode = "";
                            if (user.getType() == User.TYPE_GUEST) {
                                userCode = userDataQuery.guestCode;
                            } else {
                                userCode = userDataQuery.otp;
                            }
                            if (userCode != null) {
                                user.setUserCode(userCode);
                                localUserDataSource.setUser(user);
                                return Observable.just(localUserDataSource.getUser());
                            }
                            return Observable.error(new IOException());
                        });
            } else {
                return Observable.just(user);
            }
        }
    }

    @Override
    public Observable registerUser(String appId, User user, boolean mergeGuest) {
        String userType = "";
        switch (user.getType()) {
            case User.TYPE_FACEBOOK:
                userType = RemoteServiceApi.USER_TYPE_FACEBOOK;
                break;
            case User.TYPE_GOOGLE:
                userType = RemoteServiceApi.USER_TYPE_GOOGLE;
                break;
            case User.TYPE_TWITTER:
                userType = RemoteServiceApi.USER_TYPE_TWITTER;
            default:
        }
        if (!userType.isEmpty()) {
            final String guestCode = localUserDataSource.getUser().getUserCode();
            return remoteUserSource.getUserCode(userType, appId, user)
                    .flatMap(new Func1<String, Observable<?>>() {
                        @Override
                        public Observable<?> call(String userCode) {
                            if (mergeGuest) {
                                return remoteUserSource.linkGuestToLoginUser(userCode, guestCode)
                                        .map(new Func1<ResponseBody, Object>() {
                                            @Override
                                            public Object call(ResponseBody responseBody) {
                                                user.setUserCode(userCode);
                                                localUserDataSource.setUser(user);
                                                return Observable.just(userCode);
                                            }
                                        });
                            } else {
                                user.setUserCode(userCode);
                                localUserDataSource.setUser(user);
                                return Observable.just(userCode);
                            }
                        }
                    });
        } else {
            Observable.error(new Exception("registerUser onFailure"));
            Log.e(TAG, "registerUser onFailure");
        }
        return null;
    }

    @Override
    public void unregisterUser() {
        localUserDataSource.removeUser();
    }


    @Override
    public Observable<String> getUserCode(String userType, String appId, User user) {
        return remoteUserSource.getUserCode(userType, appId, user);
    }

    @Override
    public void linkGuestToLoginUser(String otp, String guest, Callback<ResponseBody> callback) {
        remoteUserSource.linkGuestToLoginUser(otp, guest, callback);
    }

    @Override
    public Observable<ResponseBody> linkGuestToLoginUser(String otp, String guest) {
        return null;
    }

    @Override
    public void changeUserName(Callback<ResponseBody> callback, String tokenType, String token, String name) {
        // only for remote
    }

    @Override
    public Observable<ResponseBody> changeUserName(String tokenType, String token, String name) {
        // only for remote
        return Observable.empty();
    }

    @Override
    public Observable changeCurrentUserName(String name) {
        return getUser(false)
                .flatMap(new Func1<User, Observable<ResponseBody>>() {
                    @Override
                    public Observable<ResponseBody> call(User user) {
                        user.setUserName(name);
                        localUserDataSource.setUser(user);
                        return remoteUserSource.changeUserName(user.getTokenType(), user.getUserCode(), name);
                    }
                });
    }

    @Override
    public User getUser() {
        return localUserDataSource.getUser();
    }

    @Override
    public void setUser(User user) {
        localUserDataSource.setUser(user);
    }

    @Override
    public void removeUser() {
        localUserDataSource.removeUser();
    }

    @Override
    public Observable<String> getGuestUserCode(String name) {
        return null;
    }

    @Override
    public void getUserInfo(Callback<Server.UserDataQuery> callback, User user) {
        remoteUserSource.getUserInfo(callback, user);
    }

    @Override
    public Observable<Server.UserDataQuery> getUserInfo(User user) {
        return remoteUserSource.getUserInfo(user);
    }

    @Override
    public void setGuestName(String guestName) {

    }
}
