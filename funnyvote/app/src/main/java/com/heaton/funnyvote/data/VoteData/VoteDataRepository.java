package com.heaton.funnyvote.data.VoteData;

import android.support.annotation.NonNull;
import android.util.Log;

import com.heaton.funnyvote.data.Local;
import com.heaton.funnyvote.data.Remote;
import com.heaton.funnyvote.database.Option;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.database.VoteData;

import java.io.File;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class VoteDataRepository implements VoteDataSource {
    private static VoteDataRepository INSTANCE = null;
    private final VoteDataSource voteDataRemoteSource;
    private final VoteDataSource voteDataLocalSource;

    public static final String TAB_HOT = "HOT";
    public static final String TAB_NEW = "NEW";

    public static final String TAB_CREATE = "CREATE";
    public static final String TAB_PARTICIPATE = "PARTICIPATE";
    public static final String TAB_FAVORITE = "FAVORITE";

    public static final int PAGE_COUNT = 20;

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

    @Inject
    public VoteDataRepository(@Local VoteDataSource voteDataLocalSource
            , @Remote VoteDataSource voteDataRemoteSource) {
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

        Log.d("favoriteVoteRE", "favoriteVote favoriteVote");
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
                voteDataLocalSource.saveVoteDataList(voteDataList, offset, TAB_HOT);
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
                voteDataLocalSource.saveVoteDataList(voteDataList, offset, TAB_CREATE);
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
                voteDataLocalSource.saveVoteDataList(voteDataList, offset, TAB_PARTICIPATE);
                callback.onVoteListLoaded(voteDataList);
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
                voteDataLocalSource.saveVoteDataList(voteDataList, offset, TAB_FAVORITE);
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
                voteDataLocalSource.getSearchVoteList(keyword, offset, user, new GetVoteListCallback() {
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
                voteDataLocalSource.saveVoteDataList(voteDataList, offset, TAB_NEW);
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
