package com.heaton.funnyvote.retrofit

import com.heaton.funnyvote.database.Promotion
import com.heaton.funnyvote.database.VoteData
import com.google.gson.annotations.SerializedName

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Created by heaton on 2016/12/4.
 */

object Server {
    const val BASE_URL = "https://funny-vote.com/"
    const val API_KEY = "8c6d7c7a6bfbd60a4c9f65d664f1f6ae"
    const val APP_CODE = "com.funnyvote"
    const val WEB_URL = "https://funny-vote.com/link/"

    interface UserService {
        @Headers("x-api-key: $API_KEY", "app-code: $APP_CODE")
        @POST("api/guest/{name}")
        fun getGuestCode(@Path("name") guestName: String): Call<ResponseBody>

        @Headers("x-api-key: $API_KEY", "app-code: $APP_CODE")
        @FormUrlEncoded
        @POST("api/social/member")
        fun addUser(@Field("type") type: String,
                    @Field("appid") appId: String,
                    @Field("id") id: String,
                    @Field("name") name: String,
                    @Field("imgurl") imgUrl: String,
                    @Field("email") email: String,
                    @Field("gender") gender: String): Call<ResponseBody>

        @Headers("x-api-key: $API_KEY", "app-code: $APP_CODE")
        @FormUrlEncoded
        @PUT("api/member")
        fun changeUserName(@Field("tokentype") tokenType: String,
                           @Field("token") token: String,
                           @Field("nickname") newName: String): Call<ResponseBody>

        @Headers("x-api-key: $API_KEY", "app-code: $APP_CODE")
        @PUT("api/link/{otp}/{guest}")
        fun linkGuestLoginUser(@Path("otp") otp: String,
                               @Path("guest") guest: String): Call<ResponseBody>

        @Headers("x-api-key: $API_KEY", "app-code: $APP_CODE")
        @GET("api/member")
        fun getUserInfo(@Query("tokentype") tokenType: String,
                        @Query("token") token: String): Call<UserDataQuery>
    }

    interface VoteService {
        @Headers("x-api-key: $API_KEY", "app-code: $APP_CODE")
        @Multipart
        @POST("api/poll")
        fun createVote(@PartMap parameter: Map<String,@JvmSuppressWildcards RequestBody>,
                       @Part("description") description: RequestBody,
                       @Part file: MultipartBody.Part

        ): Call<VoteData>

        @Headers("x-api-key: $API_KEY", "app-code: $APP_CODE")
        @GET("api/poll/{votecode}")
        fun getVote(@Path("votecode") voteCode: String,
                    @Query("token") token: String,
                    @Query("tokentype") tokenType: String): Call<VoteData>

        @Headers("x-api-key: $API_KEY", "app-code: $APP_CODE")
        @FormUrlEncoded
        @POST("api/vote/{votecode}")
        fun pollVote(@Path("votecode") voteCode: String,
                     @Field("p") password: String,
                     @Field("oc") optionCode: List<String>,
                     @Field("token") token: String,
                     @Field("tokentype") tokenType: String): Call<VoteData>

        @Headers("x-api-key: $API_KEY", "app-code: $APP_CODE")
        @GET("api/plist")
        fun getVoteList(@Query("p") pageNumber: Int,
                        @Query("ps") pageCount: Int,
                        @Query("o") listType: String,
                        @Query("token") token: String,
                        @Query("tokentype") tokenType: String): Call<List<VoteData>>

        @Headers("x-api-key: $API_KEY", "app-code: $APP_CODE")
        @GET("api/fav")
        fun getFavoriteVoteList(@Query("p") pageNumber: Int,
                                @Query("ps") pageCount: Int,
                                @Query("token") token: String,
                                @Query("tokentype") tokenType: String): Call<List<VoteData>>

        @Headers("x-api-key: $API_KEY", "app-code: $APP_CODE")
        @FormUrlEncoded
        @POST("api/fav")
        fun updateFavorite(@Field("c") voteCode: String,
                           @Field("action") isFavorite: String,
                           @Field("token") token: String,
                           @Field("tokentype") tokenType: String): Call<ResponseBody>

        @Headers("x-api-key: $API_KEY", "app-code: $APP_CODE")
        @GET("api/poll/history/create")
        fun getUserCreateVoteList(@Query("p") pageNumber: Int,
                                  @Query("ps") pageCount: Int,
                                  @Query("token") token: String,
                                  @Query("tokentype") tokenType: String): Call<List<VoteData>>

        @Headers("x-api-key: $API_KEY", "app-code: $APP_CODE")
        @GET("api/poll/history/vote")
        fun getUserParticipateVoteList(@Query("p") pageNumber: Int,
                                       @Query("ps") pageCount: Int,
                                       @Query("token") token: String,
                                       @Query("tokentype") tokenType: String): Call<List<VoteData>>

        @Headers("x-api-key: $API_KEY", "app-code: $APP_CODE")
        @FormUrlEncoded
        @POST("api/option")
        fun updateOption(@Field("c") voteCode: String,
                         @Field("p") password: String,
                         @Field("ot") newOption: List<String>,
                         @Field("token") token: String,
                         @Field("tokentype") tokenType: String): Call<VoteData>

        @Headers("x-api-key: $API_KEY", "app-code: $APP_CODE")
        @GET("api/search")
        fun getSearchVoteList(@Query("keyword") keyword: String,
                              @Query("p") pageNumber: Int,
                              @Query("ps") pageCount: Int,
                              @Query("token") token: String,
                              @Query("tokentype") tokenType: String): Call<List<VoteData>>

        @Headers("x-api-key: $API_KEY", "app-code: $APP_CODE")
        @GET("api/public/create")
        fun getPersonalCreateVoteList(@Query("p") pageNumber: Int,
                                      @Query("ps") pageCount: Int,
                                      @Query("token") token: String,
                                      @Query("tokentype") tokenType: String,
                                      @Query("cretoken") targettoken: String,
                                      @Query("cretokentype") targettokenType: String): Call<List<VoteData>>

        @Headers("x-api-key: $API_KEY", "app-code: $APP_CODE")
        @GET("api/public/fav")
        fun getPersonalFavoriteVoteList(@Query("p") pageNumber: Int,
                                        @Query("ps") pageCount: Int,
                                        @Query("token") token: String,
                                        @Query("tokentype") tokenType: String,
                                        @Query("favtoken") targettoken: String,
                                        @Query("favtokentype") targettokenType: String): Call<List<VoteData>>
    }

    interface PromotionService {
        @Headers("x-api-key: $API_KEY", "app-code: $APP_CODE")
        @GET("api/promotion")
        fun getPromotionList(@Query("p") pageNumber: Int,
                             @Query("ps") pageCount: Int,
                             @Query("token") token: String,
                             @Query("tokentype") tokenType: String): Call<List<Promotion>>
    }

    class UserDataQuery {
        @SerializedName("img")
        var memberImage: String? = null
        @SerializedName("nn")
        var memberName: String? = null
        @SerializedName("snt")
        var memberSource: String? = null
        @SerializedName("mc")
        var memberCode: String? = null
        @SerializedName("otp")
        var otp: String? = null
        @SerializedName("guest")
        var guestCode: String? = null
    }
}
