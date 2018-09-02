package com.heaton.funnyvote

import com.heaton.funnyvote.data.user.UserDataRepository
import com.heaton.funnyvote.data.user.UserDataSource
import com.heaton.funnyvote.database.User

class MainPagePresenter(
        private val userDataRepository: UserDataRepository
        , private val view: MainPageContract.View
) : MainPageContract.Presenter {

    init {
        this.view.setPresenter(this)
    }

    override fun IntentToSearchPage(searchKeyword: String) {
        view.showSearchPage(searchKeyword)
    }

    override fun IntentToCreatePage() {
        view.showCreatePage()
    }

    override fun IntentToUserPage() {
        view.showUserPage()
    }

    override fun IntentToMainPage() {
        view.showMainPage()
    }

    override fun IntentToAboutPage() {
        view.showAboutPage()
    }

    override fun IntentToAccountPage() {
        view.showAccountPage()
    }

    override fun loadUser() {
        userDataRepository.getUser(object : UserDataSource.GetUserCallback {
            override fun onResponse(user: User) {
                view.updateUserView(user)
            }

            override fun onFailure() {}
        }, false)
    }

    override fun start() {
        IntentToMainPage()
        loadUser()
    }
}
