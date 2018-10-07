package com.heaton.funnyvote.data.promotion;

import android.util.Log;

import com.heaton.funnyvote.data.RemoteServiceApi;
import com.heaton.funnyvote.database.Promotion;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.retrofit.Server;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Observable;

public class RemotePromotionSource implements PromotionDataSource {

    private static final String TAG = RemotePromotionSource.class.getSimpleName();
    private static RemotePromotionSource INSTANCE = null;
    private Server.PromotionService promotionService;
    public static final int PAGE_COUNT = 10;

    public static RemotePromotionSource getInstance() {
        if (INSTANCE == null) {
            synchronized (RemotePromotionSource.class) {
                if (INSTANCE == null) {
                    INSTANCE = new RemotePromotionSource();
                }
            }
        }
        return INSTANCE;
    }

    public RemotePromotionSource() {
        this.promotionService = RemoteServiceApi.getInstance().getPromotionService();
    }

    @Override
    public Observable<List<Promotion>> getPromotionList(User user) {
        return promotionService.getPromotionListRx(1, PAGE_COUNT
                , user.getUserCode()
                , user.getTokenType());
    }

    @Override
    public void savePromotionList(List<Promotion> promotionList) {
        // Nothing to do
    }

}
