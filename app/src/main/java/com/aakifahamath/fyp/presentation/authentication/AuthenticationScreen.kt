package com.aakifahamath.fyp.presentation.authentication

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aakifahamath.fyp.R
import com.aakifahamath.fyp.presentation.destinations.PlateSearchScreenDestination
import com.aakifahamath.fyp.presentation.util.SnackbarEvent
import com.aakifahamath.fyp.presentation.util.UiEvent
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.flow.collectLatest

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Destination
@RootNavGraph(start = true)
fun AuthenticationScreen(
    navigator: DestinationsNavigator,
    viewModel: AuthenticationViewModel = hiltViewModel()
) {

    val snackbarHostState = remember { SnackbarHostState() }
    var loadingState by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        when (event.message) {
                            SnackbarEvent.LOGIN -> context.resources.getString(R.string.login_failure)
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

    if (!viewModel.isUserAuthenticated) {
        Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) {

            if (loadingState) {
                Column(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(Modifier.padding(32.dp)) {
                    Column(
                        Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    )
                    {

                        Text(
                            stringResource(R.string.login_message),
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(16.dp))

                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            value = viewModel.username.value,
                            label = { Text(stringResource(R.string.username_label)) },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_account_circle),
                                    contentDescription = "Account icon"
                                )
                            },
                            onValueChange = {
                                viewModel.onEvent(
                                    AuthenticationEvent.ChangeUsernameTextField(
                                        it
                                    )
                                )
                            }
                        )

                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            value = viewModel.password.value,
                            label = { Text(stringResource(R.string.password_label)) },
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Done,
                                keyboardType = KeyboardType.Password
                            ),
                            keyboardActions = KeyboardActions(onDone = {
                                viewModel.onEvent(AuthenticationEvent.ClickLoginButton(navigator))
                            }),
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            onValueChange = {
                                viewModel.onEvent(
                                    AuthenticationEvent.ChangePasswordTextField(
                                        it
                                    )
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_password),
                                    contentDescription = "Password icon"
                                )
                            },
                            trailingIcon = {
                                if (viewModel.password.value.isNotEmpty()) {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        val icon = painterResource(
                                            if (passwordVisible) R.drawable.visibility_off
                                            else R.drawable.visibility
                                        )
                                        val description =
                                            if (passwordVisible) stringResource(R.string.hide_password)
                                            else stringResource(R.string.show_password)
                                        Icon(painter = icon, contentDescription = description)
                                    }
                                }
                            }
                        )

                        Spacer(Modifier.height(16.dp))

                        Button(modifier = Modifier.align(Alignment.End),
                            onClick = {
                                viewModel.onEvent(
                                    AuthenticationEvent.ClickLoginButton(
                                        navigator
                                    )
                                )
                            }) {
                            Text(
                                text = stringResource(R.string.login),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = {
                            viewModel.onEvent(
                                AuthenticationEvent.ClickAnonymous(
                                    navigator
                                )
                            )
                        }) {
                            Text(stringResource(R.string.continue_anon))
                        }
                    }
                }
            }
        }
    } else {
        navigator.navigate(PlateSearchScreenDestination)
    }
}