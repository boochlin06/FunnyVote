package com.heaton.funnyvote.data.promotion;

import android.util.Log;

import com.heaton.funnyvote.data.RemoteServiceApi;
import com.heaton.funnyvote.database.Promotion;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.retrofit.Server;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
    public void getPromotionList(User user, GetPromotionsCallback callback) {
        if (user == null) {
            callback.onPromotionsNotAvailable();
            return;
        }
        Call<List<Promotion>> call = promotionService.getPromotionList(1, PAGE_COUNT
                , user.getUserCode()
                , user.getTokenType());
        call.enqueue(new getPromotionListResponseCallback(callback));
    }

    @Override
    public void savePromotionList(List<Promotion> promotionList) {
        // Nothing to do
    }

    public class getPromotionListResponseCallback implements Callback<List<Promotion>> {

        private GetPromotionsCallback callback;


        public getPromotionListResponseCallback(GetPromotionsCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onResponse(Call<List<Promotion>> call, Response<List<Promotion>> response) {
            if (response.isSuccessful()) {
                callback.onPromotionsLoaded(response.body());
            } else {
                String errorMessage = "";
                try {
                    errorMessage = response.errorBody().string();
                    Log.d(TAG, "getPromotionListResponseCallback onResponse false:" + errorMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                callback.onPromotionsNotAvailable();
            }
        }

        @Override
        public void onFailure(Call<List<Promotion>> call, Throwable t) {
            Log.d(TAG, "getPromotionListResponseCallback onFailure:" + t.getMessage());
            callback.onPromotionsNotAvailable();
        }
    }
}
