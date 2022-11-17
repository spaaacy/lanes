package com.aakifahamath.fyp.presentation.authentication

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aakifahamath.fyp.common.Resource
import com.aakifahamath.fyp.domain.repository.Repository
import com.aakifahamath.fyp.domain.use_case.AuthenticationUseCase
import com.aakifahamath.fyp.presentation.destinations.PlateSearchScreenDestination
import com.aakifahamath.fyp.presentation.util.SnackbarEvent
import com.aakifahamath.fyp.presentation.util.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthenticationViewModel @Inject constructor(
    private val repo: Repository,
    private val authUseCase: AuthenticationUseCase
) : ViewModel() {

    private val _username = mutableStateOf<String>("")
    val username: State<String> = _username

    private val _password = mutableStateOf<String>("")
    val password: State<String> = _password

    private val usernameRegex = Regex("[a-zA-Z0-9_.]{0,16}")
    private val passwordRegex = Regex("[a-zA-Z0-9!@#$%^&*]{0,32}")

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent

    var isUserAuthenticated: Boolean = false

    init {
        isUserAuthenticated = isUserLoggedIn()
    }

    private fun isUserLoggedIn(): Boolean {
        return authUseCase.isUserLoggedIn()
    }

    private fun sendUiEvent(event: UiEvent) {
        viewModelScope.launch() {
            _uiEvent.emit(event)
        }
    }

    fun onEvent(event: AuthenticationEvent) {
        when (event) {
            is AuthenticationEvent.ChangeUsernameTextField -> {
                if (event.username.matches(usernameRegex)) {
                    _username.value = event.username
                }
            }
            is AuthenticationEvent.ChangePasswordTextField -> {
                if (event.password.matches(passwordRegex)) {
                    _password.value = event.password
                }
            }
            is AuthenticationEvent.ClickLoginButton -> {
                event.navigator.navigate(PlateSearchScreenDestination)
            }
            is AuthenticationEvent.ClickAnonymous -> {

                viewModelScope.launch {
                    authUseCase.signInAnonymousUser().collect { result ->
                        when(result) {
                            is Resource.Success -> {
                                event.navigator.navigate(PlateSearchScreenDestination)
                            }
                            is Resource.Loading -> {
                                sendUiEvent(UiEvent.Loading)
                            }
                            is Resource.Error -> {
                                sendUiEvent(UiEvent.ShowSnackbar(SnackbarEvent.LOGIN))
                            }
                        }
                    }
                }
            }
        }
    }
}