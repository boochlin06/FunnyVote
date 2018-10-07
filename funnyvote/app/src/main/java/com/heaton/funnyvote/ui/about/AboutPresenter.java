package com.heaton.funnyvote.ui.about;

public class AboutPresenter implements AboutContract.Presenter {

    private AboutContract.View view;

    public AboutPresenter(AboutContract.View view) {
        this.view = view;
        this.view.setPresenter(this);
    }

    @Override
    public void IntentToIntroduction() {
        view.showIntroduction();
    }

    @Override
    public void IntentToAuthorInfo() {
        view.showAuthorInfo();
    }

    @Override
    public void IntentToLicence() {
        view.showLicence();
    }

    @Override
    public void IntentToProblem() {
        view.showProblem();
    }

    @Override
    public void IntentToAppStore() {
        view.showAppStore();
    }

    @Override
    public void IntentToAbout() {
        view.showAbout();
    }

    @Override
    public void IntentToShareApp() {
        view.showShareApp();
    }

    @Override
    public void subscribe() {

    }

    @Override
    public void unsubscribe() {

    }
}
