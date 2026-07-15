package com.heaton.funnyvote.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.heaton.funnyvote.data.local.entity.VoteData

@Dao
interface VoteDao {
    @Query("SELECT * FROM votes ORDER BY displayOrder ASC")
    suspend fun getAllVotes(): List<VoteData>

    @Query("SELECT * FROM votes WHERE voteCode = :code LIMIT 1")
    suspend fun getVoteByCode(code: String): VoteData?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(votes: List<VoteData>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vote: VoteData)

    @Query("DELETE FROM votes")
    suspend fun clearAll()
}
