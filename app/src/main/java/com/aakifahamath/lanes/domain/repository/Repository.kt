package com.aakifahamath.lanes.domain.repository

import com.aakifahamath.lanes.data.local.model.PlateLocal
import com.aakifahamath.lanes.domain.model.Plate
import com.aakifahamath.lanes.domain.util.Rating
import com.aakifahamath.lanes.util.Resource
import kotlinx.coroutines.flow.Flow

interface Repository {
    //Room functions
    suspend fun insertPlateToDb(prefix: String, number: String)
    suspend fun deletePlateToDb(prefix: String, number: String)
    fun getRecentPlatesFromDb(): Flow<List<PlateLocal>>
    // Fetch functions
    fun getOwnedPlates(): MutableList<Plate>
    fun getUpvotedPlates(): MutableMap<Plate, Long>
    fun getDownvotedPlates(): MutableMap<Plate, Long>
    fun getCurrentUserId(): String?
    fun isUserAnonymous(): Boolean
    // User functions
    fun setAnonymousUser()
    fun loginExistingUser(userId: String)
    fun createNewUser(userId: String)
    fun signOutUser()
    fun startUserListeners(): Flow<Resource<Boolean>>
    fun getLatestReportTimestamp(plate: Plate): Long
    fun listenForUserReports(onUpvote: (Map<Plate, Long>) -> Unit, onDownvote: (Map<Plate, Long>) -> Unit)
    fun isPlateOwned(plate: Plate): Boolean
    fun registerPlateOwner(plate: Plate)
    fun listenForOwnedPlates(onComplete: (List<Plate>) -> Unit)
    // Plate functions
    fun listenForAndCreateNewPlate(prefix: String, number: String): Flow<Resource<Plate>>
    fun removePlateListener()
    fun modifyReputationAndLogUserReport(rating: Rating)
    fun removeUserListeners()
}