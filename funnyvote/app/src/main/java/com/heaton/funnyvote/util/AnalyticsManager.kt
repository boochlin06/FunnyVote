package com.heaton.funnyvote.util

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsManager @Inject constructor(
    private val analytics: FirebaseAnalytics,
    private val crashlytics: FirebaseCrashlytics
) {
    fun logScreenView(screenName: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenName)
        }
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    fun logVoteSubmit(voteCode: String, optionCount: Int) {
        val bundle = Bundle().apply {
            putString("vote_code", voteCode)
            putInt("option_count", optionCount)
        }
        analytics.logEvent("submit_vote", bundle)
    }

    fun logVoteCreate(voteCode: String, isPrivate: Boolean) {
        val bundle = Bundle().apply {
            putString("vote_code", voteCode)
            putBoolean("is_private", isPrivate)
        }
        analytics.logEvent("create_vote", bundle)
    }

    fun logSignIn(method: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.METHOD, method)
        }
        analytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle)
    }

    fun recordException(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }
}
