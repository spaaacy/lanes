package com.aakifahamath.lanes.data.remote

import androidx.compose.ui.test.junit4.createComposeRule
import com.aakifahamath.lanes.data.remote.model.PlateRemote
import com.aakifahamath.lanes.domain.util.Rating
import com.aakifahamath.lanes.util.*
import com.google.common.truth.Truth.assertThat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class FirebaseRealtimeTest {

    @Inject
    lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var firebaseRealtime: FirebaseRealtime

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createComposeRule()

    @Before
    fun setup() {
        hiltRule.inject()
        firebaseDatabase.reference.setValue(null)
        firebaseRealtime = FirebaseRealtime(firebaseDatabase)
    }

    // TODO: Randomize these:
    private val testPlatePrefix = "ABC"
    private val testPlateNumber = "1234"
    private var testPlateKey: String? = "test-plate-1"
    private val testUserId = "test-user-1"
    private val testPlateReputation = 111.1
    private val testPlate = PlateRemote(testPlatePrefix, testPlateNumber, testPlateReputation)

    // createNewUser()
    @Test
    fun shouldCreateNewUser_whenUserDoesNotExist() {

        firebaseRealtime.createNewUser(testUserId)
        assertThat(
            firebaseDatabase.getReference(USER_PATH).child(testUserId).key
        ).isEqualTo(testUserId)
    }

    // logUserReportInRecord()
    @Test
    fun shouldLogReport_whenUserReports() {
        firebaseRealtime.logUserReportInRecord(testPlateKey!!, testUserId, UPVOTE_PATH)
        firebaseRealtime.logUserReportInRecord(testPlateKey!!, testUserId, DOWNVOTE_PATH)

        firebaseDatabase.getReference(USER_PATH).child(testUserId).get().addOnSuccessListener {
            val currentTimestamp = System.currentTimeMillis()
            val upvoteTimestampString =
                it.child(UPVOTE_PATH).child(testPlateKey!!).child(TIMESTAMP_PATH).value
            val downvoteTimestampString =
                it.child(DOWNVOTE_PATH).child(testPlateKey!!).child(TIMESTAMP_PATH).value
            val upvoteTimestampLong = upvoteTimestampString.toString().toLong()
            val downvoteTimestampLong = downvoteTimestampString.toString().toLong()

            assertThat(upvoteTimestampLong - MONTH_IN_MS).isGreaterThan(0L)
            assertThat(upvoteTimestampLong - MONTH_IN_MS).isLessThan(currentTimestamp)
            assertThat(downvoteTimestampLong - MONTH_IN_MS).isGreaterThan(0L)
            assertThat(downvoteTimestampLong - MONTH_IN_MS).isLessThan(currentTimestamp)
        }
    }

    // registerPlateOwner()
    @Test
    fun shouldRegisterUserAsOwner_whenUserMakesRequest() {

        firebaseRealtime.registerPlateOwner(testPlateKey!!, testUserId)
        val ownedPlatesPath = firebaseDatabase.getReference(USER_PATH).child(testUserId).child(OWNED_PATH)
        val newOwnedPlate = ownedPlatesPath.child(testPlateKey!!).key
        assertThat(newOwnedPlate).isEqualTo(testPlateKey)

    }

    // getPlateDetailsFromKeys()
    @Test
    fun shouldRetrievePlates_whenPassedPlateKeys() {
        val plateKey1 = "test-plate-1"
        val plateKey2 = "test-plate-2"
        val plateKey3 = "test-plate-3"

        firebaseDatabase.getReference(PLATE_PATH).child(plateKey1).setValue(testPlate)
        firebaseDatabase.getReference(PLATE_PATH).child(plateKey2).setValue(testPlate)
        firebaseDatabase.getReference(PLATE_PATH).child(plateKey3).setValue(testPlate)

        val plateKeyList = mutableListOf<String>().apply {
            add(plateKey1)
            add(plateKey2)
            add(plateKey3)
        }

        firebaseRealtime.getPlateDetailsFromKeys(plateKeyList) { matchedPlates ->
            matchedPlates.forEach { plateFound ->
                assertThat(plateFound.prefix).isEqualTo(testPlate.prefix)
                assertThat(plateFound.number).isEqualTo(testPlate.number)
                assertThat(plateFound.reputation).isEqualTo(testPlate.reputation)
            }
        }
    }

    // createNewPlate()
    @Test
    fun shouldCreateNewPlate_whenPlateDoesNotExist() {

        firebaseRealtime.createNewPlate(testPlatePrefix, testPlateNumber)

        firebaseDatabase.getReference(PLATE_PATH)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var found = false
                    for (snap in snapshot.children) {
                        val snapPrefix = snap.child(PREFIX_PATH).value.toString()
                        val snapNumber = snap.child(NUMBER_PATH).value.toString()
                        if (snapPrefix == testPlatePrefix && snapNumber == testPlateNumber) {
                            found = true
                            assertThat(snapPrefix).isEqualTo(testPlatePrefix)
                            assertThat(snapNumber).isEqualTo(testPlateNumber)
                        }
                    }
                    assertThat(found).isTrue()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // modifyPlateReputation()
    @Test
    fun shouldIncrementReputation_whenUpvoted() {
        // Create test plate in database
        firebaseDatabase.getReference(PLATE_PATH).child(testPlateKey!!).setValue(testPlate)
        firebaseRealtime.modifyPlateReputation(testPlateKey!!, Rating.UPVOTE, testUserId)

        firebaseDatabase.getReference(PLATE_PATH).child(testPlateKey!!).get().addOnSuccessListener {
            val newReputation = it.child(REPUTATION_PATH).value.toString().toDouble()
            assertThat(newReputation).isEqualTo(testPlateReputation + 1)
        }
    }

    // modifyPlateReputation()
    @Test
    fun shouldDecrementReputation_whenDownvoted() {
        firebaseDatabase.getReference(PLATE_PATH).child(testPlateKey!!).setValue(testPlate)
        firebaseRealtime.modifyPlateReputation(testPlateKey!!, Rating.DOWNVOTE, testUserId)

        firebaseDatabase.getReference(PLATE_PATH).child(testPlateKey!!).get().addOnSuccessListener {
            val newReputation = it.child(REPUTATION_PATH).value.toString().toDouble()
            assertThat(newReputation).isEqualTo(testPlateReputation - 1)
        }
    }

    // modifyPlateReputation()
    @Test
    fun shouldHaveSameReputation_whenUpvotedAndDownvoted() {
        firebaseDatabase.getReference(PLATE_PATH).child(testPlateKey!!).setValue(testPlate)
        firebaseRealtime.modifyPlateReputation(testPlateKey!!, Rating.UPVOTE, testUserId)
        firebaseRealtime.modifyPlateReputation(testPlateKey!!, Rating.DOWNVOTE, testUserId)

        firebaseDatabase.getReference(PLATE_PATH).child(testPlateKey!!).get().addOnSuccessListener {
            val newReputation = it.child(REPUTATION_PATH).value.toString().toDouble()
            assertThat(newReputation).isEqualTo(testPlateReputation)
        }
    }
}