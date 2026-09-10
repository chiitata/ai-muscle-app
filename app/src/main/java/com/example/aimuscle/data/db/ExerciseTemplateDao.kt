package com.example.aimuscle.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.aimuscle.data.models.ExerciseTemplate

@Dao
interface ExerciseTemplateDao {
    @Insert
    suspend fun insert(template: ExerciseTemplate): Long

    @Update
    suspend fun update(template: ExerciseTemplate): Int

    @Delete
    suspend fun delete(template: ExerciseTemplate)

    @Query("SELECT * FROM exercise_template WHERE id = :id")
    suspend fun getById(id: Long): ExerciseTemplate?

    @Query("SELECT * FROM exercise_template ORDER BY createdAt DESC")
    suspend fun getAllTemplates(): List<ExerciseTemplate>

    @Query("DELETE FROM exercise_template WHERE id = :id")
    suspend fun deleteById(id: Long)
}
