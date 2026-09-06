package com.heaton.funnyvote.data.user

import android.text.TextUtils
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.heaton.funnyvote.database.User
import com.heaton.funnyvote.retrofit.Server
import okhttp3.MediaType
import okhttp3.ResponseBody
import retrofit2.Callback
import retrofit2.Response
import java.util.UUID

class RemoteUserDataSource private constructor() : UserDataSource {

    override var user: User
        get() = User()
        set(value) {}

    override fun removeUser() {}

    override fun getGuestUserCode(callback: UserDataSource.GetUserCodeCallback, name: String) {
        val auth = FirebaseAuth.getInstance()
        val current = auth.currentUser
        if (current != null) {
            val uid = current.uid
            val userData = hashMapOf<String, Any>(
                "userId" to uid,
                "userName" to if (name.isNotEmpty()) name else "Guest",
                "createdAt" to System.currentTimeMillis()
            )
            FirebaseFirestore.getInstance().collection("users").document(uid)
                .set(userData, SetOptions.merge())
                .addOnCompleteListener { callback.onSuccess(uid) }
        } else {
            auth.signInAnonymously().addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: ("guest_" + UUID.randomUUID().toString().substring(0, 8))
                val userData = hashMapOf<String, Any>(
                    "userId" to uid,
                    "userName" to if (name.isNotEmpty()) name else "Guest",
                    "createdAt" to System.currentTimeMillis()
                )
                FirebaseFirestore.getInstance().collection("users").document(uid)
                    .set(userData, SetOptions.merge())
                    .addOnCompleteListener { callback.onSuccess(uid) }
            }.addOnFailureListener { e ->
                Log.e(TAG, "signInAnonymously failed: ${e.message}")
                val fallbackUid = "guest_" + UUID.randomUUID().toString().substring(0, 8)
                callback.onSuccess(fallbackUid)
            }
        }
    }

    private fun <T> dummyCall(): retrofit2.Call<T> {
        return object : retrofit2.Call<T> {
            override fun clone(): retrofit2.Call<T> = this
            override fun execute(): Response<T> = throw UnsupportedOperationException()
            override fun enqueue(callback: Callback<T>) {}
            override fun isExecuted(): Boolean = true
            override fun cancel() {}
            override fun isCanceled(): Boolean = false
            override fun request(): okhttp3.Request = okhttp3.Request.Builder().url("https://funny-vote.com").build()
            override fun timeout(): okio.Timeout = okio.Timeout.NONE
        }
    }

    override fun getUserInfo(callback: Callback<Server.UserDataQuery>, user: User) {
        if (user.userCode.isNullOrEmpty()) {
            val query = Server.UserDataQuery().apply {
                memberName = user.userName ?: "Guest"
                guestCode = ""
            }
            callback.onResponse(dummyCall(), Response.success(query))
            return
        }

        FirebaseFirestore.getInstance().collection("users").document(user.userCode)
            .get()
            .addOnSuccessListener { doc ->
                val query = Server.UserDataQuery().apply {
                    memberName = doc.getString("userName") ?: (user.userName ?: "User")
                    guestCode = doc.getString("userId") ?: user.userCode
                }
                callback.onResponse(dummyCall(), Response.success(query))
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "getUserInfo failed: ${e.message}")
                val query = Server.UserDataQuery().apply {
                    memberName = user.userName ?: "User"
                    guestCode = user.userCode
                }
                callback.onResponse(dummyCall(), Response.success(query))
            }
    }

    override fun getUserCode(userType: String, appId: String, user: User, callback: UserDataSource.GetUserCodeCallback) {
        val auth = FirebaseAuth.getInstance()
        val current = auth.currentUser
        val uid = current?.uid ?: user.userCode

        if (!uid.isNullOrEmpty()) {
            val userData = hashMapOf<String, Any>(
                "userId" to uid,
                "userName" to (user.userName ?: "User"),
                "email" to (user.email ?: ""),
                "icon" to (user.userIcon ?: "")
            )
            FirebaseFirestore.getInstance().collection("users").document(uid)
                .set(userData, SetOptions.merge())
                .addOnCompleteListener { callback.onSuccess(uid) }
        } else {
            callback.onSuccess(if (!user.userCode.isNullOrEmpty()) user.userCode else UUID.randomUUID().toString())
        }
    }

    override fun linkGuestToLoginUser(otp: String, guest: String, callback: Callback<ResponseBody>) {
        val body = ResponseBody.create(MediaType.parse("text/plain"), "success")
        callback.onResponse(dummyCall(), Response.success(body))
    }

    override fun changeUserName(callback: Callback<ResponseBody>, tokenType: String, token: String, name: String) {
        val body = ResponseBody.create(MediaType.parse("text/plain"), "success")
        if (!token.isNullOrEmpty()) {
            val update = hashMapOf<String, Any>("userName" to name)
            FirebaseFirestore.getInstance().collection("users").document(token)
                .set(update, SetOptions.merge())
                .addOnCompleteListener {
                    callback.onResponse(dummyCall(), Response.success(body))
                }
        } else {
            callback.onResponse(dummyCall(), Response.success(body))
        }
    }

    override fun changeCurrentUserName(name: String, callback: UserDataSource.ChangeUserNameCallback) {
        val auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid
        if (!uid.isNullOrEmpty()) {
            val update = hashMapOf<String, Any>("userName" to name)
            FirebaseFirestore.getInstance().collection("users").document(uid)
                .set(update, SetOptions.merge())
                .addOnSuccessListener { callback.onSuccess() }
                .addOnFailureListener { callback.onFailure() }
        } else {
            callback.onSuccess()
        }
    }

    override fun getUser(callback: UserDataSource.GetUserCallback, forceUpdateUserCode: Boolean) {
        callback.onResponse(user)
    }

    override fun setGuestName(guestName: String) {}

    override fun registerUser(appId: String, user: User, mergeGuest: Boolean, callback: UserDataSource.RegisterUserCallback) {
        callback.onSuccess()
    }

    override fun unregisterUser() {}

    companion object {
        private const val TAG = "RemoteUserDataSource"
        private var INSTANCE: RemoteUserDataSource? = null

        @JvmStatic
        fun getInstance(): RemoteUserDataSource {
            if (INSTANCE == null) {
                synchronized(RemoteUserDataSource::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = RemoteUserDataSource()
                    }
                }
            }
            return INSTANCE!!
        }

        @JvmStatic
        fun destroyInstance() {
            INSTANCE = null
        }
    }
}
