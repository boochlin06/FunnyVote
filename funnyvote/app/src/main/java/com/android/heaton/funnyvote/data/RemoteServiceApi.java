package com.android.heaton.funnyvote.data;

import android.util.Log;

import com.android.heaton.funnyvote.database.User;
import com.android.heaton.funnyvote.database.VoteData;
import com.android.heaton.funnyvote.eventbus.EventBusController.RemoteServiceEvent;
import com.android.heaton.funnyvote.retrofit.Server;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by chiu_mac on 2016/12/6.
 */

public class RemoteServiceApi {
    private static final String TAG = RemoteServiceApi.class.getSimpleName();
    public static final String USER_TYPE_FACEBOOK = "fb";
    public static final String USER_TYPE_GOOGLE = "google";

    public interface GetUserCodeCallback {
        void onSuccess(String userCode);

        void onFalure();
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
                    //Log.d("test", "onResponse false:" + response.errorBody().string());
                    getUserCodeCallback.onFalure();
                }
            } else {
                try {
                    Log.d("test", "onResponse false:" + response.errorBody().string());
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

    //Retrofit
    Retrofit retrofit;
    Server.UserService userService;
    Server.VoteService voteService;

    public RemoteServiceApi() {
        retrofit = new Retrofit.Builder().baseUrl(Server.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        userService = retrofit.create(Server.UserService.class);
        voteService = retrofit.create(Server.VoteService.class);
    }

    public void getGuestUserCode(GetUserCodeCallback callback, String guestName) {
        Call<ResponseBody> call = userService.getGuestCode(guestName);
        call.enqueue(new GuestUserCodeResponseCallback(callback));
    }

    public void getUserCode(String type, String appId, String fbId, String name,
                            String email, String imgUrl, String gender,
                            GetUserCodeCallback callback) {
        Call<ResponseBody> call = userService.addUser(type, appId, fbId, name, imgUrl, email, gender);
        call.enqueue(new LoginUserCodeResponseCallback(callback));

    }

    class createVoteResponseCallback implements Callback<VoteData> {

        public createVoteResponseCallback() {
        }

        @Override
        public void onResponse(Call<VoteData> call, Response<VoteData> response) {
            if (response.isSuccessful()) {
                EventBus.getDefault().post(new RemoteServiceEvent(RemoteServiceEvent.CREAT_VOTE, true, call, response, null));
            } else {
                try {
                    Log.d("test", "onResponse false:" + response.errorBody().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                EventBus.getDefault().post(new RemoteServiceEvent(RemoteServiceEvent.CREAT_VOTE, false, call, response, null));
            }
        }

        @Override
        public void onFailure(Call<VoteData> call, Throwable t) {
            Log.d("test", "onFailure:" + call.toString());
            EventBus.getDefault().post(new RemoteServiceEvent(RemoteServiceEvent.CREAT_VOTE, false, call, null, null));
        }
    }

    class getVoteResponseCallback implements Callback<VoteData> {

        public getVoteResponseCallback() {
        }

        @Override
        public void onResponse(Call<VoteData> call, Response<VoteData> response) {
            if (response.isSuccessful()) {
                EventBus.getDefault().post(new RemoteServiceEvent(RemoteServiceEvent.GET_VOTE, true, call, response, null));
            } else {
                try {
                    Log.d("test", "onResponse false:" + response.errorBody().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                EventBus.getDefault().post(new RemoteServiceEvent(RemoteServiceEvent.GET_VOTE, false, call, response, null));
            }
        }

        @Override
        public void onFailure(Call<VoteData> call, Throwable t) {
            Log.d("test", "onFailure:" + call.toString());
            EventBus.getDefault().post(new RemoteServiceEvent(RemoteServiceEvent.GET_VOTE, false, call, null, null));
        }
    }

    public void getVote(String voteCode, User user) {
        Call<VoteData> call = voteService.getVote(voteCode, user.getUserCode()
                , user.getType() == User.TYPE_GUEST ? "guest" : "member");
        call.enqueue(new getVoteResponseCallback());
    }

    public void createVote(VoteData voteSetting, List<String> options, File image) {


        Map<String, RequestBody> parameter = new HashMap<>();

        RequestBody title = RequestBody.create(MediaType.parse("text/plain"), voteSetting.getTitle());
        RequestBody maxOption = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(voteSetting.getMaxOption()));
        RequestBody minOption = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(voteSetting.getMinOption()));
        RequestBody userCanAddOption = RequestBody.create(MediaType.parse("text/plain")
                , String.valueOf(voteSetting.getIsUserCanAddOption()));
        RequestBody userPanPreviewResult = RequestBody.create(MediaType.parse("text/plain")
                , String.valueOf(voteSetting.getIsCanPreviewResult()));
        RequestBody security = RequestBody.create(MediaType.parse("text/plain")
                , VoteData.SECURITY_PUBLIC);
        RequestBody category = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(voteSetting.getCategory()));
        RequestBody startTime = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(voteSetting.getStartTime()));
        RequestBody endTime = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(voteSetting.getEndTime()));


        RequestBody token = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(voteSetting.getAuthorCode()));
        RequestBody tokenType = RequestBody.create(MediaType.parse("text/plain")
                , voteSetting.author.getType() == User.TYPE_GUEST ? "guest" : "member");
        RequestBody rbOption;
        for (int i = 0; i < options.size(); i++) {
            rbOption = RequestBody.create(MediaType.parse("text/plain"), options.get(i));
            parameter.put("pt[" + i + "]", rbOption);
        }

        parameter.put("t", title);
        parameter.put("max", maxOption);
        parameter.put("min", minOption);
        parameter.put("add", userCanAddOption);
        parameter.put("res", userPanPreviewResult);
        parameter.put("sec", security);
        parameter.put("cat", category);
        parameter.put("on", startTime);
        parameter.put("off", endTime);
        parameter.put("token", token);
        parameter.put("tokentype", tokenType);

        if (voteSetting.getIsNeedPassword()) {
            RequestBody password = RequestBody.create(MediaType.parse("text/plain"), voteSetting.password);
            parameter.put("p", password);
        }

        RequestBody requestFile = null;
        MultipartBody.Part body = null;
        String descriptionString = "vote_image";
        RequestBody description = null;

        if (image != null) {
            requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), image);
            body = MultipartBody.Part.createFormData("i", image.getName(), requestFile);
            description = RequestBody.create(
                    MediaType.parse("multipart/form-data"), descriptionString);
        }

        Call<VoteData> call = voteService.createVote(parameter, description, body);
        call.enqueue(new createVoteResponseCallback());
    }
}
