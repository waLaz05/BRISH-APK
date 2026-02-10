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
import com.katchy.focuslive.data.local.dao.TaskDao
import com.katchy.focuslive.data.model.Task
import com.katchy.focuslive.data.worker.SyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface TaskRepository {
    fun getTasksForDate(dateMillis: Long): Flow<List<Task>>
    suspend fun addTask(task: Task): Result<Unit>
    suspend fun updateTask(task: Task): Result<Unit>
    suspend fun deleteTask(taskId: String): Result<Unit>
    suspend fun toggleTaskCompletion(taskId: String): Result<Unit>
    suspend fun syncPendingChanges(): Result<Unit>
    suspend fun refreshTasks(): Result<Unit>
}

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val taskDao: TaskDao,
    @ApplicationContext private val context: Context
) : TaskRepository {

    companion object {
        private const val TAG = "TaskRepository"
    }

    private val tasksCollection
        get() = auth.currentUser?.uid?.let { uid ->
            firestore.collection("users").document(uid).collection("tasks")
        }

    override fun getTasksForDate(dateMillis: Long): Flow<List<Task>> {
        return taskDao.getAllTasks()
    }

    override suspend fun addTask(task: Task): Result<Unit> {
        return try {
            // 1. Save un-synced to Local DB
            taskDao.insertTask(task.copy(isSynced = false))
            
            // 2. Schedule Sync
            scheduleSync()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTask(task: Task): Result<Unit> {
        return try {
             // 1. Save un-synced to Local DB
            taskDao.updateTask(task.copy(isSynced = false))
            
            // 2. Schedule Sync
            scheduleSync()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteTask(taskId: String): Result<Unit> {
        return try {
            // Optimization: If it was never synced, just delete local.
            // But for simplicity in this offline-first pattern, we mark as hidden or keep a "deleted_items" table.
            // Since we don't have soft-delete logic yet, we will just delete local and TRY to delete remote via Worker if possible,
            // OR strict offline pattern: We need a "DeletedTasks" table to track deletions if offline.
            // FOR NOW: We will do best-effort immediate delete + sync, but ideally we need soft delete.
            // Let's implement immediate local delete and queue a specific "Delete" work or just fire-and-forget for deletion 
            // since our Worker logic below only handles "Add/Update" (fetching unsynced items).
            
            // Correct Offline-First Deletion: Soft delete (isDeleted=true) -> Worker finds hidden items -> Deletes from server -> Deletes from local.
            // Given constraints, we'll assume soft-delete isn't in scope yet, so we stick to fire-and-forget for deletion
            // BUT we can still delete locally immediately for UI responsiveness.
            
            taskDao.deleteTask(taskId)
            
            // Fire-and-forget immediate attempt used previously is still valid for deletion for now
            // until we add soft-delete columns.
             try {
                tasksCollection?.document(taskId)?.delete()
            } catch (e: Exception) {
                 Log.e(TAG, "Failed to delete remote task $taskId", e)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleTaskCompletion(taskId: String): Result<Unit> {
        return try {
            val task = taskDao.getTaskById(taskId)
            if (task != null) {
                // Toggle and mark unsynced
                val updatedTask = task.copy(isCompleted = !task.isCompleted, isSynced = false)
                taskDao.updateTask(updatedTask)
                scheduleSync()
                Result.success(Unit)
            } else {
                Result.failure(Exception("Task not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncPendingChanges(): Result<Unit> {
        return try {
            val unsyncedTasks = taskDao.getUnsyncedTasks()
            if (unsyncedTasks.isNotEmpty()) {
                val collection = tasksCollection ?: return Result.failure(Exception("No user"))
                
                unsyncedTasks.forEach { task ->
                    // Upload
                    collection.document(task.id).set(task.copy(isSynced = true)).await() // Save as synced on server
                    // Update Local
                    taskDao.updateSyncStatus(task.id, true)
                }
                Log.d(TAG, "Synced ${unsyncedTasks.size} tasks")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed", e)
            Result.failure(e)
        }
    }

    override suspend fun refreshTasks(): Result<Unit> {
        return try {
            val collection = tasksCollection ?: return Result.failure(Exception("No user"))
            val snapshot = collection.get().await()
            val tasks = snapshot.toObjects(Task::class.java)
            
            tasks.forEach { task ->
                // Insert remote task, marking it as synced explicitly
                taskDao.insertTask(task.copy(isSynced = true))
            }
            Log.d(TAG, "Refreshed ${tasks.size} tasks from cloud")
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
