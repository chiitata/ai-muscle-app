package com.example.aimuscle.di

import android.content.Context
import androidx.room.Room
import com.example.aimuscle.data.db.AppDatabase
import com.example.aimuscle.data.repository.WorkoutRepository
import com.example.aimuscle.nlp.NLPEngine

/**
 * AppModule - Singleton Dependency Injection container
 *
 * Manages lazy initialization of core application components:
 * - Room Database for workout persistence
 * - WorkoutRepository for data operations
 * - NLPEngine for natural language parsing
 *
 * Usage:
 *   AppModule.initialize(context)
 *   val database = AppModule.getDatabase()
 *   val repository = AppModule.getRepository()
 *   val nlpEngine = AppModule.getNLPEngine(context)
 */
object AppModule {

    private var _database: AppDatabase? = null
    private var _repository: WorkoutRepository? = null
    private var _nlpEngine: NLPEngine? = null

    /**
     * Initialize the database on app startup
     * Should be called once from Application.onCreate()
     */
    fun initializeDatabase(context: Context) {
        if (_database == null) {
            _database = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "aimuscle_database"
            ).build()
        }
    }

    /**
     * Get or create the Room Database instance
     * Requires database to be initialized first via initializeDatabase()
     */
    fun getDatabase(): AppDatabase {
        return _database ?: throw IllegalStateException(
            "Database not initialized. Call AppModule.initializeDatabase(context) first."
        )
    }

    /**
     * Get or create the WorkoutRepository instance
     * Lazily initializes the repository using the current database
     */
    fun getRepository(): WorkoutRepository {
        if (_repository == null) {
            val database = getDatabase()
            _repository = WorkoutRepository(
                sessionDao = database.workoutSessionDao(),
                exerciseDao = database.workoutExerciseDao(),
                templateDao = database.exerciseTemplateDao(),
                templateExerciseDao = database.templateExerciseDao()
            )
        }
        return _repository
    }

    /**
     * Get or create the NLPEngine instance
     * Lazily initializes the engine with optional TFLite model path from assets
     *
     * @param context Android context for accessing assets
     * @param modelPath Optional path to TFLite model in assets (e.g., "models/confidence_model.tflite")
     */
    fun getNLPEngine(context: Context, modelPath: String? = null): NLPEngine {
        if (_nlpEngine == null) {
            _nlpEngine = NLPEngine(tfliteModelPath = modelPath)
        }
        return _nlpEngine
    }

    /**
     * Clear all cached instances
     * Useful for testing or app shutdown
     */
    fun clear() {
        _nlpEngine?.close()
        _nlpEngine = null
        _repository = null
        _database = null
    }
}
