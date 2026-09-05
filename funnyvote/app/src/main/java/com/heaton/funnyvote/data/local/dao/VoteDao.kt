package com.heaton.funnyvote.data.local.dao

import androidx.room.*
import com.heaton.funnyvote.data.local.entity.OptionEntity
import com.heaton.funnyvote.data.local.entity.VoteEntity
import com.heaton.funnyvote.data.local.entity.VoteWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface VoteDao {
    @Transaction
    @Query("SELECT * FROM votes ORDER BY createdAt DESC")
    fun getAllVotes(): Flow<List<VoteWithDetails>>

    @Transaction
    @Query("SELECT * FROM votes WHERE category = :category ORDER BY createdAt DESC")
    fun getVotesByCategory(category: String): Flow<List<VoteWithDetails>>

    @Transaction
    @Query("SELECT * FROM votes WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteVotes(): Flow<List<VoteWithDetails>>

    @Transaction
    @Query("SELECT * FROM votes WHERE title LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchVotes(query: String): Flow<List<VoteWithDetails>>

    @Transaction
    @Query("SELECT * FROM votes WHERE voteCode = :voteCode LIMIT 1")
    fun getVoteByCode(voteCode: String): Flow<VoteWithDetails?>

    @Transaction
    @Query("SELECT * FROM votes WHERE voteCode = :voteCode LIMIT 1")
    suspend fun getVoteByCodeOnce(voteCode: String): VoteWithDetails?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVote(vote: VoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVotes(votes: List<VoteEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOptions(options: List<OptionEntity>)

    @Query("UPDATE votes SET isFavorite = :isFavorite WHERE voteCode = :voteCode")
    suspend fun updateFavorite(voteCode: String, isFavorite: Boolean)

    @Query("UPDATE votes SET isVoted = 1, totalVotedCount = totalVotedCount + 1 WHERE voteCode = :voteCode")
    suspend fun markVoteCompleted(voteCode: String)

    @Query("UPDATE options SET count = count + 1, isUserChoiced = 1 WHERE optionCode = :optionCode")
    suspend fun incrementOptionCount(optionCode: String)

    @Query("SELECT COUNT(*) FROM votes")
    suspend fun getVoteCount(): Int
}
