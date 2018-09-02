package com.heaton.funnyvote.data.promotion

import android.util.Log
import com.heaton.funnyvote.data.RemoteServiceApi
import com.heaton.funnyvote.database.Promotion
import com.heaton.funnyvote.database.User
import com.heaton.funnyvote.retrofit.Server
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class RemotePromotionSource : PromotionDataSource {
    private val promotionService: Server.PromotionService = RemoteServiceApi.getInstance().promotionService

    override fun getPromotionList(user: User, callback: PromotionDataSource.GetPromotionsCallback) {
        if (user.userCode.isNullOrBlank()) {
            callback.onPromotionsNotAvailable()
            return
        }
        val call = promotionService.getPromotionList(1, PAGE_COUNT, user.userCode, user.tokenType)
        call.enqueue(GetPromotionListResponseCallback(callback))
    }

    override fun savePromotionList(promotionList: List<Promotion>) {
        // Nothing to do
    }

    inner class GetPromotionListResponseCallback(private val callback: PromotionDataSource.GetPromotionsCallback) : Callback<List<Promotion>> {

        override fun onResponse(call: Call<List<Promotion>>, response: Response<List<Promotion>>) {
            if (response.isSuccessful) {
                callback.onPromotionsLoaded(response.body())
            } else {
                try {
                    val errorMessage = response.errorBody().string()
                    Log.d(TAG, "GetPromotionListResponseCallback onResponse false:$errorMessage")
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                callback.onPromotionsNotAvailable()
            }
        }

        override fun onFailure(call: Call<List<Promotion>>, t: Throwable) {
            Log.d(TAG, "GetPromotionListResponseCallback onFailure:" + t.message)
            callback.onPromotionsNotAvailable()
        }
    }

    companion object {
        @JvmField
        val TAG: String? = RemotePromotionSource::class.java.simpleName
        var INSTANCE: RemotePromotionSource? = null
        const val PAGE_COUNT = 10

        @JvmStatic
        fun getInstance(): RemotePromotionSource {
            return INSTANCE
                    ?: RemotePromotionSource()
                            .apply { INSTANCE = this }
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}
