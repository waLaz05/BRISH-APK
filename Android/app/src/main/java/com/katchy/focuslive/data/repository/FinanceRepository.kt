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
import com.katchy.focuslive.data.local.dao.FinanceDao
import com.katchy.focuslive.data.model.Transaction
import com.katchy.focuslive.data.worker.SyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface FinanceRepository {
    fun getTransactions(): Flow<List<Transaction>>
    suspend fun addTransaction(transaction: Transaction): Result<Unit>
    suspend fun updateTransaction(transaction: Transaction): Result<Unit>
    suspend fun deleteTransaction(transactionId: String): Result<Unit>
    suspend fun syncPendingChanges(): Result<Unit>
    suspend fun refreshTransactions(): Result<Unit>
}

@Singleton
class FinanceRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val financeDao: FinanceDao,
    private val userStatsRepository: UserStatsRepository,
    @ApplicationContext private val context: Context
) : FinanceRepository {

    companion object {
        private const val TAG = "FinanceRepository"
    }

    private val financeCollection
        get() = auth.currentUser?.uid?.let { uid ->
            firestore.collection("users").document(uid).collection("finance")
        }

    override fun getTransactions(): Flow<List<Transaction>> {
        return financeDao.getAllTransactions()
    }

    override suspend fun addTransaction(transaction: Transaction): Result<Unit> {
        return try {
            financeDao.insertTransaction(transaction.copy(isSynced = false))
            
            // Badge Check
            val count = financeDao.getTransactionCount()
            if (count >= 30) {
                 userStatsRepository.unlockBadge(com.katchy.focuslive.util.GamificationLogic.BADGE_FINANCIER)
            }
            
            scheduleSync()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTransaction(transaction: Transaction): Result<Unit> {
        return try {
            financeDao.updateTransaction(transaction.copy(isSynced = false))
            scheduleSync()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteTransaction(transactionId: String): Result<Unit> {
        return try {
            financeDao.deleteTransaction(transactionId)
            // Fire-and-forget for deletion (limit of current offline-delete strategy)
             try {
                financeCollection?.document(transactionId)?.delete()
            } catch (e: Exception) {
                 Log.e(TAG, "Failed to delete remote transaction $transactionId", e)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncPendingChanges(): Result<Unit> {
        return try {
            val unsynced = financeDao.getUnsyncedTransactions()
            if (unsynced.isNotEmpty()) {
                val collection = financeCollection ?: return Result.failure(Exception("No user"))
                unsynced.forEach { item ->
                    collection.document(item.id).set(item.copy(isSynced = true)).await()
                    financeDao.updateSyncStatus(item.id, true)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun refreshTransactions(): Result<Unit> {
        return try {
            val collection = financeCollection ?: return Result.failure(Exception("No user"))
            val snapshot = collection.get().await()
            val transactions = snapshot.toObjects(Transaction::class.java)
            
            transactions.forEach { trans ->
                financeDao.insertTransaction(trans.copy(isSynced = true))
            }
            Log.d(TAG, "Refreshed ${transactions.size} transactions from cloud")
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
