package com.heaton.funnyvote.ui.votedetail;

import com.heaton.funnyvote.di.ActivityScoped;
import com.heaton.funnyvote.utils.Util;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

/**
 * This is a Dagger module. We use this to pass in the View dependency to the
 * {@link VoteDetailPresenter}.
 */
@Module
abstract public class VoteDetailModule {

    @Provides
    @ActivityScoped
    static String provideVoteCode(VoteDetailContentActivity activity) {
        return activity.getIntent().getExtras().getString(Util.BUNDLE_KEY_VOTE_CODE);
    }

    @Provides
    @ActivityScoped
    static VoteDetailContract.View provideVoteDetailView(VoteDetailContentActivity activity) {
        return activity;
    }

    @ActivityScoped
    @Binds
    abstract VoteDetailContract.Presenter voteDetailPresenter(VoteDetailPresenter presenter);
}
