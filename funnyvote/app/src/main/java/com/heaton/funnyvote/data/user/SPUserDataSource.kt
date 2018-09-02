package com.heaton.funnyvote.data.user

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.heaton.funnyvote.R
import com.heaton.funnyvote.database.User
import com.heaton.funnyvote.retrofit.Server
import okhttp3.ResponseBody
import retrofit2.Callback

/**
 * Created by chiu_mac on 2016/12/6.
 */

class SPUserDataSource(context: Context) : UserDataSource {

    private val defaultGuestName: String
    private val userSharedPref: SharedPreferences


    override var user: User
        get() = getUserFromLocal()
        set(user) {
            setUserToLocal(user)
        }

    init {
        userSharedPref = context.getSharedPreferences(SHARED_PREF_USER, Context.MODE_PRIVATE)
        defaultGuestName = context.getString(R.string.account_default_name)
    }

    private fun getUserFromLocal(): User {
        var localUser: User = userCache;
        if (userCache.userCode.isNullOrEmpty()) {
            Log.d("test", "getUserFromLocal");
            val name = userSharedPref.getString(KEY_NAME, defaultGuestName)
            val id = userSharedPref.getString(KEY_USER_ID, "")
            val code = userSharedPref.getString(KEY_USER_CODE, "")
            val email = userSharedPref.getString(KEY_EMAIL, "")
            val type = userSharedPref.getInt(KEY_TYPE, User.TYPE_GUEST)
            val icon = userSharedPref.getString(KEY_ICON, "")
            val gender = userSharedPref.getString(KEY_GENDER, "")
            val minAge = userSharedPref.getInt(KEY_MIN_AGE, -1)
            val maxAge = userSharedPref.getInt(KEY_MAX_AGE, -1)
            localUser.userName = name
            localUser.userID = id
            localUser.userCode = code
            localUser.email = email
            localUser.type = type
            localUser.userIcon = icon
            localUser.gender = gender
            localUser.minAge = minAge
            localUser.maxAge = maxAge
            userCache = localUser
            return localUser
        }
        return userCache
    }

    private fun setUserToLocal(user: User) {
        Log.d("test", "setUserToLocal")
        val spEditor = userSharedPref.edit()
        spEditor.putString(KEY_NAME, user.userName)
        spEditor.putString(KEY_USER_ID, user.userID)
        spEditor.putString(KEY_USER_CODE, user.userCode)
        spEditor.putInt(KEY_TYPE, user.type)
        spEditor.putString(KEY_ICON, user.userIcon)
        spEditor.putString(KEY_EMAIL, user.email)
        spEditor.putString(KEY_GENDER, user.gender)
        spEditor.putInt(KEY_MIN_AGE, user.minAge)
        spEditor.putInt(KEY_MAX_AGE, user.maxAge)
        spEditor.commit()
        userCache = user;
    }

    @Synchronized
    override fun removeUser() {
        userSharedPref.edit().clear().commit()
        this.user.userCode = ""
    }

    override fun getGuestUserCode(callback: UserDataSource.GetUserCodeCallback, name: String) {
        // Not required for the local data source
    }

    override fun getUserInfo(callback: Callback<Server.UserDataQuery>, user: User) {
        // Not required for the local data source
    }

    override fun getUser(callback: UserDataSource.GetUserCallback, forceUpdateUserCode: Boolean) {
        callback.onResponse(user)
    }

    override fun setGuestName(guestName: String) {

    }

    override fun registerUser(appId: String, user: User, mergeGuest: Boolean, callback: UserDataSource.RegisterUserCallback) {

    }


    override fun unregisterUser() {

    }

    override fun getUserCode(userType: String, appId: String, user: User, callback: UserDataSource.GetUserCodeCallback) {

    }

    override fun linkGuestToLoginUser(otp: String, guest: String, callback: Callback<ResponseBody>) {

    }

    override fun changeUserName(callback: Callback<ResponseBody>, tokenType: String, token: String, name: String) {

    }

    override fun changeCurrentUserName(name: String, callback: UserDataSource.ChangeUserNameCallback) {

    }

    companion object {
        private const val SHARED_PREF_USER = "user"
        private const val KEY_NAME = "name"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_CODE = "user_code"
        private const val KEY_EMAIL = "email"
        private const val KEY_TYPE = "account_type"
        private const val KEY_ICON = "icon"
        private const val KEY_GENDER = "gender"
        private const val KEY_MIN_AGE = "min_age"
        private const val KEY_MAX_AGE = "max_age"
        private var INSTANCE: SPUserDataSource? = null

        private var userCache: User = User()


        @JvmStatic
        fun getInstance(context: Context): SPUserDataSource {
            return INSTANCE ?: SPUserDataSource(context)
                    .apply { INSTANCE = this }
        }

        @JvmStatic
        fun destroyInstance() {
            INSTANCE = null
        }
    }

}
