package com.example.aimuscle.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.aimuscle.data.models.ExerciseTemplate
import com.example.aimuscle.presentation.viewmodels.HomeViewModel

/**
 * HomeScreen - Display exercise templates and session start button
 *
 * Shows a list of available exercise templates loaded from the repository.
 * Users can see templates and start a new session by clicking the FAB.
 *
 * Features:
 * - LazyColumn list of templates
 * - Loading indicator while fetching
 * - Error display with retry capability
 * - FAB for starting a new session
 * - Pull-to-refresh functionality
 *
 * @param homeViewModel The ViewModel managing template state
 * @param onStartSession Callback when user wants to start a new session
 * @param onSelectTemplate Callback when user selects a template
 */
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    onStartSession: () -> Unit,
    onSelectTemplate: (ExerciseTemplate) -> Unit = {}
) {
    val templates = homeViewModel.templates.collectAsState()
    val isLoading = homeViewModel.isLoading.collectAsState()
    val error = homeViewModel.error.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onStartSession,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Start New Session"
                )
            }
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
                        onRetry = { homeViewModel.loadTemplates() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                templates.value.isEmpty() -> {
                    EmptyStateMessage(
                        message = "No templates available",
                        subMessage = "Create your first template by starting a session",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    TemplatesList(
                        templates = templates.value,
                        onSelectTemplate = onSelectTemplate
                    )
                }
            }
        }
    }
}

@Composable
private fun TemplatesList(
    templates: List<ExerciseTemplate>,
    onSelectTemplate: (ExerciseTemplate) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(templates) { template ->
            TemplateCard(
                template = template,
                onClick = { onSelectTemplate(template) }
            )
        }
    }
}

@Composable
private fun TemplateCard(
    template: ExerciseTemplate,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = template.templateName,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Created: ${template.createdAt.toLocalDate()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
            Button(
                onClick = onClick,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 12.dp)
            ) {
                Text("Use Template")
            }
        }
    }
}

