package com.katchy.focuslive.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.katchy.focuslive.data.model.BrishQuotes
import com.katchy.focuslive.ui.components.BrishPose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BrishViewModel @Inject constructor(
    private val mascotRepository: com.katchy.focuslive.data.repository.MascotRepository
) : ViewModel() {

    private val _currentQuote = MutableStateFlow("")
    val currentQuote = _currentQuote.asStateFlow()

    private val _isMascotVisible = MutableStateFlow(true)
    val isMascotVisible = _isMascotVisible.asStateFlow()

    private val _currentPose = MutableStateFlow(BrishPose.DEFAULT)
    val currentPose = _currentPose.asStateFlow()
    
    val selectedMascot = mascotRepository.selectedMascot

    init {
        // Observe mascot changes to update quote immediately
        viewModelScope.launch {
            selectedMascot.collect {
                 refreshQuote()
            }
        }
    }

    fun updateContext(pose: BrishPose) {
        if (_currentPose.value != pose) {
            _currentPose.value = pose
            refreshQuote()
        }
    }

    fun refreshQuote() {
        val mascot = selectedMascot.value
        _currentQuote.value = BrishQuotes.getQuoteForPose(_currentPose.value.name, mascot)
    }

    fun toggleMascot() {
        _isMascotVisible.value = !_isMascotVisible.value
    }
}
