package com.heaton.funnyvote.data.VoteData;

import android.support.annotation.NonNull;

import com.heaton.funnyvote.database.Option;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.database.VoteData;

import java.io.File;
import java.util.List;

import javax.annotation.Nullable;

public interface VoteDataSource {
    interface GetVoteDataCallback {
        void onVoteDataLoaded(VoteData voteData);

        void onVoteDataNotAvailable();
    }

    interface GetVoteOptionsCallback {
        void onVoteOptionsLoaded(List<Option> optionList);

        void onVoteOptionsNotAvailable();
    }

    interface PollVoteCallback {
        void onSuccess(VoteData voteData);

        void onFailure();

        void onPasswordInvalid();
    }

    interface FavoriteVoteCallback {
        void onSuccess(boolean isFavorite);

        void onFailure();
    }

    interface AddNewOptionCallback {
        void onSuccess(VoteData voteData);

        void onFailure();

        void onPasswordInvalid();
    }

    interface GetVoteListCallback {
        void onVoteListLoaded(List<VoteData> voteDataList);

        void onVoteListNotAvailable();
    }


    void getVoteData(String voteCode, User user, @Nullable GetVoteDataCallback callback);

    void saveVoteData(VoteData voteData);

    void getOptions(VoteData voteData, GetVoteOptionsCallback callback);

    void saveOptions(List<Option> optionList);

    void saveVoteDataList(List<VoteData> voteDataList, int offset, String tab);

    void addNewOption(String voteCode, String password, List<String> newOptions, User user
            , AddNewOptionCallback callback);

    void pollVote(@NonNull String voteCode, String password, @NonNull List<String> pollOptions
            , @NonNull User user, @Nullable PollVoteCallback callback);

    void favoriteVote(String voteCode, boolean isFavorite, User user, FavoriteVoteCallback callback);

    void createVote(@NonNull VoteData voteSetting, @NonNull List<String> options, File image
            , GetVoteDataCallback callback);

    void getHotVoteList(int offset, User user, GetVoteListCallback callback);

    void getNewVoteList(int offset, User user, GetVoteListCallback callback);

    void getCreateVoteList(int offset, User user, User targetUser, GetVoteListCallback callback);

    void getParticipateVoteList(int offset, User user, User targetUser, GetVoteListCallback callback);

    void getFavoriteVoteList(int offset, User user, User targetUser, GetVoteListCallback callback);

    void getSearchVoteList(String keyword, int offset, @NonNull User user, GetVoteListCallback callback);
}
