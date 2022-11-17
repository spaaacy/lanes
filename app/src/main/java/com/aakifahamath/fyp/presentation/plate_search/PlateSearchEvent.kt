package com.aakifahamath.fyp.presentation.plate_search

import com.ramcosta.composedestinations.navigation.DestinationsNavigator

sealed class PlateSearchEvent {
    data class ChangePrefixTextField(val prefix: String): PlateSearchEvent()
    data class ChangeNumberTextField(val number: String): PlateSearchEvent()
    data class ClickNextButton(val navigator: DestinationsNavigator): PlateSearchEvent()
}
