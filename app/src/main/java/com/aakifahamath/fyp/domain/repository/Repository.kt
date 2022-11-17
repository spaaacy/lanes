package com.aakifahamath.fyp.domain.repository

import com.aakifahamath.fyp.data.remote.PlateRemote
import com.aakifahamath.fyp.data.remote.ReputationRating

interface Repository {

    fun insertPlateRemote(prefix: String, number: Int)
    fun startListeningForPlateRemote(prefix: String, number: Int, onValueChange: (PlateRemote) -> Unit)
    fun stopListeningForPlateRemote()
    fun modifyPlateReputationRemote(rating: ReputationRating)

}