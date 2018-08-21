package com.heaton.funnyvote.ui.personal;

import com.heaton.funnyvote.di.ActivityScoped;
import com.heaton.funnyvote.di.FragmentScoped;

import dagger.Binds;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class PersonalModule {
    @ActivityScoped
    @Binds
    abstract PersonalContract.Presenter userPresenter(UserPresenter presenter);

    @FragmentScoped
    @ContributesAndroidInjector
    abstract FavoriteTabFragment favoriteTabFragment();

    @FragmentScoped
    @ContributesAndroidInjector
    abstract CreateTabFragment createTabFragment();

    @FragmentScoped
    @ContributesAndroidInjector
    abstract ParticipateTabFragment participateTabFragment();
}
