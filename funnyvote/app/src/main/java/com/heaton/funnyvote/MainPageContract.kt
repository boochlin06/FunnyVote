package com.heaton.funnyvote

import com.heaton.funnyvote.database.User

interface MainPageContract {
    interface Presenter : BasePresenter {
        fun IntentToSearchPage(searchKeyword: String)

        fun IntentToCreatePage()

        fun IntentToUserPage()

        fun IntentToMainPage()

        fun IntentToAboutPage()

        fun IntentToAccountPage()

        fun loadUser()
    }

    interface View : BaseView<Presenter> {
        fun showSearchPage(searchKeyword: String)

        fun showCreatePage()

        fun showUserPage()

        fun showMainPage()

        fun showAboutPage()

        fun showAccountPage()

        fun updateUserView(user: User)
    }
}
