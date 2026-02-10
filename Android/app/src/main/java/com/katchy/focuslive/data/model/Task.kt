package com.katchy.focuslive.data.model

import androidx.compose.runtime.Immutable
import com.google.firebase.firestore.DocumentId

@Immutable
data class SubtaskItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String = "",
    val isCompleted: Boolean = false,
    val completedDates: List<Long> = emptyList()
)

@Immutable
@androidx.room.Entity(tableName = "tasks")
data class Task(
    @androidx.room.PrimaryKey
    @DocumentId val id: String = java.util.UUID.randomUUID().toString(),
    val title: String = "",
    val category: String = "General",
    @get:com.google.firebase.firestore.PropertyName("isCompleted")
    val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val dueDate: Long = System.currentTimeMillis(),
    val time: String? = null,
    val endTime: String? = null,
    val repeatMode: String = "NONE",
    val repeatDays: List<Int> = emptyList(),
    @get:com.google.firebase.firestore.PropertyName("isMyDay")
    val isMyDay: Boolean = false,
    val completedDates: List<Long> = emptyList(),
    val priority: String = "MEDIUM",
    val description: String = "",
    val subtasks: List<SubtaskItem> = emptyList(),
    @get:com.google.firebase.firestore.Exclude
    val isSynced: Boolean = true
)
