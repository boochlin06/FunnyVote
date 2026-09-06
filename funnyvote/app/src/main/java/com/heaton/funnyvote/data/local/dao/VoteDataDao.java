package com.heaton.funnyvote.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.heaton.funnyvote.database.VoteData;

import java.util.List;

@Dao
public interface VoteDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(VoteData voteData);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrReplace(VoteData voteData);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrReplaceInTx(List<VoteData> list);

    @Query("SELECT * FROM vote_data WHERE voteCode = :voteCode LIMIT 1")
    VoteData getVoteByCode(String voteCode);

    @Query("SELECT * FROM vote_data WHERE category = :category ORDER BY displayOrder ASC LIMIT :limit OFFSET :offset")
    List<VoteData> getHotVotes(String category, int limit, int offset);

    @Query("SELECT * FROM vote_data WHERE authorCode = :authorCode ORDER BY startTime DESC LIMIT :limit OFFSET :offset")
    List<VoteData> getCreateVotes(String authorCode, int limit, int offset);

    @Query("SELECT * FROM vote_data WHERE isPolled = 1 ORDER BY startTime DESC LIMIT :limit OFFSET :offset")
    List<VoteData> getParticipateVotes(int limit, int offset);

    @Query("SELECT * FROM vote_data WHERE isFavorite = 1 ORDER BY id DESC LIMIT :limit OFFSET :offset")
    List<VoteData> getFavoriteVotes(int limit, int offset);

    @Query("SELECT * FROM vote_data WHERE title LIKE '%' || :keyword || '%' OR authorName LIKE '%' || :keyword || '%' ORDER BY startTime DESC LIMIT :limit OFFSET :offset")
    List<VoteData> searchVotes(String keyword, int limit, int offset);

    @Query("SELECT * FROM vote_data ORDER BY startTime DESC LIMIT :limit OFFSET :offset")
    List<VoteData> getNewVotes(int limit, int offset);

    @Query("SELECT COUNT(*) FROM vote_data WHERE (isPolled = 1 OR authorCode = :authorCode) AND startTime <= :currentTime")
    long countUserVoteChanges(String authorCode, long currentTime);

    @Update
    void update(VoteData voteData);

    @Query("DELETE FROM vote_data WHERE voteCode = :voteCode")
    void deleteByCode(String voteCode);

    @Query("DELETE FROM vote_data")
    void deleteAll();
}
