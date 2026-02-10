package com.katchy.focuslive.ui.home

import android.content.Context
import com.katchy.focuslive.data.manager.TimerManager
import com.katchy.focuslive.data.model.Task
import com.katchy.focuslive.data.repository.*
import com.katchy.focuslive.util.MainDispatcherRule
import com.katchy.focuslive.ui.util.NetworkMonitor
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: HomeViewModel
    
    // Mocks
    private val taskRepository: TaskRepository = mockk(relaxed = true)
    private val goalRepository: GoalRepository = mockk(relaxed = true)
    private val financeRepository: FinanceRepository = mockk(relaxed = true)
    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val noteRepository: NoteRepository = mockk(relaxed = true)
    private val mascotRepository: MascotRepository = mockk(relaxed = true)
    private val networkMonitor: NetworkMonitor = mockk(relaxed = true)
    private val timerManager: TimerManager = mockk(relaxed = true)
    private val appPreferencesRepository: AppPreferencesRepository = mockk(relaxed = true)
    private val userStatsRepository: UserStatsRepository = mockk(relaxed = true)
    private val widgetManager: com.katchy.focuslive.data.manager.WidgetManager = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)

    @Before
    fun setup() {
        // Setup default flows
        every { taskRepository.getTasksForDate(any()) } returns flowOf(emptyList())
        every { goalRepository.getGoals() } returns flowOf(emptyList())
        every { financeRepository.getTransactions() } returns flowOf(emptyList())
        every { noteRepository.getNotes() } returns flowOf(emptyList())
        every { authRepository.authStateFlow } returns flowOf(null)
        every { mascotRepository.selectedMascot } returns MutableStateFlow(com.katchy.focuslive.data.model.MascotType.POPPIN)
        
        // TimerManager Setup
        every { timerManager.timeLeft } returns MutableStateFlow(1500L)
        every { timerManager.isTimerRunning } returns MutableStateFlow(false)
        every { timerManager.timerState } returns MutableStateFlow(TimerManager.TimerState.WORK)
        every { timerManager.activeTaskId } returns MutableStateFlow(null)
        every { timerManager.ambientSound } returns MutableStateFlow(TimerManager.AmbientSound.NONE)
        
        // Prefs
        every { appPreferencesRepository.workDuration } returns MutableStateFlow(25L)
        every { appPreferencesRepository.breakDuration } returns MutableStateFlow(5L)
        every { appPreferencesRepository.isPlannerEnabled } returns MutableStateFlow(true)
        every { appPreferencesRepository.isNotesEnabled } returns MutableStateFlow(true)
        every { appPreferencesRepository.isFinanceEnabled } returns MutableStateFlow(true)
        every { appPreferencesRepository.isHabitsEnabled } returns MutableStateFlow(true)
        every { appPreferencesRepository.isGamificationEnabled } returns MutableStateFlow(true)
        
        every { userStatsRepository.userStats } returns MutableStateFlow(com.katchy.focuslive.data.model.UserStats())

        viewModel = HomeViewModel(
            taskRepository,
            goalRepository,
            financeRepository,
            authRepository,
            noteRepository,
            mascotRepository,
            networkMonitor,
            timerManager,
            appPreferencesRepository,
            userStatsRepository,
            widgetManager,
            context
        )
    }

    @Test
    fun toggleTask_completingTask_awardsXP() = runTest {
        // Arrange
        val task = Task(id = "1", title = "Test Task", isCompleted = false)
        
        // Act
        viewModel.toggleTask(task)

        // Assert
        coVerify { userStatsRepository.addXp(10) }
        coVerify { userStatsRepository.incrementTasks() }
        coVerify { taskRepository.updateTask(match { it.isCompleted }) }
    }
}
