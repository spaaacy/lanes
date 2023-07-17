package com.aakifahamath.lanes.data.repository

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import com.aakifahamath.lanes.data.mapper.toPlate
import com.aakifahamath.lanes.data.remote.FirebaseRealtime
import com.aakifahamath.lanes.data.remote.model.PlateRemote
import com.aakifahamath.lanes.domain.model.Plate
import com.aakifahamath.lanes.domain.util.Rating
import com.aakifahamath.lanes.util.*
import com.google.common.truth.Truth.assertThat
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class RepositoryImplTest {

    @Inject
    @ApplicationContext
    lateinit var context: Context
    @Inject
    lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var firebaseRealtime: FirebaseRealtime
    private lateinit var repositoryImpl: RepositoryImpl

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    // Necessary for firebase to work
    @get:Rule(order = 1)
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        hiltRule.inject()
        firebaseDatabase.reference.setValue(null)
        firebaseRealtime = FirebaseRealtime(firebaseDatabase)
        repositoryImpl = RepositoryImpl(context, firebaseRealtime)
        repositoryImpl.clearOwnedPlates()
    }

    private val testPlatePrefix = "ABC"
    private val testPlateNumber = "1234"
    private var testPlateKey: String? = "test-plate-1"
    private val testUserId = "test-user-1"
    private val testPlateReputation = 111.1
    private val testTimestamp = 16000000L
    private val testPlate = PlateRemote(testPlatePrefix, testPlateNumber, testPlateReputation)

    // isPlateOwned()
    @Test
    fun shouldReturnTrue_whenPlateIsInOwnedList() {
        repositoryImpl.addToOwnedPlates(testPlate.toPlate())
        val searchResult = repositoryImpl.isPlateOwned(testPlate.toPlate())
        assertThat(searchResult).isTrue()
    }

    // isPlateOwned()
    @Test
    fun shouldReturnFalse_whenPlateIsNotInOwnedList() {
        val searchResult = repositoryImpl.isPlateOwned(testPlate.toPlate())
        assertThat(searchResult).isFalse()
    }

    // getReportTimestamp()
    @Test
    fun shouldReturnTimestamp_whenPlateIsInUpvoteMap() {
        val upvotedPlates = repositoryImpl.getUpvotedPlates()
        upvotedPlates[testPlate.toPlate()] = testTimestamp
        val searchResult = repositoryImpl.getLatestReportTimestamp(testPlate.toPlate())
        assertThat(searchResult).isEqualTo(testTimestamp)
    }

    // getReportTimestamp()
    @Test
    fun shouldReturnTimestamp_whenPlateIsInDownvoteMap() {
        val downvotedPlates = repositoryImpl.getDownvotedPlates()
        downvotedPlates[testPlate.toPlate()] = testTimestamp
        val searchResult = repositoryImpl.getLatestReportTimestamp(testPlate.toPlate())
        assertThat(searchResult).isEqualTo(testTimestamp)
    }

    // getReportTimestamp
    @Test
    fun shouldNotReturnTimestamp_whenPlateIsNotInUpvoteOrDownvoteMap() {
        val searchResult = repositoryImpl.getLatestReportTimestamp(testPlate.toPlate())
        assertThat(searchResult).isEqualTo(0L)
    }

    // getReportTimestamp()
    @Test
    fun shouldReturnBiggestTimestamp_whenPlateIsInUpvoteAndDownvoteMap() {
        val upvotedPlates = repositoryImpl.getUpvotedPlates()
        val downvotedPlates = repositoryImpl.getDownvotedPlates()
        val largerTimestamp = testTimestamp + 10000L
        upvotedPlates[testPlate.toPlate()] = largerTimestamp
        downvotedPlates[testPlate.toPlate()] = testTimestamp
        val searchResult = repositoryImpl.getLatestReportTimestamp(testPlate.toPlate())
        assertThat(searchResult).isEqualTo(largerTimestamp)
    }

    // TODO: listenForUserReports -> onUpvote & onDownvote callback change

    // listenForUserReports()
    @Test
    fun shouldFindUserReport_whenOnlyUpvoteExists() {
        // Ensures values currentPlateKey & currentUserId are not null
        repositoryImpl.loginExistingUser(testUserId)

        // Create entry for user reports
        val userDirectory = firebaseDatabase.getReference(USER_PATH).child(testUserId)
        userDirectory.child(UPVOTE_PATH).child(testPlateKey!!).child(TIMESTAMP_PATH).setValue(testTimestamp)
        firebaseDatabase.getReference(PLATE_PATH).child(testPlateKey!!).setValue(testPlate)

        repositoryImpl.listenForUserReports(
            onUpvote = { upvotedPlates ->
                val timestampInMap = upvotedPlates[testPlate.toPlate()]
                assertThat(timestampInMap).isEqualTo(testTimestamp)
                upvotedPlates.keys.forEach { plate ->
                    assertThat(plate.prefix).isEqualTo(testPlate.prefix)
                    assertThat(plate.number).isEqualTo(testPlate.number)
                    assertThat(plate.reputation).isEqualTo(testPlate.reputation)
                }


                repositoryImpl.removeUserListeners()
            },
            onDownvote = {}
        )
    }

    // listenForUserReports()
    @Test
    fun shouldFindUserReport_whenOnlyDownvoteExists() {
        // Ensures values currentPlateKey & currentUserId are not null
        repositoryImpl.loginExistingUser(testUserId)

        // Create entry for user reports
        val userDirectory = firebaseDatabase.getReference(USER_PATH).child(testUserId)
        userDirectory.child(DOWNVOTE_PATH).child(testPlateKey!!).child(TIMESTAMP_PATH).setValue(testTimestamp)
        firebaseDatabase.getReference(PLATE_PATH).child(testPlateKey!!).setValue(testPlate)

        repositoryImpl.listenForUserReports(
            onUpvote = {},
            onDownvote = { downvotedPlates ->
                val timestampInMap = downvotedPlates[testPlate.toPlate()]
                assertThat(timestampInMap).isEqualTo(testTimestamp)
                downvotedPlates.keys.forEach { plate ->
                    assertThat(plate.prefix).isEqualTo(testPlate.prefix)
                    assertThat(plate.number).isEqualTo(testPlate.number)
                    assertThat(plate.reputation).isEqualTo(testPlate.reputation)
                }


                repositoryImpl.removeUserListeners()
            }
        )
    }

    // listenForUserReports()
    @Test
    fun shouldFindUserReport_whenUpvoteAndDownvoteExists() {
        // Ensures values currentPlateKey & currentUserId are not null
        repositoryImpl.loginExistingUser(testUserId)

        // Create entry for user reports
        val userDirectory = firebaseDatabase.getReference(USER_PATH).child(testUserId)
        userDirectory.child(UPVOTE_PATH).child(testPlateKey!!).child(TIMESTAMP_PATH).setValue(testTimestamp)
        userDirectory.child(DOWNVOTE_PATH).child(testPlateKey!!).child(TIMESTAMP_PATH).setValue(testTimestamp)
        firebaseDatabase.getReference(PLATE_PATH).child(testPlateKey!!).setValue(testPlate)

        repositoryImpl.listenForUserReports(
            onUpvote = { upvotedPlates ->
                val timestampInMap = upvotedPlates[testPlate.toPlate()]
                assertThat(timestampInMap).isEqualTo(testTimestamp)
                upvotedPlates.keys.forEach { plate ->
                    assertThat(plate.prefix).isEqualTo(testPlate.prefix)
                    assertThat(plate.number).isEqualTo(testPlate.number)
                    assertThat(plate.reputation).isEqualTo(testPlate.reputation)
                }

                firebaseRealtime.removeUpvoteListener(testUserId)
            },
            onDownvote = { downvotedPlates ->
                val timestampInMap = downvotedPlates[testPlate.toPlate()]
                assertThat(timestampInMap).isEqualTo(testTimestamp)
                downvotedPlates.keys.forEach { plate ->
                    assertThat(plate.prefix).isEqualTo(testPlate.prefix)
                    assertThat(plate.number).isEqualTo(testPlate.number)
                    assertThat(plate.reputation).isEqualTo(testPlate.reputation)
                }

                firebaseRealtime.removeDownvoteListener(testUserId)
            }
        )
    }

    // TODO: listenForOwnedPlates onFound to onComplete callback change

    // listenForOwnedPlates()
    @Test
    fun shouldReturnOwnedPlates_whenUserLogsIn() {
        val ownedPath = firebaseDatabase.getReference(USER_PATH).child(testUserId).child(OWNED_PATH)
        ownedPath.child(testPlateKey!!).setValue("")
        val platePath = firebaseDatabase.getReference(PLATE_PATH).child(testPlateKey!!)
        platePath.setValue(testPlate)

        repositoryImpl.loginExistingUser(testUserId)
        repositoryImpl.listenForOwnedPlates(onComplete = { plates ->
            assertThat(plates.first().prefix).isEqualTo(testPlate.prefix)
            assertThat(plates.first().number).isEqualTo(testPlate.number)
            assertThat(plates.first().reputation).isEqualTo(testPlate.reputation)
            firebaseRealtime.removeOwnerListener(testUserId)
        })
    }

    // listenForAndCreateNewPlate()
    @Test
    fun shouldCreatePlate_whenPlateDoesNotExist() {
        runBlocking {
            repositoryImpl.listenForAndCreateNewPlate(testPlatePrefix, testPlateNumber)
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            val newPlate = result.data
                            newPlate?.let {
                                assertThat(newPlate.prefix).isEqualTo(testPlatePrefix)
                                assertThat(newPlate.number).isEqualTo(testPlateNumber)
                            }
                            repositoryImpl.plateListenerFlow?.close()
                            firebaseRealtime.removePlateListener()
                        }
                        else -> {}
                    }
                }
        }
    }

    // listenForAndCreateNewPlate()
    @Test
    fun shouldFindPlate_whenPlateExists() {
        runBlocking {
            firebaseDatabase.getReference(PLATE_PATH).child(testPlateKey!!).setValue(testPlate)

            repositoryImpl.listenForAndCreateNewPlate(testPlatePrefix, testPlateNumber).collect { result ->
                when(result) {
                    is Resource.Success -> {
                        val plate = result.data
                        plate?.let {
                            assertThat(plate.prefix).isEqualTo(testPlatePrefix)
                            assertThat(plate.number).isEqualTo(testPlateNumber)
                            // This last assertion will tell us if listenForAndInitializePlate() created a new plate or not
                            assertThat(plate.reputation).isEqualTo(testPlateReputation)
                        }
                        repositoryImpl.plateListenerFlow?.close()
                        firebaseRealtime.removePlateListener()
                    }
                    else -> {}
                }
            }
        }
    }

    // removePlateListener()
    @Test
    fun shouldRemovePlateListener_whenPlateListenerActive() {

        var fetchedPlate: Plate? = null

        firebaseRealtime.initializePlateListener(
            testPlatePrefix,
            testPlateNumber,
            onFound = { plateRemote, _ ->
                fetchedPlate = plateRemote
                assertThat(fetchedPlate).isNull() // This assertions is to fail the test
            },
            onNotFound = {})

        firebaseRealtime.listenForPlate()
        repositoryImpl.setPlateListener(true)
        repositoryImpl.removePlateListener()
        firebaseDatabase.getReference(PLATE_PATH).child(testPlateKey!!).setValue(testPlate)

    }

    // modifyReputationAndLogUserReport()
    @Test
    fun shouldCreateLogUserReportAndIncrementReputation_whenUpvoted() {
        firebaseDatabase.getReference(PLATE_PATH).child(testPlateKey!!).setValue(testPlate)
        repositoryImpl.loginExistingUser(testUserId)
        repositoryImpl.setPlateKey(testPlateKey)
        repositoryImpl.modifyReputationAndLogUserReport(Rating.UPVOTE)

        firebaseDatabase.reference.get().addOnSuccessListener {
            // Assert new reputation in remote is correct
            val reputationDirectory = it.child(PLATE_PATH).child(testPlateKey!!).child(REPUTATION_PATH)
            val reputation = reputationDirectory.value.toString().toDouble()
            assertThat(reputation).isEqualTo(testPlateReputation + 1)

            // Assert user has been logged for his report
            val currentTimestamp = System.currentTimeMillis()
            val userUpvoteDirectory = it.child(USER_PATH).child(testUserId).child(UPVOTE_PATH)
            val userTimestampDirectory= userUpvoteDirectory.child(testPlateKey!!).child(TIMESTAMP_PATH)
            val upvoteTimestamp = userTimestampDirectory.value.toString().toLong()
            assertThat(upvoteTimestamp - MONTH_IN_MS).isGreaterThan(0L)
            assertThat(upvoteTimestamp - MONTH_IN_MS).isLessThan(currentTimestamp)
        }
    }

    // modifyReputationAndLogUserReport()
    @Test
    fun shouldCreateLogUserReportAndDecrementReputation_whenDownvoted() {
        firebaseDatabase.getReference(PLATE_PATH).child(testPlateKey!!).setValue(testPlate)
        repositoryImpl.loginExistingUser(testUserId)
        repositoryImpl.setPlateKey(testPlateKey)
        repositoryImpl.modifyReputationAndLogUserReport(Rating.DOWNVOTE)

        firebaseDatabase.reference.get().addOnSuccessListener {
            // Assert new reputation in remote is correct
            val reputationDirectory = it.child(PLATE_PATH).child(testPlateKey!!).child(REPUTATION_PATH)
            val reputation = reputationDirectory.value.toString().toDouble()
            assertThat(reputation).isEqualTo(testPlateReputation - 1)

            // Assert user has been logged for his report
            val currentTimestamp = System.currentTimeMillis()
            val userDownvoteDirectory = it.child(USER_PATH).child(testUserId).child(DOWNVOTE_PATH)
            val userTimestampDirectory= userDownvoteDirectory.child(testPlateKey!!).child(TIMESTAMP_PATH)
            val downvoteTimestamp = userTimestampDirectory.value.toString().toLong()
            assertThat(downvoteTimestamp - MONTH_IN_MS).isGreaterThan(0L)
            assertThat(downvoteTimestamp - MONTH_IN_MS).isLessThan(currentTimestamp)
        }
    }

    // modifyReputationAndLogUserReport()
    @Test
    fun shouldCreateLogUserReportAndMaintainTheSameReputation_whenUpvotedAndDownvoted() {
        firebaseDatabase.getReference(PLATE_PATH).child(testPlateKey!!).setValue(testPlate)
        repositoryImpl.loginExistingUser(testUserId)
        repositoryImpl.setPlateKey(testPlateKey)
        repositoryImpl.modifyReputationAndLogUserReport(Rating.UPVOTE)
        repositoryImpl.modifyReputationAndLogUserReport(Rating.DOWNVOTE)

        firebaseDatabase.reference.get().addOnSuccessListener {
            // Assert new reputation in remote is correct
            val reputationDirectory = it.child(PLATE_PATH).child(testPlateKey!!).child(REPUTATION_PATH)
            val reputation = reputationDirectory.value.toString().toDouble()
            assertThat(reputation).isEqualTo(testPlateReputation)

            val userDirectory = it.child(USER_PATH).child(testUserId)
            val upvoteTimestampDirectory= userDirectory.child(UPVOTE_PATH).child(testPlateKey!!).child(TIMESTAMP_PATH)
            val upvoteTimestamp = upvoteTimestampDirectory.value.toString().toLong()
            val downvoteTimestampDirectory= userDirectory.child(DOWNVOTE_PATH).child(testPlateKey!!).child(TIMESTAMP_PATH)
            val downvoteTimestamp = downvoteTimestampDirectory.value.toString().toLong()

            val currentTimestamp = System.currentTimeMillis()
            assertThat(upvoteTimestamp - MONTH_IN_MS).isGreaterThan(0L)
            assertThat(upvoteTimestamp - MONTH_IN_MS).isLessThan(currentTimestamp)
            assertThat(downvoteTimestamp - MONTH_IN_MS).isGreaterThan(0L)
            assertThat(downvoteTimestamp - MONTH_IN_MS).isLessThan(currentTimestamp)
        }
    }

    // removeUserListeners()
    @Test
    fun shouldRemoveAllUserListeners_whenListenersAreActive() {
        // These must remain null to identify if listener was removed
        var fetchedUpvotes: Map<String?, Long>? = null
        var fetchedDownvotes: Map<String?, Long>? = null
        var fetchedOwnedPlates: List<String?>? = null

        firebaseRealtime.initializeUpvoteListener(onComplete = {
            fetchedUpvotes = it
            assertThat(fetchedUpvotes).isNull() // This assertions is to fail the test
        })

        firebaseRealtime.initializeDownvoteListener {
            fetchedDownvotes = it
            assertThat(fetchedDownvotes).isNull() // This assertions is to fail the test
        }

        firebaseRealtime.initializeOwnerListener {
            fetchedOwnedPlates = it
            assertThat(fetchedOwnedPlates).isNull() // This assertions is to fail the test
        }

        firebaseRealtime.listenForUpvote(testUserId)
        firebaseRealtime.listenForDownvote(testUserId)
        firebaseRealtime.listenForOwner(testUserId)

        // Sets preconditions for calling removeAllListeners()
        repositoryImpl.setUserReportListener(Rating.UPVOTE, true)
        repositoryImpl.setUserReportListener(Rating.DOWNVOTE, true)
        repositoryImpl.setOwnerListener(true)
        repositoryImpl.loginExistingUser(testUserId)

        repositoryImpl.removeUserListeners()

        // Make changes to check if listeners pick up on them
        val userDirectory = firebaseDatabase.getReference(USER_PATH).child(testUserId)
        userDirectory.child(UPVOTE_PATH).child(testPlateKey!!).child(TIMESTAMP_PATH).setValue(0L)
        userDirectory.child(DOWNVOTE_PATH).child(testPlateKey!!).child(TIMESTAMP_PATH).setValue(0L)
        userDirectory.child(OWNED_PATH).child(testPlateKey!!).setValue("")

    }
}
