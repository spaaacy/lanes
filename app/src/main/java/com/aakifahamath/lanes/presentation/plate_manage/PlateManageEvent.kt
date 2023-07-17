package com.aakifahamath.lanes.presentation.plate_manage

import com.ramcosta.composedestinations.navigation.DestinationsNavigator

sealed class PlateManageEvent {
    object SelectedUpvoted: PlateManageEvent()
    object SelectedDownvoted: PlateManageEvent()
    object SelectedOwned: PlateManageEvent()
    data class ClickPlate(val prefix: String, val number: String, val navigator: DestinationsNavigator): PlateManageEvent()
}