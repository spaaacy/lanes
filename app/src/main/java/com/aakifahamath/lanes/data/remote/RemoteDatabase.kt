package com.aakifahamath.lanes.data.remote

import com.aakifahamath.lanes.domain.model.Plate
import com.aakifahamath.lanes.domain.util.Rating

interface RemoteDatabase {
    // User functions
    fun createNewUser(userId: String)
    fun listenForUpvote(userId: String)
    fun removeUpvoteListener(userId: String)
    fun initializeUpvoteListener(onComplete: (Map<String?, Long>) -> Unit)
    fun listenForDownvote(userId: String)
    fun removeDownvoteListener(userId: String)
    fun initializeDownvoteListener(onComplete: (Map<String?, Long>) -> Unit)
    fun logUserReportInRecord(plateKey: String, userId: String, reportPath: String)
    fun listenForOwner(userId: String)
    fun removeOwnerListener(userId: String)
    fun registerPlateOwner(plateKey: String, userId: String)
    fun initializeOwnerListener(onComplete: (List<String?>) -> Unit)
    // Plate functions
    fun getPlateDetailsFromKeys(plateKeys: List<String?>, onFound: (List<Plate>) -> Unit)
    fun createNewPlate(prefix: String, number: String)
    fun listenForPlate()
    fun removePlateListener()
    fun initializePlateListener(prefix: String, number: String, onFound: (Plate, String?) -> Unit, onNotFound: () -> Unit)
    fun modifyPlateReputation(plateKey: String, rating: Rating, userId: String)
}