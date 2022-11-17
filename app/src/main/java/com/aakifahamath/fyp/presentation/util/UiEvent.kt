package com.aakifahamath.fyp.presentation.util

enum class SnackbarEvent {
    PREFIX, NUMBER, LETTER, LOGIN
}

sealed class UiEvent() {
    object Loading : UiEvent()
    data class ShowSnackbar(val message: SnackbarEvent): UiEvent()
}

