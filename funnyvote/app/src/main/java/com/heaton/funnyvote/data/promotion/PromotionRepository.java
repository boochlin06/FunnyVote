package com.heaton.funnyvote.data.promotion;

import com.heaton.funnyvote.database.Promotion;
import com.heaton.funnyvote.database.User;

import java.util.List;
import java.util.NoSuchElementException;

import rx.Observable;
import rx.schedulers.Schedulers;

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
    public Observable<List<Promotion>> getPromotionList(User user) {
        Observable<List<Promotion>> localVote = localPromotionSource
                .getPromotionList(user).first();
        Observable<List<Promotion>> remoteVote = remotePromotionSource
                .getPromotionList(user)
                //.subscribeOn(Schedulers.io())
                .map(promotionList -> {
                    localPromotionSource.savePromotionList(promotionList);
                    return promotionList;
                })
                .onErrorResumeNext((Throwable e) -> localVote);

        return Observable.concat(remoteVote, localVote).first()
                .map(promotionList -> {
                    if (promotionList == null) {
                        throw new NoSuchElementException("no promotion data");
                    }
                    return promotionList;
                });
    }

    @Override
    public void savePromotionList(List<Promotion> promotionList) {
        localPromotionSource.savePromotionList(promotionList);
    }
}
