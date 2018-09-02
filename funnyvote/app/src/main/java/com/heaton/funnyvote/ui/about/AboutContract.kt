package com.heaton.funnyvote.ui.about

import com.heaton.funnyvote.BasePresenter
import com.heaton.funnyvote.BaseView

interface AboutContract {
    interface Presenter : BasePresenter {
        fun IntentToIntroduction()
        fun IntentToAuthorInfo()
        fun IntentToLicence()
        fun IntentToProblem()
        fun IntentToAppStore()
        fun IntentToAbout()
        fun IntentToShareApp()
    }

    interface View : BaseView<Presenter> {
        fun showIntroduction()
        fun showAuthorInfo()
        fun showLicence()
        fun showProblem()
        fun showAppStore()
        fun showAbout()
        fun showShareApp()
    }
}
