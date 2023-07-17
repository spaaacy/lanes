package com.aakifahamath.lanes.data.local

import androidx.room.*
import com.aakifahamath.lanes.data.local.model.PlateLocal
import kotlinx.coroutines.flow.Flow

@Dao
interface PlateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlate(plate: PlateLocal)

    @Delete
    suspend fun deletePlate(plate: PlateLocal)

    @Query("SELECT * FROM platelocal WHERE userId == :userId")
    fun getRecentPlates(userId: String): Flow<List<PlateLocal>>


}