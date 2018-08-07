package com.heaton.funnyvote.data.promotion;

import com.heaton.funnyvote.database.Promotion;
import com.heaton.funnyvote.database.User;

import java.util.List;

public class PromotionRepository implements PromotionDataSource {

    private static final String TAG = PromotionRepository.class.getSimpleName();
    private static PromotionRepository INSTANCE = null;
    private PromotionDataSource remotePromotionSource, localPromotionSource;

    public static PromotionRepository getInstance(PromotionDataSource remotePromotionSource
            , PromotionDataSource localPromotionSource) {
        if (INSTANCE == null) {
            INSTANCE = new PromotionRepository(remotePromotionSource, localPromotionSource);
        }
        return INSTANCE;
    }

    public PromotionRepository(PromotionDataSource remotePromotionSource
            , PromotionDataSource localPromotionSource) {
        this.localPromotionSource = localPromotionSource;
        this.remotePromotionSource = remotePromotionSource;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

    @Override
    public void getPromotionList(final User user, final GetPromotionsCallback callback) {
        remotePromotionSource.getPromotionList(user, new GetPromotionsCallback() {
            @Override
            public void onPromotionsLoaded(List<Promotion> promotionList) {
                callback.onPromotionsLoaded(promotionList);
                localPromotionSource.savePromotionList(promotionList);
            }

            @Override
            public void onPromotionsNotAvailable() {
                localPromotionSource.getPromotionList(user, new GetPromotionsCallback() {
                    @Override
                    public void onPromotionsLoaded(List<Promotion> promotionList) {
                        callback.onPromotionsLoaded(promotionList);
                    }

                    @Override
                    public void onPromotionsNotAvailable() {
                        callback.onPromotionsNotAvailable();
                    }
                });
            }
        });
    }

    @Override
    public void savePromotionList(List<Promotion> promotionList) {
        localPromotionSource.savePromotionList(promotionList);
    }
}
