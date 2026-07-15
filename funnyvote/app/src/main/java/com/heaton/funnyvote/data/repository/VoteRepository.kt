package com.heaton.funnyvote.data.repository

import com.heaton.funnyvote.data.local.dao.VoteDao
import com.heaton.funnyvote.data.local.entity.VoteData
import com.heaton.funnyvote.data.remote.FunnyVoteApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoteRepository @Inject constructor(
    private val voteDao: VoteDao,
    private val funnyVoteApi: FunnyVoteApi
) {
    fun getHotVotes(): Flow<Result<List<VoteData>>> = flow {
        // Emit local data first (optional caching strategy)
        val localVotes = voteDao.getAllVotes()
        if (localVotes.isNotEmpty()) {
            emit(Result.success(localVotes))
        }

        try {
            // Fetch from remote
            val response = funnyVoteApi.getVoteList(0, 20, "hot", null, null)
            if (response.isSuccessful) {
                val remoteVotes = response.body() ?: emptyList()
                // Update local DB
                voteDao.clearAll()
                voteDao.insertAll(remoteVotes)
                
                // Emit new data
                emit(Result.success(remoteVotes))
            } else {
                emit(Result.failure(Exception("API Error: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun getVoteDetail(voteCode: String): Result<VoteData> {
        return try {
            val response = funnyVoteApi.getVote(voteCode, "", "")
            if (response.isSuccessful && response.body() != null) {
                val vote = response.body()!!
                voteDao.insert(vote)
                Result.success(vote)
            } else {
                // Fallback to local
                val localVote = voteDao.getVoteByCode(voteCode)
                if (localVote != null) Result.success(localVote) 
                else Result.failure(Exception("Vote not found"))
            }
        } catch (e: Exception) {
            val localVote = voteDao.getVoteByCode(voteCode)
            if (localVote != null) Result.success(localVote) 
            else Result.failure(e)
        }
    }
}
