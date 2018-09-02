package com.heaton.funnyvote.ui.about

class AboutPresenter(private val view: AboutContract.View) : AboutContract.Presenter {

    init {
        this.view.setPresenter(this)
    }

    override fun IntentToIntroduction() {
        view.showIntroduction()
    }

    override fun IntentToAuthorInfo() {
        view.showAuthorInfo()
    }

    override fun IntentToLicence() {
        view.showLicence()
    }

    override fun IntentToProblem() {
        view.showProblem()
    }

    override fun IntentToAppStore() {
        view.showAppStore()
    }

    override fun IntentToAbout() {
        view.showAbout()
    }

    override fun IntentToShareApp() {
        view.showShareApp()
    }

    override fun start() {

    }
}
