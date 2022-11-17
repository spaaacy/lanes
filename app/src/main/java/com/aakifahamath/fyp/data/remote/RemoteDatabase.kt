package com.aakifahamath.fyp.data.remote

import com.aakifahamath.fyp.data.remote.ReputationRating.THUMBS_DOWN
import com.aakifahamath.fyp.data.remote.ReputationRating.THUMBS_UP
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

// URL for firebase realtime db
private const val BASE_URL =
    "https://driverrep-9cb39-default-rtdb.asia-southeast1.firebasedatabase.app"
const val TAG = "realtime_db"

class RemoteDatabase {

    private val firebaseDb: FirebaseDatabase = Firebase.database(BASE_URL)
    private val plateRef = firebaseDb.getReference("plates")
    private lateinit var valueEventListener: ValueEventListener
    private var plateKey: String? = null

    // Insert new plate with auto-generated key
    fun insertPlate(prefix: String, number: Int) {
        val newPlate = PlateRemote(prefix, number, 500.0)
        plateRef.push().setValue(newPlate)
    }


    // Get plate item from firebase
    fun startListeningForPlate(prefix: String, number: Int, onValueChange: (PlateRemote) -> Unit) {

        valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (snap in snapshot.children) {

                    val snapPrefix = snap.child("prefix").value
                    val snapNumber = snap.child("number").value

                    // Search realtime for prefix & number match
                    if (snapPrefix == prefix && snapNumber.toString() == number.toString()) {
                        plateKey = snap.key
                        val snapReputation = snap.child("reputation").value
                        val updatedPlate = PlateRemote(
                            snapPrefix.toString(),
                            snapNumber.toString().toInt(),
                            snapReputation.toString().toDouble()
                        )
                        onValueChange(updatedPlate)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) { /* NO-OP */
            }
        }

        plateRef.addValueEventListener(valueEventListener)
    }

    // Detach listener from plate reference
    fun stopListeningForPlate() {
        plateRef.removeEventListener(valueEventListener)
    }

    // Increase/Decrease driver reputation
    fun modifyPlateReputation(rating: ReputationRating) {
        plateKey?.also { key ->
            val reputationRef = plateRef.child(key).child("reputation")
            reputationRef.get().addOnSuccessListener {
                val oldReputation = it.value.toString().toInt()
                when (rating) {
                    THUMBS_UP -> reputationRef.setValue(oldReputation + 1)
                    THUMBS_DOWN -> reputationRef.setValue(oldReputation - 1)
                }
            }
        }
    }

}