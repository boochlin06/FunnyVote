package com.heaton.funnyvote.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "options",
    foreignKeys = [
        ForeignKey(
            entity = VoteEntity::class,
            parentColumns = ["voteCode"],
            childColumns = ["voteCode"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("voteCode")]
)
data class OptionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val voteCode: String,
    val optionCode: String,
    val title: String,
    val count: Int = 0,
    val isUserChoiced: Boolean = false
)
