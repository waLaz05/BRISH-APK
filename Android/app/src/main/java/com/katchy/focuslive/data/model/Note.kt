package com.katchy.focuslive.data.model

import com.google.firebase.firestore.DocumentId
import androidx.compose.runtime.Immutable

@Immutable
@androidx.room.Entity(tableName = "notes")
data class Note(
    @androidx.room.PrimaryKey
    @DocumentId val id: String = java.util.UUID.randomUUID().toString(),
    val title: String = "",
    val content: String = "",
    val colorHex: String = "#FFF9C4", // Default yellow sticky note
    val isPinned: Boolean = false,
    val priority: String = "LOW", // LOW, MEDIUM, HIGH
    val timestamp: Long = System.currentTimeMillis(),
    val categoryId: String? = null, // Link to NoteCategory
    @get:com.google.firebase.firestore.Exclude
    val isSynced: Boolean = true
)
