package com.katchy.focuslive.ui.planner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.katchy.focuslive.data.model.Task
import com.katchy.focuslive.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class PlannerViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val mascotRepository: com.katchy.focuslive.data.repository.MascotRepository,
    private val appPreferencesRepository: com.katchy.focuslive.data.repository.AppPreferencesRepository,
    private val aiService: com.katchy.focuslive.data.service.AIService,
    private val notificationScheduler: com.katchy.focuslive.scheduler.NotificationScheduler
) : ViewModel() {

    private val _isBrainDumping = MutableStateFlow(false)
    val isBrainDumping = _isBrainDumping.asStateFlow()

    private val _brainDumpError = MutableStateFlow<String?>(null)
    val brainDumpError = _brainDumpError.asStateFlow()

    private val _uiMessage = MutableSharedFlow<String>()
    val uiMessage = _uiMessage.asSharedFlow()

    fun processBrainDump(text: String) {
        viewModelScope.launch {
            _isBrainDumping.value = true
            _brainDumpError.value = null // Clear previous errors
            try {
                val generatedTasks = aiService.generateBrainDumpTasks(text)
                if (generatedTasks.isEmpty()) {
                    _brainDumpError.value = "No pude identificar tareas. Intenta ser más claro."
                } else {
                    var tasksAdded = 0
                    generatedTasks.forEach { dumpTask ->
                        val timestamp = dumpTask.dueDateTimestamp ?: _selectedDate.value
                        taskRepository.addTask(
                            Task(
                                title = dumpTask.title,
                                timestamp = timestamp,
                                isCompleted = false,
                                repeatMode = "NONE",
                                time = dumpTask.time,
                                description = dumpTask.description
                            )
                        )
                        tasksAdded++
                    }
                    _uiMessage.emit("¡Listo! He añadido $tasksAdded tareas.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _brainDumpError.value = "Error de conexión. Verifica tu internet."
            } finally {
                _isBrainDumping.value = false
            }
        }
    }

    fun clearBrainDumpError() {
        _brainDumpError.value = null
    }

    private val _isPrioritizing = MutableStateFlow(false)
    val isPrioritizing = _isPrioritizing.asStateFlow()

    fun autoPrioritizeTasks() {
        viewModelScope.launch {
            val currentTasks = tasksForSelectedDate.value
            if(currentTasks.isEmpty()) {
                _uiMessage.emit("No hay tareas para priorizar.")
                return@launch
            }

            _isPrioritizing.value = true
            try {
                val prioritizedList = aiService.prioritizeTasks(currentTasks)
                if(prioritizedList.isEmpty()) {
                    _uiMessage.emit("No se pudieron priorizar las tareas.")
                } else {
                    var highCount = 0
                    prioritizedList.forEach { pTask ->
                        val originalTask = currentTasks.find { it.id == pTask.taskId }
                        if (originalTask != null) {
                            if (pTask.newPriority == "HIGH") highCount++
                            // Update task priority in DB
                            taskRepository.updateTask(originalTask.copy(priority = pTask.newPriority))
                        }
                    }
                    _uiMessage.emit("¡He reorganizado tu día! tienes $highCount prioridades altas.")
                }
            } catch (e: Exception) {
                _uiMessage.emit("Error al priorizar tareas.")
            } finally {
                _isPrioritizing.value = false
            }
        }
    }

    val selectedMascot = mascotRepository.selectedMascot

    private fun getStartOfDay(millis: Long): Long {
        val cal = Calendar.getInstance().apply { 
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    private val _selectedDate = MutableStateFlow(getStartOfDay(System.currentTimeMillis()))
    val selectedDate: StateFlow<Long> = _selectedDate.asStateFlow()

    val tasksForSelectedDate: StateFlow<List<Task>> = _selectedDate
        .flatMapLatest { date ->
            taskRepository.getTasksForDate(date).map { tasks ->
                val calendar = Calendar.getInstance()
                val selectedDayOfWeek = calendar.let {
                    it.timeInMillis = date
                    it.get(Calendar.DAY_OF_WEEK)
                }

                tasks.filter { task ->
                    // Use a fresh calendar specifically for this task to avoid any state reuse issues
                    val taskCal = Calendar.getInstance()
                    taskCal.timeInMillis = task.timestamp
                    taskCal.set(Calendar.HOUR_OF_DAY, 0)
                    taskCal.set(Calendar.MINUTE, 0)
                    taskCal.set(Calendar.SECOND, 0)
                    taskCal.set(Calendar.MILLISECOND, 0)
                    val taskStartOfDay = taskCal.timeInMillis

                    when (task.repeatMode) {
                        "DAILY" -> date >= taskStartOfDay
                        "WEEKLY", "CUSTOM" -> {
                            val isAfterStart = date >= taskStartOfDay
                            // Robust check: Ensure repeatDays is not empty and verify content
                            val isCorrectDay = task.repeatDays.isNotEmpty() && task.repeatDays.contains(selectedDayOfWeek)
                            isAfterStart && isCorrectDay
                        }
                        else -> {
                            // "NONE" or others
                            date == taskStartOfDay
                        }
                    }
                }.map { task ->
                    // Override isCompleted for recurring tasks based on completedDates
                    if (task.repeatMode != "NONE") {
                        val isCompletedToday = task.completedDates.any { 
                            it == date || (java.lang.Math.abs(it - date) < 1000)
                        }
                        
                        // Handle SUBTASKS for recurring tasks
                        val updatedSubtasks = task.subtasks.map { subtask ->
                            val isSubtaskCompletedToday = subtask.completedDates.any {
                                it == date || (java.lang.Math.abs(it - date) < 1000)
                            }
                            subtask.copy(isCompleted = isSubtaskCompletedToday)
                        }
                        
                        task.copy(isCompleted = isCompletedToday, subtasks = updatedSubtasks)
                    } else {
                        task
                    }
                }.sortedBy { it.time ?: "23:59" } 
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectDate(timestamp: Long) {
        _selectedDate.value = getStartOfDay(timestamp)
    }

    // Debounce map
    private val lastToggleTime = mutableMapOf<String, Long>()
    private val DEBOUNCE_DELAY = 500L

    fun addTask(
        title: String, 
        repeatMode: String = "NONE", 
        repeatDays: List<Int> = emptyList(),
        time: String? = null,
        endTime: String? = null,
        category: String = "Personal", 
        priority: String = "MEDIUM",
        initialSubtasks: List<String> = emptyList()
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val subtaskItems = initialSubtasks.map { com.katchy.focuslive.data.model.SubtaskItem(title = it) }
            val newTask = Task(
                title = title,
                timestamp = _selectedDate.value, // Already start of day
                isCompleted = false,
                repeatMode = repeatMode,
                repeatDays = repeatDays,
                time = time,
                endTime = endTime,
                category = category,
                priority = priority,
                subtasks = subtaskItems
            )
            taskRepository.addTask(newTask)
            
            // Schedule notification if time is set
            if (time != null) {
                notificationScheduler.scheduleTaskReminder(newTask)
            }
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            taskRepository.updateTask(task)
            
            // Reschedule notification
            notificationScheduler.cancelTaskReminder(task.id)
            if (task.time != null && !task.isCompleted) {
                notificationScheduler.scheduleTaskReminder(task)
            }
        }
    }

    fun toggleTask(task: Task) {
        val currentTime = System.currentTimeMillis()
        val lastTime = lastToggleTime[task.id] ?: 0L
        if (currentTime - lastTime < DEBOUNCE_DELAY) return
        lastToggleTime[task.id] = currentTime

        viewModelScope.launch {
            if (task.repeatMode != "NONE") {
                
                // Logic for Recurring Tasks: Toggle date in completedDates list
                val today = _selectedDate.value // Assumed to be start of day
                
                // Use robust check instead of simple .contains
                val isAlreadyCompleted = task.completedDates.any { 
                    it == today || 
                    (java.lang.Math.abs(it - today) < 1000) // 1 second tolerance just in case
                }
                
                val newCompletedDates = if (isAlreadyCompleted) {
                    task.completedDates.filter { 
                         it != today && (java.lang.Math.abs(it - today) >= 1000)
                    }
                } else {
                    task.completedDates + today
                }
                
                // CRITICAL: Always reset isCompleted to FALSE for recurring tasks in the DB.
                // The UI logic handles the display state. This prevents "ghost" completions on other days.
                taskRepository.updateTask(task.copy(completedDates = newCompletedDates, isCompleted = false))
            } else {
                // Logic for One-off Tasks
                val updatedTask = task.copy(isCompleted = !task.isCompleted)
                taskRepository.updateTask(updatedTask)
                
                // Cancel/Reschedule based on completion
                if (updatedTask.isCompleted) {
                    notificationScheduler.cancelTaskReminder(updatedTask.id)
                } else if (updatedTask.time != null) {
                     notificationScheduler.scheduleTaskReminder(updatedTask)
                }
            }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskRepository.deleteTask(task.id)
            notificationScheduler.cancelTaskReminder(task.id)
        }
    }

    private val _loadingTaskIds = MutableStateFlow<Set<String>>(emptySet())
    val loadingTaskIds = _loadingTaskIds.asStateFlow()

    fun autoBreakdownTask(task: Task) {
        viewModelScope.launch {
            _loadingTaskIds.value += task.id
            try {
                val subtaskTitles = aiService.generateSubtasks(task.title)
                if (subtaskTitles.isEmpty()) {
                    _uiMessage.emit("La tarea es muy simple o no pude dividirla.")
                } else {
                    val newSubtasks = subtaskTitles.map { com.katchy.focuslive.data.model.SubtaskItem(title = it) }
                    val updatedTask = task.copy(subtasks = task.subtasks + newSubtasks)
                    taskRepository.updateTask(updatedTask)
                    _uiMessage.emit("¡Desglose mágico completado! ✨")
                }
            } catch (e: Exception) {
                _uiMessage.emit("Error al dividir la tarea.")
            } finally {
                _loadingTaskIds.value -= task.id
            }
        }
    }

    fun toggleSubtask(task: Task, subtaskId: String) {
        val currentTime = System.currentTimeMillis()
        // Use composite key for safety
        val key = "${task.id}_$subtaskId"
        val lastTime = lastToggleTime[key] ?: 0L
        if (currentTime - lastTime < DEBOUNCE_DELAY) return
        lastToggleTime[key] = currentTime

        viewModelScope.launch {
            val updatedSubtasks = task.subtasks.map { subtask ->
                if (subtask.id == subtaskId) {
                    if (task.repeatMode != "NONE") {
                        // RECURRING LOGIC for subtasks
                        val today = _selectedDate.value
                        val isAlreadyCompleted = subtask.completedDates.any { 
                            it == today || (java.lang.Math.abs(it - today) < 1000) 
                        }
                        
                        val newDates = if (isAlreadyCompleted) {
                            subtask.completedDates.filter { 
                                it != today && (java.lang.Math.abs(it - today) >= 1000) 
                            }
                        } else {
                            subtask.completedDates + today
                        }
                        
                        // Force isCompleted false in DB, similar to main task
                        subtask.copy(completedDates = newDates, isCompleted = false)
                    } else {
                        // ONE-OFF LOGIC
                        subtask.copy(isCompleted = !subtask.isCompleted)
                    }
                } else {
                    subtask
                }
            }
            taskRepository.updateTask(task.copy(subtasks = updatedSubtasks))
        }
    }

    fun addSubtask(task: Task, subtaskTitle: String) {
        if (subtaskTitle.isBlank()) return
        viewModelScope.launch {
            val newSubtask = com.katchy.focuslive.data.model.SubtaskItem(title = subtaskTitle)
            val updatedTask = task.copy(subtasks = task.subtasks + newSubtask)
            taskRepository.updateTask(updatedTask)
        }
    }

    fun updateSubtask(task: Task, subtaskId: String, newTitle: String) {
        if (newTitle.isBlank()) return
        viewModelScope.launch {
            val updatedSubtasks = task.subtasks.map {
                if (it.id == subtaskId) it.copy(title = newTitle) else it
            }
            taskRepository.updateTask(task.copy(subtasks = updatedSubtasks))
        }
    }

    fun deleteSubtask(task: Task, subtaskId: String) {
         viewModelScope.launch {
            val updatedSubtasks = task.subtasks.filter { it.id != subtaskId }
            taskRepository.updateTask(task.copy(subtasks = updatedSubtasks))
        }
    }
}
