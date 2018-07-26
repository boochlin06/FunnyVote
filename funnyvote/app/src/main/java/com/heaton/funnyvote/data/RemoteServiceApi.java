package com.heaton.funnyvote.data;

import com.heaton.funnyvote.retrofit.Server;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by chiu_mac on 2016/12/6.
 */

public class RemoteServiceApi {
    private static final String TAG = RemoteServiceApi.class.getSimpleName();
    public static final String USER_TYPE_FACEBOOK = "fb";
    public static final String USER_TYPE_GOOGLE = "google";
    public static final String USER_TYPE_TWITTER = "twitter";

    //Retrofit
    Retrofit retrofit;

    public Server.UserService getUserService() {
        return userService;
    }

    public Server.VoteService getVoteService() {
        return voteService;
    }

    public Server.PromotionService getPromotionService() {
        return promotionService;
    }

    Server.UserService userService;
    Server.VoteService voteService;
    Server.PromotionService promotionService;
    private static RemoteServiceApi INSTANCE;

    public RemoteServiceApi() {
        retrofit = new Retrofit.Builder().baseUrl(Server.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        userService = retrofit.create(Server.UserService.class);
        voteService = retrofit.create(Server.VoteService.class);
        promotionService = retrofit.create(Server.PromotionService.class);
    }

    public static RemoteServiceApi getInstance() {
        if (INSTANCE == null) {
            synchronized (RemoteServiceApi.class) {
                if (INSTANCE == null) {
                    INSTANCE = new RemoteServiceApi();
                }
            }
        }
        return INSTANCE;
    }

}
