package com.aakifahamath.lanes.presentation.util

enum class AlertMessage {
    PREFIX, NUMBER, PREFIX_LETTER, PASSWORD, EMAIL
}

enum class SnackbarMessage {
    NETWORK_FAILURE, TIMEOUT_FAILURE, INVALID_CREDENTIALS, USER_NON_EXISTENT, USER_ALREADY_EXISTS, WEAK_PASSWORD
}

sealed class UiEvent() {
    data class Snackbar(val message: SnackbarMessage): UiEvent()
    data class TextFieldAlert(val message: AlertMessage): UiEvent()
}

