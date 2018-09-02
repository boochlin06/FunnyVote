package com.heaton.funnyvote.data.promotion

import android.support.annotation.VisibleForTesting

import com.heaton.funnyvote.database.Promotion
import com.heaton.funnyvote.database.PromotionDao
import com.heaton.funnyvote.database.User
import com.heaton.funnyvote.utils.AppExecutors

class LocalPromotionSource
private constructor(
        private val promotionDao: PromotionDao,
        private val mAppExecutors: AppExecutors
) : PromotionDataSource {

    override fun getPromotionList(user: User, callback: PromotionDataSource.GetPromotionsCallback) {
        val runnable = Runnable {
            if (user.userCode.isNullOrBlank()) {
                callback.onPromotionsNotAvailable()
            }
            mAppExecutors.mainThread.execute {
                val list = promotionDao.loadAll()
                callback.onPromotionsLoaded(list)
            }
        }
        mAppExecutors.diskIO.execute(runnable)
    }

    override fun savePromotionList(promotionList: List<Promotion>) {
        mAppExecutors.diskIO.execute { promotionDao.insertOrReplaceInTx(promotionList) }
    }

    companion object {
        private var INSTANCE: LocalPromotionSource? = null

        fun getInstance(promotionDao: PromotionDao, appExecutors: AppExecutors): LocalPromotionSource? {
            if (INSTANCE == null) {
                synchronized(LocalPromotionSource::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = LocalPromotionSource(promotionDao, appExecutors)
                    }
                }
            }
            return INSTANCE
        }

        @VisibleForTesting
        fun clearInstance() {
            INSTANCE = null
        }
    }
}
