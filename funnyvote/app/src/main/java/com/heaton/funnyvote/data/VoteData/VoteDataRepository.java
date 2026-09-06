package com.heaton.funnyvote.data.VoteData;

import androidx.annotation.NonNull;

import com.heaton.funnyvote.database.Option;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.database.VoteData;
import com.heaton.funnyvote.utils.schedulers.BaseSchedulerProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

public class VoteDataRepository implements VoteDataSource {

    private static final String TAG = VoteDataRepository.class.getSimpleName();
    public static final int PAGE_COUNT = 10;
    private static VoteDataRepository INSTANCE = null;

    private VoteDataSource voteDataRemoteSource, voteDataLocalSource;
    private BaseSchedulerProvider schedulerProvider;

    public static VoteDataRepository getInstance(VoteDataSource voteDataRemoteSource, VoteDataSource voteDataLocalSource) {
        return getInstance(voteDataRemoteSource, voteDataLocalSource, com.heaton.funnyvote.utils.schedulers.SchedulerProvider.getInstance());
    }

    public static VoteDataRepository getInstance(VoteDataSource voteDataRemoteSource
            , VoteDataSource voteDataLocalSource, BaseSchedulerProvider schedulerProvider) {
        if (INSTANCE == null) {
            synchronized (VoteDataRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new VoteDataRepository(voteDataRemoteSource, voteDataLocalSource, schedulerProvider);
                }
            }
        }
        return INSTANCE;
    }

    public VoteDataRepository(VoteDataSource voteDataRemoteSource
            , VoteDataSource voteDataLocalSource, BaseSchedulerProvider schedulerProvider) {
        this.voteDataLocalSource = voteDataLocalSource;
        this.voteDataRemoteSource = voteDataRemoteSource;
        this.schedulerProvider = schedulerProvider;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

    @Override
    public Observable<VoteData> getVoteData(String voteCode, User user) {
        return voteDataRemoteSource
                .getVoteData(voteCode, user)
                .doOnNext(voteData -> voteDataLocalSource.saveVoteData(voteData))
                .onErrorResumeNext(voteDataLocalSource.getVoteData(voteCode, user));
    }

    @Override
    public void saveVoteData(VoteData voteData) {
        voteDataLocalSource.saveVoteData(voteData);
    }

    @Override
    public Observable<List<Option>> getOptions(VoteData voteData) {
        if (voteData != null && voteData.getNetOptions() != null && !voteData.getNetOptions().isEmpty()) {
            return Observable.just(voteData.getNetOptions());
        }
        return voteDataRemoteSource.getOptions(voteData)
                .doOnNext(options -> voteDataLocalSource.saveOptions(options))
                .onErrorResumeNext(voteDataLocalSource.getOptions(voteData));
    }

    @Override
    public void saveOptions(List<Option> optionList) {
        voteDataLocalSource.saveOptions(optionList);
    }

    @Override
    public void saveVoteDataList(List<VoteData> voteDataList, int offset, String tab) {
        voteDataLocalSource.saveVoteDataList(voteDataList, offset, tab);
    }

    @Override
    public Observable<List<VoteData>> getHotVoteList(int offset, User user) {
        Observable<List<VoteData>> localVote = voteDataLocalSource.getHotVoteList(offset, user);
        return voteDataRemoteSource.getHotVoteList(offset, user)
                .doOnNext(voteDataList -> voteDataLocalSource.saveVoteDataList(voteDataList, offset, "hot"))
                .onErrorResumeNext(localVote);
    }

    @Override
    public Observable<List<VoteData>> getNewVoteList(int offset, User user) {
        Observable<List<VoteData>> localVote = voteDataLocalSource.getNewVoteList(offset, user);
        return voteDataRemoteSource.getNewVoteList(offset, user)
                .doOnNext(voteDataList -> voteDataLocalSource.saveVoteDataList(voteDataList, offset, "new"))
                .onErrorResumeNext(localVote);
    }

    @Override
    public Observable<VoteData> pollVote(String voteCode, String password, List<String> optionCodes, User user) {
        return voteDataRemoteSource.pollVote(voteCode, password, optionCodes, user)
                .doOnNext(voteData -> voteDataLocalSource.saveVoteData(voteData));
    }

    @Override
    public void saveFavoriteVote(String voteCode, boolean isFavorite, User user) {
        voteDataLocalSource.saveFavoriteVote(voteCode, isFavorite, user);
    }

    @Override
    public Observable<Boolean> favoriteVote(String voteCode, boolean isFavorite, User user) {
        return voteDataRemoteSource.favoriteVote(voteCode, isFavorite, user);
    }

    @Override
    public Observable<VoteData> createVote(VoteData voteData, List<String> options, File imageFile) {
        return voteDataRemoteSource.createVote(voteData, options, imageFile)
                .doOnNext(created -> voteDataLocalSource.saveVoteData(created));
    }

    @Override
    public Observable<VoteData> addNewOption(String voteCode, String password, List<String> newOptions, User user) {
        return voteDataRemoteSource.addNewOption(voteCode, password, newOptions, user)
                .doOnNext(voteData -> voteDataLocalSource.saveVoteData(voteData));
    }

    @Override
    public Observable<List<VoteData>> getSearchVoteList(String keyword, int offset, User user) {
        return voteDataRemoteSource.getSearchVoteList(keyword, offset, user);
    }

    @Override
    public Observable<List<VoteData>> getCreateVoteList(int offset, User loginUser, User targetUser) {
        Observable<List<VoteData>> localVote = voteDataLocalSource.getCreateVoteList(offset, loginUser, targetUser);
        return voteDataRemoteSource.getCreateVoteList(offset, loginUser, targetUser)
                .doOnNext(list -> voteDataLocalSource.saveVoteDataList(list, offset, "create"))
                .onErrorResumeNext(localVote);
    }

    @Override
    public Observable<List<VoteData>> getParticipateVoteList(int offset, User loginUser, User targetUser) {
        Observable<List<VoteData>> localVote = voteDataLocalSource.getParticipateVoteList(offset, loginUser, targetUser);
        return voteDataRemoteSource.getParticipateVoteList(offset, loginUser, targetUser)
                .doOnNext(list -> voteDataLocalSource.saveVoteDataList(list, offset, "participate"))
                .onErrorResumeNext(localVote);
    }

    @Override
    public Observable<List<VoteData>> getFavoriteVoteList(int offset, User loginUser, User targetUser) {
        Observable<List<VoteData>> localVote = voteDataLocalSource.getFavoriteVoteList(offset, loginUser, targetUser);
        return voteDataRemoteSource.getFavoriteVoteList(offset, loginUser, targetUser)
                .doOnNext(list -> voteDataLocalSource.saveVoteDataList(list, offset, "favorite"))
                .onErrorResumeNext(localVote);
    }
}
