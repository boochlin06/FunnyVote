package com.heaton.funnyvote.ui.about.licence;

public class LicencePresenter implements LicenceContract.Presenter {
    private final LicenceContract.View view;

    public LicencePresenter(LicenceContract.View view) {
        this.view = view;
    }


    @Override
    public void takeView(Object view) {

    }

    @Override
    public void dropView() {

    }
}