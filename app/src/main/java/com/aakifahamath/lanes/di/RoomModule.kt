package com.aakifahamath.lanes.di

import android.app.Application
import androidx.room.Room
import com.aakifahamath.lanes.data.local.PlateDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoomModule {

    @Provides
    @Singleton
    fun provideRoomDatabase(app: Application): PlateDatabase {
        return Room.databaseBuilder(
            app,
            PlateDatabase::class.java,
            "plate_db"
        ).build()
    }

}