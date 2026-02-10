package com.katchy.focuslive.data.repository

import android.content.Context
import com.katchy.focuslive.data.model.MascotType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MascotRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val prefs = context.getSharedPreferences("mascot_prefs", Context.MODE_PRIVATE)

    // Load saved mascot or default to POPPIN
    private val savedMascotId = prefs.getString(KEY_SELECTED_MASCOT, MascotType.POPPIN.id) ?: MascotType.POPPIN.id
    private val initialMascot = MascotType.values().find { it.id == savedMascotId } ?: MascotType.POPPIN

    private val _selectedMascot = MutableStateFlow(initialMascot)
    val selectedMascot: StateFlow<MascotType> = _selectedMascot.asStateFlow()

    fun setMascot(mascot: MascotType) {
        _selectedMascot.value = mascot
        prefs.edit().putString(KEY_SELECTED_MASCOT, mascot.id).apply()
    }

    companion object {
        private const val KEY_SELECTED_MASCOT = "selected_mascot_id"
    }
}
