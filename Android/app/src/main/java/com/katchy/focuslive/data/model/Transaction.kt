package com.katchy.focuslive.data.model

import androidx.compose.runtime.Immutable
import com.google.firebase.firestore.DocumentId

@Immutable
@androidx.room.Entity(tableName = "transactions")
data class Transaction(
    @androidx.room.PrimaryKey
    @DocumentId val id: String = java.util.UUID.randomUUID().toString(),
    val title: String = "",
    val amount: Double = 0.0,
    val type: String = "EXPENSE", // "INCOME", "EXPENSE"
    val category: String = "General",
    val isRecurring: Boolean = false,
    val recurrenceInterval: String = "NONE", // "NONE", "MONTHLY", "YEARLY"
    val timestamp: Long = System.currentTimeMillis(),
    @get:com.google.firebase.firestore.Exclude
    val isSynced: Boolean = true
)
