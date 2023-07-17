package com.aakifahamath.lanes.di

import com.aakifahamath.lanes.data.authentication.FirebaseAuthentication
import com.aakifahamath.lanes.data.remote.FirebaseRealtime
import com.aakifahamath.lanes.data.remote.RemoteDatabase
import com.aakifahamath.lanes.data.repository.RepositoryImpl
import com.aakifahamath.lanes.domain.authentication.Authentication
import com.aakifahamath.lanes.domain.repository.Repository
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(components = [SingletonComponent::class],
    replaces = [AppModule::class] )
abstract class TestAppModule {

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
