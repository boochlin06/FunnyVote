package com.heaton.funnyvote.data.VoteData;

import android.app.Application;

import com.heaton.funnyvote.data.Local;
import com.heaton.funnyvote.data.Remote;
import com.heaton.funnyvote.data.local.AppDatabase;
import com.heaton.funnyvote.data.local.dao.OptionDao;
import com.heaton.funnyvote.data.local.dao.VoteDataDao;
import com.heaton.funnyvote.utils.AppExecutors;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class VoteDataRepositoryModule {

    @Singleton
    @Provides
    @Local
    VoteDataSource provideVoteDataLocalDataSource(VoteDataDao voteDataDao,
                                                  OptionDao optionDao,
                                                  AppExecutors appExecutors) {
        return new LocalVoteDataSource(voteDataDao, optionDao, appExecutors);
    }

    @Singleton
    @Provides
    @Remote
    VoteDataSource provideVoteDataRemoteDataSource() {
        return new RemoteVoteDataSource();
    }

    @Singleton
    @Provides
    AppDatabase provideAppDatabase(Application context) {
        return AppDatabase.getInstance(context);
    }

    @Singleton
    @Provides
    static VoteDataDao provideVoteDataDao(AppDatabase database) {
        return database.voteDataDao();
    }

    @Singleton
    @Provides
    static OptionDao provideOptionDao(AppDatabase database) {
        return database.optionDao();
    }

    @Singleton
    @Provides
    static AppExecutors provideAppExecutors() {
        return AppExecutors.getInstance();
    }
}
