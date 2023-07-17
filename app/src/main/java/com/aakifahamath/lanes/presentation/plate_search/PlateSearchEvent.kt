package com.aakifahamath.lanes.presentation.plate_search

import com.ramcosta.composedestinations.navigation.DestinationsNavigator

sealed class PlateSearchEvent {
    data class ChangePrefixTextField(val prefix: String): PlateSearchEvent()
    data class ChangeNumberTextField(val number: String): PlateSearchEvent()
    data class ClickSearchButton(val navigator: DestinationsNavigator): PlateSearchEvent()
    data class ClickSignOut(val navigator: DestinationsNavigator): PlateSearchEvent()
    data class ClickRecentPlate(val prefix: String, val number: String, val navigator: DestinationsNavigator): PlateSearchEvent()
}
