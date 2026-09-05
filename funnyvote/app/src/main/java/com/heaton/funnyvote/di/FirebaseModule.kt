package com.heaton.funnyvote.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.storage.FirebaseStorage
import com.heaton.funnyvote.data.remote.VoteRemoteDataSource
import com.heaton.funnyvote.data.remote.firebase.FirestoreVoteDataSource
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        val firestore = FirebaseFirestore.getInstance()
        val settings = firestoreSettings {
            setLocalCacheSettings(com.google.firebase.firestore.persistentCacheSettings { })
        }
        firestore.firestoreSettings = settings
        return firestore
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceBindingModule {

    @Binds
    @Singleton
    abstract fun bindVoteRemoteDataSource(
        impl: FirestoreVoteDataSource
    ): VoteRemoteDataSource
}
