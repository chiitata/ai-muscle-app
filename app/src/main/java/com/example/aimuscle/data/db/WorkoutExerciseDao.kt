package com.example.aimuscle.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.aimuscle.data.models.WorkoutExercise

@Dao
interface WorkoutExerciseDao {
    @Insert
    suspend fun insert(exercise: WorkoutExercise): Long

    @Update
    suspend fun update(exercise: WorkoutExercise)

    @Delete
    suspend fun delete(exercise: WorkoutExercise)

    @Query("SELECT * FROM workout_exercise WHERE id = :id")
    suspend fun getById(id: Long): WorkoutExercise?

    @Query("SELECT * FROM workout_exercise WHERE sessionId = :sessionId ORDER BY `order`")
    suspend fun getBySessionId(sessionId: Long): List<WorkoutExercise>

    @Query("DELETE FROM workout_exercise WHERE sessionId = :sessionId")
    suspend fun deleteBySessionId(sessionId: Long)
}
