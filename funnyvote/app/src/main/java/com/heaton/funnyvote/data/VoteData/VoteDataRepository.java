package com.heaton.funnyvote.data.VoteData;

import android.support.annotation.NonNull;
import android.util.Log;

import com.heaton.funnyvote.database.Option;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.database.VoteData;
import com.heaton.funnyvote.ui.main.MainPageTabFragment;

import java.io.File;
import java.util.List;

import javax.annotation.Nullable;

public class VoteDataRepository implements VoteDataSource {
    private static VoteDataRepository INSTANCE = null;
    private final VoteDataSource voteDataRemoteSource;
    private final VoteDataSource voteDataLocalSource;
    public static final int PAGE_COUNT = 20;

    public static VoteDataRepository getInstance(VoteDataSource voteDataLocalSource
            , VoteDataSource voteDataRemoteSource) {
        if (INSTANCE == null) {
            INSTANCE = new VoteDataRepository(voteDataLocalSource, voteDataRemoteSource);
        }
        return INSTANCE;
    }

    public VoteDataRepository(VoteDataSource voteDataLocalSource
            , VoteDataSource voteDataRemoteSource) {
        this.voteDataRemoteSource = voteDataRemoteSource;
        this.voteDataLocalSource = voteDataLocalSource;
    }

    @Override
    public void getVoteData(final String voteCode, final User user, @Nullable final GetVoteDataCallback callback) {
        voteDataRemoteSource.getVoteData(voteCode, user, new GetVoteDataCallback() {
            @Override
            public void onVoteDataLoaded(VoteData voteData) {
                voteDataLocalSource.saveVoteData(voteData);
                callback.onVoteDataLoaded(voteData);
            }

            @Override
            public void onVoteDataNotAvailable() {
                voteDataLocalSource.getVoteData(voteCode, user, new GetVoteDataCallback() {
                    @Override
                    public void onVoteDataLoaded(VoteData voteData) {
                        callback.onVoteDataLoaded(voteData);
                    }

                    @Override
                    public void onVoteDataNotAvailable() {
                        callback.onVoteDataNotAvailable();
                    }
                });

            }
        });
    }

    @Override
    public void saveVoteData(VoteData voteData) {
        voteDataLocalSource.saveVoteData(voteData);
    }

