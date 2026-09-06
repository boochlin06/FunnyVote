package com.heaton.funnyvote.ui.account;

import com.heaton.funnyvote.di.FragmentScoped;

import dagger.Binds;
import dagger.Module;

@Module
public abstract class AccountModule {
    @FragmentScoped
    @Binds
    abstract AccountContract.Presenter accountPresenter(AccountPresenter presenter);

}
