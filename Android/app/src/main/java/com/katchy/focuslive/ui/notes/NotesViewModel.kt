package com.katchy.focuslive.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.katchy.focuslive.data.model.Note
import com.katchy.focuslive.data.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val mascotRepository: com.katchy.focuslive.data.repository.MascotRepository,
    private val appPreferencesRepository: com.katchy.focuslive.data.repository.AppPreferencesRepository,
    private val taskRepository: com.katchy.focuslive.data.repository.TaskRepository
) : ViewModel() {

    val selectedMascot = mascotRepository.selectedMascot

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _colorFilter = MutableStateFlow<String?>(null)
    val colorFilter = _colorFilter.asStateFlow()

    // Categories Flow
    val categories = noteRepository.getCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Combine flows
    val notes: StateFlow<List<Note>> = kotlinx.coroutines.flow.combine(
        noteRepository.getNotes(),
        _searchQuery,
        _colorFilter
    ) { list, query, color ->
        list.filter { note ->
            val matchesQuery = note.title.contains(query, ignoreCase = true) || note.content.contains(query, ignoreCase = true)
            // Color filter might be deprecated by sections, but let's keep it for now as a secondary filter
            val matchesColor = color == null || note.colorHex == color
            matchesQuery && matchesColor
        }.sortedWith(
            compareByDescending<Note> { it.isPinned }
                .thenByDescending { 
                    when(it.priority) {
                        "HIGH" -> 3
                        "MEDIUM" -> 2
                        else -> 1
                    }
                }
                .thenByDescending { it.timestamp }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setColorFilter(color: String?) {
        _colorFilter.value = if(_colorFilter.value == color) null else color
    }
    
    // --- Category Management ---
    fun addCategory(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            noteRepository.addCategory(
                com.katchy.focuslive.data.model.NoteCategory(name = name)
            )
        }
    }
    
    fun deleteCategory(category: com.katchy.focuslive.data.model.NoteCategory) {
        viewModelScope.launch {
            noteRepository.deleteCategory(category)
        }
    }

    fun addNote(title: String, content: String, colorHex: String, isPinned: Boolean, priority: String, categoryId: String?) {
        if (title.isBlank() && content.isBlank()) return
        viewModelScope.launch {
            val newNote = Note(
                title = title,
                content = content,
                colorHex = colorHex,
                isPinned = isPinned,
                priority = priority,
                categoryId = categoryId
            )
            noteRepository.addNote(newNote)
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch {
            noteRepository.updateNote(note)
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            noteRepository.deleteNote(noteId)
        }
    }

    fun convertToTask(note: Note) {
        viewModelScope.launch {
            val taskTitle = if (note.title.isNotBlank()) note.title else note.content.take(50)
            val newTask = com.katchy.focuslive.data.model.Task(
                title = taskTitle,
                timestamp = System.currentTimeMillis(),
                isCompleted = false
            )
            taskRepository.addTask(newTask)
            noteRepository.deleteNote(note.id)
        }
    }
}
