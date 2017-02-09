package com.android.heaton.funnyvote.data;

import android.util.Log;

import com.android.heaton.funnyvote.data.VoteData.VoteDataManager;
import com.android.heaton.funnyvote.data.promotion.PromotionManager;
import com.android.heaton.funnyvote.data.user.UserManager;
import com.android.heaton.funnyvote.database.Promotion;
import com.android.heaton.funnyvote.database.User;
import com.android.heaton.funnyvote.database.VoteData;
import com.android.heaton.funnyvote.eventbus.EventBusController;
import com.android.heaton.funnyvote.retrofit.Server;

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
    public static final String USER_TYPE_TWITTER = "twitter";

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

    //Retrofit
    Retrofit retrofit;
    Server.UserService userService;
    Server.VoteService voteService;
    Server.PromotionService promotionService;

    public RemoteServiceApi() {
        retrofit = new Retrofit.Builder().baseUrl(Server.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        userService = retrofit.create(Server.UserService.class);
        voteService = retrofit.create(Server.VoteService.class);
        promotionService = retrofit.create(Server.PromotionService.class);
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

    public void linkGuestToLoginUser(String otp, String guest, Callback<ResponseBody> callback) {
        Log.d(TAG, "linkGuestToLoginUser:" + otp + "/" + guest);
        Call<ResponseBody> call = userService.linkGuestLoginUser(otp, guest);
        call.enqueue(callback);
    }

    public void changeLoginUserName(Callback<ResponseBody> callback, String otp, String name) {
        Call<ResponseBody> call = userService.changeUserName(otp, otp, name);
        call.enqueue(callback);
    }

    public void changeGuestUserName(Callback<ResponseBody> callback, String guest, String name) {
        Call<ResponseBody> call = userService.changeGuestUserName(guest, guest, name);
        call.enqueue(callback);
    }

    public void getPersonalInfo(String userCode, String userType,
                                UserManager.getPersonalInfoResponseCallback callback) {
        Call<User> call;
        if (userType.equals(User.TYPE_TOKEN_GUEST)) {
            call = userService.getGuestInfo(userCode);
        } else {
            call = userService.getMemberInfo(userCode);
        }
        call.enqueue(callback);
    }

    public void getVote(String voteCode, User user, VoteDataManager.getVoteResponseCallback callback) {
        Call<VoteData> call = voteService.getVote(voteCode, user.getUserCode()
                , user.getTokenType());
        call.enqueue(callback);
    }

    public void getVoteList(int pageNumber, int pageCount, String eventMessage, User user
            , VoteDataManager.getVoteListResponseCallback callback) {
        pageNumber = pageNumber + 1;
        if (eventMessage.equals(EventBusController.RemoteServiceEvent.GET_VOTE_LIST_HOT)) {
            Call<List<VoteData>> call = voteService.getVoteList(pageNumber, pageCount, "hot", user.getUserCode()
                    , user.getTokenType());
            call.enqueue(callback);
        } else if (eventMessage.equals(EventBusController.RemoteServiceEvent.GET_VOTE_LIST_NEW)) {
            Call<List<VoteData>> call = voteService.getVoteList(pageNumber, pageCount, "new", user.getUserCode()
                    , user.getTokenType());
            call.enqueue(callback);
        } else if (eventMessage.equals(EventBusController.RemoteServiceEvent.GET_VOTE_LIST_FAVORITE)) {
            Call<List<VoteData>> call = voteService.getFavoriteVoteList(pageNumber, pageCount, user.getUserCode()
                    , user.getTokenType());
            call.enqueue(callback);
        } else if (eventMessage.equals(EventBusController.RemoteServiceEvent.GET_VOTE_LIST_HISTORY_CREATE)) {
            Call<List<VoteData>> call = voteService.getUserCreateVoteList(pageNumber, pageCount, user.getUserCode()
                    , user.getTokenType());
            call.enqueue(callback);
        } else if (eventMessage.equals(EventBusController.RemoteServiceEvent.GET_VOTE_LIST_HISTORY_PARTICIPATE)) {
            Call<List<VoteData>> call = voteService.getUserParticipateVoteList(pageNumber, pageCount, user.getUserCode()
                    , user.getTokenType());
            call.enqueue(callback);
        }

    }

    public void getSearchVoteList(String keyword, int pageNumber, int pageCount, User user
            , VoteDataManager.getVoteListResponseCallback callback) {
        pageNumber = pageNumber + 1;
        Call<List<VoteData>> call = voteService.getSearchVoteList(keyword, pageNumber, pageCount, user.getUserCode()
                , user.getTokenType());
        call.enqueue(callback);
    }

    public void getPersonalCreateVoteList(int pageNumber, int pageCount, User loginUser, User targetUser
            , VoteDataManager.getVoteListResponseCallback callback) {
        pageNumber = pageNumber + 1;
        Call<List<VoteData>> call = voteService.getPersonalCreateVoteList(pageNumber, pageCount, loginUser.getUserCode()
                , loginUser.getTokenType()
                , targetUser.getUserCode(), targetUser.personalTokenType);
        Log.d("test", "getPersonalCreateVoteList:" + targetUser.getUserCode() + "," + targetUser.personalTokenType
                + "login user code:" + loginUser.getUserCode() + "," + loginUser.personalTokenType);
        call.enqueue(callback);
    }

    public void getPersonalFavoriteVoteList(int pageNumber, int pageCount, User loginUser, User targetUser
            , VoteDataManager.getVoteListResponseCallback callback) {
        pageNumber = pageNumber + 1;
        Call<List<VoteData>> call = voteService.getPersonalFavoriteVoteList(pageNumber, pageCount, loginUser.getUserCode()
                , loginUser.getTokenType()
                , targetUser.getUserCode(), targetUser.personalTokenType);
        Log.d("test", "getPersonalFavoriteVoteList:" + targetUser.getUserCode() + "," + targetUser.personalTokenType
                + "login user code:" + loginUser.getUserCode() + "," + loginUser.personalTokenType);
        call.enqueue(callback);
    }

    public void favoriteVote(String voteCode, boolean isFavorite, User user, VoteDataManager.favoriteVoteResponseCallback callback) {
        Call<ResponseBody> call = voteService.updateFavorite(voteCode, isFavorite ? "01" : "00", user.getUserCode()
                , user.getTokenType());
        call.enqueue(callback);
    }


    public void pollVote(String voteCode, String password, List<String> optionList, User user, VoteDataManager.pollVoteResponseCallback callback) {
        Call<VoteData> call = voteService.pollVote(voteCode, password, optionList, user.getUserCode()
                , user.getTokenType());
        call.enqueue(callback);
    }

    public void addNewOption(String voteCode, String password, List<String> optionList, User user, VoteDataManager.addNewOptionResponseCallback callback) {
        Call<VoteData> call = voteService.updateOption(voteCode, password, optionList, user.getUserCode()
                , user.getTokenType());
        call.enqueue(callback);
    }

    public void createVote(VoteData voteSetting, List<String> options, File image
            , VoteDataManager.createVoteResponseCallback callback) {


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
                , voteSetting.author.getTokenType());
        RequestBody rbOption;
        for (int i = options.size() - 1; i >= 0; i--) {
            rbOption = RequestBody.create(MediaType.parse("text/plain"), options.get(i));
            parameter.put("pt[" + i + "]", rbOption);
            Log.d("test", "pt[" + i + "] " + options.get(i));
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
        Log.d("test", "Need pw:" + voteSetting.getIsNeedPassword() + " pw:" + voteSetting.password
                + "start time:" + voteSetting.getStartTime() + " ,end:" + voteSetting.getEndTime());

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
        call.enqueue(callback);
    }

    public void getPromotionList(int pageNumber, int pageCount, User user
            , PromotionManager.getPromotionListResponseCallback callback) {
        Call<List<Promotion>> call = promotionService.getPromotionList(pageNumber + 1, pageCount, user.getUserCode()
                , user.getTokenType());
        call.enqueue(callback);
    }
}
