package com.aakifahamath.lanes.presentation.plate_search

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aakifahamath.lanes.data.local.model.PlateLocal
import com.aakifahamath.lanes.domain.authentication.Authentication
import com.aakifahamath.lanes.domain.repository.Repository
import com.aakifahamath.lanes.presentation.destinations.LoginScreenDestination
import com.aakifahamath.lanes.presentation.destinations.PlateDetailScreenDestination
import com.aakifahamath.lanes.presentation.destinations.PlateSearchScreenDestination
import com.aakifahamath.lanes.presentation.plate_detail.PlateDetailNavArgs
import com.aakifahamath.lanes.presentation.util.AlertMessage
import com.aakifahamath.lanes.presentation.util.UiEvent
import com.aakifahamath.lanes.util.*
import com.ramcosta.composedestinations.navigation.popUpTo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlateSearchViewModel @Inject constructor(
    private val repo: Repository,
    private val authentication: Authentication
) : ViewModel() {

    private val _prefixState = mutableStateOf<String>("")
    val prefixState: State<String> = _prefixState

    private val _numberState = mutableStateOf<String>("")
    val numberState: State<String> = _numberState

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    private val _recentPlates = mutableStateOf<List<PlateLocal>>(mutableListOf())
    val recentPlates: State<List<PlateLocal>> = _recentPlates

    private val prefixPattern = Regex(PREFIX_REGEX_PATTERN)
    private val numberPattern = Regex(NUMBER_REGEX_PATTERN)

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent

    init {

        viewModelScope.launch {
            repo.startUserListeners().collect { result ->
                when(result) {
                    is Resource.Loading -> _isLoading.value = true
                    is Resource.Success -> _isLoading.value = false
                    is Resource.Error -> {
                        if (result.message == ANONYMOUS_USER) {
                            _isLoading.value = false
                        }
                    }
                }
            }
            repo.getRecentPlatesFromDb().collect { plateList ->
                _recentPlates.value = plateList.sortedBy { it.timestamp }
            }
        }
    }

    fun onEvent(event: PlateSearchEvent) {
        when (event) {
            is PlateSearchEvent.ChangePrefixTextField -> {
                if (event.prefix.matches(prefixPattern)) {
                    _prefixState.value = event.prefix.uppercase()
                }
            }
            is PlateSearchEvent.ChangeNumberTextField -> {
                if (event.number.matches(numberPattern)) {
                    _numberState.value = event.number
                }
            }
            is PlateSearchEvent.ClickSearchButton -> {

                // Input validation
                when {
                    prefixState.value.isEmpty() -> {
                        sendUiEvent(UiEvent.TextFieldAlert(AlertMessage.PREFIX))
                    }
                    numberState.value.isEmpty() -> {
                        sendUiEvent(UiEvent.TextFieldAlert(AlertMessage.NUMBER))
                    }
                    prefixState.value.contains(Regex(PREFIX_REJECT_REJECT_PATTERN)) -> {
                        sendUiEvent(UiEvent.TextFieldAlert(AlertMessage.PREFIX_LETTER))
                    }
                    else -> {
                        val newPrefix = prefixState.value
                        val newNumber = numberState.value

                        event.navigator.navigate(
                            PlateDetailScreenDestination(
                                PlateDetailNavArgs(newPrefix, newNumber)
                            )
                        )

                        viewModelScope.launch { repo.insertPlateToDb(newPrefix, newNumber) }

                    }
                }
            }
            is PlateSearchEvent.ClickSignOut -> {
                repo.removeUserListeners()
                authentication.signOutUser()
                repo.signOutUser()
                event.navigator.navigate(LoginScreenDestination) {
                    popUpTo(PlateSearchScreenDestination) { inclusive = true }
                }
            }
            is PlateSearchEvent.ClickRecentPlate -> {
                event.navigator.navigate(PlateDetailScreenDestination(
                    PlateDetailNavArgs(event.prefix, event.number)
                ))
            }
        }
    }

    private fun sendUiEvent(event: UiEvent) {
        viewModelScope.launch() {
            _uiEvent.emit(event)
        }
    }

}