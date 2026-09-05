package com.heaton.funnyvote.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "votes")
data class VoteEntity(
    @PrimaryKey
    val voteCode: String,
    val title: String,
    val authorName: String = "匿名",
    val authorIcon: String? = null,
    val category: String = "hot", // hot, new, favorite
    val minOption: Int = 1,
    val maxOption: Int = 1,
    val isNeedPassword: Boolean = false,
    val password: String? = null,
    val isUserCanAddOption: Boolean = false,
    val isCanPreviewResult: Boolean = true,
    val isFavorite: Boolean = false,
    val isVoted: Boolean = false,
    val totalVotedCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
