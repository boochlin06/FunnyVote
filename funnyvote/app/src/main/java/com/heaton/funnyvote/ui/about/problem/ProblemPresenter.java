package com.heaton.funnyvote.ui.about.problem;

public class ProblemPresenter implements ProblemContract.Presenter {
    private final ProblemContract.View view;

    public ProblemPresenter(ProblemContract.View view) {
        this.view = view;
    }

    @Override
    public void takeView(Object view) {

    }

    @Override
    public void dropView() {

    }
}
