package com.katchy.focuslive.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class UserProfile(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val username: String = "",
    val partnerId: String? = null,
    val status: String = "IDLE", // IDLE, FOCUSING
    val lastFocusSession: Long = 0L
)
