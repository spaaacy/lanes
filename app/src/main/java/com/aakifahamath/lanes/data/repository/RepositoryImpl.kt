package com.aakifahamath.lanes.data.repository

import android.content.Context
import com.aakifahamath.lanes.data.local.PlateDatabase
import com.aakifahamath.lanes.data.local.model.PlateLocal
import com.aakifahamath.lanes.domain.util.Rating
import com.aakifahamath.lanes.data.remote.RemoteDatabase
import com.aakifahamath.lanes.domain.repository.Repository
import com.aakifahamath.lanes.util.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import com.aakifahamath.lanes.domain.model.Plate as Plate

class RepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val remote: RemoteDatabase,
    private val plateDatabase: PlateDatabase
) : Repository {

    private var currentPlateKey: String? = null
    private var currentUserId: String? = null
    private var isUserAnonymous = false

    private var isPlateListenerActive = false
    private var isUpvoteListenerActive = false
    private var isDownvoteListenerActive = false
    private var isOwnerListenerActive = false

    // Left public for testing
    var plateListenerFlow: ProducerScope<Resource<Plate>>? = null

    private val ownedPlates = mutableListOf<Plate>()
    private val upvotedPlates = mutableMapOf<Plate, Long>()
    private val downvotedPlates = mutableMapOf<Plate, Long>()

    /*
    * Room functions
    * */
    override suspend fun insertPlateToDb(prefix: String, number: String) {
        val newPlate = PlateLocal(
            userId = currentUserId ?: ANONYMOUS_ID,
            prefix = prefix,
            number = number
        )
        plateDatabase.plateDao.insertPlate(newPlate)
    }

    override suspend fun deletePlateToDb(prefix: String, number: String) {
        val newPlate = PlateLocal(
            userId = currentUserId ?: ANONYMOUS_ID,
            prefix = prefix,
            number = number
        )
        plateDatabase.plateDao.deletePlate(newPlate)
    }

    override fun getRecentPlatesFromDb(): Flow<List<PlateLocal>> {
        return plateDatabase.plateDao.getRecentPlates(currentUserId ?: ANONYMOUS_ID)
    }

    /*
    * Fetch functions
    * */
    override fun getOwnedPlates(): MutableList<Plate> {
        return ownedPlates
    }

    override fun getUpvotedPlates(): MutableMap<Plate, Long> {
        return upvotedPlates
    }

    override fun getDownvotedPlates(): MutableMap<Plate, Long> {
        return downvotedPlates
    }

    override fun getCurrentUserId(): String? {
        return currentUserId
    }

    override fun isUserAnonymous(): Boolean {
        return isUserAnonymous
    }

    /*
    * User functions
    * */

    override fun setAnonymousUser() {
        isUserAnonymous = true
    }

    override fun loginExistingUser(userId: String) {
        currentUserId = userId
    }

    override fun createNewUser(userId: String) {
        remote.createNewUser(userId)
        loginExistingUser(userId)
    }

    override fun signOutUser() {
        isUserAnonymous = false
        currentUserId = null
    }

    override fun startUserListeners(): Flow<Resource<Boolean>> {
        var isOwnedPlatesLoaded = false
        var isUpvotedPlatesLoaded = false
        var isDownvotedPlatesLoaded = false
        return callbackFlow {
            trySend(Resource.Loading())
            if (!isUserAnonymous) {
                if (isOwnerListenerActive && isUpvoteListenerActive && isDownvoteListenerActive) {
                    trySend(Resource.Success(true))
                    close()
                } else {
                    listenForOwnedPlates(onComplete = {
                        isOwnedPlatesLoaded = true
                        if (isUpvotedPlatesLoaded && isDownvotedPlatesLoaded) {
                            trySend(Resource.Success(true))
                            close()
                        }
                    })
                    listenForUserReports(
                        onUpvote = {
                            isUpvotedPlatesLoaded = true
                            if (isOwnedPlatesLoaded && isDownvotedPlatesLoaded) {
                                trySend(Resource.Success(true))
                                close()
                            }
                        },
                        onDownvote = {
                            isDownvotedPlatesLoaded = true
                            if (isOwnedPlatesLoaded && isUpvotedPlatesLoaded) {
                                trySend(Resource.Success(true))
                                close()
                            }
                        }
                    )
                }
            } else {
                trySend(Resource.Error(ANONYMOUS_USER))
                close()
            }
            awaitClose()
        }
    }

    override fun getLatestReportTimestamp(plate: Plate): Long {
        var largestTimestamp = 0L
        if (!isUserAnonymous){

            upvotedPlates.forEach { pair ->
                if (pair.key.prefix == plate.prefix) {
                    if (pair.key.number == plate.number) {
                        if (pair.value > largestTimestamp) {
                            largestTimestamp = pair.value
                        }
                    }
                }
            }

            downvotedPlates.forEach { pair ->
                if (pair.key.prefix == plate.prefix) {
                    if (pair.key.number == plate.number) {
                        if (pair.value > largestTimestamp) {
                            largestTimestamp = pair.value
                        }
                    }
                }
            }
        }
        return largestTimestamp
    }

    override fun listenForUserReports(
        onUpvote: (Map<Plate, Long>) -> Unit, onDownvote: (Map<Plate, Long>) -> Unit
    ) {
        if (!isUserAnonymous) {
            currentUserId?.let { userId ->

                if(!isUpvoteListenerActive && !isDownvoteListenerActive) {
                    remote.initializeUpvoteListener { upvoteMap ->
                        upvotedPlates.clear()

                        val upvoteKeyList = mutableListOf<String?>()
                        upvoteMap.forEach {
                            upvoteKeyList.add(it.key)
                        }

                        remote.getPlateDetailsFromKeys(upvoteKeyList) { upvotedPlatesList ->
                            upvotedPlatesList.forEachIndexed { i, plateInList ->
                                val plateKey = upvoteKeyList[i]
                                upvotedPlates[plateInList] = upvoteMap[plateKey] ?: 0L
                            }
                            onUpvote(upvotedPlates) // Mainly serves testing purposes
                        }
                    }

                    remote.initializeDownvoteListener { downvoteMap ->
                        downvotedPlates.clear()

                        val downvoteKeyList = mutableListOf<String?>()
                        downvoteMap.forEach {
                            downvoteKeyList.add(it.key)
                        }

                        remote.getPlateDetailsFromKeys(downvoteKeyList) { downvotedPlatesList ->
                            downvotedPlatesList.forEachIndexed { i, plateInList ->
                                val plateKey = downvoteKeyList[i]
                                downvotedPlates[plateInList] = downvoteMap[plateKey] ?: 0L
                            }
                            onDownvote(downvotedPlates) // Mainly serves testing purposes
                        }
                    }

                    remote.listenForUpvote(userId)
                    remote.listenForDownvote(userId)

                    isUpvoteListenerActive = true
                    isDownvoteListenerActive = true
                }

            }
        }
    }

    override fun isPlateOwned(plate: Plate): Boolean {
        if (!isUserAnonymous) {
            ownedPlates.forEach { plateItem ->
                // Does not check for a match for the whole plate object as this can cause error's when reputation changes
                if (plateItem.prefix == plate.prefix)
                    if (plateItem.number == plate.number) {
                        return true
                    }
            }
        }
        return false
    }

    override fun registerPlateOwner(plate: Plate) {
        currentUserId?.let { userId ->
            currentPlateKey?.let { plateKey ->
                remote.registerPlateOwner(plateKey, userId)
            }
        }
    }

    override fun listenForOwnedPlates(onComplete: (List<Plate>) -> Unit) {
        if (!isUserAnonymous) {
            currentUserId?.let { userId ->
                if(!isOwnerListenerActive) {
                    remote.initializeOwnerListener(onComplete = { plateKeyList ->
                        remote.getPlateDetailsFromKeys(plateKeyList) { plates ->
                            ownedPlates.clear()
                            plates.forEach { plate ->
                                ownedPlates.add(plate)
                            }
                            onComplete(ownedPlates)
                        }
                    })
                    remote.listenForOwner(userId)
                    isOwnerListenerActive = true
                }
            }
        }
    }

    /*
    * Plate functions
    * */
    override fun listenForAndCreateNewPlate(
        prefix: String, number: String
    ): Flow<Resource<Plate>> {
        return callbackFlow {
            plateListenerFlow = this
            trySend(Resource.Loading())
            val isNetworkAvailable = Utility.isInternetAvailable(context)
            if (isNetworkAvailable) {
                remote.initializePlateListener(prefix, number,
                    onFound = { plate, key ->
                        currentPlateKey = key
                        trySend(Resource.Success(plate))
                    },
                    onNotFound = {
                        trySend(Resource.Loading())
                        remote.createNewPlate(prefix, number)
                    })
                remote.listenForPlate()
                isPlateListenerActive = true
            } else {
                trySend(Resource.Error(NETWORK_FAILURE))
            }
            awaitClose()
        }
    }

    override fun removePlateListener() {
        if (isPlateListenerActive) {
            plateListenerFlow?.close()
            remote.removePlateListener()
            isPlateListenerActive = false
            currentPlateKey = null
        }
    }

    override fun modifyReputationAndLogUserReport(rating: Rating) {
        if (!isUserAnonymous) {
            currentPlateKey?.let { plateKey ->
                currentUserId?.let { userId ->
                    remote.modifyPlateReputation(plateKey, rating, userId)
                    when (rating) {
                        Rating.UPVOTE -> remote.logUserReportInRecord(plateKey, userId, UPVOTE_PATH)
                        Rating.DOWNVOTE -> remote.logUserReportInRecord(plateKey,userId,DOWNVOTE_PATH)
                    }
                }
            }
        }
    }

    override fun removeUserListeners() {
        if (!isUserAnonymous) {
            currentUserId?.let { userId ->
                if (isOwnerListenerActive) {
                    ownedPlates.clear()
                    remote.removeOwnerListener(userId)
                    isOwnerListenerActive = false
                }
                if (isUpvoteListenerActive) {
                    upvotedPlates.clear()
                    remote.removeUpvoteListener(userId)
                    isUpvoteListenerActive = false
                }
                if (isDownvoteListenerActive) {
                    downvotedPlates.clear()
                    remote.removeDownvoteListener(userId)
                    isDownvoteListenerActive = false
                }
            }
        }
    }

    /*
    * For testing purposes
    * */
    fun setPlateKey(newPlateKey: String?) {
        currentPlateKey = newPlateKey
    }

    fun setPlateListener(active: Boolean) {
        isPlateListenerActive = active
    }

    fun setUserReportListener(rating: Rating, active: Boolean) {
        when (rating) {
            Rating.UPVOTE -> isUpvoteListenerActive = active
            Rating.DOWNVOTE -> isDownvoteListenerActive = active
        }
    }

    fun setOwnerListener(active: Boolean) {
        isOwnerListenerActive = active
    }

    fun clearOwnedPlates() {
        ownedPlates.clear()
    }

    fun addToOwnedPlates(newPlate: Plate) {
        ownedPlates.add(newPlate)
    }

}