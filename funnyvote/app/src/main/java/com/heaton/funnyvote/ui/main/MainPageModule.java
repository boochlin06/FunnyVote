package com.heaton.funnyvote.ui.main;

import com.heaton.funnyvote.di.ActivityScoped;
import com.heaton.funnyvote.di.ChildFragmentScoped;

import dagger.Binds;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class MainPageModule {

    @ActivityScoped
    @Binds
    abstract MainPageContract.Presenter mainPagePresenter(MainPagePresenter presenter);

    @ChildFragmentScoped
    @ContributesAndroidInjector
    abstract HotTabFragment hotTabFragment();

    @ChildFragmentScoped
    @ContributesAndroidInjector
    abstract NewsTabFragment newsTabFragment();


}

