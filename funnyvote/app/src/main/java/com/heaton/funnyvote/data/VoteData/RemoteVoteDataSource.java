package com.heaton.funnyvote.data.VoteData;

import android.support.annotation.NonNull;
import android.util.Log;

import com.heaton.funnyvote.data.RemoteServiceApi;
import com.heaton.funnyvote.database.Option;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.database.VoteData;
import com.heaton.funnyvote.retrofit.Server;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class RemoteVoteDataSource implements VoteDataSource {
    private static final String TAG = RemoteVoteDataSource.class.getSimpleName();
    private static RemoteVoteDataSource INSTANCE = null;
    private Server.VoteService voteService;


    public static RemoteVoteDataSource getInstance() {
        if (INSTANCE == null) {
            synchronized (RemoteVoteDataSource.class) {
                if (INSTANCE == null) {
                    INSTANCE = new RemoteVoteDataSource();
                }
            }
        }
        return INSTANCE;
    }

    @Inject
    public RemoteVoteDataSource() {
        this.voteService = RemoteServiceApi.getInstance().getVoteService();
    }

    @Override
    public void getVoteData(String voteCode, User user, @Nullable GetVoteDataCallback callback) {
        if (user == null) {
            callback.onVoteDataNotAvailable();
            return;
        }
        Call<VoteData> call = voteService.getVote(voteCode, user.getUserCode()
                , user.getTokenType());
        call.enqueue(new getVoteResponseCallback(callback));
    }

    @Override
    public void saveVoteData(VoteData voteData) {
        // only for local save.
    }

    @Override
    public void getOptions(VoteData voteData, GetVoteOptionsCallback callback) {
        // only for local save.
    }

    @Override
    public void saveOptions(List<Option> optionList) {
        // only for local save
    }

    @Override
    public void saveVoteDataList(List<VoteData> voteDataList, int offset, String tab) {

    }

    @Override
    public void addNewOption(String voteCode, String password, List<String> newOptions, User user, AddNewOptionCallback callback) {
        if (user == null) {
            callback.onFailure();
            return;
        }
        Call<VoteData> call = voteService.updateOption(voteCode, password, newOptions, user.getUserCode()
                , user.getTokenType());
        call.enqueue(new addNewOptionResponseCallback(callback));
    }

    @Override
    public void pollVote(@NonNull String voteCode, String password, @NonNull List<String> pollOptions
            , @NonNull User user, @Nullable PollVoteCallback callback) {
        if (user == null) {
            callback.onFailure();
            return;
        }
        Call<VoteData> call = voteService.pollVote(voteCode, password, pollOptions, user.getUserCode()
                , user.getTokenType());
        call.enqueue(new PollVoteResponseCallback(callback));
    }

    @Override
    public void favoriteVote(String voteCode, boolean isFavorite, User user, FavoriteVoteCallback callback) {
        Log.d("favoriteVoteREMOTE", "favoriteVote favoriteVote");
        if (user == null) {
            callback.onFailure();
            return;
        }
        Call<ResponseBody> call = voteService.updateFavorite(voteCode, isFavorite ? "01" : "00", user.getUserCode()
                , user.getTokenType());
        call.enqueue(new favoriteVoteResponseCallback(isFavorite, callback));
    }

    @Override
    public void createVote(@NonNull VoteData voteSetting, @NonNull List<String> options
            , File image, GetVoteDataCallback callback) {
        Map<String, RequestBody> parameter = new HashMap<>();

        RequestBody title = RequestBody.create(MediaType.parse("text/plain"), voteSetting.getTitle());
        RequestBody maxOption = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(voteSetting.getMaxOption()));
        RequestBody minOption = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(voteSetting.getMinOption()));
        RequestBody userCanAddOption = RequestBody.create(MediaType.parse("text/plain")
                , String.valueOf(voteSetting.getIsUserCanAddOption()));
        RequestBody userPanPreviewResult = RequestBody.create(MediaType.parse("text/plain")
                , String.valueOf(voteSetting.getIsCanPreviewResult()));
        RequestBody security = RequestBody.create(MediaType.parse("text/plain")
                , voteSetting.getSecurity());
        RequestBody category = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(voteSetting.getCategory()));
        RequestBody startTime = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(voteSetting.getStartTime()));
        RequestBody endTime = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(voteSetting.getEndTime()));

        RequestBody token = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(voteSetting.getAuthorCode()));
        RequestBody tokenType = RequestBody.create(MediaType.parse("text/plain")
                , voteSetting.author.getTokenType());
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
        Log.d(TAG, "Need pw:" + voteSetting.getIsNeedPassword() + " pw:" + voteSetting.password
                + "startwithSearch time:" + voteSetting.getStartTime() + " ,end:" + voteSetting.getEndTime());

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
        call.enqueue(new createVoteResponseCallback(callback));
    }

    @Override
    public void getHotVoteList(int offset, User user, GetVoteListCallback callback) {
        if (user == null) {
            callback.onVoteListNotAvailable();
            return;
        }

        int pageNumber = (offset / VoteDataRepository.PAGE_COUNT) + 1;
        int pageCount = VoteDataRepository.PAGE_COUNT;
        Call<List<VoteData>> call = voteService.getVoteList(pageNumber, pageCount, "hot", user.getUserCode()
                , user.getTokenType());
        call.enqueue(new getVoteListResponseCallback(callback));
    }

    @Override
    public void getCreateVoteList(int offset, User loginUser, User targetUser, GetVoteListCallback callback) {
        int pageNumber = (offset / VoteDataRepository.PAGE_COUNT) + 1;
        int pageCount = VoteDataRepository.PAGE_COUNT;
        Call<List<VoteData>> call;
        if (targetUser == null) {
            call = voteService.getUserCreateVoteList(pageNumber, pageCount, loginUser.getUserCode()
                    , loginUser.getTokenType());
        } else {
            call = voteService.getPersonalCreateVoteList(pageNumber, pageCount, loginUser.getUserCode()
                    , loginUser.getTokenType()
                    , targetUser.getUserCode(), targetUser.personalTokenType);
        }
        call.enqueue(new getVoteListResponseCallback(callback));
    }

    @Override
    public void getParticipateVoteList(int offset, User loginUser, User targetUser, GetVoteListCallback callback) {
        int pageNumber = (offset / VoteDataRepository.PAGE_COUNT) + 1;
        int pageCount = VoteDataRepository.PAGE_COUNT;
        Log.d(TAG, "getParticipateVoteList:" + loginUser.getUserCode()
                + "," + loginUser.getUserName() + " , " + loginUser.getTokenType() + "," + targetUser);
        Call<List<VoteData>> call = voteService.getUserParticipateVoteList(pageNumber, pageCount, loginUser.getUserCode()
                , loginUser.getTokenType());

        call.enqueue(new getVoteListResponseCallback(callback));
    }

    @Override
    public void getFavoriteVoteList(int offset, User loginUser, User targetUser, GetVoteListCallback callback) {
        int pageNumber = (offset / VoteDataRepository.PAGE_COUNT) + 1;
        int pageCount = VoteDataRepository.PAGE_COUNT;
        Call<List<VoteData>> call;
        if (targetUser == null) {
            call = voteService.getFavoriteVoteList(pageNumber, pageCount, loginUser.getUserCode()
                    , loginUser.getTokenType());
        } else {
            call = voteService.getPersonalFavoriteVoteList(pageNumber, pageCount, loginUser.getUserCode()
                    , loginUser.getTokenType()
                    , targetUser.getUserCode(), targetUser.personalTokenType);
        }
        call.enqueue(new getVoteListResponseCallback(callback));
    }

    @Override
    public void getSearchVoteList(String keyword, int offset, @NonNull User user, GetVoteListCallback callback) {
        if (user == null) {
            callback.onVoteListNotAvailable();
            return;
        }
        int pageNumber = (offset / VoteDataRepository.PAGE_COUNT) + 1;
        int pageCount = VoteDataRepository.PAGE_COUNT;
        Call<List<VoteData>> call = voteService.getSearchVoteList(keyword, pageNumber, pageCount, user.getUserCode()
                , user.getTokenType());
        call.enqueue(new getVoteListResponseCallback(callback));
    }

    @Override
    public void getNewVoteList(int offset, User user, GetVoteListCallback callback) {
        if (user == null) {
            callback.onVoteListNotAvailable();
            return;
        }
        int pageNumber = (offset / VoteDataRepository.PAGE_COUNT) + 1;
        int pageCount = VoteDataRepository.PAGE_COUNT;
        Call<List<VoteData>> call = voteService.getVoteList(pageNumber, pageCount, "new", user.getUserCode()
                , user.getTokenType());
        call.enqueue(new getVoteListResponseCallback(callback));
    }

    public class PollVoteResponseCallback implements Callback<VoteData> {

        PollVoteCallback callback;

        public PollVoteResponseCallback(PollVoteCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onResponse(Call<VoteData> call, Response<VoteData> response) {
            if (response.isSuccessful()) {
                callback.onSuccess(response.body());
            } else {
                String errorMessage = "";
                try {
                    errorMessage = response.errorBody().string();
                    if (errorMessage.equals("error_invalid_password")) {
                        callback.onPasswordInvalid();
                        return;
                    }
                    Log.e(TAG, "pollVoteResponseCallback onResponse false , error message:" + errorMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                callback.onFailure();
            }
        }

        @Override
        public void onFailure(Call<VoteData> call, Throwable t) {
            Log.e(TAG, "pollVoteResponseCallback onFailure , error message:" + t.getMessage());
            callback.onFailure();
        }
    }

    public class addNewOptionResponseCallback implements Callback<VoteData> {
        AddNewOptionCallback callback;

        public addNewOptionResponseCallback(AddNewOptionCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onResponse(Call<VoteData> call, Response<VoteData> response) {
            if (response.isSuccessful()) {
                callback.onSuccess(response.body());
            } else {
                String errorMessage = "";
                try {
                    errorMessage = response.errorBody().string();
                    if (errorMessage.equals("error_invalid_password")) {
                        callback.onPasswordInvalid();
                        return;
                    }
                    Log.e(TAG, "addNewOptionResponseCallback onResponse false , error message:" + errorMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                callback.onFailure();
            }
        }

        @Override
        public void onFailure(Call<VoteData> call, Throwable t) {
            Log.e(TAG, "addNewOptionResponseCallback onFailure , error message:" + t.getMessage());
            callback.onFailure();
        }
    }

    public class getVoteResponseCallback implements Callback<VoteData> {

        GetVoteDataCallback callback;

        public getVoteResponseCallback(GetVoteDataCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onResponse(Call<VoteData> call, Response<VoteData> response) {
            if (response.isSuccessful()) {
                callback.onVoteDataLoaded(response.body());
            } else {
                String errorMessage = "";
                try {
                    errorMessage = response.errorBody().string();
                    Log.e(TAG, "getVoteResponseCallback onResponse false, error message:" + errorMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                callback.onVoteDataNotAvailable();
            }
        }

        @Override
        public void onFailure(Call<VoteData> call, Throwable t) {
            Log.e(TAG, "getVoteResponseCallback onResponse onFailure, error message:" + t.getMessage());
            callback.onVoteDataNotAvailable();
        }
    }

    public class favoriteVoteResponseCallback implements Callback<ResponseBody> {
        private boolean isFavorite;
        private FavoriteVoteCallback callback;

        public favoriteVoteResponseCallback(boolean isFavorite, FavoriteVoteCallback callback) {
            this.callback = callback;
            this.isFavorite = isFavorite;
        }

        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            if (response.isSuccessful()) {
                callback.onSuccess(isFavorite);
            } else {
                String errorMessage = "";
                try {
                    errorMessage = response.errorBody().string();
                    Log.e(TAG, "favoriteVoteResponseCallback onResponse false, error message:" + errorMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                callback.onFailure();
            }

        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            Log.e(TAG, "favoriteVoteResponseCallback onFailure , error message:" + t.getMessage());
            callback.onFailure();
        }
    }

    public class createVoteResponseCallback implements Callback<VoteData> {

        GetVoteDataCallback callback;

        public createVoteResponseCallback(GetVoteDataCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onResponse(Call<VoteData> call, Response<VoteData> response) {
            if (response.isSuccessful()) {
                callback.onVoteDataLoaded(response.body());
            } else {
                String errorMessage = "";
                try {
                    errorMessage = response.errorBody().string();
                    Log.e(TAG, "createVoteResponseCallback onResponse false, error message:" + errorMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                callback.onVoteDataNotAvailable();
            }
        }

        @Override
        public void onFailure(Call<VoteData> call, Throwable t) {
            Log.e(TAG, "createVoteResponseCallback onResponse onFailure, error message:" + t.getMessage());
            callback.onVoteDataNotAvailable();
        }
    }

    public class getVoteListResponseCallback implements Callback<List<VoteData>> {
        GetVoteListCallback callback;

        public getVoteListResponseCallback(GetVoteListCallback callback) {
            this.callback = callback;
        }


        @Override
        public void onResponse(Call<List<VoteData>> call, Response<List<VoteData>> response) {
            if (response.isSuccessful()) {
                Log.e(TAG, "getVoteResponseCallback onResponse true, size:" + response.body().size());
                callback.onVoteListLoaded(response.body());
            } else {
                String errorMessage = "";
                try {
                    errorMessage = response.errorBody().string();
                    Log.e(TAG, "getVoteListResponseCallback onResponse false, message:" + errorMessage);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (errorMessage.equals("error_no_poll_event")) {
                    callback.onVoteListLoaded(new ArrayList<VoteData>());
                } else {
                    callback.onVoteListNotAvailable();
                }
            }
        }

        @Override
        public void onFailure(Call<List<VoteData>> call, Throwable t) {
            Log.e(TAG, "getVoteListResponseCallback onFailure:" + t.getMessage());
            callback.onVoteListNotAvailable();
        }
    }
}
