package com.aakifahamath.lanes.presentation.plate_detail

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aakifahamath.lanes.R
import com.aakifahamath.lanes.R.drawable
import com.aakifahamath.lanes.presentation.components.LicensePlate
import com.aakifahamath.lanes.presentation.util.UiEvent
import com.aakifahamath.lanes.util.*
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Destination(navArgsDelegate = PlateDetailNavArgs::class)
fun PlateDetailScreen(
    navigator: DestinationsNavigator
) {

    val viewModel: PlateDetailViewModel = hiltViewModel()
    val plate = viewModel.plate.collectAsState()
    val plateDetailState = viewModel.plateDetailState.collectAsState()

    // Tells compose what to do upon back press
    BackHandler {
        viewModel.onEvent(PlateDetailEvent.NavigateUp {
            navigator.navigateUp()
        })
    }

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is UiEvent.Snackbar -> {}
                is UiEvent.TextFieldAlert -> {}
            }
        }
    }

    if (viewModel.isLoading.value) {
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (!viewModel.isNetworkAvailable.value) {
        Scaffold(topBar = { PlateDetailTopBar {
            viewModel.onEvent(PlateDetailEvent.NavigateUp {
                navigator.navigateUp()
            })
        }
        }) { scaffoldPadding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(scaffoldPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(drawable.ic_wifi_off),
                    contentDescription = stringResource(id = R.string.connection_unavailable),
                    tint = Color.LightGray
                )
                Spacer(Modifier.height(8.dp))
                Text(stringResource(id = R.string.connection_unavailable), color = Color.LightGray)
            }
        }
    } else {
        Scaffold(topBar = { PlateDetailTopBar {
            viewModel.onEvent(PlateDetailEvent.NavigateUp {
                navigator.navigateUp()
            })
        }
        }) { scaffoldPadding ->
            Box(Modifier.padding(scaffoldPadding)){
                Column(
                    Modifier
                        .padding(DEFAULT_PADDING)
                        .fillMaxSize()
                        .animateContentSize(
                            animationSpec = tween(
                                durationMillis = 500
                            )
                        ),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // Child Component: 1
                    LicensePlate(Modifier.clickable{ } ,plate.value, PLATE_FONT_LARGE, PLATE_VERTICAL_LARGE, PLATE_HORIZONTAL_LARGE)

                    // Child Component: 2
                    Column(
                        Modifier.weight(3f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {

                        Column(
                            Modifier.weight(2f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Text(
                                text = plate.value.reputation.toInt().toString(),
                                fontSize = 80.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                Text(
                                    text = stringResource(id = R.string.out_of_1000),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onBackground,
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        Row(Modifier.weight(1f)) {
                            if (!plateDetailState.value.isOwnedPlate && !plateDetailState.value.isUserAnonymous) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(onClick = {
                                        viewModel.onEvent(PlateDetailEvent.ClickedYourCar)
                                    }) {
                                        Text(stringResource(id = R.string.your_car))
                                    }
                                }
                            } else if (plateDetailState.value.isUserAnonymous) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(stringResource(id = R.string.registered_to_own), color = Color.Gray)
                                }
                            }
                            AnimatedVisibility(
                                visible = plateDetailState.value.isOwnedPlate,
                                enter = slideInVertically { it } + fadeIn()) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painterResource(id = R.drawable.ic_check),
                                        contentDescription = stringResource(
                                            id = R.string.you_own_this_car
                                        ),
                                        modifier = Modifier.size(24.dp),
                                        tint = Color.LightGray
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(stringResource(id = R.string.you_own_this_car), color = Color.Gray)
                                }
                            }
                        }

                    }

                    // Child Component: 3
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {

                        Row(
                            Modifier.weight(2f),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            AnimatedVisibility(
                                visible = !plateDetailState.value.isReportAllowed || plateDetailState.value.isUserAnonymous,
                                enter = slideInVertically() + fadeIn()
                            ) {
                                Text(
                                    text = if (plateDetailState.value.isUserAnonymous) {
                                        stringResource(id = R.string.anonymous_unauthorized)
                                    } else {
                                        stringResource(id = R.string.already_reported)
                                    },
                                    textAlign = TextAlign.Center,
                                    color = Color.Gray
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Given weight so to be taller
                        Row(Modifier.weight(1f)) {
                            Button(modifier = Modifier
                                .weight(1f)
                                .fillMaxSize(),
                                enabled = if (!plateDetailState.value.isUserAnonymous) {
                                    plateDetailState.value.isReportAllowed
                                } else false,
                                onClick = { viewModel.onEvent(PlateDetailEvent.ClickedThumbsUp) }) {
                                // TODO: Change icon to arrow
                                Icon(
                                    painterResource(drawable.ic_thumb_up),
                                    stringResource(id = R.string.upvote),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(text = stringResource(id = R.string.upvote))
                            }

                            Spacer(Modifier.width(8.dp))

                            Button(modifier = Modifier
                                .weight(1f)
                                .fillMaxSize(),
                                enabled = if (!plateDetailState.value.isUserAnonymous) {
                                    plateDetailState.value.isReportAllowed
                                } else false,
                                onClick = { viewModel.onEvent(PlateDetailEvent.ClickedThumbsDown) }) {
                                // TODO: Change icon to arrow
                                Icon(
                                    painterResource(drawable.ic_thumb_down),
                                    stringResource(id = R.string.downvote),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(text = stringResource(id = R.string.downvote))
                            }
                        }
                    }
                }
            }
        }
    }
}