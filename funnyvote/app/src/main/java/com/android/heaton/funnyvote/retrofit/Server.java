package com.android.heaton.funnyvote.retrofit;

import com.android.heaton.funnyvote.database.VoteData;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by heaton on 2016/12/4.
 */

public class Server {
    public static final String BASE_URL = "http://138.68.23.5/";
    public static final String API_KEY = "Oj9pfxd4Z5LZx8Ha3Ond4xWy2LZU4HPk";
    public static final String APP_CODE = "com.funnyvote";
    public static final String WEB_URL = "http://138.68.23.5:5487/link/";

    public interface UserService {
        @Headers({"x-api-key: " + API_KEY, "app-code: " + APP_CODE})
        @POST("/guest/{name}")
        Call<ResponseBody> getGuestCode(@Path("name") String guestName);

        @Headers({"x-api-key: " + API_KEY, "app-code: " + APP_CODE})
        @FormUrlEncoded
        @POST("/social/member")
        Call<ResponseBody> addUser(@Field("type") String type,
                                   @Field("appid") String appId,
                                   @Field("id") String id,
                                   @Field("name") String name,
                                   @Field("imgurl") String imgUrl,
                                   @Field("email") String email,
                                   @Field("gender") String gender);

        @Headers({"x-api-key: " + API_KEY, "app-code: " + APP_CODE})
        @FormUrlEncoded
        @PUT("/member/{otp}")
        Call<ResponseBody> changeUserName(@Path("otp") String otp,
                                          @Field("otp") String fieldOtp,
                                          @Field("nickname") String newName);

        @Headers({"x-api-key: " + API_KEY, "app-code: " + APP_CODE})
        @FormUrlEncoded
        @PUT("/guest/{guest}")
        Call<ResponseBody> changeGuestUserName(@Path("guest")  String guest,
                                          @Field("guest") String fieldGuest,
                                          @Field("nickname") String newName);
        @Headers({"x-api-key: " + API_KEY, "app-code: " + APP_CODE})
        @PUT("/link/{otp}/{guest}")
        Call<ResponseBody> linkGuestLoginUser(@Path("otp") String otp,
                                              @Path("guest") String guest);
    }

    public interface VoteService {
        @Headers({"x-api-key: " + API_KEY, "app-code: " + APP_CODE})
        @Multipart
        @POST("/poll")
        Call<VoteData> createVote(@PartMap Map<String, RequestBody> parametor,
                                  @Part("description") RequestBody description,
                                  @Part MultipartBody.Part file

        );

        @Headers({"x-api-key: " + API_KEY, "app-code: " + APP_CODE})
        @GET("/poll/{votecode}")
        Call<VoteData> getVote(@Path("votecode") String voteCode,
                               @Query("token") String token,
                               @Query("tokentype") String tokenType);

        @Headers({"x-api-key: " + API_KEY, "app-code: " + APP_CODE})
        @FormUrlEncoded
        @POST("/vote/{votecode}")
        Call<VoteData> pollVote(@Path("votecode") String voteCode,
                                @Field("oc") List<String> optionCode,
                                @Field("token") String token,
                                @Field("tokentype") String tokenType);
    }
}
