package com.heaton.funnyvote.ui.about.aboutapp

import com.heaton.funnyvote.BasePresenter
import com.heaton.funnyvote.BaseView

interface AboutAppContract {
    interface View : BaseView<Presenter> {
        fun showShareApp()
    }

    interface Presenter : BasePresenter {
        fun shareApp()
    }
}
