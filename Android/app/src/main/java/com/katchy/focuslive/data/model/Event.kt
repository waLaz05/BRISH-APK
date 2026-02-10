package com.katchy.focuslive.data.model

import androidx.compose.runtime.Immutable
import com.google.firebase.firestore.DocumentId

@Immutable
data class Event(
    @DocumentId val id: String = "",
    val title: String = "",
    val startTime: Long = 0, // Timestamp
    val endTime: Long = 0, // Timestamp
    val location: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
