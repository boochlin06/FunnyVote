package com.android.heaton.funnyvote.data.user;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.android.heaton.funnyvote.R;
import com.android.heaton.funnyvote.data.RemoteServiceApi;
import com.android.heaton.funnyvote.database.User;
import com.android.heaton.funnyvote.eventbus.EventBusController;

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

    public void getUser(final GetUserCallback callback) {
        final User user = userDataSource.getUser();
        if (user.getType() == User.TYPE_GUEST && user.getUserCode().isEmpty()) {
            Log.d(TAG, "Guest!" + user.getUserCode());
            String guestName = context.getString(R.string.account_default_name);
            remoteServiceApi.getGuestUserCode(new RemoteServiceApi.GetUserCodeCallback() {
                @Override
                public void onSuccess(String userCode) {
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
            callback.onResponse(user);
        }
    }

    public void getPersonalInfo(@NonNull String userCode, String userCodeType) {
        if (userCode == null) {
            EventBus.getDefault().post(new EventBusController.RemoteServiceEvent(
                    EventBusController.RemoteServiceEvent.GET_PERSONAL_INFO
                    , false, "No user code"
            ));
        } else {
            remoteServiceApi.getPersonalInfo(userCode, userCodeType
                    , new UserManager.getPersonalInfoResponseCallback(userCode, userCodeType));
        }
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
                                        user.setUserCode(userCode);
                                        userDataSource.setUser(user);
                                        callback.onSuccess();
                                    }

                                    @Override
                                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                                        callback.onFailure();
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
                        }
                    });
        } else {
            callback.onFailure();
        }
    }

    public void changeCurrentUserName(final String name, final ChangeUserNameCallback callback) {
        getUser(new GetUserCallback() {
            @Override
            public void onResponse(User user) {
                if (user.getType() == User.TYPE_GUEST) {
                    changeGuestUserName(user, name, callback);
                } else {
                    changeLoginUserName(user, name, callback);
                }
            }

            @Override
            public void onFailure() {
                Log.w(TAG, "changeUserName Fail");
            }
        });
    }

    private void changeGuestUserName(final User user, final String newName, final ChangeUserNameCallback callback) {
        remoteServiceApi.changeGuestUserName(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    user.setUserName(newName);
                    userDataSource.setUser(user);
                    callback.onSuccess();
                } else {
                    callback.onFailure();
                }
                Log.d(TAG, "changeGuestUserName response status:" + response.code());
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                callback.onFailure();
            }
        }, user.getUserCode(), newName);
    }

    private void changeLoginUserName(final User user, final String newName, final ChangeUserNameCallback callback) {
        remoteServiceApi.changeLoginUserName(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    user.setUserName(newName);
                    userDataSource.setUser(user);
                    callback.onSuccess();
                } else {
                    callback.onFailure();
                }
                Log.d(TAG, "changeLoginUserName response status:" + response.code());
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                callback.onFailure();
            }
        }, user.getUserCode(), newName);
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
                personal.userTokenType = userCodeType;
                EventBus.getDefault().post(new EventBusController.RemoteServiceEvent(
                        EventBusController.RemoteServiceEvent.GET_PERSONAL_INFO, true
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
                EventBus.getDefault().post(new EventBusController.RemoteServiceEvent(
                        EventBusController.RemoteServiceEvent.GET_PERSONAL_INFO, false
                        , errorMessage));
            }
        }

        @Override
        public void onFailure(Call<User> call, Throwable t) {
            Log.d(TAG, "getPersonalInfoResponseCallback onResponse onFailure, error message:"
                    + t.getMessage() + " user code:" + userCode);
            EventBus.getDefault().post(new EventBusController.RemoteServiceEvent(
                    EventBusController.RemoteServiceEvent.GET_PERSONAL_INFO, false
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

    public interface RegisterUserCallback {
        void onSuccess();

        void onFailure();
    }

    public interface ChangeUserNameCallback {
        void onSuccess();

        void onFailure();
    }
}
