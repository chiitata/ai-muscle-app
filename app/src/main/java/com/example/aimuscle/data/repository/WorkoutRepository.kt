package com.example.aimuscle.data.repository

import com.example.aimuscle.data.db.ExerciseTemplateDao
import com.example.aimuscle.data.db.TemplateExerciseDao
import com.example.aimuscle.data.db.WorkoutExerciseDao
import com.example.aimuscle.data.db.WorkoutSessionDao
import com.example.aimuscle.data.models.ExerciseTemplate
import com.example.aimuscle.data.models.TemplateExercise
import com.example.aimuscle.data.models.WorkoutExercise
import com.example.aimuscle.data.models.WorkoutSession
import com.example.aimuscle.domain.models.ParsedExercise
import java.time.LocalDate

class WorkoutRepository(
    private val sessionDao: WorkoutSessionDao,
    private val exerciseDao: WorkoutExerciseDao,
    private val templateDao: ExerciseTemplateDao,
    private val templateExerciseDao: TemplateExerciseDao
) {
    // Session operations
    suspend fun saveSession(session: WorkoutSession): Long {
        return sessionDao.insert(session)
    }

    suspend fun getSessionById(id: Long): WorkoutSession? {
        return sessionDao.getById(id)
    }

    suspend fun getAllSessions(): List<WorkoutSession> {
        return sessionDao.getAllSessions()
    }

    suspend fun getSessionsByDate(date: LocalDate): List<WorkoutSession> {
        return sessionDao.getByDate(date)
    }

    // Exercise operations
    suspend fun saveExercises(exercises: List<WorkoutExercise>) {
        exercises.forEach { exercise ->
            exerciseDao.insert(exercise)
        }
    }

    suspend fun getExercisesBySession(sessionId: Long): List<WorkoutExercise> {
        return exerciseDao.getBySessionId(sessionId)
    }

    suspend fun deleteSession(sessionId: Long) {
        exerciseDao.deleteBySessionId(sessionId)
        val session = sessionDao.getById(sessionId)
        if (session != null) {
            sessionDao.delete(session)
        }
    }

    // Template operations
    suspend fun saveTemplate(name: String, exercises: List<ParsedExercise>): Long {
        val template = ExerciseTemplate(templateName = name)
        val templateId = templateDao.insert(template)

        exercises.forEachIndexed { index, exercise ->
            val templateExercise = TemplateExercise(
                templateId = templateId,
                name = exercise.name,
                sets = exercise.sets,
                reps = exercise.reps,
                weight = exercise.weight,
                order = index
            )
            templateExerciseDao.insert(templateExercise)
        }

        return templateId
    }

    suspend fun getAllTemplates(): List<ExerciseTemplate> {
        return templateDao.getAllTemplates()
    }

    suspend fun getTemplateExercises(templateId: Long): List<TemplateExercise> {
        return templateExerciseDao.getByTemplateId(templateId)
    }

    suspend fun deleteTemplate(templateId: Long) {
        templateExerciseDao.deleteByTemplateId(templateId)
        templateDao.deleteById(templateId)
    }
}