    @Override
    public void getOptions(VoteData voteData, GetVoteOptionsCallback callback) {
        voteDataLocalSource.getOptions(voteData, callback);
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
    public void addNewOption(String voteCode, String password, List<String> newOptions
            , User user, final AddNewOptionCallback callback) {
        voteDataRemoteSource.addNewOption(voteCode, password, newOptions, user, new AddNewOptionCallback() {
            @Override
            public void onSuccess(VoteData voteData) {
                voteDataLocalSource.saveVoteData(voteData);
                callback.onSuccess(voteData);
            }

            @Override
            public void onFailure() {
                callback.onFailure();
            }

            @Override
            public void onPasswordInvalid() {
                callback.onPasswordInvalid();
            }
        });

    }

    @Override
    public void pollVote(@NonNull String voteCode, String password
            , @NonNull List<String> pollOptions, @NonNull User user, @Nullable final PollVoteCallback callback) {
        voteDataRemoteSource.pollVote(voteCode, password, pollOptions, user, new PollVoteCallback() {
            @Override
            public void onSuccess(VoteData voteDataNetwork) {
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
                voteDataLocalSource.saveVoteData(voteDataNetwork);
                callback.onSuccess(voteDataNetwork);
            }

            @Override
            public void onFailure() {
                callback.onFailure();
            }

            @Override
            public void onPasswordInvalid() {
                callback.onPasswordInvalid();
            }
        });
    }

    @Override
    public void favoriteVote(final String voteCode, final boolean isFavorite, final User user, final FavoriteVoteCallback callback) {

        Log.d("favoriteVoteRE","favoriteVote favoriteVote");
        voteDataRemoteSource.favoriteVote(voteCode, isFavorite, user, new FavoriteVoteCallback() {
            @Override
            public void onSuccess(boolean isFavorite) {
                voteDataLocalSource.favoriteVote(voteCode, isFavorite, user, new FavoriteVoteCallback() {
                    @Override
                    public void onSuccess(boolean isFavorite) {
                        callback.onSuccess(isFavorite);
                    }

                    @Override
                    public void onFailure() {
                        callback.onFailure();
                    }
                });
            }

            @Override
            public void onFailure() {
                callback.onFailure();
            }
        });
    }

    @Override
    public void createVote(@NonNull VoteData voteSetting
            , @NonNull List<String> options, File image, final GetVoteDataCallback callback) {
        voteDataRemoteSource.createVote(voteSetting, options, image, new GetVoteDataCallback() {
            @Override
            public void onVoteDataLoaded(VoteData voteData) {
                voteDataLocalSource.saveVoteData(voteData);
                callback.onVoteDataLoaded(voteData);
            }

            @Override
            public void onVoteDataNotAvailable() {
                callback.onVoteDataNotAvailable();
            }
        });
    }

    @Override
    public void getHotVoteList(final int offset, final User user, final GetVoteListCallback callback) {
        voteDataRemoteSource.getHotVoteList(offset, user, new GetVoteListCallback() {
            @Override
            public void onVoteListLoaded(List<VoteData> voteDataList) {
                voteDataLocalSource.saveVoteDataList(voteDataList, offset, MainPageTabFragment.TAB_HOT);
                callback.onVoteListLoaded(voteDataList);
            }

            @Override
            public void onVoteListNotAvailable() {
                voteDataLocalSource.getHotVoteList(offset, user, new GetVoteListCallback() {
                    @Override
                    public void onVoteListLoaded(List<VoteData> voteDataList) {
                        callback.onVoteListLoaded(voteDataList);
                    }

                    @Override
                    public void onVoteListNotAvailable() {
                        callback.onVoteListNotAvailable();
                    }
                });
            }
        });

    }

    @Override
    public void getCreateVoteList(final int offset, final User user, final User targetUser, final GetVoteListCallback callback) {
        voteDataRemoteSource.getCreateVoteList(offset, user, targetUser, new GetVoteListCallback() {
            @Override
            public void onVoteListLoaded(List<VoteData> voteDataList) {
                voteDataLocalSource.saveVoteDataList(voteDataList, offset, MainPageTabFragment.TAB_CREATE);
                callback.onVoteListLoaded(voteDataList);
            }

            @Override
            public void onVoteListNotAvailable() {
                voteDataLocalSource.getCreateVoteList(offset, user, targetUser, new GetVoteListCallback() {
                    @Override
                    public void onVoteListLoaded(List<VoteData> voteDataList) {
                        callback.onVoteListLoaded(voteDataList);
                    }

                    @Override
                    public void onVoteListNotAvailable() {
                        callback.onVoteListNotAvailable();
                    }
                });
            }
        });
    }

    @Override
    public void getParticipateVoteList(final int offset, final User user, final User targetUser, final GetVoteListCallback callback) {
        if (targetUser != null) {
            callback.onVoteListNotAvailable();
            return;
        }
        voteDataRemoteSource.getParticipateVoteList(offset, user, targetUser, new GetVoteListCallback() {
            @Override
            public void onVoteListLoaded(List<VoteData> voteDataList) {
                voteDataLocalSource.saveVoteDataList(voteDataList, offset, MainPageTabFragment.TAB_PARTICIPATE);
                if (voteDataList.size() == 0) {
                    onVoteListNotAvailable();
                } else {
                    callback.onVoteListLoaded(voteDataList);
                }
            }

            @Override
            public void onVoteListNotAvailable() {
                voteDataLocalSource.getParticipateVoteList(offset, user, targetUser, new GetVoteListCallback() {
                    @Override
                    public void onVoteListLoaded(List<VoteData> voteDataList) {
                        callback.onVoteListLoaded(voteDataList);
                    }

                    @Override
                    public void onVoteListNotAvailable() {
                        callback.onVoteListNotAvailable();
                    }
                });
            }
        });
    }

    @Override
    public void getFavoriteVoteList(final int offset, final User user, final User targetUser, final GetVoteListCallback callback) {
        voteDataRemoteSource.getFavoriteVoteList(offset, user, targetUser, new GetVoteListCallback() {
            @Override
            public void onVoteListLoaded(List<VoteData> voteDataList) {
                voteDataLocalSource.saveVoteDataList(voteDataList, offset, MainPageTabFragment.TAB_FAVORITE);
                callback.onVoteListLoaded(voteDataList);
            }

            @Override
            public void onVoteListNotAvailable() {
                voteDataLocalSource.getFavoriteVoteList(offset, user, targetUser, new GetVoteListCallback() {
                    @Override
                    public void onVoteListLoaded(List<VoteData> voteDataList) {
                        callback.onVoteListLoaded(voteDataList);
                    }

                    @Override
                    public void onVoteListNotAvailable() {
                        callback.onVoteListNotAvailable();
                    }
                });
            }
        });
    }

    @Override
    public void getSearchVoteList(final String keyword, final int offset, @NonNull final User user, final GetVoteListCallback callback) {
        voteDataRemoteSource.getSearchVoteList(keyword, offset, user, new GetVoteListCallback() {
            @Override
            public void onVoteListLoaded(List<VoteData> voteDataList) {
                voteDataLocalSource.saveVoteDataList(voteDataList, offset, null);
                callback.onVoteListLoaded(voteDataList);
            }

            @Override
            public void onVoteListNotAvailable() {
                voteDataRemoteSource.getSearchVoteList(keyword, offset, user, new GetVoteListCallback() {
                    @Override
                    public void onVoteListLoaded(List<VoteData> voteDataList) {
                        callback.onVoteListLoaded(voteDataList);
                    }

                    @Override
                    public void onVoteListNotAvailable() {
                        callback.onVoteListNotAvailable();
                    }
                });
            }
        });
    }

    @Override
    public void getNewVoteList(final int offset, final User user, final GetVoteListCallback callback) {
        voteDataRemoteSource.getNewVoteList(offset, user, new GetVoteListCallback() {
            @Override
            public void onVoteListLoaded(List<VoteData> voteDataList) {
                voteDataLocalSource.saveVoteDataList(voteDataList, offset, MainPageTabFragment.TAB_NEW);
                callback.onVoteListLoaded(voteDataList);
            }

            @Override
            public void onVoteListNotAvailable() {
                voteDataLocalSource.getNewVoteList(offset, user, new GetVoteListCallback() {
                    @Override
                    public void onVoteListLoaded(List<VoteData> voteDataList) {
                        callback.onVoteListLoaded(voteDataList);
                    }

                    @Override
                    public void onVoteListNotAvailable() {
                        callback.onVoteListNotAvailable();
                    }
                });
            }
        });
    }

}
