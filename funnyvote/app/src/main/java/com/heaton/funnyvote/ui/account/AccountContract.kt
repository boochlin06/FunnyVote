package com.heaton.funnyvote.ui.account

import com.heaton.funnyvote.BasePresenter
import com.heaton.funnyvote.BaseView
import com.heaton.funnyvote.database.User

interface AccountContract {
    interface Presenter : BasePresenter {
        fun registerUser(newUser: User, appId: String)
        fun unregisterUser()
        fun updateUser()
        fun changeCurrentUserName(userName: String)
        fun logout()
        fun login(loginType: Int, mergeGuest: Boolean)
    }

    interface View : BaseView<Presenter> {
        fun showUser(user: User)
        fun showLoginView(guestName: String)
        fun showNameEditDialog()
        fun showMergeOptionDialog(loginType: Int)
        fun facebookLogout()
        fun googleSignOut()
        fun twitterlogout()
        fun facebookLogin()
        fun twitterLogin()
        fun googleSignIn()
    }
}
