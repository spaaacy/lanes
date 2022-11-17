package com.aakifahamath.fyp.presentation.plate_search

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aakifahamath.fyp.R
import com.aakifahamath.fyp.presentation.destinations.AuthenticationScreenDestination
import com.aakifahamath.fyp.presentation.util.SnackbarEvent
import com.aakifahamath.fyp.presentation.util.UiEvent
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.flow.collectLatest

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Destination
fun PlateSearchScreen(
    navigator: DestinationsNavigator,
    viewModel: PlateSearchViewModel = hiltViewModel()) {

    val snackbarHostState = remember { SnackbarHostState() }
    var loadingState by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when(event) {
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        when(event.message) {
                            SnackbarEvent.PREFIX -> context.resources.getString(R.string.warn_prefix)
                            SnackbarEvent.NUMBER -> context.resources.getString(R.string.warn_number)
                            SnackbarEvent.LETTER -> context.resources.getString(R.string.warn_letter)
                            else -> ""
                        }
                    )
                }
                is UiEvent.Loading -> {
                    loadingState = true
                }
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) {

        Column(Modifier.padding(32.dp)) {
            Column(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineSmall,
                    text = stringResource(id = R.string.welcome),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                val prefixState = viewModel.prefixState.value
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    value = prefixState,
                    label = { Text(stringResource(R.string.prefix_label)) },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    onValueChange = { viewModel.onEvent(PlateSearchEvent.ChangePrefixTextField(it)) })

                Spacer(Modifier.height(8.dp))

                val numberState = viewModel.numberState.value
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    value = numberState,
                    label = { Text(stringResource(R.string.number_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    onValueChange = { viewModel.onEvent(PlateSearchEvent.ChangeNumberTextField(it)) })

                Spacer(Modifier.height(16.dp))

                Button(
                    modifier = Modifier.align(Alignment.End),
                    onClick = {
                        viewModel.onEvent(PlateSearchEvent.ClickNextButton(navigator))
                    }) {
                    Text(
                        text = stringResource(id = R.string.next),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            Row(Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = {
                        viewModel.signOut()
                        navigator.navigate(AuthenticationScreenDestination)
                    }) {
                    Text(
                        text = stringResource(id = R.string.sign_out),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }

    }

}