package com.heaton.funnyvote.di

import android.content.Context
import androidx.room.Room
import com.heaton.funnyvote.data.local.AppDatabase
import com.heaton.funnyvote.data.local.dao.UserDao
import com.heaton.funnyvote.data.local.dao.VoteDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "funnyvote_database"
        ).fallbackToDestructiveMigration()
         .build()
    }

    @Provides
    fun provideVoteDao(database: AppDatabase): VoteDao {
        return database.voteDao()
    }

    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }
}
