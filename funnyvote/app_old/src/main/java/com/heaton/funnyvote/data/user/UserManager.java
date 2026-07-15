package com.heaton.funnyvote.data.user;

import android.content.Context;
import android.util.Log;

import com.heaton.funnyvote.R;
import com.heaton.funnyvote.Util;
import com.heaton.funnyvote.data.RemoteServiceApi;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.eventbus.EventBusManager;
import com.heaton.funnyvote.retrofit.Server;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by chiu_mac on 2016/12/6.
 */

public class UserManager {
    private static final String TAG = UserManager.class.getSimpleName();
    private static UserManager INSTANCE = null;

    private Context context;
    private UserDataSource userDataSource;
    private RemoteServiceApi remoteServiceApi;

    public static UserManager getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (UserManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new UserManager(context, new SPUserDataSource(context), new RemoteServiceApi());
                }
            }
        }
        return INSTANCE;
    }

    public UserManager(Context context, UserDataSource userDataSource, RemoteServiceApi remoteServiceApi) {
        this.context = context;
        this.userDataSource = userDataSource;
        this.remoteServiceApi = remoteServiceApi;
    }

    public void getUser(final GetUserCallback callback, boolean forceUpdateUserCode) {
        final User user = userDataSource.getUser();
        if (user.getType() == User.TYPE_GUEST && user.getUserCode().isEmpty()) {
            final String guestName = Util.randomUserName(context);
            Log.d(TAG, "Guest!" + user.getUserCode() + " name:" + guestName);
            remoteServiceApi.getGuestUserCode(new RemoteServiceApi.GetUserCodeCallback() {
                @Override
                public void onSuccess(String userCode) {
                    user.setUserName(guestName);
                    user.setUserCode(userCode);
                    userDataSource.setUser(user);
                    callback.onResponse(userDataSource.getUser());
                }

                @Override
                public void onFalure() {
                    callback.onFailure();
                }
            }, guestName);
        } else {
            if (forceUpdateUserCode) {
                getUserInfo(new Callback<Server.UserDataQuery>() {
                    @Override
                    public void onResponse(Call<Server.UserDataQuery> call, Response<Server.UserDataQuery> response) {
                        String userCode = null;
                        if (response.isSuccessful()) {
                            if (user.getType() == User.TYPE_GUEST) {
                                userCode = response.body().guestCode;
                            } else {
                                userCode = response.body().otp;
                            }
                            if (userCode != null) {
                                user.setUserCode(userCode);
                                userDataSource.setUser(user);
                                callback.onResponse(userDataSource.getUser());
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

    public void getUserInfo(final Callback<Server.UserDataQuery> callback, User user) {
        remoteServiceApi.getUserInfo(callback, user.getTokenType(), user.getUserCode());
    }

    public void registerUser(final User user, final boolean mergeGuest, final RegisterUserCallback callback) {
        String userType = "";
        String appId = "";
        switch (user.getType()) {
            case User.TYPE_FACEBOOK:
                userType = RemoteServiceApi.USER_TYPE_FACEBOOK;
                appId = context.getString(R.string.facebook_app_id);
                break;
            case User.TYPE_GOOGLE:
                userType = RemoteServiceApi.USER_TYPE_GOOGLE;
                appId = context.getString(R.string.google_app_id);
                break;
            case User.TYPE_TWITTER:
                userType = RemoteServiceApi.USER_TYPE_TWITTER;
                appId = context.getString(R.string.twitter_api_id);
            default:
        }
        if (!userType.isEmpty()) {
            final String guestCode = userDataSource.getUser().getUserCode();
            remoteServiceApi.getUserCode(userType, appId, user.getUserID(),
                    user.getUserName(), user.getEmail(), user.getUserIcon(), user.getGender(),
                    new RemoteServiceApi.GetUserCodeCallback() {
                        @Override
                        public void onSuccess(final String userCode) {
                            if (mergeGuest) {
                                remoteServiceApi.linkGuestToLoginUser(userCode, guestCode, new Callback<ResponseBody>() {
                                    @Override
                                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                        if (response.isSuccessful()) {
                                            user.setUserCode(userCode);
                                            userDataSource.setUser(user);
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
                                userDataSource.setUser(user);
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

    private void changeUserName(final User user, final String newName, final ChangeUserNameCallback callback) {
        remoteServiceApi.changeUserName(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    user.setUserName(newName);
                    userDataSource.setUser(user);
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

    public class getPersonalInfoResponseCallback implements Callback<User> {

        private String userCode;
        private String userCodeType;


        public getPersonalInfoResponseCallback(String userCode, String userCodeType) {
            this.userCode = userCode;
            this.userCodeType = userCodeType;
        }

        @Override
        public void onResponse(Call<User> call, Response<User> response) {
            if (response.isSuccessful()) {
                User personal = response.body();
                personal.setUserCode(userCode);
                personal.personalTokenType = userCodeType;
                EventBus.getDefault().post(new EventBusManager.RemoteServiceEvent(
                        EventBusManager.RemoteServiceEvent.GET_PERSONAL_INFO, true
                        , personal));
            } else {
                String errorMessage = "";
                try {
                    errorMessage = response.errorBody().string();
                    Log.d(TAG, "getPersonalInfoResponseCallback onResponse false, error message:"
                            + errorMessage + " , user code:" + userCode);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                EventBus.getDefault().post(new EventBusManager.RemoteServiceEvent(
                        EventBusManager.RemoteServiceEvent.GET_PERSONAL_INFO, false
                        , errorMessage));
            }
        }

        @Override
        public void onFailure(Call<User> call, Throwable t) {
            Log.d(TAG, "getPersonalInfoResponseCallback onResponse onFailure, error message:"
                    + t.getMessage() + " user code:" + userCode);
            EventBus.getDefault().post(new EventBusManager.RemoteServiceEvent(
                    EventBusManager.RemoteServiceEvent.GET_PERSONAL_INFO, false
                    , t.getMessage()));
        }
    }

    public void unregisterUser() {
        userDataSource.removeUser();
    }

    public interface GetUserCallback {
        void onResponse(User user);

        void onFailure();
    }

    public interface GetUserInfoCallback {
        void onResponse(Server.UserDataQuery userData);

        void onFailure();
    }

    public interface RegisterUserCallback {
        void onSuccess();

        void onFailure();
    }

    public interface ChangeUserNameCallback {
        void onSuccess();

        void onFailure();
    }
}
