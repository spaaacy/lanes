package com.aakifahamath.lanes.presentation.plate_detail

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aakifahamath.lanes.domain.util.Rating
import com.aakifahamath.lanes.domain.model.Plate
import com.aakifahamath.lanes.domain.repository.Repository
import com.aakifahamath.lanes.presentation.destinations.PlateDetailScreenDestination
import com.aakifahamath.lanes.presentation.util.UiEvent
import com.aakifahamath.lanes.util.MONTH_IN_MS
import com.aakifahamath.lanes.util.NETWORK_FAILURE
import com.aakifahamath.lanes.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlateDetailViewModel @Inject constructor(
    private val repo: Repository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val plate = MutableStateFlow(Plate("", "", 0.0))
    val plateDetailState = MutableStateFlow(PlateDetailState())
    private var plateStateInitialized = false

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    private val _isNetworkAvailable = mutableStateOf(true)
    val isNetworkAvailable: State<Boolean> = _isNetworkAvailable

    init {
        val navArgs = PlateDetailScreenDestination.argsFrom(savedStateHandle)
        val platePrefix = navArgs.prefix
        val plateNumber = navArgs.number

        initializePlate(platePrefix, plateNumber)

    }

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent

    private fun sendUiEvent(event: UiEvent) {
        viewModelScope.launch() {
            _uiEvent.emit(event)
        }
    }

    fun onEvent(event: PlateDetailEvent) {
        when (event) {
            is PlateDetailEvent.ClickedThumbsUp -> {
                repo.modifyReputationAndLogUserReport(Rating.UPVOTE)
                viewModelScope.launch {
                    plateDetailState.emit(
                        plateDetailState.value.copy(
                            isReportAllowed = false
                        )
                    )
                }
            }
            is PlateDetailEvent.ClickedThumbsDown -> {
                repo.modifyReputationAndLogUserReport(Rating.DOWNVOTE)
                viewModelScope.launch {
                    plateDetailState.emit(
                        plateDetailState.value.copy(
                            isReportAllowed = false
                        )
                    )
                }
            }
            is PlateDetailEvent.NavigateUp -> {
                repo.removePlateListener()
                event.navigateUp()
            }
            is PlateDetailEvent.ClickedYourCar -> {
                repo.registerPlateOwner(plate.value)
                viewModelScope.launch {
                    plateDetailState.emit(
                        plateDetailState.value.copy(
                            isOwnedPlate = true
                        )
                    )
                }
            }
        }
    }

    private fun initializePlate(platePrefix: String, plateNumber: String) {
        viewModelScope.launch {
            repo.listenForAndCreateNewPlate(platePrefix, plateNumber).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _isLoading.value = true
                    }
                    is Resource.Success -> {
                        result.data?.let { newPlate ->
                            plate.emit(newPlate)

                            if (!plateStateInitialized) {
                                initializePlateState()
                                plateStateInitialized = true
                            } else {
                                _isLoading.value = false
                            }

                        }
                    }
                    is Resource.Error -> {
                        _isLoading.value = false
                        when (result.message) {
                            NETWORK_FAILURE -> _isNetworkAvailable.value = false
                        }
                    }
                }
            }


        }
    }

    private fun initializePlateState() {
        viewModelScope.launch {
            val isUserAnonymous = repo.isUserAnonymous()
            val isOwnedPlate = repo.isPlateOwned(plate.value)
            val timestamp = repo.getLatestReportTimestamp(plate.value)
            val isReportAllowed = if (!isUserAnonymous) {
                timestamp + MONTH_IN_MS < System.currentTimeMillis()
            } else false
            viewModelScope.launch {
                plateDetailState.emit(
                    plateDetailState.value.copy(
                        isUserAnonymous = isUserAnonymous,
                        isOwnedPlate = isOwnedPlate,
                        isReportAllowed = isReportAllowed,
                    )
                )
            }
            _isLoading.value = false
        }
    }
}