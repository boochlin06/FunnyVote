package com.heaton.funnyvote.data.VoteData;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.heaton.funnyvote.data.local.dao.OptionDao;
import com.heaton.funnyvote.data.local.dao.VoteDataDao;
import com.heaton.funnyvote.database.Option;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.database.VoteData;
import com.heaton.funnyvote.utils.AppExecutors;

import java.io.File;
import java.util.List;

public class LocalVoteDataSource implements VoteDataSource {
    private static final String TAG = LocalVoteDataSource.class.getSimpleName();
    private VoteDataDao voteDataDao;
    private OptionDao optionDao;
    private static volatile LocalVoteDataSource INSTANCE;
    private AppExecutors mAppExecutors;

    public LocalVoteDataSource(@NonNull VoteDataDao voteDataDao, OptionDao optionDao, AppExecutors appExecutors) {
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
    public void getVoteData(final String voteCode, User user, @Nullable final GetVoteDataCallback callback) {
        mAppExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                if (TextUtils.isEmpty(voteCode)) {
                    if (callback != null) callback.onVoteDataNotAvailable();
                    return;
                }
                final VoteData voteData = voteDataDao.getVoteByCode(voteCode);
                if (voteData != null) {
                    List<Option> options = optionDao.getOptionsByVoteCode(voteCode);
                    voteData.setOptions(options);
                    mAppExecutors.mainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            if (callback != null) callback.onVoteDataLoaded(voteData);
                        }
                    });
                } else {
                    mAppExecutors.mainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            if (callback != null) callback.onVoteDataNotAvailable();
                        }
                    });
                }
            }
        });
    }

    @Override
    public void saveVoteData(final VoteData voteDataNetwork) {
        List<Option> optionList = voteDataNetwork.getNetOptions();
        if (optionList != null) {
            voteDataNetwork.setOptionCount(optionList.size());
            int maxOption = 0;
            for (int i = 0; i < optionList.size(); i++) {
                Option option = optionList.get(i);
                option.setVoteCode(voteDataNetwork.getVoteCode());
                if (option.getCount() == null) {
                    option.setCount(0);
                }
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
            }
        }
        mAppExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                voteDataDao.deleteByCode(voteDataNetwork.getVoteCode());
                optionDao.deleteByVoteCode(voteDataNetwork.getVoteCode());
                voteDataDao.insert(voteDataNetwork);
                if (voteDataNetwork.getNetOptions() != null) {
                    for (Option opt : voteDataNetwork.getNetOptions()) {
                        opt.setVoteCode(voteDataNetwork.getVoteCode());
                    }
                    optionDao.insertAll(voteDataNetwork.getNetOptions());
                }
            }
        });
    }

    @Override
    public void getOptions(final VoteData voteData, final GetVoteOptionsCallback callback) {
        mAppExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                final List<Option> optionList = optionDao.getOptionsByVoteCode(voteData.getVoteCode());
                mAppExecutors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (optionList != null && optionList.size() >= 2) {
                            callback.onVoteOptionsLoaded(optionList);
                        } else {
                            callback.onVoteOptionsNotAvailable();
                        }
                    }
                });
            }
        });
    }

    @Override
    public void saveOptions(final List<Option> optionList) {
        mAppExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                optionDao.insertAll(optionList);
            }
        });
    }

    @Override
    public void saveVoteDataList(final List<VoteData> voteDataList, final int offset, final String tab) {
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
            if (tab != null && tab.equals(VoteDataRepository.TAB_HOT)) {
                voteData.setDisplayOrder((offset) * VoteDataRepository.PAGE_COUNT + i);
                voteData.setCategory("hot");
            } else {
                voteData.setCategory(null);
            }
        }
        mAppExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                for (VoteData data : voteDataList) {
                    voteDataDao.deleteByCode(data.getVoteCode());
                }
                voteDataDao.insertOrReplaceInTx(voteDataList);
            }
        });
    }

    @Override
    public void addNewOption(String voteCode, String password, List<String> newOptions, User user, AddNewOptionCallback callback) {
    }

    @Override
    public void pollVote(@NonNull String voteCode, String password, @NonNull List<String> pollOptions, @NonNull User user, @Nullable PollVoteCallback callback) {
    }

    @Override
    public void favoriteVote(final String voteCode, final boolean isFavorite, User user, final FavoriteVoteCallback callback) {
        mAppExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                VoteData voteData = voteDataDao.getVoteByCode(voteCode);
                if (voteData != null) {
                    voteData.setIsFavorite(isFavorite);
                    voteDataDao.update(voteData);
                    mAppExecutors.mainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            if (callback != null) callback.onSuccess(isFavorite);
                        }
                    });
                }
            }
        });
    }

    @Override
    public void createVote(@NonNull VoteData voteSetting, @NonNull List<String> options, File image, GetVoteDataCallback callback) {
    }

    @Override
    public void getHotVoteList(final int offset, User user, final GetVoteListCallback callback) {
        mAppExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                final List<VoteData> list = voteDataDao.getHotVotes("hot", VoteDataRepository.PAGE_COUNT, offset);
                mAppExecutors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        callback.onVoteListLoaded(list);
                    }
                });
            }
        });
    }

    @Override
    public void getCreateVoteList(final int offset, final User user, User targetUser, final GetVoteListCallback callback) {
        if (targetUser == null && user != null) {
            mAppExecutors.diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    final List<VoteData> list = voteDataDao.getCreateVotes(user.getUserCode(), VoteDataRepository.PAGE_COUNT, offset);
                    mAppExecutors.mainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            callback.onVoteListLoaded(list);
                        }
                    });
                }
            });
        } else {
            callback.onVoteListNotAvailable();
        }
    }

    @Override
    public void getParticipateVoteList(final int offset, final User user, User targetUser, final GetVoteListCallback callback) {
        if (targetUser == null) {
            mAppExecutors.diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    final List<VoteData> list = voteDataDao.getParticipateVotes(VoteDataRepository.PAGE_COUNT, offset);
                    mAppExecutors.mainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            callback.onVoteListLoaded(list);
                        }
                    });
                }
            });
        } else {
            callback.onVoteListNotAvailable();
        }
    }

    @Override
    public void getFavoriteVoteList(final int offset, User user, User targetUser, final GetVoteListCallback callback) {
        if (targetUser == null) {
            mAppExecutors.diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    final List<VoteData> list = voteDataDao.getFavoriteVotes(VoteDataRepository.PAGE_COUNT, offset);
                    mAppExecutors.mainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            callback.onVoteListLoaded(list);
                        }
                    });
                }
            });
        } else {
            callback.onVoteListNotAvailable();
        }
    }

    @Override
    public void getSearchVoteList(final String keyword, final int offset, @NonNull User user, final GetVoteListCallback callback) {
        if (TextUtils.isEmpty(keyword)) {
            callback.onVoteListNotAvailable();
            return;
        }
        mAppExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                final List<VoteData> list = voteDataDao.searchVotes(keyword, VoteDataRepository.PAGE_COUNT, offset);
                mAppExecutors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        callback.onVoteListLoaded(list);
                    }
                });
            }
        });
    }

    @Override
    public void getNewVoteList(final int offset, User user, final GetVoteListCallback callback) {
        mAppExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                final List<VoteData> list = voteDataDao.getNewVotes(VoteDataRepository.PAGE_COUNT, offset);
                mAppExecutors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        callback.onVoteListLoaded(list);
                    }
                });
            }
        });
    }

    @VisibleForTesting
    static void clearInstance() {
        INSTANCE = null;
    }
}
