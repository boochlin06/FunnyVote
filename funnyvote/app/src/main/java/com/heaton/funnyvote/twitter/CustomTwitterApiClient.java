package com.heaton.funnyvote.twitter;

import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.User;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by chiu_mac on 2016/12/27.
 */

public class CustomTwitterApiClient extends TwitterApiClient {
    public CustomTwitterApiClient(TwitterSession session) {
        super(session);
    }

    public UserService getUserService() {
        return getService(UserService.class);
    }

    public interface UserService {
        @GET("/1.1/users/show.json")
        Call<User> show(@Query("user_id") long id);
    }
}
