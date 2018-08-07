package com.heaton.funnyvote.data.user;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.heaton.funnyvote.R;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.retrofit.Server;

import okhttp3.ResponseBody;
import retrofit2.Callback;

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
    private static final String KEY_GENDER = "gender";
    private static final String KEY_MIN_AGE = "min_age";
    private static final String KEY_MAX_AGE = "max_age";

    private final String defaultGuestName;
    private SharedPreferences userSharedPref;
    private User user;
    private static SPUserDataSource INSTANCE;

    public static SPUserDataSource getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            synchronized (SPUserDataSource.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SPUserDataSource(context);
                }
            }
        }
        return INSTANCE;
    }

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
            String gender = userSharedPref.getString(KEY_GENDER, "");
            int minAge = userSharedPref.getInt(KEY_MIN_AGE, -1);
            int maxAge = userSharedPref.getInt(KEY_MAX_AGE, -1);
            user = new User();
            user.setUserName(name);
            user.setUserID(id);
            user.setUserCode(code);
            user.setEmail(email);
            user.setType(type);
            user.setUserIcon(icon);
            user.setGender(gender);
            user.setMinAge(minAge);
            user.setMaxAge(maxAge);
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
        spEditor.putString(KEY_GENDER, user.getGender());
        spEditor.putInt(KEY_MIN_AGE, user.getMinAge());
        spEditor.putInt(KEY_MAX_AGE, user.getMaxAge());
        spEditor.commit();
        this.user = user;
    }

    @Override
    public synchronized void removeUser() {
        userSharedPref.edit().clear().commit();
        this.user = null;
    }

    @Override
    public void getGuestUserCode(GetUserCodeCallback callback, String name) {
        // Not required for the local data source
    }

    @Override
    public void getUserInfo(Callback<Server.UserDataQuery> callback, User user) {
        // Not required for the local data source
    }

    @Override
    public void getUser(GetUserCallback callback, boolean forceUpdateUserCode) {
        callback.onResponse(getUser());
    }

    @Override
    public void setGuestName(String guestName) {

    }

    @Override
    public void registerUser(String appId, User user, boolean mergeGuest, RegisterUserCallback callback) {

    }


    @Override
    public void unregisterUser() {

    }

    @Override
    public void getUserCode(String userType, String appId, User user, GetUserCodeCallback callback) {

    }

    @Override
    public void linkGuestToLoginUser(String otp, String guest, Callback<ResponseBody> callback) {

    }

    @Override
    public void changeUserName(Callback<ResponseBody> callback, String tokenType, String token, String name) {

    }

    @Override
    public void changeCurrentUserName(String name, ChangeUserNameCallback callback) {

    }

}
