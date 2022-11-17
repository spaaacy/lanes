package com.aakifahamath.fyp.presentation.authentication

import com.ramcosta.composedestinations.navigation.DestinationsNavigator

sealed class AuthenticationEvent {
    data class ChangeUsernameTextField(val username: String): AuthenticationEvent()
    data class ChangePasswordTextField(val password: String): AuthenticationEvent()
    data class ClickLoginButton(val navigator: DestinationsNavigator) : AuthenticationEvent()
    data class ClickAnonymous(val navigator: DestinationsNavigator) : AuthenticationEvent()
}
