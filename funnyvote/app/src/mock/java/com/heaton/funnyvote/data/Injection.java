package com.heaton.funnyvote.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.heaton.funnyvote.FunnyVoteApplication;
import com.heaton.funnyvote.data.VoteData.FakeRemoteVoteDataRepository;
import com.heaton.funnyvote.data.VoteData.LocalVoteDataSource;
import com.heaton.funnyvote.data.VoteData.VoteDataRepository;
import com.heaton.funnyvote.data.promotion.LocalPromotionSource;
import com.heaton.funnyvote.data.promotion.PromotionRepository;
import com.heaton.funnyvote.data.promotion.RemotePromotionSource;
import com.heaton.funnyvote.data.user.RemoteUserDataSource;
import com.heaton.funnyvote.data.user.SPUserDataSource;
import com.heaton.funnyvote.data.user.UserDataRepository;
import com.heaton.funnyvote.utils.AppExecutors;
import com.heaton.funnyvote.utils.schedulers.BaseSchedulerProvider;
import com.heaton.funnyvote.utils.schedulers.SchedulerProvider;

import static com.google.common.base.Preconditions.checkNotNull;

public class Injection {
    public static VoteDataRepository provideVoteDataRepository(@NonNull Context context) {
        checkNotNull(context);
        return VoteDataRepository.getInstance(LocalVoteDataSource.getInstance(
                ((FunnyVoteApplication) (context.getApplicationContext()))
                        .getDaoSession().getVoteDataDao()
                , ((FunnyVoteApplication) (context.getApplicationContext()))
                        .getDaoSession().getOptionDao(), AppExecutors.getInstance()
        ), FakeRemoteVoteDataRepository.getInstance());
    }

    public static UserDataRepository provideUserRepository(@NonNull Context context) {
        checkNotNull(context);
        return UserDataRepository.getInstance(SPUserDataSource.getInstance(context)
                , RemoteUserDataSource.getInstance());
    }

    public static PromotionRepository providePromotionRepository(@NonNull Context context) {
        checkNotNull(context);
        return PromotionRepository.getInstance(RemotePromotionSource.getInstance()
                , LocalPromotionSource.getInstance(((FunnyVoteApplication) (context.getApplicationContext()))
                        .getDaoSession().getPromotionDao(), AppExecutors.getInstance()));
    }

    public static SharedPreferences provideFirstTimePref(@NonNull Context context) {
        checkNotNull(context);
        return FakeFirstTimePref.getInstance(context).getPreferences();
    }

    public static BaseSchedulerProvider provideSchedulerProvider() {
        return SchedulerProvider.getInstance();
    }
}
