package com.aakifahamath.lanes.presentation.login

import android.annotation.SuppressLint
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
import com.aakifahamath.lanes.R
import com.aakifahamath.lanes.presentation.destinations.LoginScreenDestination
import com.aakifahamath.lanes.presentation.destinations.PlateSearchScreenDestination
import com.aakifahamath.lanes.presentation.util.AlertMessage
import com.aakifahamath.lanes.presentation.util.SnackbarMessage
import com.aakifahamath.lanes.presentation.util.UiEvent
import com.aakifahamath.lanes.util.DEFAULT_PADDING
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.popUpTo
import kotlinx.coroutines.flow.collectLatest

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Destination
@RootNavGraph(start = true)
fun LoginScreen(
    navigator: DestinationsNavigator,
    viewModel: LoginViewModel = hiltViewModel()
) {

    val snackbarHostState = remember { SnackbarHostState() }
    var passwordVisible by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is UiEvent.Snackbar -> {
                    snackbarHostState.showSnackbar(
                        when (event.message) {
                            SnackbarMessage.NETWORK_FAILURE -> context.resources.getString(R.string.network_failure)
                            SnackbarMessage.TIMEOUT_FAILURE -> context.resources.getString(R.string.timeout_failure)
                            SnackbarMessage.USER_ALREADY_EXISTS -> context.resources.getString(R.string.user_exists)
                            SnackbarMessage.WEAK_PASSWORD -> context.resources.getString(R.string.weak_password)
                            SnackbarMessage.USER_NON_EXISTENT -> context.resources.getString(R.string.user_non_existent)
                            SnackbarMessage.INVALID_CREDENTIALS -> context.resources.getString(R.string.invalid_credentials)
                            else -> ""
                        }
                    )
                }
                is UiEvent.TextFieldAlert -> {
                    when(event.message) {
                        AlertMessage.PASSWORD -> {
                            passwordError = true
                        }

                        AlertMessage.EMAIL -> {
                            emailError = true
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    if (!viewModel.isUserLoggedIn.value) {
        Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) {

            if (viewModel.isLoading.value) {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(Modifier.padding(DEFAULT_PADDING)) {
                    Column(
                        Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    )
                    {

                        Text(
                            text = stringResource(R.string.login_message),
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(16.dp))

                        OutlinedTextField(
                            isError = emailError,
                            supportingText = if (emailError) {
                                {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            painterResource(id = R.drawable.ic_error),
                                            contentDescription = stringResource(id = R.string.email_invalid),
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(text = stringResource(id = R.string.email_invalid))
                                    }
                                }
                            } else null,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            value = viewModel.email.value,
                            label = { Text(stringResource(R.string.email_label)) },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_account_circle),
                                    contentDescription = "Account icon" // TODO: Use string resource
                                )
                            },
                            onValueChange = {
                                if (emailError) {
                                    emailError = false
                                }
                                viewModel.onEvent(
                                    LoginEvent.ChangeEmailTextField(
                                        it
                                    )
                                )
                            }
                        )

                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            isError = passwordError,
                            supportingText = if (passwordError) {
                                {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            painterResource(id = R.drawable.ic_error),
                                            contentDescription = stringResource(id = R.string.password_invalid),
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(text = stringResource(id = R.string.password_invalid))
                                    }
                                }
                            } else null,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            value = viewModel.password.value,
                            label = { Text(stringResource(R.string.password_label)) },
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Done,
                                keyboardType = KeyboardType.Password
                            ),
                            keyboardActions = KeyboardActions(onDone = {
                                viewModel.onEvent(LoginEvent.ClickLoginButton(navigator))
                            }),
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            onValueChange = {
                                if (passwordError) {
                                    passwordError = false
                                }
                                viewModel.onEvent(
                                    LoginEvent.ChangePasswordTextField(
                                        it
                                    )
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_password),
                                    contentDescription = "Password icon" // TODO: Use string resource
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

                        Spacer(Modifier.height(8.dp))

                        Row(Modifier.padding(8.dp)) {

                            Button(
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                onClick = {
                                    viewModel.onEvent(
                                        LoginEvent.ClickSignUpButton(
                                            navigator
                                        )
                                    )
                                }) {
                                Text(
                                    text = stringResource(R.string.sign_up),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    viewModel.onEvent(
                                        LoginEvent.ClickLoginButton(
                                            navigator
                                        )
                                    )
                                }
                            ) {
                                Text(
                                    text = stringResource(R.string.login),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = {
                            viewModel.onEvent(
                                LoginEvent.ClickAnonymous(
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
        navigator.navigate(PlateSearchScreenDestination) {
            popUpTo(LoginScreenDestination) { inclusive = true }
        }
    }
}