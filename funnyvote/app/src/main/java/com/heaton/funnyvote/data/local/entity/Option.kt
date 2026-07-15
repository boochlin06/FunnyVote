package com.heaton.funnyvote.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "options")
data class Option(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val voteCode: String = "",
    
    @SerializedName("ot")
    val title: String? = null,
    
    @SerializedName("v")
    val count: Int? = 0,
    
    @SerializedName("oc")
    val code: String? = null,
    
    @SerializedName("voted")
    val isUserChoiced: Boolean = false
)
