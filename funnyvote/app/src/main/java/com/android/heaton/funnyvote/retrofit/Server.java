package com.android.heaton.funnyvote.retrofit;

import com.android.heaton.funnyvote.database.VoteData;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;

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
        @Multipart
        @POST("/poll")
        Call<VoteData> createVote(@Field("t") String title,
                                  @Field("max") int maxOption,
                                  @Field("min") int minOption,
                                  @Field("pt") List<String> options,
                                  @Field("add") boolean isUserCanAddOption,
                                  @Field("res") boolean isUserCanPreview,
                                  @Field("pub") boolean security,
                                  @Part("description") RequestBody description,
                                  @Part MultipartBody.Part file,
                                  @Field("cat") String category,
                                  @Field("otp") String userCode,
                                  @Field("guest") String guestCode
        );

        @Headers({"x-api-key: " + API_KEY, "app-code: " + APP_CODE})
        @Multipart
        @POST("/poll")
        Call<VoteData> createVote(@PartMap Map<String, RequestBody> parametor,
                                  @Part("description") RequestBody description,
                                  @Part MultipartBody.Part file

        );
    }
}
