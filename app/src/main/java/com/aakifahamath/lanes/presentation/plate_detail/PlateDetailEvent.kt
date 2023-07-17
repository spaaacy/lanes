package com.aakifahamath.lanes.presentation.plate_detail

sealed class PlateDetailEvent {
    object ClickedThumbsUp: PlateDetailEvent()
    object ClickedThumbsDown: PlateDetailEvent()
    data class NavigateUp(val navigateUp: () -> Unit): PlateDetailEvent()
    object ClickedYourCar: PlateDetailEvent()
}
