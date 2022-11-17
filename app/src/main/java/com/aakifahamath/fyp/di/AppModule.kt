package com.aakifahamath.fyp.di

import com.aakifahamath.fyp.data.remote.RemoteDatabase
import com.aakifahamath.fyp.data.repository.RepositoryImpl
import com.aakifahamath.fyp.domain.repository.Repository
import com.aakifahamath.fyp.domain.use_case.AuthenticationUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePlateRepo(remote: RemoteDatabase): Repository {
        return RepositoryImpl(remote)
    }

    @Provides
    @Singleton
    fun provideRemoteDatabase(): RemoteDatabase {
        return RemoteDatabase()
    }

    @Provides
    @Singleton
    fun provideAuthenticationUseCase(): AuthenticationUseCase {
        return AuthenticationUseCase()
    }

}