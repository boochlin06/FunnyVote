package com.heaton.funnyvote.data.VoteData;

import android.app.Application;

import com.heaton.funnyvote.data.Local;
import com.heaton.funnyvote.data.Remote;
import com.heaton.funnyvote.database.DaoMaster;
import com.heaton.funnyvote.database.DaoSession;
import com.heaton.funnyvote.database.OptionDao;
import com.heaton.funnyvote.database.VoteDataDao;
import com.heaton.funnyvote.utils.AppExecutors;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;

import java.util.concurrent.Executors;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static com.heaton.funnyvote.FunnyVoteApplication.ENCRYPTED;

@Module
public class VoteDataRepositoryModule {
    private static final int THREAD_COUNT = 3;

    @Singleton
    @Provides
    @Local
    VoteDataSource provideVoteDataLocalDataSource(VoteDataDao voteDataDao
            , OptionDao optionDao, AppExecutors appExecutors) {
        return new LocalVoteDataSource(voteDataDao, optionDao, appExecutors);
    }

    @Singleton
    @Provides
    @Remote
    VoteDataSource provideVoteDataRemoteDataSource() {
        return new FakeRemoteVoteDataRepository();
    }

    @Singleton
    @Provides
    DaoSession provideDaoSession(Application context) {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, ENCRYPTED ? "votes-db-encrypted" : "votes-db");
        Database db = ENCRYPTED ? helper.getEncryptedWritableDb("super-secret") : helper.getWritableDb();
        return new DaoMaster(db).newSession(IdentityScopeType.Session);
    }

    @Singleton
    @Provides
    static VoteDataDao provideVoteDataDao(DaoSession daoSession) {
        return daoSession.getVoteDataDao();
    }

    @Singleton
    @Provides
    static OptionDao provideOptionDao(DaoSession session) {
        return session.getOptionDao();
    }

    @Singleton
    @Provides
    static AppExecutors provideAppExecutors() {
        return new AppExecutors(Executors.newSingleThreadExecutor()
                , Executors.newFixedThreadPool(THREAD_COUNT),
                new AppExecutors.MainThreadExecutor());
    }
}
