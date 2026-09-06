package com.heaton.funnyvote.ui.about;

import com.heaton.funnyvote.di.FragmentScoped;

import dagger.Binds;
import dagger.Module;

@Module
public abstract class AboutModule {
    @FragmentScoped
    @Binds
    abstract AboutContract.Presenter aboutPresenter(AboutPresenter presenter);
}
