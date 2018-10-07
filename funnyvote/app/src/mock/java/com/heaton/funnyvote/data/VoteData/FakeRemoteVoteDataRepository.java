package com.heaton.funnyvote.data.VoteData;

import android.support.annotation.NonNull;

import com.google.common.collect.Lists;
import com.heaton.funnyvote.database.Option;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.database.VoteData;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;

public class FakeRemoteVoteDataRepository implements VoteDataSource {

    private static final String TAG = FakeRemoteVoteDataRepository.class.getSimpleName();
    private static FakeRemoteVoteDataRepository INSTANCE;


    private static final Map<String, VoteData> VOTES_SERVICE_DATA = new LinkedHashMap<>();

    // Prevent direct instantiation.
    private FakeRemoteVoteDataRepository() {
    }

    public static FakeRemoteVoteDataRepository getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FakeRemoteVoteDataRepository();
        }
        return INSTANCE;
    }


    @Override
    public Observable<VoteData> getVoteData(String voteCode, User user) {
        return Observable.just(VOTES_SERVICE_DATA.get(voteCode));
    }

    @Override
    public void saveVoteData(VoteData voteData) {
        VOTES_SERVICE_DATA.put(voteData.getVoteCode(), voteData);
    }

    @Override
    public Observable<List<Option>> getOptions(VoteData voteData) {
        return Observable.just(voteData.getNetOptions());
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
    public Observable<VoteData> addNewOption(String voteCode, String password, List<String> newOptions, User user) {
        return Observable.just(VOTES_SERVICE_DATA.get(voteCode));
    }


    @Override
    public Observable<VoteData> pollVote(@NonNull String voteCode, String password, @NonNull List<String> pollOptions, @NonNull User user) {
        return Observable.just(VOTES_SERVICE_DATA.get(voteCode));
    }


    @Override
    public Observable<Boolean> favoriteVote(String voteCode, boolean isFavorite, User user) {
        return Observable.just(VOTES_SERVICE_DATA.get(voteCode).getIsFavorite());
    }

    @Override
    public void saveFavoriteVote(String voteCode, boolean isFavorite, User user) {

    }


    @Override
    public Observable<VoteData> createVote(@NonNull VoteData voteSetting, @NonNull List<String> options, File image) {
        VoteData voteData = voteSetting;
        voteData.setVoteCode("CODE_0");
        return Observable.just(voteData);
    }


    @Override
    public Observable<List<VoteData>> getHotVoteList(int offset, User user) {
        return Observable.just(Lists.newArrayList(VOTES_SERVICE_DATA.values()));
    }


    @Override
    public Observable<List<VoteData>> getNewVoteList(int offset, User user) {
        return Observable.just(Lists.newArrayList(VOTES_SERVICE_DATA.values()));
    }

    @Override
    public Observable<List<VoteData>> getCreateVoteList(int offset, User user, User targetUser) {
        return Observable.just(Lists.newArrayList(VOTES_SERVICE_DATA.values()));
    }


    @Override
    public Observable<List<VoteData>> getParticipateVoteList(int offset, User user, User targetUser) {
        return Observable.just(Lists.newArrayList(VOTES_SERVICE_DATA.values()));
    }

    @Override
    public Observable<List<VoteData>> getFavoriteVoteList(int offset, User user, User targetUser) {
        return Observable.just(Lists.newArrayList(VOTES_SERVICE_DATA.values()));
    }


    @Override
    public Observable<List<VoteData>> getSearchVoteList(String keyword, int offset, @NonNull User user) {
        return Observable.just(Lists.newArrayList(VOTES_SERVICE_DATA.values()));
    }
}
