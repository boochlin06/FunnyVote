package com.heaton.funnyvote.data.user

import android.util.Log
import com.heaton.funnyvote.data.RemoteServiceApi
import com.heaton.funnyvote.database.User
import com.heaton.funnyvote.retrofit.Server
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class UserDataRepository(
        private val localUserDataSource: UserDataSource,
        private val remoteUserSource: UserDataSource
) : UserDataSource {

    override var user: User
        get() = localUserDataSource.user
        set(user) {
            localUserDataSource.user = user
        }

    override fun getUser(callback: UserDataSource.GetUserCallback, forceUpdateUserCode: Boolean) {
        val user = localUserDataSource.user
        if (user.type == User.TYPE_GUEST && user.userCode.isEmpty()) {
            val guestName = "Guest" + (Math.random() * 1000).toInt()//Util.randomUserName(context);
            Log.d(TAG, "Guest!" + user.userCode + " name:" + guestName)
            remoteUserSource.getGuestUserCode(object : UserDataSource.GetUserCodeCallback {
                override fun onSuccess(userCode: String) {
                    user.userName = guestName
                    user.userCode = userCode
                    localUserDataSource.user = user
                    callback.onResponse(localUserDataSource.user)
                }

                override fun onFalure() {
                    callback.onFailure()
                }
            }, guestName)
        } else {
            if (forceUpdateUserCode) {
                remoteUserSource.getUserInfo(object : Callback<Server.UserDataQuery> {
                    override fun onResponse(call: Call<Server.UserDataQuery>, response: Response<Server.UserDataQuery>) {
                        if (response.isSuccessful) {
                            val userCode = if (user.type == User.TYPE_GUEST) {
                                response.body().guestCode
                            } else {
                                response.body().otp
                            }

                            if (userCode != null) {
                                user.userCode = userCode
                                localUserDataSource.user = user
                                callback.onResponse(localUserDataSource.user)
                            }
                        } else {
                            try {
                                val errorMessage = response.errorBody().string()
                                Log.e(TAG, "getUser onResponse false$errorMessage")
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }

                            callback.onFailure()
                        }
                    }

                    override fun onFailure(call: Call<Server.UserDataQuery>, t: Throwable) {
                        callback.onFailure()
                    }
                }, user)
            } else {
                callback.onResponse(user)
            }
        }
    }

    override fun registerUser(appId: String, user: User, mergeGuest: Boolean, callback: UserDataSource.RegisterUserCallback) {
        var userType = ""
        when (user.type) {
            User.TYPE_FACEBOOK -> userType = RemoteServiceApi.USER_TYPE_FACEBOOK
            User.TYPE_GOOGLE -> userType = RemoteServiceApi.USER_TYPE_GOOGLE
            User.TYPE_TWITTER -> userType = RemoteServiceApi.USER_TYPE_TWITTER
        }
        if (!userType.isEmpty()) {
            val guestCode = localUserDataSource.user.userCode
            remoteUserSource.getUserCode(userType, appId, user, object : UserDataSource.GetUserCodeCallback {
                override fun onSuccess(userCode: String) {
                    if (mergeGuest) {
                        remoteUserSource.linkGuestToLoginUser(userCode, guestCode
                                , object : Callback<ResponseBody> {
                            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                                if (response.isSuccessful) {
                                    user.userCode = userCode
                                    localUserDataSource.user = user
                                    callback.onSuccess()
                                } else {
                                    callback.onFailure()
                                    try {
                                        Log.e(TAG, "registerUser" + response.errorBody().string())
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }

                                }
                            }

                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                callback.onFailure()
                                Log.e(TAG, "registerUser onFailure , error message:" + t.message)
                            }
                        })
                    } else {
                        user.userCode = userCode
                        localUserDataSource.user = user
                        callback.onSuccess()
                    }
                }

                override fun onFalure() {
                    callback.onFailure()
                    Log.e(TAG, "registerUser onFailure")
                }
            })

        } else {
            callback.onFailure()
            Log.e(TAG, "registerUser onFailure")
        }
    }

    override fun unregisterUser() {
        localUserDataSource.removeUser()
    }

    override fun getUserCode(userType: String, appId: String, user: User, callback: UserDataSource.GetUserCodeCallback) {
        remoteUserSource.getUserCode(userType, appId, user, callback)
    }

    override fun linkGuestToLoginUser(otp: String, guest: String, callback: Callback<ResponseBody>) {
        remoteUserSource.linkGuestToLoginUser(otp, guest, callback)
    }

    override fun changeUserName(callback: Callback<ResponseBody>, tokenType: String, token: String, name: String) {
        // only for remote
    }

    private fun changeUserName(user: User, newName: String, callback: UserDataSource.ChangeUserNameCallback) {
        remoteUserSource.changeUserName(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    user.userName = newName
                    localUserDataSource.user = user
                    callback.onSuccess()
                } else {
                    try {
                        Log.d(TAG, "changeUserName response status:" + response.code() + " ,message:" + response.errorBody().string())
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    callback.onFailure()
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
                callback.onFailure()
            }
        }, user.tokenType, user.userCode, newName)
    }

    override fun changeCurrentUserName(name: String, callback: UserDataSource.ChangeUserNameCallback) {
        getUser(object : UserDataSource.GetUserCallback {
            override fun onResponse(user: User) {
                changeUserName(user, name, callback)
            }

            override fun onFailure() {
                Log.w(TAG, "changeUserName Fail")
            }
        }, true)
    }

    override fun removeUser() {
        localUserDataSource.removeUser()
    }

    override fun getGuestUserCode(callback: UserDataSource.GetUserCodeCallback, name: String) {
        remoteUserSource.getGuestUserCode(callback, name)
    }

    override fun getUserInfo(callback: Callback<Server.UserDataQuery>, user: User) {
        remoteUserSource.getUserInfo(callback, user)
    }

    override fun setGuestName(guestName: String) {

    }

    companion object {

        @JvmField val TAG: String? = UserDataRepository::class.java.simpleName
        private var INSTANCE: UserDataRepository? = null

        @JvmStatic
        fun getInstance(localUserDataSource: UserDataSource
                        , remoteUserDataSource: UserDataSource): UserDataRepository {
            return INSTANCE ?: UserDataRepository(localUserDataSource, remoteUserDataSource)
                    .apply { INSTANCE = this }
        }

        @JvmStatic
        fun destroyInstance() {
            INSTANCE = null
        }
    }
}
