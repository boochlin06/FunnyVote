package com.heaton.funnyvote.retrofit;

import com.heaton.funnyvote.database.Promotion;
import com.heaton.funnyvote.database.VoteData;
import com.google.gson.annotations.SerializedName;

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
    public static final String BASE_URL = "https://funny-vote.com/";
    public static final String API_KEY = "xxxxxxxxxx";
    public static final String APP_CODE = "com.funnyvote";
    public static final String WEB_URL = "https://funny-vote.com/link/";

    public interface UserService {
        @Headers({"x-api-key: " + API_KEY, "app-code: " + APP_CODE})
        @POST("api/guest/{name}")
        Call<ResponseBody> getGuestCode(@Path("name") String guestName);

        @Headers({"x-api-key: " + API_KEY, "app-code: " + APP_CODE})
        @FormUrlEncoded
        @POST("api/social/member")
        Call<ResponseBody> addUser(@Field("type") String type,
                                   @Field("appid") String appId,
                                   @Field("id") String id,
                                   @Field("name") String name,
                                   @Field("imgurl") String imgUrl,
                                   @Field("email") String email,
                                   @Field("gender") String gender);

        @Headers({"x-api-key: " + API_KEY, "app-code: " + APP_CODE})
        @FormUrlEncoded
        @PUT("api/member")
        Call<ResponseBody> changeUserName(@Field("tokentype") String tokenType,
                                          @Field("token") String token,
                                          @Field("nickname") String newName);

        @Headers({"x-api-key: " + API_KEY, "app-code: " + APP_CODE})
        @PUT("api/link/{otp}/{guest}")
        Call<ResponseBody> linkGuestLoginUser(@Path("otp") String otp,
                                              @Path("guest") String guest);

        @Headers({"x-api-key: " + API_KEY, "app-code: " + APP_CODE})
        @GET("api/member")
        Call<UserDataQuery> getUserInfo(@Query("tokentype") String tokenType,
                                 @Query("token") String token);
    }

    public interface VoteService {
        @Headers({"x-api-key: " + API_KEY, "app-code: " + APP_CODE})
        @Multipart
        @POST("api/poll")
        Call<VoteData> createVote(@PartMap Map<String, RequestBody> parameter,
                                  @Part("description") RequestBody description,
                                  @Part MultipartBody.Part file

        );

        @Headers({"x-api-key: " + API_KEY, "app-code: " + APP_CODE})
        @GET("api/poll/{votecode}")
        Call<VoteData> getVote(@Path("votecode") String voteCode,
                               @Query("token") String token,
                               @Query("tokentype") String tokenType);

        @Headers({"x-api-key: " + API_KEY, "app-code: " + APP_CODE})
        @FormUrlEncoded
        @POST("api/vote/{votecode}")
        Call<VoteData> pollVote(@Path("votecode") String voteCode,
                                @Field("p") String password,
                                @Field("oc") List<String> optionCode,
                                @Field("token") String token,
                                @Field("tokentype") String tokenType);

        @Headers({"x-api-key: " + API_KEY, "app-code: " + APP_CODE})
        @GET("api/plist")
        Call<List<VoteData>> getVoteList(@Query("p") int pageNumber,
                                         @Query("ps") int pageCount,
                                         @Query("o") String listType,
                                         @Query("token") String token,
                                         @Query("tokentype") String tokenType);

        @Headers({"x-api-key: " + API_KEY, "app-code: " + APP_CODE})
        @GET("api/fav")
        Call<List<VoteData>> getFavoriteVoteList(@Query("p") int pageNumber,
                                                 @Query("ps") int pageCount,
                                                 @Query("token") String token,
                                                 @Query("tokentype") String tokenType);

        @Headers({"x-api-key: " + API_KEY, "app-code: " + APP_CODE})
        @FormUrlEncoded
        @POST("api/fav")
        Call<ResponseBody> updateFavorite(@Field("c") String voteCode,
                                          @Field("action") String isFavorite,
                                          @Field("token") String token,
                                          @Field("tokentype") String tokenType);

        @Headers({"x-api-key: " + API_KEY, "app-code: " + APP_CODE})
        @GET("api/poll/history/create")
        Call<List<VoteData>> getUserCreateVoteList(@Query("p") int pageNumber,
                                                   @Query("ps") int pageCount,
                                                   @Query("token") String token,
                                                   @Query("tokentype") String tokenType);

        @Headers({"x-api-key: " + API_KEY, "app-code: " + APP_CODE})
        @GET("api/poll/history/vote")
        Call<List<VoteData>> getUserParticipateVoteList(@Query("p") int pageNumber,
                                                        @Query("ps") int pageCount,
                                                        @Query("token") String token,
                                                        @Query("tokentype") String tokenType);

        @Headers({"x-api-key: " + API_KEY, "app-code: " + APP_CODE})
        @FormUrlEncoded
        @POST("api/option")
        Call<VoteData> updateOption(@Field("c") String voteCode,
                                    @Field("p") String password,
                                    @Field("ot") List<String> newOption,
                                    @Field("token") String token,
                                    @Field("tokentype") String tokenType);

        @Headers({"x-api-key: " + API_KEY, "app-code: " + APP_CODE})
        @GET("api/search")
        Call<List<VoteData>> getSearchVoteList(@Query("keyword") String keyword,
                                               @Query("p") int pageNumber,
                                               @Query("ps") int pageCount,
                                               @Query("token") String token,
                                               @Query("tokentype") String tokenType);

        @Headers({"x-api-key: " + API_KEY, "app-code: " + APP_CODE})
        @GET("api/public/create")
        Call<List<VoteData>> getPersonalCreateVoteList(@Query("p") int pageNumber,
                                                       @Query("ps") int pageCount,
                                                       @Query("token") String token,
                                                       @Query("tokentype") String tokenType,
                                                       @Query("cretoken") String targettoken,
                                                       @Query("cretokentype") String targettokenType);

        @Headers({"x-api-key: " + API_KEY, "app-code: " + APP_CODE})
        @GET("api/public/fav")
        Call<List<VoteData>> getPersonalFavoriteVoteList(@Query("p") int pageNumber,
                                                         @Query("ps") int pageCount,
                                                         @Query("token") String token,
                                                         @Query("tokentype") String tokenType,
                                                         @Query("favtoken") String targettoken,
                                                         @Query("favtokentype") String targettokenType);
    }

    public interface PromotionService {
        @Headers({"x-api-key: " + API_KEY, "app-code: " + APP_CODE})
        @GET("api/promotion")
        Call<List<Promotion>> getPromotionList(@Query("p") int pageNumber,
                                               @Query("ps") int pageCount,
                                               @Query("token") String token,
                                               @Query("tokentype") String tokenType);
    }

    public static class UserDataQuery {
        @SerializedName("img")
        public String memberImage;
        @SerializedName("nn")
        public String memberName;
        @SerializedName("snt")
        public String memberSource;
        @SerializedName("mc")
        public String memberCode;
        @SerializedName("otp")
        public String otp;
        @SerializedName("guest")
        public String guestCode;
    }
}
