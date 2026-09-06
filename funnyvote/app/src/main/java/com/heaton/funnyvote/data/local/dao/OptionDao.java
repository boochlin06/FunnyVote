package com.heaton.funnyvote.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.heaton.funnyvote.database.Option;

import java.util.List;

@Dao
public interface OptionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Option> options);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Option option);

    @Query("SELECT * FROM options WHERE voteCode = :voteCode ORDER BY id ASC")
    List<Option> getOptionsByVoteCode(String voteCode);

    @Query("DELETE FROM options WHERE voteCode = :voteCode")
    void deleteByVoteCode(String voteCode);

    @Query("DELETE FROM options")
    void deleteAll();
}
