package com.heaton.funnyvote.ui.about.aboutapp;

import android.support.annotation.NonNull;

public class AboutAppPresenter implements AboutAppContract.Presenter {
    private final AboutAppContract.View aboutAppView;

    public AboutAppPresenter(@NonNull AboutAppContract.View view) {
        aboutAppView = view;
    }

    @Override
    public void shareApp() {
        aboutAppView.showShareApp();
    }

    @Override
    public void takeView(Object view) {

    }

    @Override
    public void dropView() {

    }
}
