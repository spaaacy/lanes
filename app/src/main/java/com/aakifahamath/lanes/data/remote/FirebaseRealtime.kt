package com.aakifahamath.lanes.data.remote

import com.aakifahamath.lanes.data.remote.model.PlateRemote
import com.aakifahamath.lanes.domain.model.Plate
import com.aakifahamath.lanes.domain.util.Rating
import com.aakifahamath.lanes.domain.util.Rating.DOWNVOTE
import com.aakifahamath.lanes.domain.util.Rating.UPVOTE
import com.aakifahamath.lanes.util.*
import com.google.firebase.database.*
import com.google.firebase.database.FirebaseDatabase
import javax.inject.Inject

class FirebaseRealtime @Inject constructor(
    private val firebaseDb: FirebaseDatabase
) : RemoteDatabase {

    private val plateRef = firebaseDb.getReference(PLATE_PATH)
    private val userRef = firebaseDb.getReference(USER_PATH)

    private lateinit var plateEventListener: ValueEventListener
    private lateinit var downvoteEventListener: ValueEventListener
    private lateinit var upvoteEventListener: ValueEventListener
    private lateinit var ownerEventListener: ValueEventListener

    /*
    * User functions
    * */
    override fun createNewUser(userId: String) {
        userRef.child(userId).setValue("")
    }

    override fun listenForUpvote(userId: String) {
        userRef.child(userId).child(UPVOTE_PATH).addValueEventListener(upvoteEventListener)
    }

    override fun removeUpvoteListener(userId: String) {
        userRef.child(userId).child(UPVOTE_PATH).removeEventListener(upvoteEventListener)
    }

    override fun initializeUpvoteListener(
        onComplete: (Map<String?, Long>) -> Unit
    ) {
        upvoteEventListener = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                val newUpvoteMap = mutableMapOf<String?, Long>()
                for (snap in snapshot.children) {
                    val timestamp = snap.child(TIMESTAMP_PATH).value.toString().toLong()
                    newUpvoteMap.put(snap.key, timestamp)
                }
                onComplete(newUpvoteMap)
            }

            override fun onCancelled(error: DatabaseError) { /* NO-OP */
            }

        }
    }

    override fun listenForDownvote(userId: String) {
        userRef.child(userId).child(DOWNVOTE_PATH).addValueEventListener(downvoteEventListener)
    }

    override fun removeDownvoteListener(userId: String) {
        userRef.child(userId).child(DOWNVOTE_PATH).removeEventListener(downvoteEventListener)
    }

    override fun initializeDownvoteListener(
        onComplete: (Map<String?, Long>) -> Unit
    ) {
        downvoteEventListener = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                val newDownvoteMap = mutableMapOf<String?, Long>()
                for (snap in snapshot.children) {
                    val timestamp = snap.child(TIMESTAMP_PATH).value.toString().toLong()
                    newDownvoteMap.put(snap.key, timestamp)
                }
                onComplete(newDownvoteMap)
            }

            override fun onCancelled(error: DatabaseError) { /* NO-OP */
            }

        }
    }

    override fun logUserReportInRecord(
        plateKey: String,
        userId: String,
        reportPath: String
    ) {
        val currentUserRef = userRef.child(userId)
        val referenceToInsert =
            currentUserRef.child(reportPath).child(plateKey).child(TIMESTAMP_PATH)
        referenceToInsert.setValue(ServerValue.TIMESTAMP)
    }

    override fun listenForOwner(userId: String) {
        userRef.child(userId).child(OWNED_PATH).addValueEventListener(ownerEventListener)
    }

    override fun removeOwnerListener(userId: String) {
        userRef.child(userId).child(OWNED_PATH).removeEventListener(ownerEventListener)
    }

    override fun registerPlateOwner(plateKey: String, userId: String) {
        val ownedPlatesPath = userRef.child(userId).child(OWNED_PATH)
        ownedPlatesPath.child(plateKey).setValue("")
    }

    override fun initializeOwnerListener(onComplete: (List<String?>) -> Unit) {
        ownerEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // TODO: Return reputation
                val newPlateList = mutableListOf<String?>()
                for (snap in snapshot.children) {
                    newPlateList.add(snap.key)
                }
                onComplete(newPlateList)
            }

            override fun onCancelled(error: DatabaseError) { /* NO-OP */}
        }
    }

    /*
    * Plate functions
    * */
    override fun getPlateDetailsFromKeys(plateKeys: List<String?>, onFound: (List<Plate>) -> Unit) {
        plateRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val platesFound = mutableListOf<Plate>()
                for(snap in snapshot.children) {
                    plateKeys.forEach { key ->
                        if (snap.key == key) {
                            val snapPrefix = snap.child(PREFIX_PATH).value.toString()
                            val snapNumber = snap.child(NUMBER_PATH).value.toString()
                            val snapReputation =
                                snap.child(REPUTATION_PATH).value.toString().toDouble()
                            val snapPlate = Plate(snapPrefix, snapNumber, snapReputation)
                            platesFound.add(snapPlate)
                        }
                    }
                }
                onFound(platesFound)
            }

            override fun onCancelled(error: DatabaseError) { /* NO-OP */}
        })
    }

    override fun createNewPlate(prefix: String, number: String) {
        val newPlate = PlateRemote(prefix, number, 500.0)
        plateRef.push().setValue(newPlate)
    }

    override fun listenForPlate() {
        plateRef.addValueEventListener(plateEventListener)
    }

    override fun removePlateListener() {
        plateRef.removeEventListener(plateEventListener)
    }
    override fun initializePlateListener(
        prefix: String,
        number: String,
        onFound: (Plate, String?) -> Unit,
        onNotFound: () -> Unit
    ) {
        plateEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var found = false
                for (snap in snapshot.children) {

                    val snapPrefix = snap.child(PREFIX_PATH).value.toString()
                    val prefixExists = (snapPrefix == prefix)
                    val snapNumber = snap.child(NUMBER_PATH).value.toString()
                    val numberExists = (snapNumber == number)

                    if (prefixExists && numberExists) {
                        found = true
                        val plateKey = snap.key
                        val snapReputation = snap.child(REPUTATION_PATH).value
                        val updatedPlate = Plate(
                            snapPrefix,
                            snapNumber,
                            snapReputation.toString().toDouble()
                        )
                        onFound(updatedPlate, plateKey)
                    }
                }
                if (!found) {
                    onNotFound()
                }
            }

            override fun onCancelled(error: DatabaseError) { /* NO-OP */
            }
        }
    }

    override fun modifyPlateReputation(plateKey: String, rating: Rating, userId: String) {
        val reputationRef = plateRef.child(plateKey).child(REPUTATION_PATH)
        when (rating) {
            UPVOTE -> reputationRef.setValue(ServerValue.increment(1))
            DOWNVOTE -> reputationRef.setValue(ServerValue.increment(-1))
        }
    }
}
