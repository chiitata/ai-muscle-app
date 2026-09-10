package com.example.aimuscle.presentation.viewmodels

import com.example.aimuscle.data.models.WorkoutExercise
import com.example.aimuscle.data.models.WorkoutSession
import com.example.aimuscle.data.repository.WorkoutRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import org.mockito.kotlin.verify
import java.time.LocalDate
import java.time.LocalDateTime

class HistoryViewModelTest {

    @Mock
    private lateinit var mockRepository: WorkoutRepository

    private lateinit var historyViewModel: HistoryViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testLoadAllSessionsSuccess() = runBlocking {
        // Arrange
        val mockSessions = listOf(
            WorkoutSession(1, LocalDate.now(), "Upper Body", null, LocalDateTime.now()),
            WorkoutSession(2, LocalDate.now(), "Lower Body", null, LocalDateTime.now())
        )
        whenever(mockRepository.getAllSessions()).thenReturn(mockSessions)

        // Act
        historyViewModel = HistoryViewModel(mockRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val sessions = historyViewModel.allSessions.first()
        assert(sessions.size == 2)
        assert(sessions[0].menuName == "Upper Body")
        assert(!historyViewModel.isLoading.first())
    }

    @Test
    fun testLoadSessionsByDate() = runBlocking {
        // Arrange
        val date = LocalDate.now()
        val mockSessions = listOf(
            WorkoutSession(1, date, "Morning Workout", null, LocalDateTime.now())
        )
        whenever(mockRepository.getAllSessions()).thenReturn(emptyList())
        whenever(mockRepository.getSessionsByDate(date)).thenReturn(mockSessions)

        historyViewModel = HistoryViewModel(mockRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Act
        historyViewModel.loadSessionsByDate(date)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val sessions = historyViewModel.sessionsByDate.first()
        assert(sessions.size == 1)
        assert(sessions[0].menuName == "Morning Workout")
    }

    @Test
    fun testLoadSessionExercises() = runBlocking {
        // Arrange
        val mockSessions = listOf(
            WorkoutSession(1, LocalDate.now(), "Upper", null, LocalDateTime.now())
        )
        val mockExercises = listOf(
            WorkoutExercise(1, 1, "Bench Press", 3, 8, 185.0, null, 0),
            WorkoutExercise(2, 1, "Incline Press", 3, 10, 155.0, null, 1)
        )
        whenever(mockRepository.getAllSessions()).thenReturn(mockSessions)
        whenever(mockRepository.getExercisesBySession(1)).thenReturn(mockExercises)

        historyViewModel = HistoryViewModel(mockRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Act
        historyViewModel.loadSessionExercises(1)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val exercises = historyViewModel.selectedSessionExercises.first()
        assert(exercises.size == 2)
        assert(exercises[0].name == "Bench Press")
    }

    @Test
    fun testDeleteSessionSuccess() = runBlocking {
        // Arrange
        val mockSessions = listOf(
            WorkoutSession(1, LocalDate.now(), "Upper", null, LocalDateTime.now()),
            WorkoutSession(2, LocalDate.now(), "Lower", null, LocalDateTime.now())
        )
        whenever(mockRepository.getAllSessions()).thenReturn(mockSessions)
        whenever(mockRepository.deleteSession(1)).then { }

        historyViewModel = HistoryViewModel(mockRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Act
        historyViewModel.deleteSession(1)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        assert(historyViewModel.deleteSuccess.first())
        verify(mockRepository).deleteSession(1)
    }

    @Test
    fun testClearSelectedSession() = runBlocking {
        // Arrange
        val mockSessions = listOf(
            WorkoutSession(1, LocalDate.now(), "Upper", null, LocalDateTime.now())
        )
        val mockExercises = listOf(
            WorkoutExercise(1, 1, "Bench Press", 3, 8, 185.0, null, 0)
        )
        whenever(mockRepository.getAllSessions()).thenReturn(mockSessions)
        whenever(mockRepository.getExercisesBySession(1)).thenReturn(mockExercises)

        historyViewModel = HistoryViewModel(mockRepository)
        testDispatcher.scheduler.advanceUntilIdle()
        historyViewModel.loadSessionExercises(1)
        testDispatcher.scheduler.advanceUntilIdle()

        // Act
        historyViewModel.clearSelectedSession()

        // Assert
        assert(historyViewModel.selectedSessionExercises.first().isEmpty())
    }

    @Test
    fun testGetSessionById() = runBlocking {
        // Arrange
        val mockSession = WorkoutSession(1, LocalDate.now(), "Test", null, LocalDateTime.now())
        whenever(mockRepository.getSessionById(1)).thenReturn(mockSession)

        historyViewModel = HistoryViewModel(mockRepository)

        // Act
        val session = historyViewModel.getSessionById(1)

        // Assert
        assert(session != null)
        assert(session?.menuName == "Test")
    }

    @Test
    fun testClearError() = runBlocking {
        // Arrange
        whenever(mockRepository.getAllSessions()).thenThrow(RuntimeException("Error"))
        historyViewModel = HistoryViewModel(mockRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Act
        historyViewModel.clearError()

        // Assert
        assert(historyViewModel.error.first() == null)
    }

    @Test
    fun testRefreshSessions() = runBlocking {
        // Arrange
        val mockSessions = listOf(
            WorkoutSession(1, LocalDate.now(), "Upper", null, LocalDateTime.now())
        )
        whenever(mockRepository.getAllSessions()).thenReturn(mockSessions)

        historyViewModel = HistoryViewModel(mockRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Act
        historyViewModel.refreshSessions()
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val sessions = historyViewModel.allSessions.first()
        assert(sessions.size == 1)
    }
}
