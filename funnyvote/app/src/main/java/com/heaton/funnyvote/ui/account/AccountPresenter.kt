package com.heaton.funnyvote.ui.account

import android.util.Log

import com.heaton.funnyvote.data.user.UserDataRepository
import com.heaton.funnyvote.data.user.UserDataSource
import com.heaton.funnyvote.database.User

class AccountPresenter(
        private val userDataRepository: UserDataRepository
        , private val view: AccountContract.View
) : AccountContract.Presenter {

    private lateinit var user: User
    private var mergeGuest: Boolean = false

    init {
        this.view.setPresenter(this)
    }

    override fun start() {
        updateUser()
    }

    override fun registerUser(newUser: User, appId: String) {
        userDataRepository.registerUser(appId, newUser, mergeGuest, object : UserDataSource.RegisterUserCallback {
            override fun onSuccess() {
                updateUser()
            }

            override fun onFailure() {
                updateUser()
            }
        })
    }

    override fun unregisterUser() {
        userDataRepository.unregisterUser()
    }

    override fun updateUser() {
        userDataRepository.getUser(object : UserDataSource.GetUserCallback {
            override fun onResponse(user: User) {
                this@AccountPresenter.user = user
                if (this@AccountPresenter.user.type != User.TYPE_GUEST) {
                    view.showUser(user)
                } else {
                    view.showLoginView(user.userName)
                }
            }

            override fun onFailure() {
                this@AccountPresenter.user = User()
                view.showLoginView("Guest")
            }
        }, false)
    }

    override fun changeCurrentUserName(userName: String) {
        userDataRepository.changeCurrentUserName(userName, object : UserDataSource.ChangeUserNameCallback {
            override fun onSuccess() {
                updateUser()
            }

            override fun onFailure() {
                Log.d(TAG, "ChangeUserNameCallback onFailure")
            }
        })
    }

    override fun logout() {
        when (user.type) {
            User.TYPE_FACEBOOK -> view.facebookLogout()
            User.TYPE_GOOGLE -> view.googleSignOut()
            User.TYPE_TWITTER -> view.twitterlogout()
        }
    }

    override fun login(loginType: Int, mergeGuest: Boolean) {
        this.mergeGuest = mergeGuest
        when (loginType) {
            LOGIN_FB -> view.facebookLogin()
            LOGIN_GOOGLE -> view.googleSignIn()
            LOGIN_TWITTER -> view.twitterLogin()
        }
    }

    companion object {

        private val RC_GOOGLE_SIGN_IN = 101
        val LOGIN_FB = 111
        val LOGIN_GOOGLE = 112
        val LOGIN_TWITTER = 113
        private val TAG = AccountPresenter::class.java.simpleName
    }
}
