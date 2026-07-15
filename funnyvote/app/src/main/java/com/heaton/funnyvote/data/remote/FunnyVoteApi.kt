package com.heaton.funnyvote.data.remote

import com.heaton.funnyvote.data.local.entity.VoteData
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface FunnyVoteApi {
    companion object {
        const val BASE_URL = "http://172.16.101.12:18000/client/dynamic/"
        // Add headers via OkHttp Interceptor in DI instead of hardcoding here
    }

    // --- User Services ---

    @POST("api/guest/{name}")
    suspend fun getGuestCode(@Path("name") guestName: String): Response<ResponseBody>

    @FormUrlEncoded
    @POST("api/social/member")
    suspend fun addUser(
        @Field("type") type: String,
        @Field("appid") appId: String,
        @Field("id") id: String,
        @Field("name") name: String,
        @Field("imgurl") imgUrl: String,
        @Field("email") email: String,
        @Field("gender") gender: String
    ): Response<ResponseBody>

    @FormUrlEncoded
    @PUT("api/member")
    suspend fun changeUserName(
        @Field("tokentype") tokenType: String,
        @Field("token") token: String,
        @Field("nickname") newName: String
    ): Response<ResponseBody>

    @PUT("api/link/{otp}/{guest}")
    suspend fun linkGuestLoginUser(
        @Path("otp") otp: String,
        @Path("guest") guest: String
    ): Response<ResponseBody>

    @GET("api/member")
    suspend fun getUserInfo(
        @Query("tokentype") tokenType: String,
        @Query("token") token: String
    ): Response<UserDataQueryResponse>

    // --- Vote Services ---

    @Multipart
    @POST("api/poll")
    suspend fun createVote(
        @PartMap parameter: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part("description") description: RequestBody,
        @Part file: MultipartBody.Part?
    ): Response<VoteData>

    @GET("api/poll/{votecode}")
    suspend fun getVote(
        @Path("votecode") voteCode: String,
        @Query("token") token: String,
        @Query("tokentype") tokenType: String
    ): Response<VoteData>

    @FormUrlEncoded
    @POST("api/vote/{votecode}")
    suspend fun pollVote(
        @Path("votecode") voteCode: String,
        @Field("p") password: String?,
        @Field("oc") optionCode: List<String>,
        @Field("token") token: String,
        @Field("tokentype") tokenType: String
    ): Response<VoteData>

    @GET("api/plist")
    suspend fun getVoteList(
        @Query("p") pageNumber: Int,
        @Query("ps") pageCount: Int,
        @Query("o") listType: String,
        @Query("token") token: String?,
        @Query("tokentype") tokenType: String?
    ): Response<List<VoteData>>

    @GET("api/fav")
    suspend fun getFavoriteVoteList(
        @Query("p") pageNumber: Int,
        @Query("ps") pageCount: Int,
        @Query("token") token: String,
        @Query("tokentype") tokenType: String
    ): Response<List<VoteData>>

    @FormUrlEncoded
    @POST("api/fav")
    suspend fun updateFavorite(
        @Field("c") voteCode: String,
        @Field("action") isFavorite: String,
        @Field("token") token: String,
        @Field("tokentype") tokenType: String
    ): Response<ResponseBody>

    @GET("api/poll/history/create")
    suspend fun getUserCreateVoteList(
        @Query("p") pageNumber: Int,
        @Query("ps") pageCount: Int,
        @Query("token") token: String,
        @Query("tokentype") tokenType: String
    ): Response<List<VoteData>>

    @GET("api/poll/history/vote")
    suspend fun getUserParticipateVoteList(
        @Query("p") pageNumber: Int,
        @Query("ps") pageCount: Int,
        @Query("token") token: String,
        @Query("tokentype") tokenType: String
    ): Response<List<VoteData>>

    @FormUrlEncoded
    @POST("api/option")
    suspend fun updateOption(
        @Field("c") voteCode: String,
        @Field("p") password: String?,
        @Field("ot") newOption: List<String>,
        @Field("token") token: String,
        @Field("tokentype") tokenType: String
    ): Response<VoteData>

    @GET("api/search")
    suspend fun getSearchVoteList(
        @Query("keyword") keyword: String,
        @Query("p") pageNumber: Int,
        @Query("ps") pageCount: Int,
        @Query("token") token: String?,
        @Query("tokentype") tokenType: String?
    ): Response<List<VoteData>>
}

data class UserDataQueryResponse(
    val img: String?,
    val nn: String?,
    val snt: String?,
    val mc: String?,
    val otp: String?,
    val guest: String?
)
