package com.katchy.focuslive.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.katchy.focuslive.data.model.AppTheme
import com.katchy.focuslive.data.repository.AuthRepository
import com.katchy.focuslive.data.repository.ThemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themeRepository: ThemeRepository,
    private val authRepository: AuthRepository,
    private val appPreferencesRepository: com.katchy.focuslive.data.repository.AppPreferencesRepository
) : ViewModel() {
    
    val isPlannerEnabled = appPreferencesRepository.isPlannerEnabled
    val isNotesEnabled = appPreferencesRepository.isNotesEnabled
    val isFinanceEnabled = appPreferencesRepository.isFinanceEnabled
    val isHabitsEnabled = appPreferencesRepository.isHabitsEnabled
    val isGamificationEnabled = appPreferencesRepository.isGamificationEnabled
    
    val accentColor = appPreferencesRepository.accentColor
    val isPomodoroSoundEnabled = appPreferencesRepository.isPomodoroSoundEnabled


    fun setPlannerEnabled(enabled: Boolean) = appPreferencesRepository.setPlannerEnabled(enabled)
    fun setNotesEnabled(enabled: Boolean) = appPreferencesRepository.setNotesEnabled(enabled)
    fun setFinanceEnabled(enabled: Boolean) = appPreferencesRepository.setFinanceEnabled(enabled)
    fun setHabitsEnabled(enabled: Boolean) = appPreferencesRepository.setHabitsEnabled(enabled)
    fun setGamificationEnabled(enabled: Boolean) = appPreferencesRepository.setGamificationEnabled(enabled)
    
    fun setAccentColor(color: Int) = appPreferencesRepository.setAccentColor(color)
    fun setPomodoroSoundEnabled(enabled: Boolean) = appPreferencesRepository.setPomodoroSoundEnabled(enabled)


    val currentTheme: StateFlow<AppTheme> = themeRepository.currentTheme
    val currentUser = authRepository.authStateFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setTheme(theme: AppTheme) {
        themeRepository.setTheme(theme)
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
