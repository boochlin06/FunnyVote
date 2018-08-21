package com.heaton.funnyvote.ui.search;

import com.heaton.funnyvote.di.ActivityScoped;

import dagger.Binds;
import dagger.Module;

@Module
public abstract class SearchModule {

    @ActivityScoped
    @Binds
    abstract SearchContract.Presenter searchPresenter(SearchPresenter presenter);
//
//    @FragmentScoped
//    @ContributesAndroidInjector
//    abstract SearchFragment searchFragment();

//    @Provides
//    @Named("keyword")
//    static String provideKeyword(SearchFragment fragment) {
//        Bundle searchArgument = fragment.getArguments();
//        String keyword = "";
//        if (searchArgument != null) {
//            keyword = searchArgument.getString(KEY_SEARCH_KEYWORD, "");
//        }
//        return keyword;
//    }

}
