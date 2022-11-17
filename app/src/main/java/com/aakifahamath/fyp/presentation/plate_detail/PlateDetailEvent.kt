package com.aakifahamath.fyp.presentation.plate_detail

sealed class PlateDetailEvent {
    object ClickedThumbsUp: PlateDetailEvent()
    object ClickedThumbsDown: PlateDetailEvent()
}
