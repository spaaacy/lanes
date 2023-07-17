package com.aakifahamath.lanes.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aakifahamath.lanes.data.local.model.PlateLocal

@Database(entities = [PlateLocal::class], version = 1)
abstract class PlateDatabase: RoomDatabase() {
    abstract val plateDao: PlateDao
}