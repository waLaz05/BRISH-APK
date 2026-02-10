package com.katchy.focuslive.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.katchy.focuslive.data.model.NoteCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteCategoryDao {
    @Query("SELECT * FROM note_categories ORDER BY sortOrder ASC")
    fun getAllCategories(): Flow<List<NoteCategory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: NoteCategory)

    @Update
    suspend fun updateCategory(category: NoteCategory)

    @Delete
    suspend fun deleteCategory(category: NoteCategory)
    
    @Query("UPDATE notes SET categoryId = null WHERE categoryId = :categoryId")
    suspend fun detachNotesFromCategory(categoryId: String)
}
