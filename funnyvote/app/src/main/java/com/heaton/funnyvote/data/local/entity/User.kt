package com.heaton.funnyvote.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @SerializedName("nickname")
    val userName: String? = null,
    val email: String? = null,
    val userID: String? = null,
    
    @SerializedName("guest")
    val userCode: String? = null,
    
    @SerializedName("img")
    val userIcon: String? = null,
    
    val type: Int = 0,
    val gender: String? = null,
    val minAge: Int = 0,
    val maxAge: Int = 0
) {
    companion object {
        const val TYPE_FACEBOOK = 100
        const val TYPE_GOOGLE = 101
        const val TYPE_TWITTER = 102
        const val TYPE_GUEST = 103

        const val GENDER_MALE = "male"
        const val GENDER_FEMALE = "female"

        const val TYPE_TOKEN_GUEST = "guest"
        const val TYPE_TOKEN_OTP = "otp"
    }

    fun getTokenType(): String {
        return if (type == TYPE_GUEST) TYPE_TOKEN_GUEST else TYPE_TOKEN_OTP
    }
}
