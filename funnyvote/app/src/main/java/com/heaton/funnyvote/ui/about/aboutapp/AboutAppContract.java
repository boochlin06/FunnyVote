package com.heaton.funnyvote.ui.about.aboutapp;

import com.heaton.funnyvote.BasePresenter;
import com.heaton.funnyvote.BaseView;

public interface AboutAppContract {
    interface View extends BaseView<Presenter> {
        void showShareApp();
    }
    interface Presenter extends BasePresenter{
        void shareApp();
    }
}
