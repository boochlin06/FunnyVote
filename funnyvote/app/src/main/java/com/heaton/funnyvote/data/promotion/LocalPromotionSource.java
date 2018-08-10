package com.heaton.funnyvote.data.promotion;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.heaton.funnyvote.database.Promotion;
import com.heaton.funnyvote.database.PromotionDao;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.utils.AppExecutors;

import java.util.List;

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
    public void getPromotionList(final User user, final GetPromotionsCallback callback) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (user == null) {
                    callback.onPromotionsNotAvailable();
                }
                mAppExecutors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        List<Promotion> list = promotionDao.loadAll();
                        callback.onPromotionsLoaded(list);
                    }
                });
            }
        };
        mAppExecutors.diskIO().execute(runnable);
    }

    @Override
    public void savePromotionList(final List<Promotion> promotionList) {
        mAppExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                promotionDao.insertOrReplaceInTx(promotionList);
            }
        });
    }

    @VisibleForTesting
    public static void clearInstance() {
        INSTANCE = null;
    }
}
