package com.heaton.funnyvote.ui.about.licence;

public class LicencePresenter implements LicenceContract.Presenter {
    private LicenceContract.View view;

    public LicencePresenter(LicenceContract.View view) {
        this.view = view;
    }

    @Override
    public void takeView(LicenceContract.View view) {
        this.view = view;
    }

    @Override
    public void dropView() {
        this.view = null;
    }
}