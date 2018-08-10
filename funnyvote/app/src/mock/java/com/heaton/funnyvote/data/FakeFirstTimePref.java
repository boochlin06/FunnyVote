package com.heaton.funnyvote.data;

import android.content.Context;
import android.content.SharedPreferences;

public class FakeFirstTimePref {
    private static final String SP_FIRST_TIME = "first_time_use";
    public static final String SP_FIRST_MOCK_DATA = "first_mock_data";
    public static final String SP_FIRST_INTRODUCTION_PAGE = "first_introduction_page";
    public static final String SP_FIRST_INTRODUTCION_QUICK_POLL = "first_introduction_quick_poll";
    public static final String SP_FIRST_ENTER_UNPOLL_VOTE = "first_enter_unpoll_vote";
    SharedPreferences preferences;

    private static FakeFirstTimePref instance;

    public static FakeFirstTimePref getInstance(Context context) {
        if (instance == null) {
            instance = new FakeFirstTimePref(context);
        }
        return instance;
    }

    private FakeFirstTimePref(Context context) {
        preferences = context.getSharedPreferences(SP_FIRST_TIME, Context.MODE_PRIVATE);
        preferences.edit().putBoolean(SP_FIRST_MOCK_DATA, false).apply();
        preferences.edit().putBoolean(SP_FIRST_INTRODUCTION_PAGE, false).apply();
        preferences.edit().putBoolean(SP_FIRST_INTRODUTCION_QUICK_POLL, false).apply();
        preferences.edit().putBoolean(SP_FIRST_ENTER_UNPOLL_VOTE, false).apply();
    }

    public SharedPreferences getPreferences() {
        return preferences;
    }
}
