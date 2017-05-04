package com.heaton.funnyvote.data.promotion;

import android.content.Context;
import android.util.Log;

import com.heaton.funnyvote.data.RemoteServiceApi;
import com.heaton.funnyvote.database.DataLoader;
import com.heaton.funnyvote.database.Promotion;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.eventbus.EventBusManager;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by heaton on 2017/1/13.
 */

public class PromotionManager {
    private static final String TAG = PromotionManager.class.getSimpleName();
    public static final int PAGE_COUNT = 10;
    private static PromotionManager INSTANCE = null;
    private ExecutorService executorService;

    private Context context;
    private RemoteServiceApi remoteServiceApi;


    public static PromotionManager getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (PromotionManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PromotionManager(context, new RemoteServiceApi());
                }
            }
        }
        return INSTANCE;
    }

    public PromotionManager(Context context, RemoteServiceApi remoteServiceApi) {
        this.context = context;
        this.remoteServiceApi = remoteServiceApi;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public class getPromotionListResponseCallback implements Callback<List<Promotion>> {

        private String message;


        public getPromotionListResponseCallback(int offset, String message) {
            this.message = message;
        }

        @Override
        public void onResponse(Call<List<Promotion>> call, Response<List<Promotion>> response) {
            if (response.isSuccessful()) {
                executorService.execute(new SaveAndLoadListDBRunnable(response.body(), message, true));
            } else {
                String errorMessage = "";
                try {
                    errorMessage = response.errorBody().string();
                    Log.d(TAG, "getPromotionListResponseCallback onResponse false:" + errorMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                executorService.execute(new LoadListDBRunnable(message, false, errorMessage));
            }
        }

        @Override
        public void onFailure(Call<List<Promotion>> call, Throwable t) {
            Log.d(TAG, "getPromotionListResponseCallback onFailure:" + t.getMessage() + " MESSAGE:" + this.message);
            executorService.execute(new LoadListDBRunnable(message, false, t.getMessage()));
        }
    }

    public void getPromotionList(User user) {
        if (user == null) {
            Log.d(TAG, "getPromotionListResponseCallback onFailure: user is null");
            executorService.execute(new LoadListDBRunnable(EventBusManager.RemoteServiceEvent.GET_PROMOTION_LIST
                    , false, "user is null"));
        } else {
            remoteServiceApi.getPromotionList(0, PAGE_COUNT, user, new getPromotionListResponseCallback(0
                    , EventBusManager.RemoteServiceEvent.GET_PROMOTION_LIST));
        }
    }

    private class SaveAndLoadListDBRunnable implements Runnable {
        private List<Promotion> promotionList;
        private String message;
        private boolean success;

        public SaveAndLoadListDBRunnable(List<Promotion> promotionList, String message, boolean success) {
            this.message = message;
            this.success = success;
            this.promotionList = promotionList;
        }

        @Override
        public void run() {
            Log.d(TAG, "promotion count:" + promotionList.size() + " message:" + message +" success:"+success);
            DataLoader.getInstance(context.getApplicationContext()).getPromotionDao().deleteAll();
            DataLoader.getInstance(context.getApplicationContext()).getPromotionDao().insertInTx(promotionList);
            EventBus.getDefault().post(new EventBusManager.RemoteServiceEvent(message, success, promotionList));
        }
    }

    private class LoadListDBRunnable implements Runnable {

        private boolean success;
        private String message;
        private String errorResponse;


        public LoadListDBRunnable(String message, boolean success, String errorResponse) {
            this.success = success;
            this.message = message;
            this.errorResponse = errorResponse;
        }

        @Override
        public void run() {
            List<Promotion> promotionList = DataLoader.getInstance(context.getApplicationContext()).getPromotionDao().loadAll();
            EventBus.getDefault().post(new EventBusManager.RemoteServiceEvent(message, success, errorResponse));
        }
    }
}
