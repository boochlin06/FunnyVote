package com.heaton.funnyvote;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by heaton on 2017/2/26.
 */
public class FirstTimePref {
    private static final String SP_FIRST_TIME = "first_time";
    public static final String SP_FIRST_MOCK_DATA = "first_mock_data";
    public static final String SP_FIRST_INTRODUCTION_PAGE = "first_introduction_page";
    public static final String SP_FIRST_INTRODUTCION_QUICK_POLL = "first_introduction_quick_poll";
    public static final String SP_FIRST_ENTER_UNPOLL_VOTE = "first_enter_unpoll_vote";
    SharedPreferences preferences;

    private static FirstTimePref instance;

    public static FirstTimePref getInstance(Context context) {
        if (instance == null) {
            instance = new FirstTimePref(context);
        }
        return instance;
    }

    private FirstTimePref(Context context) {
        preferences = context.getSharedPreferences(SP_FIRST_TIME,Context.MODE_PRIVATE);
    }
    public SharedPreferences getPreferences() {
        return preferences;
    }
}
