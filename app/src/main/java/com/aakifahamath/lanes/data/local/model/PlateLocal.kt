package com.aakifahamath.lanes.data.local.model

import androidx.room.Entity

@Entity(primaryKeys = ["userId", "prefix", "number"])
data class PlateLocal(
    val userId: String,
    val prefix: String,
    val number: String,
    val timestamp: Long = System.currentTimeMillis()
)
