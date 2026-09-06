package com.heaton.funnyvote

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.analytics.GoogleAnalytics
import com.google.android.gms.analytics.Tracker
import com.google.firebase.FirebaseApp
import com.heaton.funnyvote.data.local.AppDatabase

class FunnyVoteApplication : Application() {
    private var tracker: Tracker? = null

    val defaultTracker: Tracker
        @Synchronized get() {
            if (tracker == null) {
                val analytics = GoogleAnalytics.getInstance(this)
                tracker = analytics.newTracker(R.xml.global_tracker)
            }
            return tracker!!
        }

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        AppDatabase.getInstance(this)
        MobileAds.initialize(applicationContext) {}
    }
}

