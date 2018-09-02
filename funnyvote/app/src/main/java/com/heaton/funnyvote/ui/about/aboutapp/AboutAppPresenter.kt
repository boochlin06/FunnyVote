package com.heaton.funnyvote.ui.about.aboutapp


class AboutAppPresenter(private val aboutAppView: AboutAppContract.View) : AboutAppContract.Presenter {

    override fun shareApp() {
        aboutAppView.showShareApp()
    }

    override fun start() {

    }
}
