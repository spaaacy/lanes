package com.aakifahamath.fyp.presentation.plate_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aakifahamath.fyp.data.mapper.toPlate
import com.aakifahamath.fyp.data.remote.ReputationRating
import com.aakifahamath.fyp.domain.model.Plate
import com.aakifahamath.fyp.domain.repository.Repository
import com.aakifahamath.fyp.presentation.destinations.UserHomeScreenDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlateDetailViewModel @Inject constructor(
    private val repo: Repository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val plate = MutableStateFlow(Plate("", 0, 0.0))

    init {
        val navArgs = UserHomeScreenDestination.argsFrom(savedStateHandle)
        val platePrefix = navArgs.prefix
        val plateNumber = navArgs.number

        repo.startListeningForPlateRemote(platePrefix, plateNumber) {
            viewModelScope.launch { plate.emit(it.toPlate()) }
        }
    }

    fun onEvent(event: PlateDetailEvent) {
        when (event) {
            is PlateDetailEvent.ClickedThumbsUp -> {
                repo.modifyPlateReputationRemote(ReputationRating.THUMBS_UP)
            }
            is PlateDetailEvent.ClickedThumbsDown -> {
                repo.modifyPlateReputationRemote(ReputationRating.THUMBS_DOWN)
            }
        }
    }

}