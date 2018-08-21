package com.heaton.funnyvote.ui.createvote;

import com.heaton.funnyvote.di.ActivityScoped;
import com.heaton.funnyvote.di.FragmentScoped;

import dagger.Binds;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class CreateVoteModule {
    @ActivityScoped
    @Binds
    abstract CreateVoteContract.Presenter createVotePresenter(CreateVoteActivityPresenter presenter);

    @FragmentScoped
    @ContributesAndroidInjector
    abstract CreateVoteTabOptionFragment createVoteTabOptionFragment();

    @FragmentScoped
    @ContributesAndroidInjector
    abstract CreateVoteTabSettingFragment createVoteTabSettingFragment();

}
