package com.katchy.focuslive.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.katchy.focuslive.data.local.dao.FinanceDao
import com.katchy.focuslive.data.local.dao.HabitDao
import com.katchy.focuslive.data.local.dao.NoteDao
import com.katchy.focuslive.data.local.dao.TaskDao
import com.katchy.focuslive.data.model.Habit
import com.katchy.focuslive.data.model.Note
import com.katchy.focuslive.data.model.Task
import com.katchy.focuslive.data.model.Transaction

@Database(
    entities = [Task::class, Transaction::class, Note::class, Habit::class, com.katchy.focuslive.data.model.NoteCategory::class],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BrishDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun financeDao(): FinanceDao
    abstract fun noteDao(): NoteDao
    abstract fun habitDao(): HabitDao
    abstract fun noteCategoryDao(): com.katchy.focuslive.data.local.dao.NoteCategoryDao
}
