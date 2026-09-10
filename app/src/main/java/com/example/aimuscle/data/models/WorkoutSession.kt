package com.example.aimuscle.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "workout_session")
data class WorkoutSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: LocalDate,
    val menuName: String,           // 例：「月曜の上半身」
    val notes: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
