package com.heaton.funnyvote.data.user;

import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.retrofit.Server;

import okhttp3.ResponseBody;
import retrofit2.Callback;

/**
 * Created by chiu_mac on 2016/12/6.
 */

public interface UserDataSource {
    interface GetUserCallback {
        void onResponse(User user);

        void onFailure();
    }

    interface GetUserInfoCallback {
        void onResponse(Server.UserDataQuery userData);

        void onFailure();
    }

    interface RegisterUserCallback {
        void onSuccess();

        void onFailure();
    }

    interface ChangeUserNameCallback {
        void onSuccess();

        void onFailure();
    }

    interface GetUserCodeCallback {
        void onSuccess(String userCode);

        void onFalure();
    }

    User getUser();

    void setUser(User user);

    void removeUser();

    void getGuestUserCode(GetUserCodeCallback callback, String name);

    void getUserInfo(Callback<Server.UserDataQuery> callback, User user);

    void getUser(GetUserCallback callback, boolean forceUpdateUserCode);

    void setGuestName(String guestName);

    void registerUser(final String appId, final User user, final boolean mergeGuest
            , final RegisterUserCallback callback);

    void unregisterUser();

    void getUserCode(String userType, String appId, User user, GetUserCodeCallback callback);

    void linkGuestToLoginUser(String otp, String guest, Callback<ResponseBody> callback);

    void changeUserName(Callback<ResponseBody> callback, String tokenType, String token, String name);

    void changeCurrentUserName(final String name, final ChangeUserNameCallback callback);

}
