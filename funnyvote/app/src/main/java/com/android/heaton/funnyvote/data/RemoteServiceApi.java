package com.android.heaton.funnyvote.data;

import com.android.heaton.funnyvote.retrofit.Server;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by chiu_mac on 2016/12/6.
 */

public class RemoteServiceApi {
    public interface GetUserCodeCallback {
        void onSuccess(String userCode);
        void onFalure();
    }

    class UserCodeResponseCallback implements Callback<ResponseBody> {
        GetUserCodeCallback getUserCodeCallback;

        public UserCodeResponseCallback(GetUserCodeCallback getUserCodeCallback) {
            this.getUserCodeCallback = getUserCodeCallback;
        }

        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            if (response.isSuccessful()) {
                try {
                    String userCode = response.body().string();
                    getUserCodeCallback.onSuccess(userCode);
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

    //Retrofit
    Retrofit retrofit;
    Server.UserService userService;

    public RemoteServiceApi() {
        retrofit = new Retrofit.Builder().baseUrl(Server.BASE_URL).build();
        userService = retrofit.create(Server.UserService.class);
    }

    public void getGuestUserCode(GetUserCodeCallback callback) {
        Call<ResponseBody> call = userService.getGuestCode();
        call.enqueue(new UserCodeResponseCallback(callback));
    }

    public void getFacebookUserCode(String appId, String fbId, String name,
                                    String email, String imgUrl, String gender,
                                    GetUserCodeCallback callback) {
        Call<ResponseBody> call = userService.addFBUser(appId, fbId, name, imgUrl, email, gender);
        call.enqueue(new UserCodeResponseCallback(callback));

    }

    public void getGoogleUserCode(GetUserCodeCallback callback) {
    }
}
