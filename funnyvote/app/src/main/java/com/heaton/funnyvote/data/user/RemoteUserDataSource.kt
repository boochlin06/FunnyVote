package com.heaton.funnyvote.data.user

import android.util.Log
import com.heaton.funnyvote.data.RemoteServiceApi
import com.heaton.funnyvote.database.User
import com.heaton.funnyvote.retrofit.Server
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class RemoteUserDataSource : UserDataSource {
    private val userService: Server.UserService = RemoteServiceApi.getInstance().userService

    override
    var user: User
        get() = User()
        set(user) {
            // Not required for the network data source
        }

    override fun removeUser() {
        // Not required for the network data source
    }

    override fun getGuestUserCode(callback: UserDataSource.GetUserCodeCallback, name: String) {
        val call = userService.getGuestCode(name)
        call.enqueue(GuestUserCodeResponseCallback(callback))
    }

    override fun getUserInfo(callback: Callback<Server.UserDataQuery>, user: User) {
        val call = userService.getUserInfo(user.tokenType, user.userCode)
        call.enqueue(callback)
    }

    override fun getUser(callback: UserDataSource.GetUserCallback, forceUpdateUserCode: Boolean) {

    }

    override fun setGuestName(guestName: String) {

    }

    override fun registerUser(appId: String, user: User, mergeGuest: Boolean, callback: UserDataSource.RegisterUserCallback) {

        //Not required for the network data source
    }


    override fun unregisterUser() {
        //Not required for the network data source
    }

    override fun getUserCode(userType: String, appId: String, user: User, callback: UserDataSource.GetUserCodeCallback) {
        val call = userService.addUser(userType, appId, user.userID,
                user.userName, user.email, user.userIcon, user.gender)
        call.enqueue(LoginUserCodeResponseCallback(callback))
    }

    override fun linkGuestToLoginUser(otp: String, guest: String, callback: Callback<ResponseBody>) {
        val call = userService.linkGuestLoginUser(otp, guest)
        call.enqueue(callback)
    }

    override fun changeUserName(callback: Callback<ResponseBody>, tokenType: String, token: String, name: String) {
        val call = userService.changeUserName(tokenType, token, name)
        call.enqueue(callback)
    }

    override fun changeCurrentUserName(name: String, callback: UserDataSource.ChangeUserNameCallback) {

    }

    internal inner class LoginUserCodeResponseCallback(var getUserCodeCallback: UserDataSource.GetUserCodeCallback) : Callback<ResponseBody> {

        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
            Log.d(TAG, "Response Status:" + response.code())

            if (response.isSuccessful) {
                try {
                    val responseStr = response.body().string()
                    val jsonObject = JSONObject(responseStr)
                    val otpString = jsonObject.getString("otp")
                    getUserCodeCallback.onSuccess(otpString)
                } catch (e: Exception) {
                    e.printStackTrace()
                    getUserCodeCallback.onFalure()
                }

            } else {
                try {
                    Log.d(TAG, "onResponse false:" + response.errorBody().string())
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                getUserCodeCallback.onFalure()
            }
        }

        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            getUserCodeCallback.onFalure()
        }
    }

    internal inner class GuestUserCodeResponseCallback(var getUserCodeCallback: UserDataSource.GetUserCodeCallback) : Callback<ResponseBody> {

        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
            Log.d(TAG, "Response Status:" + response.code())
            if (response.isSuccessful) {
                try {
                    val responseStr = response.body().string()
                    val jsonObject = JSONObject(responseStr)
                    val guestCode = jsonObject.getString("guest")
                    getUserCodeCallback.onSuccess(guestCode)
                } catch (e: Exception) {
                    e.printStackTrace()
                    getUserCodeCallback.onFalure()
                }

            } else {
                getUserCodeCallback.onFalure()
            }
        }

        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            getUserCodeCallback.onFalure()
        }
    }

    companion object {
        private val TAG = RemoteUserDataSource::class.java.simpleName
        private var INSTANCE: RemoteUserDataSource? = null

        @JvmStatic
        fun getInstance(): RemoteUserDataSource {
            return INSTANCE ?: RemoteUserDataSource()
                    .apply { INSTANCE = this }
        }

        @JvmStatic
        fun destroyInstance() {
            INSTANCE = null
        }
    }
}
