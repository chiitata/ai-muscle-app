package com.example.aimuscle.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.aimuscle.data.models.TemplateExercise

@Dao
interface TemplateExerciseDao {
    @Insert
    suspend fun insert(exercise: TemplateExercise): Long

    @Update
    suspend fun update(exercise: TemplateExercise): Int

    @Delete
    suspend fun delete(exercise: TemplateExercise)

    @Query("SELECT * FROM template_exercise WHERE id = :id")
    suspend fun getById(id: Long): TemplateExercise?

    @Query("SELECT * FROM template_exercise WHERE templateId = :templateId ORDER BY `order`")
    suspend fun getByTemplateId(templateId: Long): List<TemplateExercise>

    @Query("DELETE FROM template_exercise WHERE templateId = :templateId")
    suspend fun deleteByTemplateId(templateId: Long)
}
