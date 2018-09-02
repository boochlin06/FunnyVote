package com.heaton.funnyvote.data.user

import com.heaton.funnyvote.database.User
import com.heaton.funnyvote.retrofit.Server

import okhttp3.ResponseBody
import retrofit2.Callback

/**
 * Created by chiu_mac on 2016/12/6.
 */

interface UserDataSource {

    var user: User

    interface GetUserCallback {
        fun onResponse(user: User)

        fun onFailure()
    }

    interface GetUserInfoCallback {
        fun onResponse(userData: Server.UserDataQuery)

        fun onFailure()
    }

    interface RegisterUserCallback {
        fun onSuccess()

        fun onFailure()
    }

    interface ChangeUserNameCallback {
        fun onSuccess()

        fun onFailure()
    }

    interface GetUserCodeCallback {
        fun onSuccess(userCode: String)

        fun onFalure()
    }

    fun removeUser()

    fun getGuestUserCode(callback: GetUserCodeCallback, name: String)

    fun getUserInfo(callback: Callback<Server.UserDataQuery>, user: User)

    fun getUser(callback: GetUserCallback, forceUpdateUserCode: Boolean)

    fun setGuestName(guestName: String)

    fun registerUser(appId: String, user: User, mergeGuest: Boolean, callback: RegisterUserCallback)

    fun unregisterUser()

    fun getUserCode(userType: String, appId: String, user: User, callback: GetUserCodeCallback)

    fun linkGuestToLoginUser(otp: String, guest: String, callback: Callback<ResponseBody>)

    fun changeUserName(callback: Callback<ResponseBody>, tokenType: String, token: String, name: String)

    fun changeCurrentUserName(name: String, callback: ChangeUserNameCallback)

}
