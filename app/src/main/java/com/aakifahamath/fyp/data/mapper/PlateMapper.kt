package com.aakifahamath.fyp.data.mapper

import com.aakifahamath.fyp.data.remote.PlateRemote
import com.aakifahamath.fyp.domain.model.Plate

fun PlateRemote.toPlate(): Plate {
    return Plate(
        prefix = prefix ?: "",
        number = number ?: 0,
        reputation = reputation ?: 0.0
    )
}