package com.android.heaton.funnyvote.retrofit;

import com.android.heaton.funnyvote.database.VoteData;

import java.io.File;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by heaton on 2016/12/4.
 */

public class Server {
    public static final String BASE_URL = "http://138.68.23.5/";
    public static final String API_KEY = "Oj9pfxd4Z5LZx8Ha3Ond4xWy2LZU4HPk";
    public static final String APP_CODE = "com.funnyvote";

    public interface UserService {
        @Headers({"x-api-key: " + API_KEY, "app-code: " + APP_CODE})
        @POST("/guest")
        Call<ResponseBody> getGuestCode();

        @Headers({"x-api-key: " + API_KEY, "app-code: " + APP_CODE})
        @FormUrlEncoded
        @POST("/fbmember")
        Call<ResponseBody> addFBUser(@Field("appid") String appId,
                                     @Field("id") String id,
                                     @Field("name") String name,
                                     @Field("imgurl") String imgUrl,
                                     @Field("email") String email,
                                     @Field("gender") String gender);

    }

    public interface VoteService {
        @Headers({"x-api-key: " + API_KEY, "app-code: " + APP_CODE})
        @FormUrlEncoded
        @POST("/poll")
        Call<VoteData> createVote(@Field("t") String title,
                                  @Field("max") int maxOption,
                                  @Field("min") int minOption,
                                  @Field("pt") List<String> options,
                                  @Field("add") boolean isUserCanAddOption,
                                  @Field("res") boolean isUserCanPreview,
                                  @Field("pub") boolean security,
                                  @Field("i") File image,
                                  @Field("cat") String category,
                                  @Field("otp") String userCode,
                                  @Field("guest") String guestCode
        );
    }
}
