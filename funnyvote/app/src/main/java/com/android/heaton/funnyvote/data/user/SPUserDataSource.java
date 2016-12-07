package com.android.heaton.funnyvote.data.user;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.heaton.funnyvote.R;
import com.android.heaton.funnyvote.database.User;

/**
 * Created by chiu_mac on 2016/12/6.
 */

public class SPUserDataSource implements UserDataSource {
    private static final String SHARED_PREF_USER = "user";
    private static final String KEY_NAME = "name";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_CODE = "user_code";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_TYPE = "account_type";
    private static final String KEY_ICON = "icon";

    private final String defaultGuestName;
    private SharedPreferences userSharedPref;
    private User user;

    public SPUserDataSource(Context context) {
        userSharedPref = context.getSharedPreferences(SHARED_PREF_USER, Context.MODE_PRIVATE);
        defaultGuestName = context.getString(R.string.account_default_name);
    }

    @Override
    public synchronized User getUser() {
        if (user == null) {
            String name = userSharedPref.getString(KEY_NAME, defaultGuestName);
            String id = userSharedPref.getString(KEY_USER_ID, "");
            String code = userSharedPref.getString(KEY_USER_CODE, "");
            String email = userSharedPref.getString(KEY_EMAIL, "");
            int type = userSharedPref.getInt(KEY_TYPE, User.TYPE_GUEST);
            String icon = userSharedPref.getString(KEY_ICON, "");
            user = new User();
            user.setUserName(name);
            user.setUserID(id);
            user.setUserCode(code);
            user.setEmail(email);
            user.setType(type);
            user.setUserIcon(icon);
        }
        return user;
    }

    @Override
    public synchronized void setUser(User user) {
        SharedPreferences.Editor spEditor = userSharedPref.edit();
        spEditor.putString(KEY_NAME, user.getUserName());
        spEditor.putString(KEY_USER_ID, user.getUserID());
        spEditor.putString(KEY_USER_CODE, user.getUserCode());
        spEditor.putInt(KEY_TYPE, user.getType());
        spEditor.putString(KEY_ICON, user.getUserIcon());
        spEditor.putString(KEY_EMAIL, user.getEmail());
        spEditor.commit();
        this.user = user;
    }

    @Override
    public synchronized void removeUser() {
        userSharedPref.edit().clear().commit();
        this.user = null;
    }
}
