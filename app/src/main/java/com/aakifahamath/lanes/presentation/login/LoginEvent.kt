package com.aakifahamath.lanes.presentation.login

import com.ramcosta.composedestinations.navigation.DestinationsNavigator

sealed class LoginEvent {
    data class ChangeEmailTextField(val email: String): LoginEvent()
    data class ChangePasswordTextField(val password: String): LoginEvent()
    data class ClickLoginButton(val navigator: DestinationsNavigator) : LoginEvent()
    data class ClickSignUpButton(val navigator: DestinationsNavigator) : LoginEvent()
    data class ClickAnonymous(val navigator: DestinationsNavigator) : LoginEvent()
}
