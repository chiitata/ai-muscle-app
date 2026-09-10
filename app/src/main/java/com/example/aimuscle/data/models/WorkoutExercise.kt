package com.example.aimuscle.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "workout_exercise",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class WorkoutExercise(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val name: String,               // 例：「ベンチプレス」
    val sets: Int,
    val reps: Int,
    val weight: Double? = null,     // kg, オプション
    val notes: String? = null,
    val order: Int                  // 表示順序
)
