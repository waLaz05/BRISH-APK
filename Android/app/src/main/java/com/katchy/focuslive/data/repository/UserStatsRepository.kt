package com.katchy.focuslive.data.repository

import android.content.Context
import com.katchy.focuslive.data.model.UserStats
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserStatsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("user_stats_prefs", Context.MODE_PRIVATE)

    private val _userStats = MutableStateFlow(loadStats())
    val userStats: StateFlow<UserStats> = _userStats.asStateFlow()

    private fun loadStats(): UserStats {
        val xp = prefs.getInt(KEY_XP, 0)
        val level = (xp / 100) + 1
        val nextLevelXp = (level) * 100
        val currentLevelMinXp = (level - 1) * 100
        val progress = if (nextLevelXp > currentLevelMinXp) {
            (xp - currentLevelMinXp).toFloat() / (nextLevelXp - currentLevelMinXp).toFloat()
        } else 0f

        return UserStats(
            totalTasksCompleted = prefs.getInt(KEY_TASKS, 0),
            totalFocusMinutes = prefs.getInt(KEY_FOCUS_MINS, 0),
            currentXp = xp,
            currentLevel = level,
            nextLevelXp = nextLevelXp,
            progressToNextLevel = progress,
            streak = prefs.getInt(KEY_STREAK, 0),
            coins = prefs.getInt(KEY_COINS, 0),
            lastLoginDate = prefs.getLong(KEY_LAST_LOGIN, 0),
            badges = (prefs.getStringSet(KEY_BADGES, emptySet()) ?: emptySet()).toList()
        )
    }

    fun addXp(amount: Int) {
        val currentXp = prefs.getInt(KEY_XP, 0)
        val newXp = currentXp + amount
        prefs.edit().putInt(KEY_XP, newXp).apply()
        _userStats.value = loadStats()
    }

    fun addFocusMinutes(minutes: Int) {
        val currentMins = prefs.getInt(KEY_FOCUS_MINS, 0)
        prefs.edit().putInt(KEY_FOCUS_MINS, currentMins + minutes).apply()
        _userStats.value = loadStats()
    }

    fun incrementTasks() {
        val currentTasks = prefs.getInt(KEY_TASKS, 0)
        prefs.edit().putInt(KEY_TASKS, currentTasks + 1).apply()
        _userStats.value = loadStats()
    }

    fun checkDailyLogin() {
        val lastLogin = prefs.getLong(KEY_LAST_LOGIN, 0L)
        val today = System.currentTimeMillis()
        
        // Simple day check (not timezone perfect but efficient for MVP)
        // 86400000 = 24 hours
        val isSameDay = (today / 86400000) == (lastLogin / 86400000)
        val isConsecutiveDay = (today / 86400000) == (lastLogin / 86400000) + 1

        if (isSameDay) return

        if (isConsecutiveDay) {
            val currentStreak = prefs.getInt(KEY_STREAK, 0) + 1
            prefs.edit().putInt(KEY_STREAK, currentStreak).apply()
            
            // Checks for Streak Badges
            if (currentStreak == 3) unlockBadge("STREAK_3")
            if (currentStreak == 7) unlockBadge("STREAK_7")
            if (currentStreak == 30) unlockBadge("STREAK_30")
        } else {
            // Reset streak
            prefs.edit().putInt(KEY_STREAK, 1).apply()
        }
        
        prefs.edit().putLong(KEY_LAST_LOGIN, today).apply()
        _userStats.value = loadStats()
    }

    fun unlockBadge(badgeId: String) {
        val currentBadges = prefs.getStringSet(KEY_BADGES, emptySet()) ?: emptySet()
        if (!currentBadges.contains(badgeId)) {
            val newBadges = currentBadges.toMutableSet().apply { add(badgeId) }
            prefs.edit().putStringSet(KEY_BADGES, newBadges).apply()
            _userStats.value = loadStats()
        }
    }

    companion object {
        private const val KEY_XP = "current_xp"
        private const val KEY_TASKS = "total_tasks"
        private const val KEY_FOCUS_MINS = "focus_mins"
        private const val KEY_STREAK = "current_streak"
        private const val KEY_COINS = "coins"
        private const val KEY_LAST_LOGIN = "last_login_date"
        private const val KEY_BADGES = "earned_badges"
    }
}
