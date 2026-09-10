package com.example.aimuscle.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.aimuscle.data.models.WorkoutSession
import com.example.aimuscle.data.models.WorkoutExercise
import com.example.aimuscle.data.models.ExerciseTemplate
import com.example.aimuscle.data.models.TemplateExercise

@Database(
    entities = [
        WorkoutSession::class,
        WorkoutExercise::class,
        ExerciseTemplate::class,
        TemplateExercise::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(LocalDateTimeConverter::class, LocalDateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutSessionDao(): WorkoutSessionDao
    abstract fun workoutExerciseDao(): WorkoutExerciseDao
    abstract fun exerciseTemplateDao(): ExerciseTemplateDao
    abstract fun templateExerciseDao(): TemplateExerciseDao
}
