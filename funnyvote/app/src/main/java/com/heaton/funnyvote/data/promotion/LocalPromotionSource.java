package com.heaton.funnyvote.data.promotion;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.heaton.funnyvote.data.local.dao.PromotionDao;
import com.heaton.funnyvote.database.Promotion;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.utils.AppExecutors;

import java.util.List;

import io.reactivex.Observable;

public class LocalPromotionSource implements PromotionDataSource {
    private PromotionDao promotionDao;
    private static volatile LocalPromotionSource INSTANCE;
    private AppExecutors mAppExecutors;

    private LocalPromotionSource(@NonNull PromotionDao promotionDao, AppExecutors appExecutors) {
        this.promotionDao = promotionDao;
        this.mAppExecutors = appExecutors;
    }

    public static LocalPromotionSource getInstance(@NonNull PromotionDao promotionDao
            , AppExecutors appExecutors) {
        if (INSTANCE == null) {
            synchronized (LocalPromotionSource.class) {
                if (INSTANCE == null) {
                    INSTANCE = new LocalPromotionSource(promotionDao, appExecutors);
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public Observable<List<Promotion>> getPromotionList(User user) {
        return Observable.fromCallable(() -> promotionDao.loadAll());
    }

    @Override
    public void savePromotionList(final List<Promotion> promotionList) {
        mAppExecutors.diskIO().execute(() -> promotionDao.insertOrReplaceInTx(promotionList));
    }

    @VisibleForTesting
    public static void clearInstance() {
        INSTANCE = null;
    }
}
