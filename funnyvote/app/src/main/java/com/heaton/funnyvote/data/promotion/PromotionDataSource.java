package com.heaton.funnyvote.data.promotion;

import com.heaton.funnyvote.database.Promotion;
import com.heaton.funnyvote.database.User;

import java.util.List;

import rx.Observable;

public interface PromotionDataSource {
    Observable<List<Promotion>> getPromotionList(User user);

    void savePromotionList(List<Promotion> promotionList);
}
