package com.heaton.funnyvote.data.user;

import android.util.Log;

import com.heaton.funnyvote.data.Local;
import com.heaton.funnyvote.data.Remote;
import com.heaton.funnyvote.data.RemoteServiceApi;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.retrofit.Server;

import java.io.IOException;

import javax.inject.Inject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserDataRepository implements UserDataSource {

    private static final String TAG = UserDataRepository.class.getSimpleName();
    private static UserDataRepository INSTANCE = null;

    private UserDataSource localUserDataSource;
    private UserDataSource remoteUserSource;

    public static UserDataRepository getInstance(UserDataSource localUserDataSource
            , UserDataSource remoteUserSource) {
        if (INSTANCE == null) {
            synchronized (UserDataRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new UserDataRepository(localUserDataSource
                            , remoteUserSource);
                }
            }
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

    @Inject
    public UserDataRepository(@Local UserDataSource userDataSource, @Remote UserDataSource remoteUserSource) {
        this.localUserDataSource = userDataSource;
        this.remoteUserSource = remoteUserSource;
    }

    @Override
    public void getUser(final GetUserCallback callback, boolean forceUpdateUserCode) {
        final User user = localUserDataSource.getUser();
        if (user.getType() == User.TYPE_GUEST && user.getUserCode().isEmpty()) {
            final String guestName = "Guest" + (int) (Math.random() * 1000);//Util.randomUserName(context);
            Log.d(TAG, "Guest!" + user.getUserCode() + " name:" + guestName);
            remoteUserSource.getGuestUserCode(new GetUserCodeCallback() {
                @Override
                public void onSuccess(String userCode) {
                    user.setUserName(guestName);
                    user.setUserCode(userCode);
                    localUserDataSource.setUser(user);
                    callback.onResponse(localUserDataSource.getUser());
                }

                @Override
                public void onFalure() {
                    callback.onFailure();
                }
            }, guestName);
        } else {
            if (forceUpdateUserCode) {
                remoteUserSource.getUserInfo(new Callback<Server.UserDataQuery>() {
                    @Override
                    public void onResponse(Call<Server.UserDataQuery> call
                            , Response<Server.UserDataQuery> response) {
                        String userCode = null;
                        if (response.isSuccessful()) {
                            if (user.getType() == User.TYPE_GUEST) {
                                userCode = response.body().guestCode;
                            } else {
                                userCode = response.body().otp;
                            }
                            if (userCode != null) {
                                user.setUserCode(userCode);
                                localUserDataSource.setUser(user);
                                callback.onResponse(localUserDataSource.getUser());
                            }
                        } else {
                            String errorMessage = "";
                            try {
                                errorMessage = response.errorBody().string();
                                Log.e(TAG, "getUser onResponse false" + errorMessage);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            callback.onFailure();
                        }
                    }

                    @Override
                    public void onFailure(Call<Server.UserDataQuery> call, Throwable t) {
                        callback.onFailure();
                    }
                }, user);
            } else {
                callback.onResponse(user);
            }
        }
    }


    public void registerUser(final String appId, final User user, final boolean mergeGuest, final RegisterUserCallback callback) {
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
            remoteUserSource.getUserCode(userType, appId, user, new GetUserCodeCallback() {
                @Override
                public void onSuccess(final String userCode) {

                    if (mergeGuest) {
                        remoteUserSource.linkGuestToLoginUser(userCode, guestCode, new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                if (response.isSuccessful()) {
                                    user.setUserCode(userCode);
                                    localUserDataSource.setUser(user);
                                    callback.onSuccess();
                                } else {
                                    callback.onFailure();
                                    try {
                                        Log.e(TAG, "registerUser" + response.errorBody().string());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                callback.onFailure();
                                Log.e(TAG, "registerUser onFailure , error message:" + t.getMessage());
                            }
                        });
                    } else {
                        user.setUserCode(userCode);
                        localUserDataSource.setUser(user);
                        callback.onSuccess();
                    }
                }

                @Override
                public void onFalure() {
                    callback.onFailure();
                    Log.e(TAG, "registerUser onFailure");
                }
            });

        } else {
            callback.onFailure();
            Log.e(TAG, "registerUser onFailure");
        }
    }

    @Override
    public void unregisterUser() {
        localUserDataSource.removeUser();
    }

    @Override
    public void getUserCode(String userType, String appId, User user, GetUserCodeCallback callback) {
        remoteUserSource.getUserCode(userType, appId, user, callback);
    }

    @Override
    public void linkGuestToLoginUser(String otp, String guest, Callback<ResponseBody> callback) {
        remoteUserSource.linkGuestToLoginUser(otp, guest, callback);
    }

    @Override
    public void changeUserName(Callback<ResponseBody> callback, String tokenType, String token, String name) {
        // only for remote
    }

    private void changeUserName(final User user, final String newName, final ChangeUserNameCallback callback) {
        remoteUserSource.changeUserName(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    user.setUserName(newName);
                    localUserDataSource.setUser(user);
                    callback.onSuccess();
                } else {
                    try {
                        Log.d(TAG, "changeUserName response status:" + response.code() + " ,message:" + response.errorBody().string());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    callback.onFailure();
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                callback.onFailure();
            }
        }, user.getTokenType(), user.getUserCode(), newName);
    }

    @Override
    public void changeCurrentUserName(final String name, final ChangeUserNameCallback callback) {
        getUser(new GetUserCallback() {
            @Override
            public void onResponse(User user) {
                changeUserName(user, name, callback);
            }

            @Override
            public void onFailure() {
                Log.w(TAG, "changeUserName Fail");
            }
        }, true);
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
    public void getGuestUserCode(GetUserCodeCallback callback, String name) {
        remoteUserSource.getGuestUserCode(callback, name);
    }

    @Override
    public void getUserInfo(Callback<Server.UserDataQuery> callback, User user) {
        remoteUserSource.getUserInfo(callback, user);
    }

    @Override
    public void setGuestName(String guestName) {

    }
}
