package com.heaton.funnyvote.ui.about;

import com.heaton.funnyvote.di.ActivityScoped;

import dagger.Binds;
import dagger.Module;

@Module
public abstract class AboutModule {
    @ActivityScoped
    @Binds
    abstract AboutContract.Presenter aboutPresenter(AboutPresenter presenter);
}
