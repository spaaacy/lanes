package com.aakifahamath.lanes.presentation.bottom_bar

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.aakifahamath.lanes.R
import com.aakifahamath.lanes.presentation.destinations.PlateManageScreenDestination
import com.aakifahamath.lanes.presentation.destinations.PlateSearchScreenDestination
import com.ramcosta.composedestinations.spec.DirectionDestinationSpec

enum class BottomBarDestination(
    val direction: DirectionDestinationSpec,
    @DrawableRes val icon: Int,
    @StringRes val label: Int
) {
    PlateSearch(PlateSearchScreenDestination, R.drawable.ic_search, R.string.search_screen),
    PlateManage(PlateManageScreenDestination, R.drawable.ic_menu, R.string.manage_plates)
}