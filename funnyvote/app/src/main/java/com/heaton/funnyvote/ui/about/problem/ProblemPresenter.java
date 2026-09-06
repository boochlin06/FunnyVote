package com.heaton.funnyvote.ui.about.problem;

public class ProblemPresenter implements ProblemContract.Presenter {
    private ProblemContract.View view;

    public ProblemPresenter(ProblemContract.View view) {
        this.view = view;
    }

    @Override
    public void takeView(ProblemContract.View view) {
        this.view = view;
    }

    @Override
    public void dropView() {
        this.view = null;
    }
}
