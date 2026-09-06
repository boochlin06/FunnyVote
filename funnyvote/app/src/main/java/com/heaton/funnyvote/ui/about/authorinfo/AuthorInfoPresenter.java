package com.heaton.funnyvote.ui.about.authorinfo;

public class AuthorInfoPresenter implements AuthorInfoContract.Presenter {
    private AuthorInfoContract.View authorInfoView;

    public AuthorInfoPresenter(AuthorInfoContract.View authorInfoView) {
        this.authorInfoView = authorInfoView;
    }

    @Override
    public void takeView(AuthorInfoContract.View view) {
        this.authorInfoView = view;
    }

    @Override
    public void dropView() {
        this.authorInfoView = null;
    }
}
