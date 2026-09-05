package com.heaton.funnyvote.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.serialization.Serializable

@Serializable
data class VoteWithDetails(
    @Embedded
    val vote: VoteEntity,
    @Relation(
        parentColumn = "voteCode",
        entityColumn = "voteCode"
    )
    val options: List<OptionEntity> = emptyList()
)
