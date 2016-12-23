package com.android.heaton.funnyvote.data.user;

import android.content.Context;
import android.util.Log;

import com.android.heaton.funnyvote.R;
import com.android.heaton.funnyvote.data.RemoteServiceApi;
import com.android.heaton.funnyvote.database.User;
import com.android.heaton.funnyvote.retrofit.Server;

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

    public void registerUser(final User user, final RegisterUserCallback callback) {
        String userType = "";
        switch (user.getType()) {
            case User.TYPE_FACEBOOK:
                userType = RemoteServiceApi.USER_TYPE_FACEBOOK;
                break;
            case User.TYPE_GOOGLE:
                userType = RemoteServiceApi.USER_TYPE_GOOGLE;
                break;
            default:
        }
        if (!userType.isEmpty()) {
            String appId = context.getString(R.string.facebook_app_id);
            remoteServiceApi.getUserCode(userType, appId, user.getUserID(),
                    user.getUserName(), user.getEmail(), user.getUserIcon(), user.getGender(),
                    new RemoteServiceApi.GetUserCodeCallback() {
                        @Override
                        public void onSuccess(String userCode) {
                            user.setUserCode(userCode);
                            userDataSource.setUser(user);
                            callback.onSuccess();
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
}
