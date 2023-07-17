package com.aakifahamath.lanes.data.mapper

import com.aakifahamath.lanes.data.local.model.PlateLocal
import com.aakifahamath.lanes.data.remote.model.PlateRemote
import com.aakifahamath.lanes.domain.model.Plate

fun PlateLocal.toPlate(): Plate {
    return Plate(
        prefix = prefix,
        number = number,
        reputation = 0.0
    )
}


fun PlateRemote.toPlate(): Plate {
    return Plate(
        prefix = prefix ?: "",
        number = number ?: "",
        reputation = reputation ?: 0.0
    )
}