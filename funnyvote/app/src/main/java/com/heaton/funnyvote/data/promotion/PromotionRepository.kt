package com.heaton.funnyvote.data.promotion

import com.heaton.funnyvote.database.Promotion
import com.heaton.funnyvote.database.User

class PromotionRepository(
        private val remotePromotionSource: PromotionDataSource,
        private val localPromotionSource: PromotionDataSource
) : PromotionDataSource {

    override fun getPromotionList(user: User, callback: PromotionDataSource.GetPromotionsCallback) {
        remotePromotionSource.getPromotionList(user, object : PromotionDataSource.GetPromotionsCallback {
            override fun onPromotionsLoaded(promotionList: List<Promotion>) {
                callback.onPromotionsLoaded(promotionList)
                localPromotionSource.savePromotionList(promotionList)
            }

            override fun onPromotionsNotAvailable() {
                localPromotionSource.getPromotionList(user, object : PromotionDataSource.GetPromotionsCallback {
                    override fun onPromotionsLoaded(promotionList: List<Promotion>) {
                        callback.onPromotionsLoaded(promotionList)
                    }

                    override fun onPromotionsNotAvailable() {
                        callback.onPromotionsNotAvailable()
                    }
                })
            }
        })
    }

    override fun savePromotionList(promotionList: List<Promotion>) {
        localPromotionSource.savePromotionList(promotionList)
    }

    companion object {

        @JvmField
        val TAG: String? = PromotionRepository::class.java.simpleName
        private var INSTANCE: PromotionRepository? = null

        @JvmStatic
        fun getInstance(remotePromotionSource: PromotionDataSource
                        , localPromotionSource: PromotionDataSource): PromotionRepository {
            return INSTANCE
                    ?: PromotionRepository(remotePromotionSource, localPromotionSource)
                            .apply {
                                INSTANCE = this
                            }
        }

        fun destroyInstance() {
            INSTANCE = null
        }

    }
}
