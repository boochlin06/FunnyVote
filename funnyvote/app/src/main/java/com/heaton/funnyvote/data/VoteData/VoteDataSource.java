package com.heaton.funnyvote.data.VoteData;

import androidx.annotation.NonNull;

import com.heaton.funnyvote.database.Option;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.database.VoteData;

import java.io.File;
import java.util.List;

import io.reactivex.Observable;

public interface VoteDataSource {

    Observable<VoteData> getVoteData(String voteCode, User user);

    void saveVoteData(VoteData voteData);

    Observable<List<Option>> getOptions(VoteData voteData);

    void saveOptions(List<Option> optionList);

    void saveVoteDataList(List<VoteData> voteDataList, int offset, String tab);

    Observable<VoteData> addNewOption(String voteCode, String password, List<String> newOptions, User user);

    Observable<VoteData> pollVote(@NonNull String voteCode, String password, @NonNull List<String> pollOptions, @NonNull User user);

    Observable<Boolean> favoriteVote(String voteCode, boolean isFavorite, User user);

    void saveFavoriteVote(String voteCode, boolean isFavorite, User user);

    Observable<VoteData> createVote(@NonNull VoteData voteSetting, @NonNull List<String> options, File image);

    Observable<List<VoteData>> getHotVoteList(int offset, User user);

    Observable<List<VoteData>> getNewVoteList(int offset, User user);

    Observable<List<VoteData>> getCreateVoteList(int offset, User user, User targetUser);

    Observable<List<VoteData>> getParticipateVoteList(int offset, User user, User targetUser);

    Observable<List<VoteData>> getFavoriteVoteList(int offset, User user, User targetUser);

    Observable<List<VoteData>> getSearchVoteList(String keyword, int offset, @NonNull User user);
}

