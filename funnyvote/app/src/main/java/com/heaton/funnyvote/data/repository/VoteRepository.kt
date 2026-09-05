package com.heaton.funnyvote.data.repository

import com.heaton.funnyvote.data.local.dao.VoteDao
import com.heaton.funnyvote.data.local.entity.VoteWithDetails
import com.heaton.funnyvote.data.remote.FunnyVoteApi
import com.heaton.funnyvote.data.remote.MockVoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoteRepository @Inject constructor(
    private val voteDao: VoteDao,
    private val api: FunnyVoteApi
) {
    private suspend fun checkAndSeedData() {
        if (voteDao.getVoteCount() == 0) {
            val seeds = MockVoteDataSource.getInitialSeedData()
            seeds.forEach { item ->
                voteDao.insertVote(item.vote)
                voteDao.insertOptions(item.options)
            }
        }
    }

    fun getAllVotes(): Flow<List<VoteWithDetails>> {
        return voteDao.getAllVotes().onStart { checkAndSeedData() }
    }

    fun getVotesByCategory(category: String): Flow<List<VoteWithDetails>> {
        return (if (category == "favorite") {
            voteDao.getFavoriteVotes()
        } else {
            voteDao.getVotesByCategory(category)
        }).onStart { checkAndSeedData() }
    }

    fun searchVotes(query: String): Flow<List<VoteWithDetails>> {
        return voteDao.searchVotes(query)
    }

    fun getVoteDetail(voteCode: String): Flow<VoteWithDetails?> {
        return voteDao.getVoteByCode(voteCode)
    }

    suspend fun toggleFavorite(voteCode: String, currentFavorite: Boolean) {
        voteDao.updateFavorite(voteCode, !currentFavorite)
    }

    suspend fun submitVote(voteCode: String, selectedOptionCodes: List<String>): Result<Unit> {
        return runCatching {
            selectedOptionCodes.forEach { optCode ->
                voteDao.incrementOptionCount(optCode)
            }
            voteDao.markVoteCompleted(voteCode)
        }
    }

    suspend fun createNewVote(
        title: String,
        options: List<String>,
        isPrivate: Boolean,
        password: String?,
        isMultiChoice: Boolean
    ): Result<String> {
        return runCatching {
            val created = MockVoteDataSource.createVote(
                title = title,
                options = options,
                isPrivate = isPrivate,
                password = password,
                isMultiChoice = isMultiChoice
            )
            voteDao.insertVote(created.vote)
            voteDao.insertOptions(created.options)
            created.vote.voteCode
        }
    }
}
