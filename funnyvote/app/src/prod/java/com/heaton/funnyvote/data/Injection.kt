package com.heaton.funnyvote.data

import android.content.Context
import android.content.SharedPreferences
import com.google.common.base.Preconditions.checkNotNull
import com.heaton.funnyvote.FirstTimePref
import com.heaton.funnyvote.data.VoteData.LocalVoteDataSource
import com.heaton.funnyvote.data.VoteData.RemoteVoteDataSource
import com.heaton.funnyvote.data.VoteData.VoteDataRepository
import com.heaton.funnyvote.data.local.AppDatabase
import com.heaton.funnyvote.data.promotion.LocalPromotionSource
import com.heaton.funnyvote.data.promotion.PromotionRepository
import com.heaton.funnyvote.data.promotion.RemotePromotionSource
import com.heaton.funnyvote.data.user.RemoteUserDataSource
import com.heaton.funnyvote.data.user.SPUserDataSource
import com.heaton.funnyvote.data.user.UserDataRepository
import com.heaton.funnyvote.utils.AppExecutors

object Injection {
    fun provideVoteDataRepository(context: Context): VoteDataRepository {
        checkNotNull(context)
        val db = AppDatabase.getInstance(context)
        return VoteDataRepository.getInstance(
            LocalVoteDataSource.getInstance(
                db.voteDataDao(),
                db.optionDao(),
                AppExecutors.getInstance()!!
            ),
            RemoteVoteDataSource.getInstance()
        )
    }

    fun provideUserRepository(context: Context): UserDataRepository {
        checkNotNull(context)
        return UserDataRepository.getInstance(
            SPUserDataSource.getInstance(context),
            RemoteUserDataSource.getInstance()
        )
    }

    fun providePromotionRepository(context: Context): PromotionRepository {
        checkNotNull(context)
        val db = AppDatabase.getInstance(context)
        return PromotionRepository.getInstance(
            RemotePromotionSource.getInstance(),
            LocalPromotionSource.getInstance(
                db.promotionDao(),
                AppExecutors.getInstance()!!
            )!!
        )

    }

    fun provideFirstTimePref(context: Context): SharedPreferences {
        checkNotNull(context)
        return FirstTimePref.getInstance(context).preferences
    }
}

