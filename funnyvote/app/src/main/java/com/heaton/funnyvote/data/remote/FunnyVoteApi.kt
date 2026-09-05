package com.heaton.funnyvote.data.remote

import com.heaton.funnyvote.data.local.entity.VoteEntity
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FunnyVoteApi {
    @GET("api/plist")
    suspend fun getVoteList(
        @Query("p") page: Int = 0,
        @Query("ps") pageSize: Int = 20,
        @Query("o") category: String = "hot"
    ): Response<List<VoteEntity>>

    @GET("api/poll/{voteCode}")
    suspend fun getVoteDetail(
        @Path("voteCode") voteCode: String
    ): Response<VoteEntity>
}
