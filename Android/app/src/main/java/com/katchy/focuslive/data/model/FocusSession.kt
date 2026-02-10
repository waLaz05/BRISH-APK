package com.katchy.focuslive.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class FocusSession(
    val id: String = "",
    val userId: String = "",
    val startTime: Long = 0,
    val endTime: Long = 0,
    val durationMinutes: Int = 0,
    val mode: String = "Focus" // "Focus", "Short Break", "Long Break"
)
