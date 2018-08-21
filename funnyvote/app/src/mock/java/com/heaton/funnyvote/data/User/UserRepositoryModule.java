package com.heaton.funnyvote.data.User;

import android.content.Context;

import com.heaton.funnyvote.data.Local;
import com.heaton.funnyvote.data.Remote;
import com.heaton.funnyvote.data.user.RemoteUserDataSource;
import com.heaton.funnyvote.data.user.SPUserDataSource;
import com.heaton.funnyvote.data.user.UserDataSource;
import com.heaton.funnyvote.database.DaoSession;
import com.heaton.funnyvote.database.UserDao;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

@Module
public class UserRepositoryModule {

    @Singleton
    @Provides
    @Local
    UserDataSource provideUserLocalDataSource(Context context) {
        return new LocalUserDataSource(context);
    }

    @Singleton
    @Provides
    @Remote
    UserDataSource provideUserRemoteDataSource() {
        return new RemoteUserDataSource();
    }

    @Singleton
    @Provides
    UserDao provideUserDao(DaoSession session) {
        return session.getUserDao();
    }
}
