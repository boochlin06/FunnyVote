package com.heaton.funnyvote.data.promotion;

import com.heaton.funnyvote.database.Promotion;
import com.heaton.funnyvote.database.User;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

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
                .getPromotionList(user);
        Observable<List<Promotion>> remoteVote = remotePromotionSource
                .getPromotionList(user)
                .doOnNext(promotionList -> localPromotionSource.savePromotionList(promotionList))
                .onErrorResumeNext(localVote);

        return Observable.concat(remoteVote, localVote)
                .filter(list -> list != null && !list.isEmpty())
                .firstOrError()
                .toObservable()
                .onErrorResumeNext(Observable.just(new ArrayList<>()));
    }

    @Override
    public void savePromotionList(List<Promotion> promotionList) {
        localPromotionSource.savePromotionList(promotionList);
    }
}
