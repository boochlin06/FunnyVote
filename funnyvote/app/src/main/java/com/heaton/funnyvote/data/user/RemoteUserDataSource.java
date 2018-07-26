package com.heaton.funnyvote.data.user;

import android.util.Log;

import com.heaton.funnyvote.data.RemoteServiceApi;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.retrofit.Server;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RemoteUserDataSource implements UserDataSource {
    private static final String TAG = RemoteUserDataSource.class.getSimpleName();
    private static RemoteUserDataSource INSTANCE;
    private Server.UserService userService;

    public static RemoteUserDataSource getInstance() {
        if (INSTANCE == null) {
            synchronized (RemoteUserDataSource.class) {
                if (INSTANCE == null) {
                    INSTANCE = new RemoteUserDataSource();
                }
            }
        }
        return INSTANCE;
    }

    public RemoteUserDataSource() {
        userService = RemoteServiceApi.getInstance().getUserService();
    }

    @Override
    public User getUser() {
        // Not required for the network data source
        return null;
    }

    @Override
    public void setUser(User user) {
        // Not required for the network data source
    }

    @Override
    public void removeUser() {
        // Not required for the network data source
    }

    @Override
    public void getGuestUserCode(GetUserCodeCallback callback, String name) {
        Call<ResponseBody> call = userService.getGuestCode(name);
        call.enqueue(new GuestUserCodeResponseCallback(callback));
    }

    @Override
    public void getUserInfo(Callback<Server.UserDataQuery> callback, User user) {
        Call<Server.UserDataQuery> call = userService.getUserInfo(user.getTokenType(), user.getUserCode());
        call.enqueue(callback);
    }

    @Override
    public void getUser(GetUserCallback callback, boolean forceUpdateUserCode) {

    }

    @Override
    public void registerUser(User user, boolean mergeGuest, RegisterUserCallback callback) {

        //Not required for the network data source
    }


    @Override
    public void unregisterUser() {
        //Not required for the network data source
    }

    @Override
    public void getUserCode(String userType, String appId, User user, GetUserCodeCallback callback) {
        Call<ResponseBody> call = userService.addUser(userType, appId, user.getUserID(),
                user.getUserName(), user.getEmail(), user.getUserIcon(), user.getGender());
        call.enqueue(new LoginUserCodeResponseCallback(callback));
    }

    @Override
    public void linkGuestToLoginUser(String otp, String guest, Callback<ResponseBody> callback) {
        Call<ResponseBody> call = userService.linkGuestLoginUser(otp, guest);
        call.enqueue(callback);
    }

    public void changeUserName(Callback<ResponseBody> callback, String tokenType, String token, String name) {
        Call<ResponseBody> call = userService.changeUserName(tokenType, token, name);
        call.enqueue(callback);
    }

    @Override
    public void changeCurrentUserName(String name, ChangeUserNameCallback callback) {

    }

    class LoginUserCodeResponseCallback implements Callback<ResponseBody> {
        GetUserCodeCallback getUserCodeCallback;

        public LoginUserCodeResponseCallback(GetUserCodeCallback getUserCodeCallback) {
            this.getUserCodeCallback = getUserCodeCallback;
        }

        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            Log.d(TAG, "Response Status:" + response.code());

            if (response.isSuccessful()) {
                try {
                    String responseStr = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseStr);
                    String otpString = jsonObject.getString("otp");
                    getUserCodeCallback.onSuccess(otpString);
                } catch (Exception e) {
                    e.printStackTrace();
                    getUserCodeCallback.onFalure();
                }
            } else {
                try {
                    Log.d(TAG, "onResponse false:" + response.errorBody().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                getUserCodeCallback.onFalure();
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            getUserCodeCallback.onFalure();
        }
    }

    class GuestUserCodeResponseCallback implements Callback<ResponseBody> {
        GetUserCodeCallback getUserCodeCallback;

        public GuestUserCodeResponseCallback(GetUserCodeCallback getUserCodeCallback) {
            this.getUserCodeCallback = getUserCodeCallback;
        }

        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            Log.d(TAG, "Response Status:" + response.code());
            if (response.isSuccessful()) {
                try {
                    String responseStr = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseStr);
                    String guestCode = jsonObject.getString("guest");
                    getUserCodeCallback.onSuccess(guestCode);
                } catch (Exception e) {
                    e.printStackTrace();
                    getUserCodeCallback.onFalure();
                }
            } else {
                getUserCodeCallback.onFalure();
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            getUserCodeCallback.onFalure();
        }
    }
}
