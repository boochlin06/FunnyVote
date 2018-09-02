package com.heaton.funnyvote.data.promotion

import com.heaton.funnyvote.database.Promotion
import com.heaton.funnyvote.database.User
import com.heaton.funnyvote.database.VoteData

interface PromotionDataSource {

    interface GetPromotionsCallback {
        fun onPromotionsLoaded(promotionList: List<Promotion>)

        fun onPromotionsNotAvailable()
    }

    fun getPromotionList(user: User, callback: GetPromotionsCallback)

    fun savePromotionList(promotionList: List<Promotion>)
}
