package com.heaton.funnyvote.data

import com.heaton.funnyvote.retrofit.Server

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Created by chiu_mac on 2016/12/6.
 */

class RemoteServiceApi {

    //Retrofit
    internal var retrofit: Retrofit = Retrofit.Builder().baseUrl(Server.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    var userService: Server.UserService
        internal set
    var voteService: Server.VoteService
        internal set
    var promotionService: Server.PromotionService
        internal set

    init {
        this.userService = retrofit.create(Server.UserService::class.java)
        this.voteService = retrofit.create(Server.VoteService::class.java)
        this.promotionService = retrofit.create(Server.PromotionService::class.java)
    }

    companion object {
        @JvmField
        val TAG: String? = RemoteServiceApi::class.java.simpleName
        const val USER_TYPE_FACEBOOK = "fb"
        const val USER_TYPE_GOOGLE = "google"
        const val USER_TYPE_TWITTER = "twitter"
        private var INSTANCE: RemoteServiceApi? = null

        @JvmStatic
        fun getInstance(): RemoteServiceApi {
            return INSTANCE ?: RemoteServiceApi().apply {
                INSTANCE = this
            }
        }
    }

}
