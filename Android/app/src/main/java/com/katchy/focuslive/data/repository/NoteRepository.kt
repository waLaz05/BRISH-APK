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
import com.katchy.focuslive.data.local.dao.NoteDao
import com.katchy.focuslive.data.model.Note
import com.katchy.focuslive.data.worker.SyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface NoteRepository {
    fun getNotes(): Flow<List<Note>>
    suspend fun addNote(note: Note): Result<Unit>
    suspend fun updateNote(note: Note): Result<Unit>
    suspend fun deleteNote(noteId: String): Result<Unit>
    suspend fun syncPendingChanges(): Result<Unit>
    suspend fun refreshNotes(): Result<Unit>
    
    // Categories
    fun getCategories(): Flow<List<com.katchy.focuslive.data.model.NoteCategory>>
    suspend fun addCategory(category: com.katchy.focuslive.data.model.NoteCategory)
    suspend fun updateCategory(category: com.katchy.focuslive.data.model.NoteCategory)
    suspend fun deleteCategory(category: com.katchy.focuslive.data.model.NoteCategory)
}

@Singleton
class NoteRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val noteDao: NoteDao,
    private val noteCategoryDao: com.katchy.focuslive.data.local.dao.NoteCategoryDao,
    @ApplicationContext private val context: Context
) : NoteRepository {

    companion object {
        private const val TAG = "NoteRepository"
    }

    private val notesCollection
        get() = auth.currentUser?.uid?.let { uid ->
            firestore.collection("users").document(uid).collection("notes")
        }
        
    // --- Categories ---
    override fun getCategories(): Flow<List<com.katchy.focuslive.data.model.NoteCategory>> {
        return noteCategoryDao.getAllCategories()
    }
    
    override suspend fun addCategory(category: com.katchy.focuslive.data.model.NoteCategory) {
        noteCategoryDao.insertCategory(category)
    }
    
    override suspend fun updateCategory(category: com.katchy.focuslive.data.model.NoteCategory) {
        noteCategoryDao.updateCategory(category)
    }
    
    override suspend fun deleteCategory(category: com.katchy.focuslive.data.model.NoteCategory) {
        noteCategoryDao.deleteCategory(category)
        // Set notes in this category to null categoryId
        noteCategoryDao.detachNotesFromCategory(category.id)
    }

    override fun getNotes(): Flow<List<Note>> {
        return noteDao.getAllNotes()
    }

    override suspend fun addNote(note: Note): Result<Unit> {
        return try {
            noteDao.insertNote(note.copy(isSynced = false))
            scheduleSync()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateNote(note: Note): Result<Unit> {
        return try {
            noteDao.updateNote(note.copy(isSynced = false))
            scheduleSync()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteNote(noteId: String): Result<Unit> {
        return try {
            noteDao.deleteNote(noteId)
            // Fire-and-forget for deletion
            try {
                notesCollection?.document(noteId)?.delete()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete remote note $noteId", e)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncPendingChanges(): Result<Unit> {
        return try {
            val unsynced = noteDao.getUnsyncedNotes()
            if (unsynced.isNotEmpty()) {
                val collection = notesCollection ?: return Result.failure(Exception("No user"))
                unsynced.forEach { item ->
                    val docRef = collection.document(item.id)
                    docRef.set(item.copy(isSynced = true)).await()
                    noteDao.updateSyncStatus(item.id, true)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun refreshNotes(): Result<Unit> {
        return try {
            val collection = notesCollection ?: return Result.failure(Exception("No user"))
            val snapshot = collection.get().await()
            val notes = snapshot.toObjects(Note::class.java)
            
            notes.forEach { note ->
                // Insert remote note, marking it as synced explicitly
                noteDao.insertNote(note.copy(isSynced = true))
            }
            Log.d(TAG, "Refreshed ${notes.size} notes from cloud")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Refresh Notes failed", e)
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
