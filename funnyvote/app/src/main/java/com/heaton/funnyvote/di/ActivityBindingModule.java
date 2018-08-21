package com.heaton.funnyvote.di;

import com.heaton.funnyvote.ui.createvote.CreateVoteActivity;
import com.heaton.funnyvote.ui.createvote.CreateVoteModule;
import com.heaton.funnyvote.ui.main.MainPageContract;
import com.heaton.funnyvote.ui.main.MainPagePresenter;
import com.heaton.funnyvote.ui.mainactivity.MainActivity;
import com.heaton.funnyvote.ui.mainactivity.MainActivityModule;
import com.heaton.funnyvote.ui.personal.PersonalActivity;
import com.heaton.funnyvote.ui.personal.PersonalModule;
import com.heaton.funnyvote.ui.personal.UserActivity;
import com.heaton.funnyvote.ui.search.SearchFragment;
import com.heaton.funnyvote.ui.search.SearchModule;
import com.heaton.funnyvote.ui.votedetail.VoteDetailContentActivity;
import com.heaton.funnyvote.ui.votedetail.VoteDetailModule;

import dagger.Binds;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

/**
 * We want Dagger.Android to create a Subcomponent which has a parent Component of whichever module ActivityBindingModule is on,
 * in our case that will be AppComponent. The beautiful part about this setup is that you never need to tell AppComponent that it is going to have all these subcomponents
 * nor do you need to tell these subcomponents that AppComponent exists.
 * We are also telling Dagger.Android that this generated SubComponent needs to include the specified modules and be aware of a scope annotation @ActivityScoped
 * When Dagger.Android annotation processor runs it will create 4 subcomponents for us.
 */
@Module
public abstract class ActivityBindingModule {

    @ActivityScoped
    @ContributesAndroidInjector(modules = VoteDetailModule.class)
    abstract VoteDetailContentActivity voteDetailContentActivity();

    @ActivityScoped
    @ContributesAndroidInjector(modules = MainActivityModule.class)
    abstract MainActivity mainActivity();

    @ActivityScoped
    @ContributesAndroidInjector(modules = PersonalModule.class)
    abstract PersonalActivity personalActivity();

    @ActivityScoped
    @ContributesAndroidInjector(modules = PersonalModule.class)
    abstract UserActivity userActivity();

    @ActivityScoped
    @ContributesAndroidInjector(modules = CreateVoteModule.class)
    abstract CreateVoteActivity createVoteActivity();

}
