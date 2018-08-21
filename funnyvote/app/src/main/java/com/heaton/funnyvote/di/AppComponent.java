package com.heaton.funnyvote.di;

import android.app.Application;

import com.heaton.funnyvote.FunnyVoteApplication;
import com.heaton.funnyvote.data.Promotion.PromotionRepositoryModule;
import com.heaton.funnyvote.data.User.UserRepositoryModule;
import com.heaton.funnyvote.data.VoteData.VoteDataRepository;
import com.heaton.funnyvote.data.VoteData.VoteDataRepositoryModule;
import com.heaton.funnyvote.data.promotion.PromotionRepository;
import com.heaton.funnyvote.data.user.UserDataRepository;
import com.heaton.funnyvote.database.DaoSession;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;

/**
 * This is a Dagger component. Refer to {@link FunnyVoteApplication} for the list of Dagger components
 * used in this application.
 * <p>
 * Even though Dagger allows annotating a {@link Component} as a singleton, the code
 * itself must ensure only one instance of the class is created. This is done in {@link
 * com.heaton.funnyvote.FunnyVoteApplication}.
 * //{@link AndroidSupportInjectionModule}
 * // is the module from Dagger.Android that helps with the generation
 * // and location of subcomponents.
 */
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

    // Gives us syntactic sugar. we can then do DaggerAppComponent.builder().application(this).build().inject(this);
    // never having to instantiate any modules or say which module we are passing the application to.
    // Application will just be provided into our app graph now.
    @Component.Builder
    interface Builder {

        @BindsInstance
        AppComponent.Builder application(Application context);

        AppComponent build();
    }
}
