package com.android.heaton.funnyvote.ui;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.heaton.funnyvote.R;
import com.android.heaton.funnyvote.database.User;

/**
 * Created by heaton on 2016/11/16.
 */

public class UserSharepreferenceController {
    public static final String SHARED_PREF_USER = "user";
    public static final String KEY_NAME = "name";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_TYPE = "account_type";
    public static final String KEY_ICON = "icon";
    public static final String PROFILE_PICTURE_FILE = "profile_pic.png";

    public static SharedPreferences getUserSp(Context context) {
        return context.getSharedPreferences(SHARED_PREF_USER, Context.MODE_PRIVATE);
    }
    public static boolean isGuest(Context context) {
        return getUserSp(context).getString(KEY_TYPE, User.TYPE_GUEST).equals(User.TYPE_GUEST);
    }

    public static User getUser(Context context) {
        String name = getUserSp(context).getString(KEY_NAME, context.getString(R.string.account_default_name));
        String code = getUserSp(context).getString(KEY_USER_ID, "");
        String email = getUserSp(context).getString(KEY_EMAIL, "");
        String type = getUserSp(context).getString(KEY_TYPE, User.TYPE_GUEST);
        String icon = getUserSp(context).getString(KEY_ICON, "");
        User user = new User();
        user.setUserName(name);
        user.setUserCode(code);
        user.setEmail(email);
        user.setType(type);
        user.setUserIcon(icon);
        return user;
    }

    public static void updtaeUser(Context context, User user) {
        SharedPreferences.Editor editor = getUserSp(context).edit();
        editor.putString(KEY_NAME, user.getUserName());
        editor.putString(KEY_USER_ID, user.getUserCode());
        editor.putString(KEY_EMAIL, user.getEmail());
        editor.putString(KEY_TYPE, user.getType());
        editor.putString(KEY_ICON, user.getUserIcon());
        editor.commit();
    }

    public static void removeUser(Context context) {
        SharedPreferences.Editor editor = getUserSp(context).edit();
        editor.putString(KEY_NAME, context.getString(R.string.account_default_name));
        editor.putString(KEY_USER_ID, Long.toString(System.currentTimeMillis()));
        editor.putString(KEY_EMAIL, "");
        editor.putString(KEY_TYPE, User.TYPE_GUEST);
        editor.putString(KEY_ICON, "");
        editor.commit();
    }

}
