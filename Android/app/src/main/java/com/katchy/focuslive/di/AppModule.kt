package com.katchy.focuslive.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.katchy.focuslive.BuildConfig
import com.katchy.focuslive.data.repository.AppPreferencesRepository
import com.katchy.focuslive.data.repository.AuthRepository
import com.katchy.focuslive.data.repository.AuthRepositoryImpl

import com.katchy.focuslive.data.repository.FinanceRepository
import com.katchy.focuslive.data.repository.FinanceRepositoryImpl
import com.katchy.focuslive.data.repository.GoalRepository
import com.katchy.focuslive.data.repository.GoalRepositoryImpl
import com.katchy.focuslive.data.repository.HabitRepository
import com.katchy.focuslive.data.repository.HabitRepositoryImpl
import com.katchy.focuslive.data.repository.NoteRepository
import com.katchy.focuslive.data.repository.NoteRepositoryImpl
import com.katchy.focuslive.data.repository.TaskRepository
import com.katchy.focuslive.data.repository.TaskRepositoryImpl
import com.katchy.focuslive.data.repository.ThemeRepository
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.katchy.focuslive.data.local.dao.TaskDao
import com.katchy.focuslive.data.local.dao.FinanceDao
import com.katchy.focuslive.data.local.dao.NoteDao
import com.katchy.focuslive.data.local.dao.HabitDao
import com.katchy.focuslive.data.local.BrishDatabase

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        val firestore = FirebaseFirestore.getInstance()
        val settings = com.google.firebase.firestore.FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(
                com.google.firebase.firestore.PersistentCacheSettings.newBuilder()
                    .setSizeBytes(com.google.firebase.firestore.FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                    .build()
            )
            .build()
        firestore.firestoreSettings = settings
        return firestore
    }

    @Provides
    @Singleton
    fun provideBrishDatabase(
        @ApplicationContext context: Context
    ): BrishDatabase {
        val MIGRATION_4_5 = object : androidx.room.migration.Migration(4, 5) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Add daysOfWeek column with default value (JSON array of 1..7)
                database.execSQL("ALTER TABLE habits ADD COLUMN daysOfWeek TEXT NOT NULL DEFAULT '[1,2,3,4,5,6,7]'")
            }
        }

        val MIGRATION_5_6 = object : androidx.room.migration.Migration(5, 6) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Create NoteCategory table
                database.execSQL("CREATE TABLE IF NOT EXISTS `note_categories` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `sortOrder` INTEGER NOT NULL, `isDefault` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                
                // Add categoryId to Notes
                database.execSQL("ALTER TABLE notes ADD COLUMN categoryId TEXT")
            }
        }

        return androidx.room.Room.databaseBuilder(
            context,
            BrishDatabase::class.java,
            "brish_database"
        )
        .addMigrations(MIGRATION_4_5, MIGRATION_5_6)
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideTaskDao(db: BrishDatabase) = db.taskDao()

    @Provides
    fun provideFinanceDao(db: BrishDatabase) = db.financeDao()

    @Provides
    fun provideNoteDao(db: BrishDatabase) = db.noteDao()

    @Provides
    fun provideNoteCategoryDao(db: BrishDatabase) = db.noteCategoryDao()

    @Provides
    fun provideHabitDao(db: BrishDatabase) = db.habitDao()

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): AuthRepository = AuthRepositoryImpl(firebaseAuth, firestore)

    @Provides
    @Singleton
    fun provideThemeRepository(
        @ApplicationContext context: Context
    ): ThemeRepository = ThemeRepository(context)

    @Provides
    @Singleton
    fun provideTaskRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        taskDao: TaskDao,
        @ApplicationContext context: Context
    ): TaskRepository = TaskRepositoryImpl(firestore, auth, taskDao, context)

    @Provides
    @Singleton
    fun provideGoalRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): GoalRepository = GoalRepositoryImpl(firestore, auth)

    @Provides
    @Singleton
    fun provideFinanceRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        financeDao: FinanceDao,
        @ApplicationContext context: Context,
        userStatsRepository: com.katchy.focuslive.data.repository.UserStatsRepository
    ): FinanceRepository {
        return com.katchy.focuslive.data.repository.FinanceRepositoryImpl(firestore, auth, financeDao, userStatsRepository, context)
    }

    @Provides
    @Singleton
    fun provideNoteRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        noteDao: NoteDao,
        noteCategoryDao: com.katchy.focuslive.data.local.dao.NoteCategoryDao,
        @ApplicationContext context: Context
    ): NoteRepository = NoteRepositoryImpl(firestore, auth, noteDao, noteCategoryDao, context)

    @Provides
    @Singleton
    fun provideAppPreferencesRepository(
        @ApplicationContext context: Context
    ): AppPreferencesRepository = AppPreferencesRepository(context)

    @Provides
    @Singleton
    fun provideGroqApi(): com.katchy.focuslive.data.api.GroqApi {
        val apiKey = BuildConfig.GROQ_API_KEY
        
        val authInterceptor = okhttp3.Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $apiKey")
                .build()
            chain.proceed(request)
        }

        val client = okhttp3.OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()

        return retrofit2.Retrofit.Builder()
            .baseUrl("https://api.groq.com/openai/v1/")
            .client(client)
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
            .create(com.katchy.focuslive.data.api.GroqApi::class.java)
    }

    @Provides
    @Singleton
    fun provideNetworkMonitor(
        @ApplicationContext context: Context
    ): com.katchy.focuslive.ui.util.NetworkMonitor = com.katchy.focuslive.ui.util.NetworkMonitor(context)



    @Provides
    @Singleton
    fun provideHabitRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        habitDao: HabitDao,
        @ApplicationContext context: Context
    ): HabitRepository = HabitRepositoryImpl(firestore, auth, habitDao, context)

    @Provides
    @Singleton
    fun provideTimerManager(
        appPreferencesRepository: AppPreferencesRepository
    ): com.katchy.focuslive.data.manager.TimerManager = com.katchy.focuslive.data.manager.TimerManager(appPreferencesRepository)

    @Provides
    @Singleton
    fun provideWidgetManager(
        @ApplicationContext context: Context
    ): com.katchy.focuslive.data.manager.WidgetManager = com.katchy.focuslive.data.manager.WidgetManagerImpl(context)
}
