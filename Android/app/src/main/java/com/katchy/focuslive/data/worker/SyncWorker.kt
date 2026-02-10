package com.katchy.focuslive.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.katchy.focuslive.data.repository.FinanceRepository
import com.katchy.focuslive.data.repository.HabitRepository
import com.katchy.focuslive.data.repository.NoteRepository
import com.katchy.focuslive.data.repository.TaskRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val taskRepository: TaskRepository,
    private val financeRepository: FinanceRepository,
    private val noteRepository: NoteRepository,
    private val habitRepository: HabitRepository
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "SyncWorker"
    }

    override suspend fun doWork(): Result = coroutineScope {
        try {
            Log.d(TAG, "Starting synchronization...")

            // 1. Upload Pending Changes
            val taskUpload = async { taskRepository.syncPendingChanges() }
            val financeUpload = async { financeRepository.syncPendingChanges() }
            val noteUpload = async { noteRepository.syncPendingChanges() }
            val habitUpload = async { habitRepository.syncPendingChanges() }
            
            awaitAll(taskUpload, financeUpload, noteUpload, habitUpload)

            // 2. Download Latest Data (Refresh/Restore) which ensures local DB is up to date even after a wipe
            // We run this sequentially after upload to avoid overwriting newer local changes with older remote ones immediately (though our copy(isSynced=true) handles it basically)
            // Ideally we'd have timestamp checks, but for restoration purposes, pulling is key.
            val taskDownload = async { taskRepository.refreshTasks() }
            val financeDownload = async { financeRepository.refreshTransactions() }
            val noteDownload = async { noteRepository.refreshNotes() }
            val habitDownload = async { habitRepository.refreshHabits() }

            val downloadResults = awaitAll(taskDownload, financeDownload, noteDownload, habitDownload)
            
            val allSuccessful = downloadResults.all { it.isSuccess }

            if (allSuccessful) {
                Log.d(TAG, "Synchronization completed successfully.")
                Result.success()
            } else {
                Log.w(TAG, "Some synchronizations failed. Retrying...")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "SyncWorker failed", e)
            Result.retry()
        }
    }
}
