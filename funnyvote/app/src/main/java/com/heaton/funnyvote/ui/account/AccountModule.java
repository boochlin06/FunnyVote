package com.heaton.funnyvote.ui.account;

import com.heaton.funnyvote.di.ActivityScoped;

import dagger.Binds;
import dagger.Module;

@Module
public abstract class AccountModule {
    @ActivityScoped
    @Binds
    abstract AccountContract.Presenter accountPresenter(AccountPresenter presenter);

}
