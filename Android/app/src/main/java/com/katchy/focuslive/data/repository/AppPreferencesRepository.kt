package com.katchy.focuslive.data.repository

import android.content.Context

import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    private val _onboardingCompleted = MutableStateFlow(
        prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    )
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

    private val _userName = MutableStateFlow(
        prefs.getString(KEY_USER_NAME, "") ?: ""
    )
    val userName: StateFlow<String> = _userName.asStateFlow()



    fun setUserName(name: String) {
        prefs.edit().putString(KEY_USER_NAME, name).apply()
        _userName.value = name
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
        _onboardingCompleted.value = completed
    }



    private val _workDuration = MutableStateFlow(
        prefs.getLong(KEY_WORK_DURATION, 25L)
    )
    val workDuration: StateFlow<Long> = _workDuration.asStateFlow()

    private val _breakDuration = MutableStateFlow(
        prefs.getLong(KEY_BREAK_DURATION, 5L)
    )
    val breakDuration: StateFlow<Long> = _breakDuration.asStateFlow()

    private val _notificationsScheduled = MutableStateFlow(
        prefs.getBoolean(KEY_NOTIFICATIONS_SCHEDULED, false)
    )
    val notificationsScheduled: StateFlow<Boolean> = _notificationsScheduled.asStateFlow()

    // Module Visibility
    private val _isPlannerEnabled = MutableStateFlow(prefs.getBoolean(KEY_PLANNER_ENABLED, true))
    val isPlannerEnabled: StateFlow<Boolean> = _isPlannerEnabled.asStateFlow()

    private val _isNotesEnabled = MutableStateFlow(prefs.getBoolean(KEY_NOTES_ENABLED, true))
    val isNotesEnabled: StateFlow<Boolean> = _isNotesEnabled.asStateFlow()

    private val _isFinanceEnabled = MutableStateFlow(prefs.getBoolean(KEY_FINANCE_ENABLED, true))
    val isFinanceEnabled: StateFlow<Boolean> = _isFinanceEnabled.asStateFlow()

    private val _isHabitsEnabled = MutableStateFlow(prefs.getBoolean(KEY_HABITS_ENABLED, true))
    val isHabitsEnabled: StateFlow<Boolean> = _isHabitsEnabled.asStateFlow()

    private val _isGamificationEnabled = MutableStateFlow(prefs.getBoolean(KEY_GAMIFICATION_ENABLED, true))
    val isGamificationEnabled: StateFlow<Boolean> = _isGamificationEnabled.asStateFlow()

    private val _accentColor = MutableStateFlow(prefs.getInt(KEY_ACCENT_COLOR, android.graphics.Color.parseColor("#4F46E5"))) // Default Indigo-like
    val accentColor: StateFlow<Int> = _accentColor.asStateFlow()

    private val _isPomodoroSoundEnabled = MutableStateFlow(prefs.getBoolean(KEY_POMODORO_SOUND_ENABLED, true))
    val isPomodoroSoundEnabled: StateFlow<Boolean> = _isPomodoroSoundEnabled.asStateFlow()

    fun setWorkDuration(minutes: Long) {
        prefs.edit().putLong(KEY_WORK_DURATION, minutes).apply()
        _workDuration.value = minutes
    }

    fun setBreakDuration(minutes: Long) {
        prefs.edit().putLong(KEY_BREAK_DURATION, minutes).apply()
        _breakDuration.value = minutes
    }
    
    fun setNotificationsScheduled(scheduled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_SCHEDULED, scheduled).apply()
        _notificationsScheduled.value = scheduled
    }

    fun setPlannerEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PLANNER_ENABLED, enabled).apply()
        _isPlannerEnabled.value = enabled
    }

    fun setNotesEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTES_ENABLED, enabled).apply()
        _isNotesEnabled.value = enabled
    }

    fun setFinanceEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_FINANCE_ENABLED, enabled).apply()
        _isFinanceEnabled.value = enabled
    }

    fun setHabitsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HABITS_ENABLED, enabled).apply()
        _isHabitsEnabled.value = enabled
    }

    fun setGamificationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_GAMIFICATION_ENABLED, enabled).apply()
        _isGamificationEnabled.value = enabled
    }

    fun setAccentColor(color: Int) {
        prefs.edit().putInt(KEY_ACCENT_COLOR, color).apply()
        _accentColor.value = color
    }

    fun setPomodoroSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_POMODORO_SOUND_ENABLED, enabled).apply()
        _isPomodoroSoundEnabled.value = enabled
    }



    companion object {
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_WORK_DURATION = "work_duration"
        private const val KEY_BREAK_DURATION = "break_duration"
        private const val KEY_NOTIFICATIONS_SCHEDULED = "notifications_scheduled"
        private const val KEY_PLANNER_ENABLED = "planner_enabled"
        private const val KEY_NOTES_ENABLED = "notes_enabled"
        private const val KEY_FINANCE_ENABLED = "finance_enabled"
        private const val KEY_HABITS_ENABLED = "habits_enabled"
        private const val KEY_GAMIFICATION_ENABLED = "gamification_enabled"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_ACCENT_COLOR = "accent_color"
        private const val KEY_POMODORO_SOUND_ENABLED = "pomodoro_sound_enabled"

    }
}
