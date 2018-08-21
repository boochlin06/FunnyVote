package com.heaton.funnyvote.data.VoteData;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.common.collect.Lists;
import com.heaton.funnyvote.database.Option;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.database.VoteData;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

public class FakeRemoteVoteDataRepository implements VoteDataSource {

    private static final String TAG = FakeRemoteVoteDataRepository.class.getSimpleName();
    private static FakeRemoteVoteDataRepository INSTANCE;


    private static final Map<String, VoteData> VOTES_SERVICE_DATA = new LinkedHashMap<>();

    // Prevent direct instantiation.
    @Inject
    public FakeRemoteVoteDataRepository() {
    }

    public static FakeRemoteVoteDataRepository getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FakeRemoteVoteDataRepository();
        }
        return INSTANCE;
    }

    @Override
    public void getVoteData(String voteCode, User user, @Nullable GetVoteDataCallback callback) {
        Log.d(TAG, "getVoteData");
        VoteData voteData = VOTES_SERVICE_DATA.get(voteCode);
        callback.onVoteDataLoaded(voteData);
    }

    @Override
    public void saveVoteData(VoteData voteData) {
        VOTES_SERVICE_DATA.put(voteData.getVoteCode(), voteData);
    }

    @Override
    public void getOptions(VoteData voteData, GetVoteOptionsCallback callback) {
        callback.onVoteOptionsLoaded(voteData.getNetOptions());
    }

    @Override
    public void saveOptions(List<Option> optionList) {
        //NONE
    }

    @Override
    public void saveVoteDataList(List<VoteData> voteDataList, int offset, String tab) {
        //NONE
    }

    @Override
    public void addNewOption(String voteCode, String password, List<String> newOptions, User user, AddNewOptionCallback callback) {
        VoteData voteData = VOTES_SERVICE_DATA.get(voteCode);
        callback.onSuccess(voteData);
    }

    @Override
    public void pollVote(@NonNull String voteCode, String password, @NonNull List<String> pollOptions, @NonNull User user, @Nullable PollVoteCallback callback) {
        VoteData voteData = VOTES_SERVICE_DATA.get(voteCode);
        callback.onSuccess(voteData);
    }

    @Override
    public void favoriteVote(String voteCode, boolean isFavorite, User user, FavoriteVoteCallback callback) {
        VoteData voteData = VOTES_SERVICE_DATA.get(voteCode);
        callback.onSuccess(voteData.getIsFavorite());
    }

    @Override
    public void createVote(@NonNull VoteData voteSetting, @NonNull List<String> options, File image, GetVoteDataCallback callback) {
        VoteData voteData = voteSetting;
        voteData.setVoteCode("CODE_0");
        callback.onVoteDataLoaded(voteData);
    }

    @Override
    public void getHotVoteList(int offset, User user, GetVoteListCallback callback) {
        callback.onVoteListLoaded(Lists.newArrayList(VOTES_SERVICE_DATA.values()));
    }

    @Override
    public void getNewVoteList(int offset, User user, GetVoteListCallback callback) {
        callback.onVoteListLoaded(Lists.newArrayList(VOTES_SERVICE_DATA.values()));
    }

    @Override
    public void getCreateVoteList(int offset, User user, User targetUser, GetVoteListCallback callback) {
        callback.onVoteListLoaded(Lists.newArrayList(VOTES_SERVICE_DATA.values()));
    }

    @Override
    public void getParticipateVoteList(int offset, User user, User targetUser, GetVoteListCallback callback) {
        callback.onVoteListLoaded(Lists.newArrayList(VOTES_SERVICE_DATA.values()));
    }

    @Override
    public void getFavoriteVoteList(int offset, User user, User targetUser, GetVoteListCallback callback) {
        callback.onVoteListLoaded(Lists.newArrayList(VOTES_SERVICE_DATA.values()));
    }

    @Override
    public void getSearchVoteList(String keyword, int offset, @NonNull User user, GetVoteListCallback callback) {
        callback.onVoteListLoaded(Lists.newArrayList(VOTES_SERVICE_DATA.values()));
    }
}
