package com.heaton.funnyvote.data.VoteData;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.heaton.funnyvote.database.Option;
import com.heaton.funnyvote.database.OptionDao;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.database.VoteData;
import com.heaton.funnyvote.database.VoteDataDao;
import com.heaton.funnyvote.ui.main.MainPageTabFragment;
import com.heaton.funnyvote.utils.AppExecutors;

import org.greenrobot.greendao.query.QueryBuilder;
import org.greenrobot.greendao.query.WhereCondition;
import org.greenrobot.greendao.rx.RxQuery;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rx.Observable;
import rx.schedulers.Schedulers;

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

    public static LocalVoteDataSource getInstance(@NonNull VoteDataDao voteDataDao
            , @NonNull OptionDao optionDao, AppExecutors appExecutors) {
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
        RxQuery<VoteData> rxQuery = voteDataDao.queryBuilder()
                .where(VoteDataDao.Properties.VoteCode.eq(voteCode)).rx();
        return rxQuery.unique();
    }

    @Override
    public void saveVoteData(VoteData voteDataNetwork) {
        List<Option> optionList = voteDataNetwork.getNetOptions();
        voteDataNetwork.setOptionCount(optionList.size());
        int maxOption = 0;
        for (int i = 0; i < optionList.size(); i++) {
            Option option = optionList.get(i);
            option.setVoteCode(voteDataNetwork.getVoteCode());
            if (option.getCount() == null) {
                option.setCount(0);
            }
            option.setId(null);
            if (i == 0) {
                voteDataNetwork.setOption1Title(option.getTitle());
                voteDataNetwork.setOption1Code(option.getCode());
                voteDataNetwork.setOption1Count(option.getCount());
                voteDataNetwork.setOption1Polled(option.getIsUserChoiced());
            } else if (i == 1) {
                voteDataNetwork.setOption2Title(option.getTitle());
                voteDataNetwork.setOption2Code(option.getCode());
                voteDataNetwork.setOption2Count(option.getCount());
                voteDataNetwork.setOption2Polled(option.getIsUserChoiced());
            }
            if (option.getCount() > maxOption && option.getCount() >= 1) {
                maxOption = option.getCount();
                voteDataNetwork.setOptionTopCount(option.getCount());
                voteDataNetwork.setOptionTopCode(option.getCode());
                voteDataNetwork.setOptionTopTitle(option.getTitle());
                voteDataNetwork.setOptionTopPolled(option.getIsUserChoiced());
            }
            if (option.getIsUserChoiced()) {
                voteDataNetwork.setOptionUserChoiceCode(option.getCode());
                voteDataNetwork.setOptionUserChoiceTitle(option.getTitle());
                voteDataNetwork.setOptionUserChoiceCount(option.getCount());
            }

            option.dumpDetail();
        }
        voteDataDao.queryBuilder().where(VoteDataDao.Properties.VoteCode.eq(voteDataNetwork.getVoteCode())).buildDelete()
                .executeDeleteWithoutDetachingEntities();
        optionDao.queryBuilder().where(OptionDao.Properties.VoteCode.eq(voteDataNetwork.getVoteCode())).buildDelete()
                .executeDeleteWithoutDetachingEntities();
        voteDataDao.insertOrReplace(voteDataNetwork);
        for (int i = 0; i < voteDataNetwork.getNetOptions().size(); i++) {
            voteDataNetwork.getNetOptions().get(i).setVoteCode(voteDataNetwork.getVoteCode());
        }
        optionDao.insertOrReplaceInTx(voteDataNetwork.getNetOptions());
        //mAppExecutors.diskIO().execute(new SaveDBRunnable(voteDataNetwork));
    }


    @Override
    public Observable<List<Option>> getOptions(VoteData voteData) {
        return optionDao.queryBuilder()
                .where(OptionDao.Properties.VoteCode.eq(voteData.getVoteCode())).rx().list();
    }

    @Override
    public void saveOptions(final List<Option> optionList) {
        mAppExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                optionDao.insertOrReplaceInTx(optionList);
            }
        });
    }

    @Override
    public void saveVoteDataList(final List<VoteData> voteDataList, int offset, String tab) {

        ArrayList<WhereCondition> whereConditions = new ArrayList<WhereCondition>();
        //List<String> favVoteCodeList = new ArrayList<>();

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
            //Log.d(TAG, tab + "," + i + ",save item polled:" + voteData.getIsPolled());
//                if (!isLoginUser) {
//                    //todo: temp reset fav for login user
//                    voteData.setIsFavorite(favVoteCodeList.contains(voteData.getVoteCode()));
//                }
            whereConditions.add(VoteDataDao.Properties.VoteCode.eq(voteData.getVoteCode()));
        }
        WhereCondition[] conditionsArray = new WhereCondition[whereConditions.size()];
        conditionsArray = whereConditions.toArray(conditionsArray);
        QueryBuilder queryBuilder = voteDataDao.queryBuilder();
        if (conditionsArray.length > 2) {
            queryBuilder.whereOr(conditionsArray[0], conditionsArray[1], Arrays.copyOfRange(conditionsArray
                    , 2, conditionsArray.length));
        } else if (conditionsArray.length == 2) {
            queryBuilder.whereOr(conditionsArray[0], conditionsArray[1]);
        } else if (conditionsArray.length == 1) {
            queryBuilder.where(conditionsArray[0]);
        } else if (conditionsArray.length == 0) {
            return;
        }
        queryBuilder.buildDelete().executeDeleteWithoutDetachingEntities();
        voteDataDao.insertOrReplaceInTx(voteDataList);

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
        Observable<Boolean> observable = voteDataDao.queryBuilder()
                .where(VoteDataDao.Properties.VoteCode.eq(voteCode)).rx().list()
                .subscribeOn(Schedulers.io())
                .map(voteDataList -> {
                    if (voteDataList.size() > 0) {
                        VoteData data = new VoteData();
                        data.setIsFavorite(isFavorite);
                        data.setVoteCode(voteCode);
                        VoteData voteData = voteDataList.get(0);
                        data.setId(voteData.getId());
                        voteDataDao.update(data);
                    }
                    return isFavorite;
                });
        return observable;
    }

    @Override
    public void saveFavoriteVote(String voteCode, boolean isFavorite, User user) {
        List<VoteData> voteDataList = voteDataDao.queryBuilder()
                .where(VoteDataDao.Properties.VoteCode.eq(voteCode)).list();
        if (voteDataList.size() > 0) {
            VoteData data = new VoteData();
            data.setIsFavorite(isFavorite);
            data.setVoteCode(voteCode);
            VoteData voteData = voteDataList.get(0);
            data.setId(voteData.getId());
            voteDataDao.update(data);
        }
    }


    @Override
    public Observable<VoteData> createVote(@NonNull VoteData voteSetting, @NonNull List<String> options, File image) {
        return Observable.empty();
    }


    @Override
    public Observable<List<VoteData>> getHotVoteList(int offset, User user) {
        return voteDataDao.queryBuilder().where(VoteDataDao.Properties.Category.eq("hot")
                , VoteDataDao.Properties.StartTime.le(System.currentTimeMillis())).offset(offset)
                .orderAsc(VoteDataDao.Properties.DisplayOrder)
                .limit(VoteDataRepository.PAGE_COUNT).rx()
                .list();
    }


    @Override
    public Observable<List<VoteData>> getCreateVoteList(int offset, User user, User targetUser) {
        return voteDataDao.queryBuilder()
                .where(VoteDataDao.Properties.AuthorCode.eq(user.getUserCode()))
                .limit(VoteDataRepository.PAGE_COUNT)
                .offset(offset).orderDesc(VoteDataDao.Properties.StartTime).rx().list();
    }


    @Override
    public Observable<List<VoteData>> getParticipateVoteList(int offset, User user, User targetUser) {
        return voteDataDao.queryBuilder().where(VoteDataDao.Properties.IsPolled.eq(true))
                .limit(VoteDataRepository.PAGE_COUNT)
                .offset(offset).orderDesc(VoteDataDao.Properties.StartTime).rx().list();
    }


    @Override
    public Observable<List<VoteData>> getFavoriteVoteList(int offset, User user, User targetUser) {
        return voteDataDao.queryBuilder()
                .where(VoteDataDao.Properties.IsFavorite.eq(true))
                .offset(offset).limit(VoteDataRepository.PAGE_COUNT).rx().list();
    }


    @Override
    public Observable<List<VoteData>> getSearchVoteList(String keyword, int offset, @NonNull User user) {
        return voteDataDao.queryBuilder()
                .whereOr(VoteDataDao.Properties.Title.like(keyword)
                        , VoteDataDao.Properties.AuthorName.like(keyword))
                .orderDesc(VoteDataDao.Properties.StartTime)
                .offset(offset).limit(VoteDataRepository.PAGE_COUNT).rx().list();
    }

    @Override
    public Observable<List<VoteData>> getNewVoteList(int offset, User user) {
        return voteDataDao.queryBuilder().where(VoteDataDao.Properties.StartTime.le(System.currentTimeMillis()))
                .orderDesc(VoteDataDao.Properties.StartTime)
                .orderDesc().offset(offset).limit(VoteDataRepository.PAGE_COUNT)
                .rx().list();
    }


    @VisibleForTesting
    static void clearInstance() {
        INSTANCE = null;
    }
}
