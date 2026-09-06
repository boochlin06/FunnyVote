package com.heaton.funnyvote.data.promotion

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.heaton.funnyvote.database.Promotion
import com.heaton.funnyvote.database.User

class RemotePromotionSource private constructor() : PromotionDataSource {

    override fun getPromotionList(user: User, callback: PromotionDataSource.GetPromotionsCallback) {
        FirebaseFirestore.getInstance().collection("promotions").limit(PAGE_COUNT.toLong()).get()
            .addOnSuccessListener { snapshot ->
                val list = mutableListOf<Promotion>()
                if (snapshot != null && !snapshot.isEmpty) {
                    for (doc in snapshot.documents) {
                        val p = Promotion().apply {
                            title = doc.getString("title")
                            imageURL = doc.getString("imageUrl") ?: doc.getString("imgurl")
                            actionURL = doc.getString("link")
                        }
                        list.add(p)
                    }
                }
                if (list.isEmpty()) {
                    list.addAll(createDefaultPromotions())
                }
                callback.onPromotionsLoaded(list)
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Failed to load promotions from Firestore, using defaults: ${e.message}")
                callback.onPromotionsLoaded(createDefaultPromotions())
            }
    }

    private fun createDefaultPromotions(): List<Promotion> {
        return listOf(
            Promotion(1L, "https://picsum.photos/800/400?random=1", "https://github.com/boochlin06/FunnyVote", "歡迎來到 FunnyVote 投票平台！"),
            Promotion(2L, "https://picsum.photos/800/400?random=2", "https://firebase.google.com", "全面串接 Firebase Firestore 後端架構")
        )
    }

    override fun savePromotionList(promotionList: List<Promotion>) {}

    companion object {
        private const val TAG = "RemotePromotionSource"
        private var INSTANCE: RemotePromotionSource? = null
        const val PAGE_COUNT = 10

        @JvmStatic
        fun getInstance(): RemotePromotionSource {
            if (INSTANCE == null) {
                synchronized(RemotePromotionSource::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = RemotePromotionSource()
                    }
                }
            }
            return INSTANCE!!
        }

        @JvmStatic
        fun destroyInstance() {
            INSTANCE = null
        }
    }
}

