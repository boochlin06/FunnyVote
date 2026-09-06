package com.heaton.funnyvote.data.VoteData;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.heaton.funnyvote.data.local.dao.OptionDao;
import com.heaton.funnyvote.data.local.dao.VoteDataDao;
import com.heaton.funnyvote.database.Option;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.database.VoteData;
import com.heaton.funnyvote.ui.main.MainPageTabFragment;
import com.heaton.funnyvote.utils.AppExecutors;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import io.reactivex.Observable;

public class LocalVoteDataSource implements VoteDataSource {
    private static final String TAG = LocalVoteDataSource.class.getSimpleName();
    private VoteDataDao voteDataDao;
    private OptionDao optionDao;
    private static volatile LocalVoteDataSource INSTANCE;
    private AppExecutors mAppExecutors;

    private LocalVoteDataSource(@NonNull VoteDataDao voteDataDao, OptionDao optionDao, AppExecutors appExecutors) {
        this.voteDataDao = voteDataDao;
        this.optionDao = optionDao;
        this.mAppExecutors = appExecutors;
    }

    public static LocalVoteDataSource getInstance(@NonNull VoteDataDao voteDataDao,
                                                  @NonNull OptionDao optionDao,
                                                  AppExecutors appExecutors) {
        if (INSTANCE == null) {
            synchronized (LocalVoteDataSource.class) {
                if (INSTANCE == null) {
                    INSTANCE = new LocalVoteDataSource(voteDataDao, optionDao, appExecutors);
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public Observable<VoteData> getVoteData(String voteCode, User user) {
        return Observable.fromCallable(() -> {
            VoteData voteData = voteDataDao.getVoteByCode(voteCode);
            if (voteData == null) {
                throw new NoSuchElementException("Vote not found: " + voteCode);
            }
            return voteData;
        });
    }

    @Override
    public void saveVoteData(VoteData voteData) {
        if (voteData == null) return;
        mAppExecutors.diskIO().execute(() -> {
            voteDataDao.deleteByCode(voteData.getVoteCode());
            voteDataDao.insert(voteData);
            if (voteData.getOptions() != null && !voteData.getOptions().isEmpty()) {
                optionDao.deleteByVoteCode(voteData.getVoteCode());
                for (Option opt : voteData.getOptions()) {
                    opt.setVoteCode(voteData.getVoteCode());
                }
                optionDao.insertAll(voteData.getOptions());
            }
        });
    }

    @Override
    public Observable<List<Option>> getOptions(VoteData voteData) {
        return Observable.fromCallable(() -> {
            if (voteData == null || voteData.getVoteCode() == null) {
                return new ArrayList<>();
            }
            return optionDao.getOptionsByVoteCode(voteData.getVoteCode());
        });
    }

    @Override
    public void saveOptions(List<Option> optionList) {
        if (optionList == null || optionList.isEmpty()) return;
        mAppExecutors.diskIO().execute(() -> optionDao.insertAll(optionList));
    }

    @Override
    public void saveVoteDataList(List<VoteData> voteDataList, int offset, String tab) {
        if (voteDataList == null || voteDataList.isEmpty()) return;
        for (int i = 0; i < voteDataList.size(); i++) {
            VoteData voteData = voteDataList.get(i);
            if (voteData.getFirstOption() != null) {
                voteData.setOption1Code(voteData.getFirstOption().getCode());
                voteData.setOption1Title(voteData.getFirstOption().getTitle());
                voteData.setOption1Count(voteData.getFirstOption().getCount());
                voteData.setOption1Polled(voteData.getFirstOption().getIsUserChoiced());
            }
            if (voteData.getSecondOption() != null) {
                voteData.setOption2Code(voteData.getSecondOption().getCode());
                voteData.setOption2Title(voteData.getSecondOption().getTitle());
                voteData.setOption2Count(voteData.getSecondOption().getCount());
                voteData.setOption2Polled(voteData.getSecondOption().getIsUserChoiced());
            }
            if (voteData.getTopOption() != null) {
                voteData.setOptionTopCode(voteData.getTopOption().getCode());
                voteData.setOptionTopTitle(voteData.getTopOption().getTitle());
                voteData.setOptionTopCount(voteData.getTopOption().getCount());
                voteData.setOptionTopPolled(voteData.getTopOption().getIsUserChoiced());
            }
            if (voteData.getUserOption() != null) {
                voteData.setOptionUserChoiceCode(voteData.getUserOption().getCode());
                voteData.setOptionUserChoiceTitle(voteData.getUserOption().getTitle());
                voteData.setOptionUserChoiceCount(voteData.getUserOption().getCount());
            }
            if (tab != null && tab.equals(MainPageTabFragment.TAB_HOT)) {
                voteData.setDisplayOrder((offset) * VoteDataRepository.PAGE_COUNT + i);
                voteData.setCategory("hot");
            } else {
                voteData.setCategory(null);
            }
        }
        mAppExecutors.diskIO().execute(() -> {
            for (VoteData data : voteDataList) {
                voteDataDao.deleteByCode(data.getVoteCode());
            }
            voteDataDao.insertOrReplaceInTx(voteDataList);
        });
    }

    @Override
    public Observable<VoteData> addNewOption(String voteCode, String password, List<String> newOptions, User user) {
        return Observable.empty();
    }

    @Override
    public Observable<VoteData> pollVote(@NonNull String voteCode, String password, @NonNull List<String> pollOptions, @NonNull User user) {
        return Observable.empty();
    }

    @Override
    public Observable<Boolean> favoriteVote(String voteCode, boolean isFavorite, User user) {
        return Observable.fromCallable(() -> {
            saveFavoriteVote(voteCode, isFavorite, user);
            return isFavorite;
        });
    }

    @Override
    public void saveFavoriteVote(String voteCode, boolean isFavorite, User user) {
        mAppExecutors.diskIO().execute(() -> {
            VoteData voteData = voteDataDao.getVoteByCode(voteCode);
            if (voteData != null) {
                voteData.setIsFavorite(isFavorite);
                voteDataDao.update(voteData);
            }
        });
    }

    @Override
    public Observable<VoteData> createVote(@NonNull VoteData voteSetting, @NonNull List<String> options, File image) {
        return Observable.empty();
    }

    @Override
    public Observable<List<VoteData>> getHotVoteList(int offset, User user) {
        return Observable.fromCallable(() -> voteDataDao.getHotVotes("hot", VoteDataRepository.PAGE_COUNT, offset));
    }

    @Override
    public Observable<List<VoteData>> getNewVoteList(int offset, User user) {
        return Observable.fromCallable(() -> voteDataDao.getNewVotes(VoteDataRepository.PAGE_COUNT, offset));
    }

    @Override
    public Observable<List<VoteData>> getCreateVoteList(int offset, User user, User targetUser) {
        String uid = targetUser != null ? targetUser.getUserCode() : (user != null ? user.getUserCode() : "");
        return Observable.fromCallable(() -> voteDataDao.getCreateVotes(uid, VoteDataRepository.PAGE_COUNT, offset));
    }

    @Override
    public Observable<List<VoteData>> getParticipateVoteList(int offset, User user, User targetUser) {
        return Observable.fromCallable(() -> voteDataDao.getParticipateVotes(VoteDataRepository.PAGE_COUNT, offset));
    }

    @Override
    public Observable<List<VoteData>> getFavoriteVoteList(int offset, User user, User targetUser) {
        return Observable.fromCallable(() -> voteDataDao.getFavoriteVotes(VoteDataRepository.PAGE_COUNT, offset));
    }

    @Override
    public Observable<List<VoteData>> getSearchVoteList(String keyword, int offset, @NonNull User user) {
        if (TextUtils.isEmpty(keyword)) {
            return Observable.just(new ArrayList<>());
        }
        return Observable.fromCallable(() -> voteDataDao.searchVotes(keyword, VoteDataRepository.PAGE_COUNT, offset));
    }

    @VisibleForTesting
    public static void clearInstance() {
        INSTANCE = null;
    }
}
