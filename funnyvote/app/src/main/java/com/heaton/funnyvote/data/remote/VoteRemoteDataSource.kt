package com.heaton.funnyvote.data.remote

import com.heaton.funnyvote.data.local.entity.VoteWithDetails
import kotlinx.coroutines.flow.Flow

interface VoteRemoteDataSource {
    fun getAllVotes(): Flow<List<VoteWithDetails>>
    fun getVotesByCategory(category: String): Flow<List<VoteWithDetails>>
    fun getVoteDetail(voteCode: String): Flow<VoteWithDetails?>
    fun searchVotes(query: String): Flow<List<VoteWithDetails>>
    suspend fun submitVote(voteCode: String, selectedOptionCodes: List<String>, userId: String): Result<Unit>
    suspend fun addNewOption(voteCode: String, optionTitle: String, userId: String): Result<Unit>
    suspend fun createVote(
        title: String,
        options: List<String>,
        isPrivate: Boolean,
        password: String?,
        isMultiChoice: Boolean,
        authorId: String,
        authorName: String
    ): Result<String>
    suspend fun toggleFavorite(voteCode: String, isFavorite: Boolean, userId: String): Result<Unit>
}
