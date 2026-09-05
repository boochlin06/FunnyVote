package com.heaton.funnyvote.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val userId: String,
    val userName: String = "FunnyVote 使用者",
    val userIcon: String? = null,
    val email: String? = null,
    val gender: String = "unknown"
)
