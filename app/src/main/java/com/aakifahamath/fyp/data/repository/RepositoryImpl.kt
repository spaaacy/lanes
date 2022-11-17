package com.aakifahamath.fyp.data.repository

import com.aakifahamath.fyp.data.remote.PlateRemote
import com.aakifahamath.fyp.data.remote.RemoteDatabase
import com.aakifahamath.fyp.data.remote.ReputationRating
import com.aakifahamath.fyp.domain.repository.Repository

class RepositoryImpl (
    private val remote: RemoteDatabase
): Repository {

    override fun insertPlateRemote(prefix: String, number: Int) {
        remote.insertPlate(prefix, number)
    }

    override fun startListeningForPlateRemote(
        prefix: String, number: Int, onValueChange: (PlateRemote) -> Unit
    ) {
        remote.startListeningForPlate(prefix, number, onValueChange)
    }

    override fun stopListeningForPlateRemote() {
        remote.stopListeningForPlate()
    }

    override fun modifyPlateReputationRemote(rating: ReputationRating) {
        remote.modifyPlateReputation(rating)
    }
}