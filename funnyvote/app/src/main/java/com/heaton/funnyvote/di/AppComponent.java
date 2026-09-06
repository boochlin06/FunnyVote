package com.heaton.funnyvote.di;

import android.app.Application;

import com.heaton.funnyvote.FunnyVoteApplication;
import com.heaton.funnyvote.data.Promotion.PromotionRepositoryModule;
import com.heaton.funnyvote.data.User.UserRepositoryModule;
import com.heaton.funnyvote.data.VoteData.VoteDataRepository;
import com.heaton.funnyvote.data.VoteData.VoteDataRepositoryModule;
import com.heaton.funnyvote.data.promotion.PromotionRepository;
import com.heaton.funnyvote.data.user.UserDataRepository;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;

@Singleton
@Component(modules = {VoteDataRepositoryModule.class,
        PromotionRepositoryModule.class,
        UserRepositoryModule.class,
        ApplicationModule.class,
        ActivityBindingModule.class,
        AndroidSupportInjectionModule.class})
public interface AppComponent extends AndroidInjector<FunnyVoteApplication> {

    VoteDataRepository getVoteDataRepository();

    PromotionRepository getPromotionRepository();

    UserDataRepository getUserDataRepository();

    @Component.Builder
    interface Builder {

        @BindsInstance
        AppComponent.Builder application(Application context);

        AppComponent build();
    }
}
