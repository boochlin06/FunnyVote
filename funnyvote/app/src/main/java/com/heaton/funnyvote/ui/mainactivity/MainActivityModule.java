package com.heaton.funnyvote.ui.mainactivity;

import com.heaton.funnyvote.di.ActivityScoped;
import com.heaton.funnyvote.di.FragmentScoped;
import com.heaton.funnyvote.ui.about.AboutFragment;
import com.heaton.funnyvote.ui.about.AboutModule;
import com.heaton.funnyvote.ui.account.AccountFragment;
import com.heaton.funnyvote.ui.account.AccountModule;
import com.heaton.funnyvote.ui.main.MainPageFragment;
import com.heaton.funnyvote.ui.main.MainPageModule;
import com.heaton.funnyvote.ui.search.SearchFragment;
import com.heaton.funnyvote.ui.search.SearchModule;

import dagger.Binds;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class MainActivityModule {
    @ActivityScoped
    @Binds
    abstract MainActivityContract.Presenter mainActivityPresenter(MainActivityPresenter presenter);

    @FragmentScoped
    @ContributesAndroidInjector(modules = MainPageModule.class)
    abstract MainPageFragment mainPageFragment();

    @FragmentScoped
    @ContributesAndroidInjector(modules = AboutModule.class)
    abstract AboutFragment aboutFragment();

    @FragmentScoped
    @ContributesAndroidInjector(modules = SearchModule.class)
    abstract SearchFragment searchFragment();

    @FragmentScoped
    @ContributesAndroidInjector(modules = AccountModule.class)
    abstract AccountFragment accountFragment();
}
