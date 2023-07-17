package com.aakifahamath.lanes.presentation.bottom_bar

import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.aakifahamath.lanes.presentation.NavGraphs
import com.aakifahamath.lanes.presentation.appCurrentDestinationAsState
import com.aakifahamath.lanes.presentation.destinations.Destination
import com.aakifahamath.lanes.presentation.startAppDestination
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.navigation.popUpTo

@Composable
fun BottomBar(navController: NavController) {

    val currentDestination: Destination = navController.appCurrentDestinationAsState().value
        ?: NavGraphs.root.startAppDestination

    var isCurrentDestinationInBottomBarNav = false
    BottomBarDestination.values().forEach { destination ->
        if (destination.direction == currentDestination) {
            isCurrentDestinationInBottomBarNav = true
        }
    }

    if(isCurrentDestinationInBottomBarNav) {
        BottomNavigation(
            backgroundColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary
        ) {
            BottomBarDestination.values().forEach { destination ->
                BottomNavigationItem(
                    selected = currentDestination == destination.direction,
                    onClick = {
                        navController.navigate(destination.direction) {
                            popUpTo(currentDestination) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    },
                    icon = {
                        Icon(
                            painterResource(destination.icon),
                            contentDescription = stringResource(destination.label)
                        )
                    }
                )

            }
        }
    }
}