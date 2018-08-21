package com.heaton.funnyvote.data.Promotion;

import com.heaton.funnyvote.data.Local;
import com.heaton.funnyvote.data.Remote;
import com.heaton.funnyvote.data.VoteData.VoteDataSource;
import com.heaton.funnyvote.data.promotion.LocalPromotionSource;
import com.heaton.funnyvote.data.promotion.PromotionDataSource;
import com.heaton.funnyvote.data.promotion.RemotePromotionSource;
import com.heaton.funnyvote.database.DaoSession;
import com.heaton.funnyvote.database.PromotionDao;
import com.heaton.funnyvote.database.VoteDataDao;
import com.heaton.funnyvote.utils.AppExecutors;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

@Module
public class PromotionRepositoryModule {

    @Singleton
    @Provides
    @Local
    PromotionDataSource providePromotionLocalDataSource(PromotionDao promotionDao, AppExecutors appExecutors) {
        return new LocalPromotionSource(promotionDao, appExecutors);
    }

    @Singleton
    @Provides
    @Remote
    PromotionDataSource providePromotionRemoteDataSource() {
        return new RemotePromotionSource();
    }

    @Singleton
    @Provides
    static PromotionDao providePromotionDao(DaoSession session) {
        return session.getPromotionDao();
    }
}
