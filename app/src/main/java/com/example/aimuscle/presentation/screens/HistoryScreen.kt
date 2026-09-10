package com.example.aimuscle.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.aimuscle.data.models.WorkoutSession
import com.example.aimuscle.presentation.viewmodels.HistoryViewModel
import java.time.format.DateTimeFormatter

/**
 * HistoryScreen - Session list with stats
 *
 * Displays a history of completed workout sessions with summary statistics.
 * Users can view details, delete sessions, and analyze past workouts.
 *
 * Features:
 * - LazyColumn list of sessions
 * - Session stats (date, name, exercise count)
 * - Delete button per session
 * - Loading and error states
 * - Total workouts summary
 * - Empty state message
 *
 * @param historyViewModel The ViewModel managing history state
 * @param onBack Callback when user presses back
 * @param onSelectSession Callback when user selects a session
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    historyViewModel: HistoryViewModel,
    onBack: () -> Unit,
    onSelectSession: (Long) -> Unit = {}
) {
    val sessions = historyViewModel.allSessions.collectAsState()
    val isLoading = historyViewModel.isLoading.collectAsState()
    val error = historyViewModel.error.collectAsState()
    val deleteSuccess = historyViewModel.deleteSuccess.collectAsState()
    val isDeleting = historyViewModel.isDeleting.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workout History") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading.value -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                error.value != null -> {
                    ErrorMessageWithRetry(
                        message = error.value ?: "Unknown error",
                        onRetry = { historyViewModel.loadAllSessions() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                sessions.value.isEmpty() -> {
                    EmptyStateMessage(
                        message = "No workout history",
                        subMessage = "Start by creating your first workout session",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    SessionsListView(
                        sessions = sessions.value,
                        isDeleting = isDeleting.value,
                        onSelectSession = onSelectSession,
                        onDeleteSession = { historyViewModel.deleteSession(it) }
                    )
                }
            }

            if (deleteSuccess.value) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Session deleted successfully",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun SessionsListView(
    sessions: List<WorkoutSession>,
    isDeleting: Boolean,
    onSelectSession: (Long) -> Unit,
    onDeleteSession: (Long) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Stats Header
        StatsSummary(sessions = sessions)

        // Sessions List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sessions) { session ->
                SessionCard(
                    session = session,
                    isDeleting = isDeleting,
                    onSelect = { onSelectSession(session.id) },
                    onDelete = { onDeleteSession(session.id) }
                )
            }
        }
    }
}

@Composable
private fun StatsSummary(sessions: List<WorkoutSession>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                label = "Total Workouts",
                value = sessions.size.toString()
            )
            StatItem(
                label = "This Week",
                value = sessions
                    .filter {
                        val daysSince = java.time.temporal.ChronoUnit.DAYS
                            .between(it.date, java.time.LocalDate.now())
                        daysSince in 0..7
                    }
                    .size
                    .toString()
            )
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun SessionCard(
    session: WorkoutSession,
    isDeleting: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.menuName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = session.date.format(dateFormatter),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!session.notes.isNullOrEmpty()) {
                    Text(
                        text = session.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Row {
                Button(
                    onClick = onSelect,
                    enabled = !isDeleting
                ) {
                    Text("Details")
                }
                IconButton(
                    onClick = onDelete,
                    enabled = !isDeleting
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
