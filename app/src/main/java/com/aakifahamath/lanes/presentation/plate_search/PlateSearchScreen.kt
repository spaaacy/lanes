package com.aakifahamath.lanes.presentation.plate_search

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aakifahamath.lanes.R
import com.aakifahamath.lanes.data.mapper.toPlate
import com.aakifahamath.lanes.presentation.components.LicensePlate
import com.aakifahamath.lanes.presentation.theme.md_theme_light_onSecondaryContainer
import com.aakifahamath.lanes.presentation.theme.md_theme_light_secondaryContainer
import com.aakifahamath.lanes.presentation.util.AlertMessage
import com.aakifahamath.lanes.presentation.util.UiEvent
import com.aakifahamath.lanes.util.*
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.flow.collectLatest

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Destination
fun PlateSearchScreen(
    navigator: DestinationsNavigator,
    viewModel: PlateSearchViewModel = hiltViewModel()
) {

    val snackbarHostState = remember { SnackbarHostState() }
    var prefixError by remember { mutableStateOf(false) }
    var numberError by remember { mutableStateOf(false) }
    var prefixAlert by remember { mutableStateOf("") }
    var numberAlert by remember { mutableStateOf("") }

    val context = LocalContext.current

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is UiEvent.Snackbar -> {}
                is UiEvent.TextFieldAlert -> {
                    when (event.message) {
                        AlertMessage.PREFIX, AlertMessage.PREFIX_LETTER -> {
                            prefixError = true
                            if (event.message == AlertMessage.PREFIX) {
                                prefixAlert = context.resources.getString(R.string.warn_prefix)
                            } else {
                                prefixAlert = context.resources.getString(R.string.warn_letter)
                            }
                        }
                        AlertMessage.NUMBER -> {
                            numberError = true
                            numberAlert = context.resources.getString(R.string.warn_number)
                        }
                        else -> {}
                    }
                }
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
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(id = R.string.plate_search)) },
                    colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor = md_theme_light_secondaryContainer,
                        titleContentColor = md_theme_light_onSecondaryContainer
                    )
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }) { scaffoldPadding ->

            Box(Modifier.padding(scaffoldPadding)) {
                Column(Modifier.padding(DEFAULT_PADDING),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally) {

                    // Child Component: 1
                    Column {
                        val prefixState = viewModel.prefixState.value
                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(PREFIX_TEXT_FIELD),
                            isError = prefixError,
                            supportingText = if (prefixError) {
                                {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            painterResource(id = R.drawable.ic_error),
                                            contentDescription = prefixAlert,
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(text = prefixAlert)
                                    }
                                }
                            } else null,
                            singleLine = true,
                            value = prefixState,
                            label = { Text(stringResource(R.string.prefix_label)) },
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Characters,
                                imeAction = ImeAction.Next),
                            onValueChange = {
                                if (prefixError) {
                                    prefixError = false
                                }
                                viewModel.onEvent(PlateSearchEvent.ChangePrefixTextField(it))
                            })

                        Spacer(Modifier.height(8.dp))

                        val numberState = viewModel.numberState.value
                        OutlinedTextField(
                            isError = numberError,
                            supportingText = if (numberError) {
                                {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            painterResource(id = R.drawable.ic_error),
                                            contentDescription = numberAlert,
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(text = numberAlert)
                                    }
                                }
                            } else null,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            value = numberState,
                            label = { Text(stringResource(R.string.number_label)) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    viewModel.onEvent(PlateSearchEvent.ClickSearchButton(navigator))
                                }
                            ),
                            onValueChange = {
                                if (numberError) {
                                    numberError = false
                                }
                                viewModel.onEvent(PlateSearchEvent.ChangeNumberTextField(it))
                            })
                    }

                    Spacer(Modifier.height(16.dp))

                    // Child Component: 2
                    Column(Modifier.weight(1f)) {

                        Row(Modifier.fillMaxWidth()) {
                            Text(
                                stringResource(R.string.recent_searches),
                                style = MaterialTheme.typography.titleLarge
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        LazyColumn {
                            val recentPlates = viewModel.recentPlates.value
                            itemsIndexed(viewModel.recentPlates.value) { i, item ->
                                Column(Modifier.clickable(
                                    onClick = {
                                        viewModel.onEvent(
                                            PlateSearchEvent.ClickRecentPlate(
                                                item.prefix, item.number, navigator
                                            )
                                        )
                                    }
                                )) {
                                    Spacer(Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        LicensePlate(
                                            plate = item.toPlate(),
                                            fontSize = PLATE_FONT_SMALL,
                                            horizontalPadding = PLATE_HORIZONTAL_SMALL,
                                            verticalPadding = PLATE_VERTICAL_SMALL
                                        )

                                        Text(text = Utility.getFormattedDateFromMs(item.timestamp),
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color.Gray)
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    if (i < recentPlates.size - 1) {
                                        Divider(Modifier.fillMaxWidth())
                                    }
                                }
                            }
                        }

                    }

                    Spacer(Modifier.height(8.dp))

                    // Child Component: 3
                    Row(Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            onClick = {
                                viewModel.onEvent(PlateSearchEvent.ClickSignOut(navigator))
                            }) {
                            Text(
                                text = stringResource(id = R.string.sign_out),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Button(onClick = {
                            viewModel.onEvent(PlateSearchEvent.ClickSearchButton(navigator))
                        }) {
                        Text(
                            text = stringResource(id = R.string.search),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    }
                }
            }

        }
    }

}