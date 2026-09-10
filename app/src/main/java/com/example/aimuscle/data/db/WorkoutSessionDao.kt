package com.example.aimuscle.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.aimuscle.data.models.WorkoutSession
import java.time.LocalDate

@Dao
interface WorkoutSessionDao {
    @Insert
    suspend fun insert(session: WorkoutSession): Long

    @Update
    suspend fun update(session: WorkoutSession)

    @Delete
    suspend fun delete(session: WorkoutSession)

    @Query("SELECT * FROM workout_session WHERE id = :id")
    suspend fun getById(id: Long): WorkoutSession?

    @Query("SELECT * FROM workout_session ORDER BY date DESC")
    suspend fun getAllSessions(): List<WorkoutSession>

    @Query("SELECT * FROM workout_session WHERE date = :date")
    suspend fun getByDate(date: LocalDate): List<WorkoutSession>

    @Query("SELECT * FROM workout_session WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    suspend fun getInDateRange(startDate: LocalDate, endDate: LocalDate): List<WorkoutSession>
}
