package com.heaton.funnyvote.data;

import android.content.Context;
import android.support.annotation.NonNull;

import com.heaton.funnyvote.FunnyVoteApplication;
import com.heaton.funnyvote.data.VoteData.LocalVoteDataSource;
import com.heaton.funnyvote.data.VoteData.RemoteVoteDataSource;
import com.heaton.funnyvote.data.VoteData.VoteDataRepository;
import com.heaton.funnyvote.data.promotion.LocalPromotionSource;
import com.heaton.funnyvote.data.promotion.PromotionRepository;
import com.heaton.funnyvote.data.promotion.RemotePromotionSource;
import com.heaton.funnyvote.data.user.RemoteUserDataSource;
import com.heaton.funnyvote.data.user.SPUserDataSource;
import com.heaton.funnyvote.data.user.UserDataRepository;
import com.heaton.funnyvote.utils.AppExecutors;

import static com.google.common.base.Preconditions.checkNotNull;

public class Injection {
    public static VoteDataRepository provideVoteDataRepository(@NonNull Context context) {
        checkNotNull(context);
        return VoteDataRepository.getInstance(LocalVoteDataSource.getInstance(
                ((FunnyVoteApplication) (context.getApplicationContext()))
                        .getDaoSession().getVoteDataDao()
                , ((FunnyVoteApplication) (context.getApplicationContext()))
                        .getDaoSession().getOptionDao(), AppExecutors.getInstance()
        ), RemoteVoteDataSource.getInstance());
    }

    public static UserDataRepository provideUserRepository(@NonNull Context context) {
        checkNotNull(context);
        return UserDataRepository.getInstance(context, SPUserDataSource.getInstance(context)
                , RemoteUserDataSource.getInstance());
    }

    public static PromotionRepository providePromotionRepository(@NonNull Context context) {
        checkNotNull(context);
        return PromotionRepository.getInstance(RemotePromotionSource.getInstance()
                , LocalPromotionSource.getInstance(((FunnyVoteApplication) (context.getApplicationContext()))
                        .getDaoSession().getPromotionDao(), AppExecutors.getInstance()));
    }
}
