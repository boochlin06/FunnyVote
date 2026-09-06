package com.heaton.funnyvote.data.User;

import android.content.Context;

import com.heaton.funnyvote.data.Local;
import com.heaton.funnyvote.data.Remote;
import com.heaton.funnyvote.data.user.RemoteUserDataSource;
import com.heaton.funnyvote.data.user.SPUserDataSource;
import com.heaton.funnyvote.data.user.UserDataSource;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class UserRepositoryModule {

    @Singleton
    @Provides
    @Local
    UserDataSource provideUserLocalDataSource(Context context) {
        return new SPUserDataSource(context);
    }

    @Singleton
    @Provides
    @Remote
    UserDataSource provideUserRemoteDataSource() {
        return new RemoteUserDataSource();
    }
}
