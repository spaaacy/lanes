package com.aakifahamath.lanes.presentation.plate_detail

data class PlateDetailState (
    val isUserAnonymous: Boolean = true,
    val isOwnedPlate: Boolean = true,
    val isReportAllowed: Boolean = false,
)