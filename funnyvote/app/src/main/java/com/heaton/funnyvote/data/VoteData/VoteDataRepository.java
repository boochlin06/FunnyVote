package com.heaton.funnyvote.data.VoteData;

import android.support.annotation.NonNull;

import com.heaton.funnyvote.data.Injection;
import com.heaton.funnyvote.database.Option;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.database.VoteData;
import com.heaton.funnyvote.ui.main.MainPageTabFragment;
import com.heaton.funnyvote.utils.schedulers.BaseSchedulerProvider;

import java.io.File;
import java.util.List;
import java.util.NoSuchElementException;

import rx.Observable;

public class VoteDataRepository implements VoteDataSource {
    private static VoteDataRepository INSTANCE = null;
    private final VoteDataSource voteDataRemoteSource;
    private final VoteDataSource voteDataLocalSource;
    public static final int PAGE_COUNT = 20;
    private BaseSchedulerProvider schedulerProvider;

    public static VoteDataRepository getInstance(VoteDataSource voteDataLocalSource
            , VoteDataSource voteDataRemoteSource) {
        if (INSTANCE == null) {
            INSTANCE = new VoteDataRepository(voteDataLocalSource, voteDataRemoteSource);
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

    public VoteDataRepository(VoteDataSource voteDataLocalSource
            , VoteDataSource voteDataRemoteSource) {
        this.voteDataRemoteSource = voteDataRemoteSource;
        this.voteDataLocalSource = voteDataLocalSource;
        this.schedulerProvider = Injection.provideSchedulerProvider();
    }

    @Override
    public Observable<VoteData> getVoteData(String voteCode, User user) {
        Observable<VoteData> localVote = voteDataLocalSource.getVoteData(voteCode, user).first();
        Observable<VoteData> remoteVote = voteDataRemoteSource
                .getVoteData(voteCode, user)
                .subscribeOn(schedulerProvider.io())
                .map(voteData -> {
                    voteDataLocalSource.saveVoteData(voteData);
                    return voteData;
                })
                .onErrorResumeNext((Throwable e) -> localVote);

        return Observable.concat(remoteVote, localVote).first()
                .map(voteData -> {
                    if (voteData == null) {
                        throw new NoSuchElementException("no vote data");
                    }
                    return voteData;
                });
    }

    @Override
    public void saveVoteData(VoteData voteData) {
        voteDataLocalSource.saveVoteData(voteData);
    }


    @Override
    public Observable<List<Option>> getOptions(VoteData voteData) {
        return voteDataLocalSource.getOptions(voteData);
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
    public Observable<VoteData> addNewOption(String voteCode, String password, List<String> newOptions, User user) {
        return voteDataRemoteSource.addNewOption(voteCode, password, newOptions, user)
                .map(voteData -> {
                    voteDataLocalSource.saveVoteData(voteData);
                    return voteData;
                }).observeOn(schedulerProvider.io());
    }

    @Override
    public Observable<VoteData> pollVote(@NonNull String voteCode, String password
            , @NonNull List<String> pollOptions, @NonNull User user) {
        return voteDataRemoteSource.pollVote(voteCode, password, pollOptions, user)
                .map(voteData -> {
                    voteDataLocalSource.saveVoteData(voteData);
                    return voteData;
                });
    }

    @Override
    public Observable<Boolean> favoriteVote(String voteCode, boolean isFavorite, User user) {
        // save local after get remote
        return voteDataRemoteSource.favoriteVote(voteCode, isFavorite, user)
                .map(isSFavorite -> {
                    voteDataLocalSource.saveFavoriteVote(voteCode, isFavorite, user);
                    return isSFavorite;
                });
    }

    @Override
    public void saveFavoriteVote(String voteCode, boolean isFavorite, User user) {
        voteDataLocalSource.saveFavoriteVote(voteCode, isFavorite, user);
    }


    @Override
    public Observable<VoteData> createVote(@NonNull VoteData voteSetting, @NonNull List<String> options, File image) {
        return voteDataRemoteSource.createVote(voteSetting, options, image)
                .map(voteData -> {
                    voteDataLocalSource.saveVoteData(voteData);
                    return voteData;
                }).observeOn(schedulerProvider.io());
    }


    @Override
    public Observable<List<VoteData>> getHotVoteList(int offset, User user) {
        Observable<List<VoteData>> localVote = voteDataLocalSource
                .getHotVoteList(offset, user).first();
        Observable<List<VoteData>> remoteVote = voteDataRemoteSource
                .getHotVoteList(offset, user)
                .subscribeOn(schedulerProvider.io())
                .map(voteDataList -> {
                    voteDataLocalSource.saveVoteDataList(voteDataList, offset, MainPageTabFragment.TAB_HOT);
                    return voteDataList;
                })
                .onErrorResumeNext((Throwable e) -> localVote);

        return Observable.concat(remoteVote, localVote).first()
                .map(voteDataList -> {
                    if (voteDataList == null) {
                        throw new NoSuchElementException("no vote data");
                    }
                    return voteDataList;
                });
    }


    @Override
    public Observable<List<VoteData>> getCreateVoteList(int offset, User user, User targetUser) {
        Observable<List<VoteData>> localVote = voteDataLocalSource
                .getCreateVoteList(offset, user, targetUser).first();
        Observable<List<VoteData>> remoteVote = voteDataRemoteSource
                .getCreateVoteList(offset, user, targetUser).first()
                .subscribeOn(schedulerProvider.io())
                .map(voteDataList -> {
                    voteDataLocalSource.saveVoteDataList(voteDataList, offset, MainPageTabFragment.TAB_CREATE);
                    return voteDataList;
                })
                .onErrorResumeNext((Throwable e) -> localVote);

        return Observable.concat(remoteVote, localVote).first()
                .map(voteDataList -> {
                    if (voteDataList == null) {
                        throw new NoSuchElementException("no vote data");
                    }
                    return voteDataList;
                });
    }


    @Override
    public Observable<List<VoteData>> getParticipateVoteList(int offset, User user, User targetUser) {
        Observable<List<VoteData>> localVote = voteDataLocalSource
                .getParticipateVoteList(offset, user, targetUser).first();
        Observable<List<VoteData>> remoteVote = voteDataRemoteSource
                .getParticipateVoteList(offset, user, targetUser).first()
                .subscribeOn(schedulerProvider.io())
                .map(voteDataList -> {
                    voteDataLocalSource.saveVoteDataList(voteDataList, offset, MainPageTabFragment.TAB_PARTICIPATE);
                    return voteDataList;
                })
                .onErrorResumeNext((Throwable e) -> localVote);

        return Observable.concat(remoteVote, localVote).first()
                .map(voteDataList -> {
                    if (voteDataList == null) {
                        throw new NoSuchElementException("no vote data");
                    }
                    return voteDataList;
                });
    }

    @Override
    public Observable<List<VoteData>> getFavoriteVoteList(int offset, User user, User targetUser) {
        Observable<List<VoteData>> localVote = voteDataLocalSource
                .getFavoriteVoteList(offset, user, targetUser).first();
        Observable<List<VoteData>> remoteVote = voteDataRemoteSource
                .getFavoriteVoteList(offset, user, targetUser).first()
                .subscribeOn(schedulerProvider.io())
                .map(voteDataList -> {
                    voteDataLocalSource.saveVoteDataList(voteDataList, offset, MainPageTabFragment.TAB_FAVORITE);
                    return voteDataList;
                })
                .onErrorResumeNext((Throwable e) -> localVote);

        return Observable.concat(remoteVote, localVote).first()
                .map(voteDataList -> {
                    if (voteDataList == null) {
                        throw new NoSuchElementException("no vote data");
                    }
                    return voteDataList;
                });
    }

    @Override
    public Observable<List<VoteData>> getSearchVoteList(String keyword, int offset, @NonNull User user) {
        Observable<List<VoteData>> localVote = voteDataLocalSource
                .getSearchVoteList(keyword, offset, user).first();
        Observable<List<VoteData>> remoteVote = voteDataRemoteSource
                .getSearchVoteList(keyword, offset, user)
                .subscribeOn(schedulerProvider.io())
                .map(voteDataList -> {
                    voteDataLocalSource.saveVoteDataList(voteDataList, offset, MainPageTabFragment.TAB_FAVORITE);
                    return voteDataList;
                })
                .onErrorResumeNext((Throwable e) -> localVote);

        return Observable.concat(remoteVote, localVote).first()
                .map(voteDataList -> {
                    if (voteDataList == null) {
                        throw new NoSuchElementException("no vote data");
                    }
                    return voteDataList;
                });
    }


    @Override
    public Observable<List<VoteData>> getNewVoteList(int offset, User user) {
        Observable<List<VoteData>> localVote = voteDataLocalSource
                .getNewVoteList(offset, user).first();
        Observable<List<VoteData>> remoteVote = voteDataRemoteSource
                .getNewVoteList(offset, user)
                .subscribeOn(schedulerProvider.io())
                .map(voteDataList -> {
                    voteDataLocalSource.saveVoteDataList(voteDataList, offset, MainPageTabFragment.TAB_NEW);
                    return voteDataList;
                })
                .onErrorResumeNext((Throwable e) -> localVote);

        return Observable.concat(remoteVote, localVote).first()
                .map(voteDataList -> {
                    if (voteDataList == null) {
                        throw new NoSuchElementException("no vote data");
                    }
                    return voteDataList;
                });
    }

}
