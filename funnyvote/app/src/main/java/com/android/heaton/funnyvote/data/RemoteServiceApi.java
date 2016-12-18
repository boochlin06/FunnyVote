package com.android.heaton.funnyvote.data;

import android.util.Log;

import com.android.heaton.funnyvote.database.User;
import com.android.heaton.funnyvote.database.VoteData;
import com.android.heaton.funnyvote.eventbus.EventBusController.RemoteServiceEvent;
import com.android.heaton.funnyvote.retrofit.Server;

import org.greenrobot.eventbus.EventBus;

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
    Server.VoteService voteService;

    public RemoteServiceApi() {
        retrofit = new Retrofit.Builder().baseUrl(Server.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        userService = retrofit.create(Server.UserService.class);
        voteService = retrofit.create(Server.VoteService.class);
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

    public void createVote(VoteData voteSetting, List<String> options, File image) {


        Map<String, RequestBody> parameter = new HashMap<>();

        RequestBody title = RequestBody.create(MediaType.parse("text/plain"), voteSetting.getTitle());
        RequestBody maxOption = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(voteSetting.getMaxOption()));
        RequestBody minOption = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(voteSetting.getMinOption()));
        RequestBody rbOption;
        for (int i = 0; i < options.size(); i++) {
            rbOption = RequestBody.create(MediaType.parse("text/plain"), options.get(i));
            parameter.put("pt[" + i + "]", rbOption);
        }
        RequestBody userCanAddOption = RequestBody.create(MediaType.parse("text/plain")
                , String.valueOf(voteSetting.getIsUserCanAddOption()));
        RequestBody userPanPreviewResult = RequestBody.create(MediaType.parse("text/plain")
                , String.valueOf(voteSetting.getIsCanPreviewResult()));
        RequestBody security = RequestBody.create(MediaType.parse("text/plain")
                , String.valueOf(voteSetting.getSecurity().equals(VoteData.SECURITY_PUBLIC)));
        //String testcode = "otp08aff6877d1d373dc00a7383ff437e09a586a93a693d9c92d2d1e52e05ceb701";
        RequestBody author = RequestBody.create(MediaType.parse("text/plain"), voteSetting.getAuthorCode());
        RequestBody category = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(voteSetting.getCategory()));


        parameter.put("t", title);
        parameter.put("max", maxOption);
        parameter.put("min", minOption);
        parameter.put("add", userCanAddOption);
        parameter.put("res", userPanPreviewResult);
        parameter.put("pub", security);
        parameter.put("cat", category);

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
        if (voteSetting.author.getType() == User.TYPE_GUEST) {
            parameter.put("guest", author);
            Log.d("test", "otp create vote: user code:" + voteSetting.getAuthorCode());
            Call<VoteData> call = voteService.createVote(parameter, description, body);
            call.enqueue(new createVoteResponseCallback());
        } else {

            RequestBody otp = RequestBody.create(MediaType.parse("text/plain"), voteSetting.getAuthorCode());
            parameter.put("otp", author);
            Log.d("test", "otp create vote: user code:" + voteSetting.getAuthorCode());
            Call<VoteData> call = voteService.createVote(parameter, description, body);
            call.enqueue(new createVoteResponseCallback());
        }
    }
}
