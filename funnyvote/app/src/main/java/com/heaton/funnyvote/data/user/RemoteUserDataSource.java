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
import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

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
    public Observable<String> getGuestUserCode(String name) {
        return userService.getGuestCodeRx(name).flatMap(new Func1<Response<ResponseBody>, Observable<String>>() {
            @Override
            public Observable<String> call(Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String responseStr = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseStr);
                        String otpString = jsonObject.getString("guest");
                        return Observable.just(otpString);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Observable.error(e);
                    }
                } else {
                    try {
                        Log.d(TAG, "onResponse false:" + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return Observable.error(new IOException("network failure"));
                }
            }
        }).subscribeOn(Schedulers.io());
    }

    @Override
    public void getUserInfo(Callback<Server.UserDataQuery> callback, User user) {
        Call<Server.UserDataQuery> call = userService.getUserInfo(user.getTokenType(), user.getUserCode());
        call.enqueue(callback);
    }

    @Override
    public Observable<Server.UserDataQuery> getUserInfo(User user) {
        return userService.getUserInfoRx(user.getTokenType(), user.getUserCode())
                .flatMap(new Func1<Response<Server.UserDataQuery>, Observable<Server.UserDataQuery>>() {
                    @Override
                    public Observable<Server.UserDataQuery> call(Response<Server.UserDataQuery> response) {
                        if (response.isSuccessful()) {
                            return Observable.just(response.body());
                        } else {
                            String errorMessage = "";
                            try {
                                errorMessage = response.errorBody().string();
                                Log.e(TAG, "getUser onResponse false" + errorMessage);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return Observable.error(new Exception(errorMessage));
                        }
                    }
                });
    }

    @Override
    public Observable<User> getUser(boolean forceUpdateUserCode) {
        return null;
    }

    @Override
    public void setGuestName(String guestName) {

    }


    @Override
    public Observable registerUser(String appId, User user, boolean mergeGuest) {
        return null;
    }


    @Override
    public void unregisterUser() {
        //Not required for the network data source
    }


    @Override
    public Observable<String> getUserCode(String userType, String appId, User user) {
        return userService.addUserRx(userType, appId, user.getUserID(),
                user.getUserName(), user.getEmail(), user.getUserIcon(), user.getGender())
                .flatMap(new Func1<Response<ResponseBody>, Observable<String>>() {
                    @Override
                    public Observable<String> call(Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            try {
                                String responseStr = response.body().string();
                                JSONObject jsonObject = new JSONObject(responseStr);
                                String otpString = jsonObject.getString("otp");
                                return Observable.just(otpString);
                            } catch (Exception e) {
                                e.printStackTrace();
                                return Observable.error(e);
                            }
                        } else {
                            try {
                                Log.d(TAG, "onResponse false:" + response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return Observable.error(new IOException("network failure"));
                        }
                    }
                });
    }

    @Override
    public void linkGuestToLoginUser(String otp, String guest, Callback<ResponseBody> callback) {
        Call<ResponseBody> call = userService.linkGuestLoginUser(otp, guest);
        call.enqueue(callback);
    }

    @Override
    public Observable<ResponseBody> linkGuestToLoginUser(String otp, String guest) {
        return userService.linkGuestLoginUserRx(otp, guest)
                .flatMap(new Func1<Response<ResponseBody>, Observable<ResponseBody>>() {
                    @Override
                    public Observable<ResponseBody> call(Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            return Observable.just(response.body());
                        } else {
                            try {
                                Log.e(TAG, "registerUser" + response.errorBody().string());
                                return Observable.error(new Exception(response.errorBody().string()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return Observable.error(new Exception("linkGuestToLoginUser error"));
                        }
                    }
                });
    }

    public void changeUserName(Callback<ResponseBody> callback, String tokenType, String token, String name) {
        Call<ResponseBody> call = userService.changeUserName(tokenType, token, name);
        call.enqueue(callback);
    }

    @Override
    public Observable<ResponseBody> changeUserName(String tokenType, String token, String name) {
        return userService.changeUserNameRx(tokenType, token, name)
                .flatMap((Func1<Response<ResponseBody>, Observable<ResponseBody>>) response -> {
                    if (response.isSuccessful()) {
                        return Observable.just(response.body());
                    } else {
                        String errorMessage = "";
                        try {
                            errorMessage = response.errorBody().string();
                            Log.e(TAG, "changeUserName onResponse false" + errorMessage);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return Observable.error(new Exception(errorMessage));
                    }
                });
    }

    @Override
    public Observable changeCurrentUserName(String name) {
        return null;
    }

}
