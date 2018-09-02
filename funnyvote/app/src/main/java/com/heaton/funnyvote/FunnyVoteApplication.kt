package com.heaton.funnyvote

import android.app.Application
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.facebook.stetho.Stetho
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.analytics.GoogleAnalytics
import com.google.android.gms.analytics.Tracker
import com.heaton.funnyvote.database.DaoMaster
import com.heaton.funnyvote.database.DaoSession
import com.twitter.sdk.android.Twitter
import com.twitter.sdk.android.core.TwitterAuthConfig
import io.fabric.sdk.android.Fabric
import org.greenrobot.greendao.identityscope.IdentityScopeType
import kotlin.properties.Delegates

/**
 * Created by heaton on 2016/10/25.
 */

class FunnyVoteApplication : Application() {
    lateinit var daoSession: DaoSession
//        private set
    private var tracker: Tracker? = null
    /**
     * Gets the default [Tracker] for this [Application].
     * @return tracker
     */
    // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
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
        val authConfig = TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET)
        Fabric.with(this, Twitter(authConfig))
        Stetho.initializeWithDefaults(this)
        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(this)
        val helper = DaoMaster.DevOpenHelper(this
                , if (ENCRYPTED) "votes-db-encrypted" else "votes-db")
        val db = if (ENCRYPTED) helper.getEncryptedWritableDb("super-secret") else helper.writableDb
        daoSession = DaoMaster(db).newSession(IdentityScopeType.Session)

        MobileAds.initialize(applicationContext, "ca-app-pub-3940256099942544~3347511713")
    }

    companion object {

        // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
        private val TWITTER_KEY = "x3l5s5Xo0Ns7B0woDW9Jg9fuD"
        private val TWITTER_SECRET = "F7MccZvZXRr9LFexSd2yKpqNiN6JckS697svkAHxWP9UbEqDRd"

        val ENCRYPTED = false
    }
}
