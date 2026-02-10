package com.katchy.focuslive.data.repository

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.katchy.focuslive.data.local.dao.HabitDao
import com.katchy.focuslive.data.model.Habit
import com.katchy.focuslive.data.worker.SyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface HabitRepository {
    fun getHabits(): Flow<List<Habit>>
    suspend fun addHabit(habit: Habit): Result<Unit>
    suspend fun updateHabit(habit: Habit): Result<Unit>
    suspend fun deleteHabit(habitId: String): Result<Unit>
    suspend fun syncPendingChanges(): Result<Unit>
    suspend fun refreshHabits(): Result<Unit>
}

@Singleton
class HabitRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val habitDao: HabitDao,
    @ApplicationContext private val context: Context
) : HabitRepository {

    companion object {
        private const val TAG = "HabitRepository"
    }

    private val habitsCollection
        get() = auth.currentUser?.uid?.let { uid ->
            firestore.collection("users").document(uid).collection("habits")
        }

    override fun getHabits(): Flow<List<Habit>> {
        return habitDao.getAllHabits()
    }

    override suspend fun addHabit(habit: Habit): Result<Unit> {
        return try {
            habitDao.insertHabit(habit.copy(isSynced = false))
            scheduleSync()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateHabit(habit: Habit): Result<Unit> {
        return try {
            habitDao.updateHabit(habit.copy(isSynced = false))
            scheduleSync()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteHabit(habitId: String): Result<Unit> {
        return try {
            habitDao.deleteHabit(habitId)
            // Fire-and-forget for deletion
            try {
                habitsCollection?.document(habitId)?.delete()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete remote habit $habitId", e)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncPendingChanges(): Result<Unit> {
        return try {
            val unsynced = habitDao.getUnsyncedHabits()
            if (unsynced.isNotEmpty()) {
                val collection = habitsCollection ?: return Result.failure(Exception("No user"))
                unsynced.forEach { item ->
                    collection.document(item.id).set(item.copy(isSynced = true)).await()
                    habitDao.updateSyncStatus(item.id, true)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun refreshHabits(): Result<Unit> {
        return try {
            val collection = habitsCollection ?: return Result.failure(Exception("No user"))
            val snapshot = collection.get().await()
            val habits = snapshot.toObjects(Habit::class.java)
            
            habits.forEach { habit ->
                // Recalculate streak to ensure consistency with local time and actual completion dates
                val correctedStreak = com.katchy.focuslive.util.HabitUtils.calculateStreak(habit.completedDates)
                habitDao.insertHabit(habit.copy(isSynced = true, currentStreak = correctedStreak))
            }
            Log.d(TAG, "Refreshed ${habits.size} habits from cloud")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Refresh failed", e)
            Result.failure(e)
        }
    }

    private fun scheduleSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "SyncWork",
            ExistingWorkPolicy.KEEP,
            syncRequest
        )
    }
}
