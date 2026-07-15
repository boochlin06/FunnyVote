package com.heaton.funnyvote.data.local.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "votes")
data class VoteData(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,

    @SerializedName("c")
    var voteCode: String = "",

    @SerializedName("t")
    var title: String? = null,

    @SerializedName("nn")
    var authorName: String? = null,

    @SerializedName("token")
    var authorCode: String? = null,

    @SerializedName("tokentype")
    var authorCodeType: String? = null,

    @SerializedName("mi")
    var authorIcon: String? = null,

    @SerializedName("i")
    var voteImage: String? = null,

    var localImage: Int = 0,

    @SerializedName("on")
    var startTime: Long = 0,

    @SerializedName("off")
    var endTime: Long = 0,

    @SerializedName("min")
    var minOption: Int = 0,

    @SerializedName("max")
    var maxOption: Int = 0,

    @SerializedName("osn")
    var optionCount: Int = 0,

    @SerializedName("voted")
    var pollCount: Int = 0,

    @SerializedName("isVoted")
    var isPolled: Boolean = false,

    @SerializedName("fav")
    var isFavorite: Boolean = false,

    @SerializedName("res")
    var isCanPreviewResult: Boolean = false,

    @SerializedName("add")
    var isUserCanAddOption: Boolean = false,

    @SerializedName("p")
    var isNeedPassword: Boolean = false,

    @SerializedName("sec")
    var security: String = SECURITY_PUBLIC,

    var category: String? = null,
    var displayOrder: Int = 0,
    var pollType: String? = null,

    // Extracted option fields for local DB ease, as original did
    var option1Title: String? = null,
    var option1Code: String? = null,
    var option1Count: Int = 0,
    var option1Polled: Boolean = false,

    var option2Title: String? = null,
    var option2Code: String? = null,
    var option2Count: Int = 0,
    var option2Polled: Boolean = false,

    var optionTopTitle: String? = null,
    var optionTopCode: String? = null,
    var optionTopCount: Int = 0,
    var optionTopPolled: Boolean = false,

    var optionUserChoiceTitle: String? = null,
    var optionUserChoiceCode: String? = null,
    var optionUserChoiceCount: Int = 0
) {
    @Ignore
    @SerializedName("os")
    var netOptions: List<Option>? = null

    companion object {
        const val SECURITY_PRIVATE = "01"
        const val SECURITY_PUBLIC = "00"
        const val CATEGORY_HOT = "hot"
    }

    val isMultiChoice: Boolean
        get() = !(maxOption == 1 && minOption == 1)
}
