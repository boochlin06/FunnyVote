package com.heaton.funnyvote.data

import android.content.Context
import android.content.SharedPreferences

class FakeFirstTimePref private constructor(context: Context) {
    var preferences: SharedPreferences? = null
        internal set

    init {
        preferences = context.getSharedPreferences(SP_FIRST_TIME, Context.MODE_PRIVATE)
        preferences!!.edit().putBoolean(SP_FIRST_MOCK_DATA, false).apply()
        preferences!!.edit().putBoolean(SP_FIRST_INTRODUCTION_PAGE, false).apply()
        preferences!!.edit().putBoolean(SP_FIRST_INTRODUTCION_QUICK_POLL, false).apply()
        preferences!!.edit().putBoolean(SP_FIRST_ENTER_UNPOLL_VOTE, false).apply()
    }

    companion object {
        private val SP_FIRST_TIME = "first_time_use"
        val SP_FIRST_MOCK_DATA = "first_mock_data"
        val SP_FIRST_INTRODUCTION_PAGE = "first_introduction_page"
        val SP_FIRST_INTRODUTCION_QUICK_POLL = "first_introduction_quick_poll"
        val SP_FIRST_ENTER_UNPOLL_VOTE = "first_enter_unpoll_vote"

        private var instance: FakeFirstTimePref? = null

        fun getInstance(context: Context): FakeFirstTimePref {
            if (instance == null) {
                instance = FakeFirstTimePref(context)
            }
            return instance as FakeFirstTimePref
        }
    }
}
