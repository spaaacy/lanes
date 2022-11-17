package com.aakifahamath.fyp.presentation.plate_search

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aakifahamath.fyp.domain.repository.Repository
import com.aakifahamath.fyp.domain.use_case.AuthenticationUseCase
import com.aakifahamath.fyp.presentation.destinations.UserHomeScreenDestination
import com.aakifahamath.fyp.presentation.plate_detail.PlateDetailNavArgs
import com.aakifahamath.fyp.presentation.util.SnackbarEvent
import com.aakifahamath.fyp.presentation.util.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlateSearchViewModel @Inject constructor(
    private val repo: Repository,
    private val authUseCase: AuthenticationUseCase
) : ViewModel() {

    private val _prefixState = mutableStateOf<String>("")
    val prefixState: State<String> = _prefixState

    private val _numberState = mutableStateOf<String>("")
    val numberState: State<String> = _numberState


    private val charPattern = Regex("[a-zA-Z]{0,3}")
    private val numPattern = Regex("[0-9]{0,4}")

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent

    fun signOut() {
        authUseCase.signOutUser()
    }

    fun onEvent(event: PlateSearchEvent) {
        when (event) {
            is PlateSearchEvent.ChangePrefixTextField -> {
                if (event.prefix.matches(charPattern)) {
                    _prefixState.value = event.prefix.uppercase()
                }
            }
            is PlateSearchEvent.ChangeNumberTextField -> {
                if (event.number.matches(numPattern)) {
                    _numberState.value = event.number
                }
            }
            is PlateSearchEvent.ClickNextButton -> {

                // Input validation
                when {
                    prefixState.value.isEmpty() -> {
                        sendUiEvent(UiEvent.ShowSnackbar(SnackbarEvent.PREFIX))
                    }
                    numberState.value.isEmpty() -> {
                        sendUiEvent(UiEvent.ShowSnackbar(SnackbarEvent.NUMBER))
                    }
                    prefixState.value.contains(Regex("[ioIO]+")) -> {
                        sendUiEvent(UiEvent.ShowSnackbar(SnackbarEvent.LETTER))
                    }
                    else -> {
                        persistPlate()
                        event.navigator.navigate(
                            UserHomeScreenDestination(
                                PlateDetailNavArgs(
                                    prefixState.value,
                                    numberState.value.toInt()
                                )
                            )
                        )
                    }
                }
            }
        }
    }

    private fun sendUiEvent(event: UiEvent) {
        viewModelScope.launch() {
            _uiEvent.emit(event)
        }
    }

    // Persist plate database
    private fun persistPlate() {
        viewModelScope.launch() {
            val newPrefix = prefixState.value
            val newNumber = numberState.value.toInt()
            // Add plate to remote db
            repo.insertPlateRemote(newPrefix, newNumber)
        }
    }

}