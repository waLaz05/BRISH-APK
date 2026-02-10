package com.katchy.focuslive.ui.planner

import app.cash.turbine.test
import com.katchy.focuslive.data.model.Task
import com.katchy.focuslive.data.repository.AppPreferencesRepository
import com.katchy.focuslive.data.repository.MascotRepository
import com.katchy.focuslive.data.repository.TaskRepository
import com.katchy.focuslive.data.service.AIService
import com.katchy.focuslive.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class PlannerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: PlannerViewModel
    private val taskRepository: TaskRepository = mockk()
    private val mascotRepository: MascotRepository = mockk()
    private val appPreferencesRepository: AppPreferencesRepository = mockk()
    private val aiService: AIService = mockk()

    @Before
    fun setup() {
        // Default mocks
        every { mascotRepository.selectedMascot } returns kotlinx.coroutines.flow.MutableStateFlow(com.katchy.focuslive.data.model.MascotType.POPPIN)
        
        viewModel = PlannerViewModel(
            taskRepository,
            mascotRepository,
            appPreferencesRepository,
            aiService
        )
    }

    @Test
    fun `tasksForSelectedDate emits correct tasks for selected date`() = runTest {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val task1 = Task(id = "1", title = "Task 1", timestamp = today, repeatMode = "NONE")
        val task2 = Task(id = "2", title = "Task 2", timestamp = today + 86400000, repeatMode = "NONE") // Tomorrow

        every { taskRepository.getTasksForDate(any()) } returns flowOf(listOf(task1, task2))

        viewModel.tasksForSelectedDate.test {
            val items = awaitItem()
            // The ViewModel filters tasks based on the selected date within the flatMapLatest
            // We expect only task1 to remain after filtering if today is selected
            assertEquals(1, items.size)
            assertEquals("Task 1", items[0].title)
        }
    }

    @Test
    fun `addTask calls repository with correct task data`() = runTest {
        val title = "New Task"
        coEvery { taskRepository.addTask(any()) } returns Result.success(Unit)

        viewModel.addTask(title)

        io.mockk.coVerify { 
            taskRepository.addTask(match { it.title == title }) 
        }
    }
}
