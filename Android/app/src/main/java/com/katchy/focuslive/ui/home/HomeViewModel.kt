package com.katchy.focuslive.ui.home

import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.katchy.focuslive.data.manager.TimerManager
import com.katchy.focuslive.data.model.Task
import com.katchy.focuslive.data.repository.GoalRepository
import com.katchy.focuslive.data.repository.TaskRepository
import com.katchy.focuslive.service.TimerService
import android.app.NotificationManager
import android.content.Context
import kotlinx.coroutines.channels.Channel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val goalRepository: GoalRepository,
    private val financeRepository: com.katchy.focuslive.data.repository.FinanceRepository,
    private val authRepository: com.katchy.focuslive.data.repository.AuthRepository,
    private val noteRepository: com.katchy.focuslive.data.repository.NoteRepository,
    private val mascotRepository: com.katchy.focuslive.data.repository.MascotRepository,
    private val networkMonitor: com.katchy.focuslive.ui.util.NetworkMonitor,
    private val timerManager: TimerManager,
    private val appPreferencesRepository: com.katchy.focuslive.data.repository.AppPreferencesRepository,
    private val userStatsRepository: com.katchy.focuslive.data.repository.UserStatsRepository,
    private val widgetManager: com.katchy.focuslive.data.manager.WidgetManager,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) : ViewModel() {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    // --- Aggregated State ---
    data class HomeState(
        val tasks: List<Task> = emptyList(),
        val activeHabits: Int = 0,
        val bestStreak: Int = 0,
        val totalBalance: Double = 0.0,
        val notesCount: Int = 0,
        val connectionState: com.katchy.focuslive.ui.util.ConnectionState = com.katchy.focuslive.ui.util.ConnectionState.Available,
        val userName: String? = null,
        val userStats: com.katchy.focuslive.data.model.UserStats = com.katchy.focuslive.data.model.UserStats()
    )

    private val _tasks = taskRepository.getTasksForDate(System.currentTimeMillis())
    private val _goals = goalRepository.getGoals()
    private val _transactions = financeRepository.getTransactions()
    private val _notes = noteRepository.getNotes()
    private val _connection = networkMonitor.connectionState
    private val _user = authRepository.authStateFlow
    
    val selectedMascot = mascotRepository.selectedMascot

    // --- Separate reactive flows for efficiency ---
    private val _totalBalance = _transactions.map { transactions ->
        var income = 0.0
        var expense = 0.0
        transactions.forEach { trans ->
            when (trans.type) {
                "INCOME" -> income += trans.amount
                "EXPENSE" -> expense += trans.amount
            }
        }
        income - expense
    }.distinctUntilChanged()

    private val _habitStats = _goals.map { goals ->
        var activeHabits = 0
        var bestStreak = 0
        goals.forEach { goal ->
            if (goal.type == "HABIT") {
                activeHabits++
                if (goal.currentStreak > bestStreak) bestStreak = goal.currentStreak
            }
        }
        Pair(activeHabits, bestStreak)
    }.distinctUntilChanged()

    private val _userNameFlow = _user.map { user ->
        user?.displayName ?: user?.email?.substringBefore("@")
    }.distinctUntilChanged()

    private val _notesCount = _notes.map { it.size }.distinctUntilChanged()

    val homeState: StateFlow<HomeState> = combine(
        combine(_tasks, _habitStats, _totalBalance, _notesCount) { tasks, habitStats, balance, notesCount ->
            // Partial state holder
            Triple(tasks, habitStats, Pair(balance, notesCount))
        },
        _connection,
        _userNameFlow,
        userStatsRepository.userStats
    ) { data, connection, name, stats ->
        val (tasks, habitStats, counts) = data
        HomeState(
            tasks = tasks,
            activeHabits = habitStats.first,
            bestStreak = habitStats.second,
            totalBalance = counts.first,
            notesCount = counts.second,
            connectionState = connection,
            userName = name,
            userStats = stats
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeState())

    // --- Timer State (Delegated to TimerManager) ---
    val timeLeft: StateFlow<Long> = timerManager.timeLeft
    val isTimerRunning: StateFlow<Boolean> = timerManager.isTimerRunning
    val timerState: StateFlow<TimerManager.TimerState> = timerManager.timerState
    val activeTaskId: StateFlow<String?> = timerManager.activeTaskId
    val ambientSound: StateFlow<TimerManager.AmbientSound> = timerManager.ambientSound
    
    // Timer Settings
    val workDuration = appPreferencesRepository.workDuration
    val breakDuration = appPreferencesRepository.breakDuration
    
    // Module Visibility
    val isPlannerEnabled = appPreferencesRepository.isPlannerEnabled
    val isNotesEnabled = appPreferencesRepository.isNotesEnabled
    val isFinanceEnabled = appPreferencesRepository.isFinanceEnabled
    val isHabitsEnabled = appPreferencesRepository.isHabitsEnabled
    val isGamificationEnabled = appPreferencesRepository.isGamificationEnabled


    val userStats = userStatsRepository.userStats

    // --- Zen Mode (DND) ---
    private val _isZenModeEnabled = MutableStateFlow(false)
    val isZenModeEnabled: StateFlow<Boolean> = _isZenModeEnabled.asStateFlow()

    private val _zenModeEffect = Channel<ZenModeEffect>(Channel.BUFFERED)
    val zenModeEffect = _zenModeEffect.receiveAsFlow()

    sealed interface ZenModeEffect {
        data object RequestDndPermission : ZenModeEffect
    }
    // Lazy Vibrator
    private val vibrator: android.os.Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as android.os.VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as android.os.Vibrator
        }
    }

    // Optimized ToneGenerator
    private val toneGenerator: android.media.ToneGenerator by lazy {
        android.media.ToneGenerator(android.media.AudioManager.STREAM_ALARM, 100)
    }

    init {
        timerManager.onTimerFinished = {
            com.katchy.focuslive.ui.util.SoundManager.playCompletionSound(context)
            com.katchy.focuslive.ui.util.NotificationHelper.showPomodoroFinishedNotification(context)
            
            if (timerManager.timerState.value == TimerManager.TimerState.BREAK) {
                // Focus session ended (timer transitioned to BREAK)
                userStatsRepository.addXp(25)
                userStatsRepository.addFocusMinutes(appPreferencesRepository.workDuration.value.toInt())
                vibrateCompletion()
            }
        }
        
        timerManager.onTickSound = { secondsLeft ->
            try {
                if (secondsLeft <= 5) {
                    // Countdown Tock (Distinct, louder)
                    toneGenerator.startTone(android.media.ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 150)
                }
            } catch (e: Exception) {
                // Fallback
                com.katchy.focuslive.ui.util.SoundManager.playTickSound(context)
            }
            
            // Vibration Feedback for Tick (Short and crisp)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = android.os.VibrationEffect.createOneShot(30, 100)
                vibrator.vibrate(effect)
            } else {
                 @Suppress("DEPRECATION")
                vibrator.vibrate(30)
            }
        }
        
        // Sync durations with TimerManager
        viewModelScope.launch {
            combine(workDuration, breakDuration) { work, breakTime ->
                Pair(work, breakTime)
            }.collect { (work, breakTime) ->
                timerManager.updateDurations(work, breakTime)
            }
        }

        // Handle Ambient Sound Playback
        viewModelScope.launch {
            combine(ambientSound, isTimerRunning) { sound, running ->
                Pair(sound, running)
            }.collect { (sound, running) ->
                if (running && sound != TimerManager.AmbientSound.NONE) {
                    val resId = when(sound) {
                        TimerManager.AmbientSound.RAIN -> com.katchy.focuslive.R.raw.rain
                        TimerManager.AmbientSound.FIRE -> com.katchy.focuslive.R.raw.fire
                        TimerManager.AmbientSound.CAFE -> com.katchy.focuslive.R.raw.cafe
                        TimerManager.AmbientSound.AMBIENT -> com.katchy.focuslive.R.raw.ambient
                        else -> 0
                    }
                    com.katchy.focuslive.ui.util.SoundManager.playFocusSound(context, resId)
                } else {
                    com.katchy.focuslive.ui.util.SoundManager.stopFocusSound()
                }
            }
        }
        // Initial Full Sync on functionality load
        val constraints = androidx.work.Constraints.Builder()
            .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
            .build()
        val syncRequest = androidx.work.OneTimeWorkRequestBuilder<com.katchy.focuslive.data.worker.SyncWorker>()
            .setConstraints(constraints)
            .build()
        androidx.work.WorkManager.getInstance(context).enqueueUniqueWork(
            "InitialSync",
            androidx.work.ExistingWorkPolicy.KEEP,
            syncRequest
        )
    }
    
    fun setWorkDuration(minutes: Long) {
        appPreferencesRepository.setWorkDuration(minutes)
    }

    fun setBreakDuration(minutes: Long) {
        appPreferencesRepository.setBreakDuration(minutes)
    }

    // --- Timer Logic ---
    fun toggleTimer() {
        timerManager.toggleTimer()
        val intent = Intent(context, TimerService::class.java)
        
        // Start foreground service if timer is running to ensure it keeps going in background
        if (timerManager.isTimerRunning.value) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    fun resetTimer() {
        timerManager.resetTimer()
    }

    fun skipSession() {
        timerManager.skipSession()
    }

    fun setActiveTask(taskId: String?) {
        timerManager.setActiveTask(taskId)
    }

    fun setAmbientSound(sound: TimerManager.AmbientSound) {
        timerManager.setAmbientSound(sound)
    }

    // --- Task Logic ---
    fun toggleTask(task: Task) {
        viewModelScope.launch {
            val updatedTask = task.copy(isCompleted = !task.isCompleted)
            taskRepository.updateTask(updatedTask)
            if (updatedTask.isCompleted) {
                userStatsRepository.addXp(10)
                userStatsRepository.incrementTasks()
                
                // Early Bird Check (4 AM - 7 AM)
                val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                if (hour in 4..7) {
                    userStatsRepository.unlockBadge(com.katchy.focuslive.util.GamificationLogic.BADGE_EARLY_BIRD)
                }

                // If it's the active task, clear it
                if (timerManager.activeTaskId.value == task.id) {
                    timerManager.setActiveTask(null)
                }
            }
            updateTasksWidget()
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskRepository.deleteTask(task.id)
            updateTasksWidget()
        }
    }

    fun addTask(title: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val newTask = Task(
                title = title,
                timestamp = System.currentTimeMillis(),
                isCompleted = false
            )
            taskRepository.addTask(newTask)
            updateTasksWidget()
        }
    }
    
    private suspend fun updateTasksWidget() {
        widgetManager.updateTasksWidget()
    }

    fun setMascot(mascot: com.katchy.focuslive.data.model.MascotType) {
        mascotRepository.setMascot(mascot)
    }

    fun toggleZenMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!notificationManager.isNotificationPolicyAccessGranted) {
                viewModelScope.launch { _zenModeEffect.send(ZenModeEffect.RequestDndPermission) }
                return
            }
        }

        val newValue = !_isZenModeEnabled.value
        _isZenModeEnabled.value = newValue
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (newValue) {
                // Total Concentration: Allow only Alarms + Priority (user can configure priority)
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
            } else {
                // Normal: Allow All
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
            }
        }
    }
    
    fun onResume() {
        // Sync state with actual system status if user changed it outside app
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (notificationManager.isNotificationPolicyAccessGranted) {
                val currentFilter = notificationManager.currentInterruptionFilter
                _isZenModeEnabled.value = currentFilter != NotificationManager.INTERRUPTION_FILTER_ALL
            } else {
                _isZenModeEnabled.value = false
            }
        }
    }

    private fun vibrateCompletion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = android.os.VibrationEffect.createWaveform(longArrayOf(0, 100, 100, 200), -1)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(500)
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Release ToneGenerator resources
        try {
            toneGenerator.release()
        } catch (e: Exception) {
            // Ignore
        }
    }
}
