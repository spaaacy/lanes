package com.aakifahamath.lanes.presentation.plate_manage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aakifahamath.lanes.R
import com.aakifahamath.lanes.presentation.components.LicensePlate
import com.aakifahamath.lanes.presentation.theme.md_theme_light_onSecondaryContainer
import com.aakifahamath.lanes.presentation.theme.md_theme_light_secondaryContainer
import com.aakifahamath.lanes.util.*
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Destination
fun PlateManageScreen(
    navigator: DestinationsNavigator,
    viewModel: PlateManageViewModel = hiltViewModel()
) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.plate_manage)) },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = md_theme_light_secondaryContainer,
                    titleContentColor = md_theme_light_onSecondaryContainer
                )
            )
        }
    ) { scaffoldPadding ->
        if (!viewModel.isUserAnonymous) {
            Box(Modifier.padding(scaffoldPadding)) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(DEFAULT_PADDING),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        ElevatedButton(onClick = {
                            viewModel.onEvent(PlateManageEvent.SelectedOwned)
                        }) {
                            Text(
                                stringResource(R.string.owned_plates),
                                textAlign = TextAlign.Center
                            )
                        }
                        ElevatedButton(onClick = {
                            viewModel.onEvent(PlateManageEvent.SelectedUpvoted)
                        }) {
                            Text(
                                stringResource(R.string.upvoted_plates),
                                textAlign = TextAlign.Center
                            )
                        }
                        ElevatedButton(onClick = {
                            viewModel.onEvent(PlateManageEvent.SelectedDownvoted)
                        }) {
                            Text(
                                stringResource(R.string.downvoted_plates),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    when (viewModel.selectedOption.value) {
                        PlateManageViewModel.MenuOptions.OWNED -> {
                            if (viewModel.ownedPlates.isNotEmpty()) {
                                LazyColumn(
                                    Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    itemsIndexed(viewModel.ownedPlates) { i, plate ->

                                        Column(Modifier.clickable(
                                            onClick = {
                                                viewModel.onEvent(
                                                    PlateManageEvent.ClickPlate(
                                                        plate.prefix, plate.number, navigator
                                                    )
                                                )
                                            }
                                        )) {

                                            Spacer(Modifier.height(8.dp))

                                            Row(
                                                Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                LicensePlate(
                                                    plate = plate,
                                                    fontSize = PLATE_FONT_MEDIUM,
                                                    horizontalPadding = PLATE_VERTICAL_MEDIUM,
                                                    verticalPadding = PLATE_HORIZONTAL_MEDIUM
                                                )

                                                Text(plate.reputation.toInt().toString(),
                                                    style = MaterialTheme.typography.headlineSmall,
                                                    color = Color.Gray
                                                )
                                            }

                                            Spacer(Modifier.height(8.dp))

                                        }
                                    }
                                }
                            } else {
                                Box(Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center) {
                                    Text(stringResource(R.string.no_owned_plates), color = Color.Gray)
                                }
                            }
                        }
                        PlateManageViewModel.MenuOptions.UPVOTE -> {
                            val upvotedPlates = viewModel.upvotedPlates
                            if(upvotedPlates.isNotEmpty()) {
                                LazyColumn(
                                    Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    itemsIndexed(upvotedPlates.toList()) { i, pair ->

                                        val plate = pair.first
                                        val timestamp = pair.second

                                        Column(Modifier.clickable(
                                            onClick = {
                                                viewModel.onEvent(
                                                    PlateManageEvent.ClickPlate(
                                                        plate.prefix, plate.number, navigator
                                                    )
                                                )
                                            }
                                        )) {
                                            Spacer(Modifier.height(8.dp))

                                            Row(
                                                Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                LicensePlate(
                                                    plate = plate,
                                                    fontSize = PLATE_FONT_SMALL,
                                                    horizontalPadding = PLATE_VERTICAL_SMALL,
                                                    verticalPadding = PLATE_HORIZONTAL_SMALL
                                                )
                                                Text(
                                                    Utility.getFormattedDateFromMs(timestamp),
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = Color.Gray
                                                )
                                            }

                                            Spacer(Modifier.height(8.dp))
                                            if (i < upvotedPlates.size - 1) {
                                                Divider(Modifier.fillMaxWidth())
                                            }
                                        }
                                    }
                                }
                            } else {
                                Box(Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center) {
                                    Text(stringResource(R.string.no_upvoted_plates), color = Color.Gray)
                                }
                            }
                        }
                        PlateManageViewModel.MenuOptions.DOWNVOTE -> {
                            val downvotedPlates = viewModel.downvotedPlates
                            if(downvotedPlates.isNotEmpty()) {
                                LazyColumn(
                                    Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    itemsIndexed(downvotedPlates.toList()) { i, pair ->

                                        val plate = pair.first
                                        val timestamp = pair.second

                                        Column(Modifier.clickable(
                                            onClick = {
                                                viewModel.onEvent(
                                                    PlateManageEvent.ClickPlate(
                                                        plate.prefix, plate.number, navigator
                                                    )
                                                )
                                            }
                                        )) {

                                            Spacer(Modifier.height(8.dp))

                                            Row(
                                                Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                LicensePlate(
                                                    plate = plate,
                                                    fontSize = PLATE_FONT_SMALL,
                                                    horizontalPadding = PLATE_VERTICAL_SMALL,
                                                    verticalPadding = PLATE_HORIZONTAL_SMALL
                                                )
                                                Text(
                                                    Utility.getFormattedDateFromMs(timestamp),
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = Color.Gray
                                                )
                                            }

                                            Spacer(Modifier.height(8.dp))
                                            if (i < downvotedPlates.size - 1) {
                                                Divider(Modifier.fillMaxWidth())
                                            }

                                        }

                                    }
                                }
                            } else {
                                Box(Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center) {
                                    Text(stringResource(R.string.no_downvoted_plates), color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(scaffoldPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    painterResource(id = R.drawable.ic_register),
                    contentDescription = stringResource(id = R.string.register_to_begin),
                    tint = Color.LightGray
                )
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.register_to_begin), color = Color.LightGray)
            }
        }
    }
}
