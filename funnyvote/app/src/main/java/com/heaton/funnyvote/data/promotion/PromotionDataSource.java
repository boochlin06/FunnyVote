package com.heaton.funnyvote.data.promotion;

import com.heaton.funnyvote.database.Promotion;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.database.VoteData;

import java.util.List;

public interface PromotionDataSource {

    interface GetPromotionsCallback {
        void onPromotionsLoaded(List<Promotion> promotionList);

        void onPromotionsNotAvailable();
    }

    void getPromotionList(User user, GetPromotionsCallback callback);

    void savePromotions(List<Promotion> promotionList);
}
