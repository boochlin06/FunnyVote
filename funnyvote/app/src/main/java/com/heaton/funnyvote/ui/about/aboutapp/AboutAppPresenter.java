package com.heaton.funnyvote.ui.about.aboutapp;

import androidx.annotation.NonNull;

import com.heaton.funnyvote.ui.about.aboutapp.AboutAppContract;

public class AboutAppPresenter implements AboutAppContract.Presenter{
    private AboutAppContract.View aboutAppView;

    public AboutAppPresenter(@NonNull AboutAppContract.View view) {
        aboutAppView = view;
    }

    @Override
    public void shareApp() {
        if (aboutAppView != null) {
            aboutAppView.showShareApp();
        }
    }

    @Override
    public void takeView(AboutAppContract.View view) {
        this.aboutAppView = view;
    }

    @Override
    public void dropView() {
        this.aboutAppView = null;
    }
}
