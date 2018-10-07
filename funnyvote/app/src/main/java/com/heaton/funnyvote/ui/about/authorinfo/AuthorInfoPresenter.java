package com.heaton.funnyvote.ui.about.authorinfo;

public class AuthorInfoPresenter implements AuthorInfoContract.Presenter {
    private final AuthorInfoContract.View authorInfoView;

    public AuthorInfoPresenter(AuthorInfoContract.View authorInfoView) {
        this.authorInfoView = authorInfoView;
    }

    @Override
    public void subscribe() {

    }

    @Override
    public void unsubscribe() {

    }
}
