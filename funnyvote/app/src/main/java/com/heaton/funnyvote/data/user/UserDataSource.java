package com.heaton.funnyvote.data.user;

import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.retrofit.Server;

import okhttp3.ResponseBody;
import retrofit2.Callback;
import rx.Observable;

/**
 * Created by chiu_mac on 2016/12/6.
 */

public interface UserDataSource {
//    interface GetUserCallback {
//        void onResponse(User user);
//
//        void onFailure();
//    }
//
//    interface GetUserInfoCallback {
//        void onResponse(Server.UserDataQuery userData);
//
//        void onFailure();
//    }
//
//    interface RegisterUserCallback {
//        void onSuccess();
//
//        void onFailure();
//    }
//
//    interface ChangeUserNameCallback {
//        void onSuccess();
//
//        void onFailure();
//    }
//
//    interface GetUserCodeCallback {
//        void onSuccess(String userCode);
//
//        void onFalure();
//    }

    User getUser();

    void setUser(User user);

    void removeUser();


    Observable<String> getGuestUserCode(String name);

    void getUserInfo(Callback<Server.UserDataQuery> callback, User user);

    Observable<Server.UserDataQuery> getUserInfo(User user);


    Observable<User> getUser(boolean forceUpdateUserCode);

    void setGuestName(String guestName);


    Observable registerUser(final String appId, final User user, final boolean mergeGuest);

    void unregisterUser();


    Observable<String> getUserCode(String userType, String appId, User user);

    void linkGuestToLoginUser(String otp, String guest, Callback<ResponseBody> callback);

    Observable<ResponseBody> linkGuestToLoginUser(String otp, String guest);

    void changeUserName(Callback<ResponseBody> callback, String tokenType, String token, String name);

    Observable<ResponseBody> changeUserName(String tokenType, String token, String name);


    Observable changeCurrentUserName(final String name);

}
