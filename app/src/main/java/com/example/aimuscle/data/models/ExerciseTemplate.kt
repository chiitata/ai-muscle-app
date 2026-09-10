package com.example.aimuscle.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "exercise_template")
data class ExerciseTemplate(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val templateName: String,       // 例：「月曜の上半身」
    val createdAt: LocalDateTime = LocalDateTime.now()
)
