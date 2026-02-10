package com.katchy.focuslive.ui.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.katchy.focuslive.data.model.MascotType
import com.katchy.focuslive.data.repository.MascotRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MascotSelectionViewModel @Inject constructor(
    private val mascotRepository: MascotRepository
) : ViewModel() {

    val selectedMascot: StateFlow<MascotType> = mascotRepository.selectedMascot
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MascotType.POPPIN)

    fun setMascot(mascot: MascotType) {
        mascotRepository.setMascot(mascot)
    }
}
