package com.aakifahamath.lanes.di

import com.aakifahamath.lanes.data.remote.FirebaseRealtime
import com.aakifahamath.lanes.data.remote.RemoteDatabase
import com.aakifahamath.lanes.data.repository.RepositoryImpl
import com.aakifahamath.lanes.domain.repository.Repository
import com.aakifahamath.lanes.domain.authentication.Authentication
import com.aakifahamath.lanes.data.authentication.FirebaseAuthentication
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun provideRepository(
        repositoryImpl: RepositoryImpl
    ): Repository

    @Binds
    @Singleton
    abstract fun bindRemoteDatabase(
        firebaseRealtime: FirebaseRealtime
    ): RemoteDatabase

    @Binds
    @Singleton
    abstract fun provideAuthentication(
        firebaseAuthentication: FirebaseAuthentication
    ) : Authentication

}