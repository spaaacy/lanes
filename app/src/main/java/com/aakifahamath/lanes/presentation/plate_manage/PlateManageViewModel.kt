package com.aakifahamath.lanes.presentation.plate_manage

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.aakifahamath.lanes.domain.model.Plate
import com.aakifahamath.lanes.domain.repository.Repository
import com.aakifahamath.lanes.presentation.destinations.PlateDetailScreenDestination
import com.aakifahamath.lanes.presentation.plate_detail.PlateDetailNavArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PlateManageViewModel @Inject constructor(
    private val repo: Repository
): ViewModel()  {

    enum class MenuOptions { UPVOTE, DOWNVOTE, OWNED }

    private val _selectedOption = mutableStateOf(MenuOptions.OWNED)
    val selectedOption: State<MenuOptions> = _selectedOption

    private var _ownedPlates: MutableList<Plate> = mutableListOf()
    val ownedPlates: List<Plate> = _ownedPlates

    private val _upvotedPlates: MutableMap<Plate, Long> = mutableMapOf()
    val upvotedPlates: Map<Plate, Long> = _upvotedPlates

    private val _downvotedPlates: MutableMap<Plate, Long> = mutableMapOf()
    val downvotedPlates: Map<Plate, Long> = _downvotedPlates

    val isUserAnonymous = repo.isUserAnonymous()

    init {
        initializeOwnedList()
        initializeUpvoteMap()
        initializeDownvoteMap()
    }

    fun onEvent(event: PlateManageEvent) {
        when(event) {
            PlateManageEvent.SelectedOwned -> {
                _selectedOption.value = MenuOptions.OWNED
                initializeOwnedList()
            }
            PlateManageEvent.SelectedUpvoted -> {
                _selectedOption.value = MenuOptions.UPVOTE
                initializeUpvoteMap()
            }
            PlateManageEvent.SelectedDownvoted -> {
                _selectedOption.value = MenuOptions.DOWNVOTE
                initializeDownvoteMap()
            }
            is PlateManageEvent.ClickPlate -> {
                event.navigator.navigate(PlateDetailScreenDestination(
                    PlateDetailNavArgs(event.prefix, event.number)
                ))
            }
        }
    }

    private fun initializeOwnedList() {
        val fetchedOwnedPlates = repo.getOwnedPlates()
        _ownedPlates.clear()
        fetchedOwnedPlates.forEach { plate ->
            _ownedPlates.add(plate)
        }
    }

    private fun initializeUpvoteMap() {
        val fetchedUpvotedPlates = repo.getUpvotedPlates()
        _upvotedPlates.clear()
        fetchedUpvotedPlates.forEach { plate ->
            _upvotedPlates[plate.key] = plate.value
        }
    }

    private fun initializeDownvoteMap() {
        val fetchedDownvotedPlates = repo.getDownvotedPlates()
        _downvotedPlates.clear()
        fetchedDownvotedPlates.forEach { plate ->
            _downvotedPlates[plate.key] = plate.value
        }
    }

}