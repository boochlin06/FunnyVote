package com.heaton.funnyvote

import android.content.Context
import android.content.SharedPreferences

/**
 * Created by heaton on 2017/2/26.
 */
class FirstTimePref private constructor(context: Context) {
    var preferences: SharedPreferences
        internal set

    init {
        preferences = context.getSharedPreferences(SP_FIRST_TIME, Context.MODE_PRIVATE)
    }

    companion object {
        private val SP_FIRST_TIME = "first_time_use"
        val SP_FIRST_MOCK_DATA = "first_mock_data"
        val SP_FIRST_INTRODUCTION_PAGE = "first_introduction_page"
        val SP_FIRST_INTRODUTCION_QUICK_POLL = "first_introduction_quick_poll"
        val SP_FIRST_ENTER_UNPOLL_VOTE = "first_enter_unpoll_vote"

        private var instance: FirstTimePref? = null

        fun getInstance(context: Context): FirstTimePref {
            if (instance == null) {
                instance = FirstTimePref(context)
            }
            return instance as FirstTimePref
        }
    }
}
