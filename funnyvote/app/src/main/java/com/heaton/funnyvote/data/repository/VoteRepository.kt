package com.heaton.funnyvote.data.repository

import com.heaton.funnyvote.data.local.dao.VoteDao
import com.heaton.funnyvote.data.local.entity.VoteWithDetails
import com.heaton.funnyvote.data.remote.MockVoteDataSource
import com.heaton.funnyvote.data.remote.VoteRemoteDataSource
import com.heaton.funnyvote.data.remote.firebase.FirebaseAuthDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoteRepository @Inject constructor(
    private val voteDao: VoteDao,
    private val remoteDataSource: VoteRemoteDataSource,
    private val authDataSource: FirebaseAuthDataSource
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
        return remoteDataSource.getAllVotes().catch {
            emitAll(voteDao.getAllVotes().onStart { checkAndSeedData() })
        }
    }

    fun getVotesByCategory(category: String): Flow<List<VoteWithDetails>> {
        return remoteDataSource.getVotesByCategory(category).catch {
            val fallback = if (category == "favorite") {
                voteDao.getFavoriteVotes()
            } else {
                voteDao.getVotesByCategory(category)
            }
            emitAll(fallback.onStart { checkAndSeedData() })
        }
    }

    fun searchVotes(query: String): Flow<List<VoteWithDetails>> {
        return remoteDataSource.searchVotes(query).catch {
            emitAll(voteDao.searchVotes(query))
        }
    }

    fun getVoteDetail(voteCode: String): Flow<VoteWithDetails?> {
        return remoteDataSource.getVoteDetail(voteCode).catch {
            emitAll(voteDao.getVoteByCode(voteCode))
        }
    }

    fun getUserParticipatedVotes(userId: String): Flow<List<VoteWithDetails>> {
        return remoteDataSource.getUserParticipatedVotes(userId).catch {
            emitAll(voteDao.getAllVotes().map { list -> list.filter { it.vote.isVoted } })
        }
    }

    fun getVotesByAuthor(authorId: String): Flow<List<VoteWithDetails>> {
        return remoteDataSource.getVotesByAuthor(authorId).catch {
            emitAll(voteDao.getAllVotes().map { list -> list.filter { it.vote.authorId == authorId } })
        }
    }

    suspend fun loadMoreVotes(category: String, lastVoteCode: String, limit: Long = 20): Result<List<VoteWithDetails>> {
        return remoteDataSource.loadMoreVotes(category, lastVoteCode, limit)
    }

    suspend fun toggleFavorite(voteCode: String, currentFavorite: Boolean) {
        val uid = authDataSource.getCurrentUserId() ?: authDataSource.ensureAuthenticated()
        remoteDataSource.toggleFavorite(voteCode, !currentFavorite, uid)
        voteDao.updateFavorite(voteCode, !currentFavorite)
    }

    suspend fun toggleFavorite(voteCode: String) {
        val current = voteDao.getVoteByCodeOnce(voteCode)?.vote
        val isFav = current?.isFavorite ?: false
        toggleFavorite(voteCode, isFav)
    }

    suspend fun addNewOption(voteCode: String, optionTitle: String): Result<Unit> {
        return runCatching {
            val uid = authDataSource.ensureAuthenticated()
            val remoteResult = remoteDataSource.addNewOption(voteCode, optionTitle, uid)
            if (remoteResult.isFailure) {
                // Fallback to local
                val optCode = "opt_${System.currentTimeMillis()}_${(100..999).random()}"
                val newOption = com.heaton.funnyvote.data.local.entity.OptionEntity(
                    voteCode = voteCode,
                    optionCode = optCode,
                    title = optionTitle,
                    count = 0,
                    isUserChoiced = false
                )
                voteDao.insertOption(newOption)
            }
        }
    }

    suspend fun submitVote(voteCode: String, selectedOptionCodes: List<String>): Result<Unit> {
        return runCatching {
            val uid = authDataSource.ensureAuthenticated()
            val remoteResult = remoteDataSource.submitVote(voteCode, selectedOptionCodes, uid)
            if (remoteResult.isFailure) {
                // Fallback to local
                selectedOptionCodes.forEach { optCode ->
                    voteDao.incrementOptionCount(optCode)
                }
                voteDao.markVoteCompleted(voteCode)
            }
        }
    }

    suspend fun createNewVote(
        title: String,
        options: List<String>,
        isPrivate: Boolean,
        password: String?,
        isMultiChoice: Boolean,
        description: String? = null,
        imageUrl: String? = null,
        endTime: Long? = null
    ): Result<String> {
        return runCatching {
            val uid = authDataSource.ensureAuthenticated()
            val remoteResult = remoteDataSource.createVote(
                title = title,
                options = options,
                isPrivate = isPrivate,
                password = password,
                isMultiChoice = isMultiChoice,
                authorId = uid,
                authorName = "FunnyVote 使用者",
                description = description,
                imageUrl = imageUrl,
                endTime = endTime
            )

            if (remoteResult.isSuccess) {
                remoteResult.getOrThrow()
            } else {
                // Fallback to local
                val created = MockVoteDataSource.createVote(
                    title = title,
                    options = options,
                    isPrivate = isPrivate,
                    password = password,
                    isMultiChoice = isMultiChoice
                )
                voteDao.insertVote(created.vote.copy(description = description, imageUrl = imageUrl, endTime = endTime ?: 0L))
                voteDao.insertOptions(created.options)
                created.vote.voteCode
            }
        }
    }

    suspend fun verifyPollPassword(voteCode: String, password: String): Boolean {
        return remoteDataSource.verifyPollPassword(voteCode, password)
    }
}
