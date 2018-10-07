package com.heaton.funnyvote.data.VoteData;

import android.support.annotation.NonNull;
import android.util.Log;

import com.heaton.funnyvote.data.RemoteServiceApi;
import com.heaton.funnyvote.database.Option;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.database.VoteData;
import com.heaton.funnyvote.retrofit.Server;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import rx.Observable;
import rx.functions.Func1;

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

    public RemoteVoteDataSource() {
        this.voteService = RemoteServiceApi.getInstance().getVoteService();
    }


    @Override
    public Observable<VoteData> getVoteData(String voteCode, User user) {
        return voteService.getVoteRx(voteCode, user.getUserCode()
                , user.getTokenType());
    }

    @Override
    public void saveVoteData(VoteData voteData) {
        // only for local save.
    }


    @Override
    public Observable<List<Option>> getOptions(VoteData voteData) {
        return Observable.empty();
    }

    @Override
    public void saveOptions(List<Option> optionList) {
        // only for local save
    }

    @Override
    public void saveVoteDataList(List<VoteData> voteDataList, int offset, String tab) {

    }


    @Override
    public Observable<VoteData> addNewOption(String voteCode, String password, List<String> newOptions, User user) {
        return voteService.updateOptionRx(voteCode, password, newOptions, user.getUserCode()
                , user.getTokenType());
    }

    @Override
    public Observable<VoteData> pollVote(@NonNull String voteCode, String password
            , @NonNull List<String> pollOptions, @NonNull User user) {
        return voteService.pollVoteRx(voteCode, password, pollOptions, user.getUserCode()
                , user.getTokenType());
    }

    @Override
    public Observable<Boolean> favoriteVote(String voteCode, boolean isFavorite, User user) {
        return voteService.updateFavoriteRx(voteCode, isFavorite ? "01" : "00", user.getUserCode()
                , user.getTokenType())
                .flatMap((Func1<ResponseBody, Observable<Boolean>>)
                        responseBody -> Observable.just(isFavorite));
    }

    @Override
    public void saveFavoriteVote(String voteCode, boolean isFavorite, User user) {
        // Nothing to do
    }


    @Override
    public Observable<VoteData> createVote(@NonNull VoteData voteSetting, @NonNull List<String> options, File image) {

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
                + "subscribe time:" + voteSetting.getStartTime() + " ,end:" + voteSetting.getEndTime());

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

        return voteService.createVoteRx(parameter, description, body);
    }

    @Override
    public Observable<List<VoteData>> getHotVoteList(int offset, User user) {
        int pageNumber = (offset / VoteDataRepository.PAGE_COUNT) + 1;
        int pageCount = VoteDataRepository.PAGE_COUNT;
        return voteService.getVoteListRx(pageNumber, pageCount, "hot", user.getUserCode()
                , user.getTokenType());
    }


    @Override
    public Observable<List<VoteData>> getCreateVoteList(int offset, User loginUser, User targetUser) {
        int pageNumber = (offset / VoteDataRepository.PAGE_COUNT) + 1;
        int pageCount = VoteDataRepository.PAGE_COUNT;
        if (targetUser == null) {
            return voteService.getUserCreateVoteListRx(pageNumber, pageCount, loginUser.getUserCode()
                    , loginUser.getTokenType());
        } else {
            return voteService.getPersonalCreateVoteListRx(pageNumber, pageCount, loginUser.getUserCode()
                    , loginUser.getTokenType()
                    , targetUser.getUserCode(), targetUser.personalTokenType);
        }
    }

    @Override
    public Observable<List<VoteData>> getParticipateVoteList(int offset, User loginUser, User targetUser) {
        int pageNumber = (offset / VoteDataRepository.PAGE_COUNT) + 1;
        int pageCount = VoteDataRepository.PAGE_COUNT;
        return voteService.getUserParticipateVoteListRx(pageNumber, pageCount, loginUser.getUserCode()
                , loginUser.getTokenType());
    }

    @Override
    public Observable<List<VoteData>> getFavoriteVoteList(int offset, User loginUser, User targetUser) {
        int pageNumber = (offset / VoteDataRepository.PAGE_COUNT) + 1;
        int pageCount = VoteDataRepository.PAGE_COUNT;
        if (targetUser == null) {
            return voteService.getFavoriteVoteListRx(pageNumber, pageCount, loginUser.getUserCode()
                    , loginUser.getTokenType());
        } else {
            return voteService.getPersonalFavoriteVoteListRx(pageNumber, pageCount, loginUser.getUserCode()
                    , loginUser.getTokenType()
                    , targetUser.getUserCode(), targetUser.personalTokenType);
        }
    }

    @Override
    public Observable<List<VoteData>> getSearchVoteList(String keyword, int offset, @NonNull User user) {
        int pageNumber = (offset / VoteDataRepository.PAGE_COUNT) + 1;
        int pageCount = VoteDataRepository.PAGE_COUNT;
        return voteService.getSearchVoteListRx(keyword, pageNumber, pageCount, user.getUserCode()
                , user.getTokenType());
    }

    @Override
    public Observable<List<VoteData>> getNewVoteList(int offset, User user) {
        int pageNumber = (offset / VoteDataRepository.PAGE_COUNT) + 1;
        int pageCount = VoteDataRepository.PAGE_COUNT;
        return voteService.getVoteListRx(pageNumber, pageCount, "new", user.getUserCode()
                , user.getTokenType());
    }

}
