package com.heaton.funnyvote.data

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.NonNull
import com.google.common.base.Preconditions.checkNotNull
import com.heaton.funnyvote.data.VoteData.FakeRemoteVoteDataRepository
import com.heaton.funnyvote.data.VoteData.LocalVoteDataSource
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
    fun provideVoteDataRepository(@NonNull context: Context): VoteDataRepository {
        checkNotNull(context)
        val db = AppDatabase.getInstance(context)
        return VoteDataRepository.getInstance(
            LocalVoteDataSource.getInstance(
                db.voteDataDao(),
                db.optionDao(),
                AppExecutors.getInstance()!!
            ),
            FakeRemoteVoteDataRepository.getInstance()
        )
    }

    fun provideUserRepository(@NonNull context: Context): UserDataRepository {
        checkNotNull(context)
        return UserDataRepository.getInstance(SPUserDataSource.getInstance(context), RemoteUserDataSource.getInstance())
    }

    fun providePromotionRepository(@NonNull context: Context): PromotionRepository {
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

    fun provideFirstTimePref(@NonNull context: Context): SharedPreferences {
        checkNotNull(context)
        return FakeFirstTimePref.getInstance(context).preferences!!
    }
}

