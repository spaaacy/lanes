package com.aakifahamath.lanes.presentation.login

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aakifahamath.lanes.domain.authentication.Authentication
import com.aakifahamath.lanes.domain.repository.Repository
import com.aakifahamath.lanes.presentation.destinations.LoginScreenDestination
import com.aakifahamath.lanes.presentation.destinations.PlateSearchScreenDestination
import com.aakifahamath.lanes.presentation.util.AlertMessage
import com.aakifahamath.lanes.presentation.util.SnackbarMessage
import com.aakifahamath.lanes.presentation.util.UiEvent
import com.aakifahamath.lanes.util.*
import com.ramcosta.composedestinations.navigation.popUpTo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authentication: Authentication,
    private val repo: Repository
) : ViewModel() {

    private val _password = mutableStateOf<String>("")
    val password: State<String> = _password

    private val _email = mutableStateOf<String>("")
    val email: State<String> = _email

    private val emailRegex = Regex(EMAIL_REGEX_PATTERN)
    private val passwordRegex = Regex(PASSWORD_REGEX_PATTERN)

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent

    private val _isUserLoggedIn = mutableStateOf(false)
    val isUserLoggedIn: State<Boolean> = _isUserLoggedIn

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading


    init {
        _isUserLoggedIn.value = checkIfUserLoggedIn()
    }

    private fun checkIfUserLoggedIn(): Boolean {
        val loginStatus = authentication.isUserLoggedIn()
        if (loginStatus) {
            val isUserAnonymous = authentication.isUserAnonymous()
            if (!isUserAnonymous) {
                val userId = authentication.getUserId()
                userId?.let {
                    repo.loginExistingUser(userId)
                }
            } else {
                repo.setAnonymousUser()
            }
        }
        return loginStatus
    }

    private fun sendUiEvent(event: UiEvent) {
        viewModelScope.launch() {
            _uiEvent.emit(event)
        }
    }

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.ChangeEmailTextField -> {
                _email.value = event.email
            }
            is LoginEvent.ChangePasswordTextField -> {
                if (event.password.matches(passwordRegex)) {
                    _password.value = event.password
                }
            }
            is LoginEvent.ClickAnonymous -> {

                viewModelScope.launch {
                    authentication.signInAnonymousUser().collect { result ->
                        when(result) {
                            is Resource.Success -> {
                                repo.setAnonymousUser()
                                _isLoading.value = false
                                event.navigator.navigate(PlateSearchScreenDestination){
                                    popUpTo(LoginScreenDestination) { inclusive = true }
                                }
                            }
                            is Resource.Loading -> {
                                _isLoading.value = true
                            }
                            is Resource.Error -> {
                                _isLoading.value = false
                                when(result.message) {
                                    NETWORK_FAILURE -> sendUiEvent(UiEvent.Snackbar(SnackbarMessage.NETWORK_FAILURE))
                                    TIMEOUT_FAILURE -> sendUiEvent(UiEvent.Snackbar(SnackbarMessage.TIMEOUT_FAILURE))
                                }
                            }
                        }
                    }
                }
            }
            is LoginEvent.ClickLoginButton -> {
                if (!email.value.matches(emailRegex)) {
                    sendUiEvent(UiEvent.TextFieldAlert(AlertMessage.EMAIL))
                } else if (password.value.length < 6) {
                    sendUiEvent(UiEvent.TextFieldAlert(AlertMessage.PASSWORD))
                } else {
                    viewModelScope.launch {
                        authentication.signInEmailUser(email.value, password.value).collect() { result ->
                            when(result) {
                                is Resource.Success -> {
                                    val userId = authentication.getUserId()
                                    userId?.let {
                                        repo.loginExistingUser(it)
                                    }
                                    _isLoading.value = false
                                    event.navigator.navigate(PlateSearchScreenDestination) {
                                        popUpTo(LoginScreenDestination) { inclusive = true }
                                    }
                                }
                                is Resource.Loading -> {
                                    _isLoading.value = true
                                }
                                is Resource.Error -> {
                                    _isLoading.value = false
                                    when(result.message) {
                                        NETWORK_FAILURE -> sendUiEvent(UiEvent.Snackbar(SnackbarMessage.NETWORK_FAILURE))
                                        TIMEOUT_FAILURE -> sendUiEvent(UiEvent.Snackbar(SnackbarMessage.TIMEOUT_FAILURE))
                                        INVALID_CREDENTIALS -> sendUiEvent(UiEvent.Snackbar(SnackbarMessage.INVALID_CREDENTIALS))
                                        USER_NON_EXISTENT -> sendUiEvent(UiEvent.Snackbar(SnackbarMessage.USER_NON_EXISTENT))
                                    }
                                }
                            }
                        }
                    }
                }

            }
            is LoginEvent.ClickSignUpButton -> {
                if (!email.value.matches(emailRegex)) {
                    sendUiEvent(UiEvent.TextFieldAlert(AlertMessage.EMAIL))
                } else if (password.value.length < 6) {
                    sendUiEvent(UiEvent.TextFieldAlert(AlertMessage.PASSWORD))
                } else {
                    viewModelScope.launch {
                        authentication.createEmailUser(email.value, password.value)
                            .collect() { result ->
                                when (result) {
                                    is Resource.Success -> {
                                        val userId = authentication.getUserId()
                                        userId?.let {
                                            repo.createNewUser(it)
                                        }
                                        _isLoading.value = false
                                        event.navigator.navigate(PlateSearchScreenDestination) {
                                            popUpTo(LoginScreenDestination) {
                                                inclusive = true
                                            }
                                        }
                                    }
                                    is Resource.Loading -> {
                                        _isLoading.value = true
                                    }
                                    is Resource.Error -> {
                                        _isLoading.value = false
                                        when(result.message) {
                                            NETWORK_FAILURE -> sendUiEvent(UiEvent.Snackbar(SnackbarMessage.NETWORK_FAILURE))
                                            TIMEOUT_FAILURE -> sendUiEvent(UiEvent.Snackbar(SnackbarMessage.TIMEOUT_FAILURE))
                                            USER_ALREADY_EXISTS -> sendUiEvent(UiEvent.Snackbar(SnackbarMessage.USER_ALREADY_EXISTS))
                                            WEAK_PASSWORD -> sendUiEvent(UiEvent.Snackbar(SnackbarMessage.WEAK_PASSWORD))
                                        }

                                    }
                                }
                            }
                    }
                }
            }
        }
    }
}