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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import com.example.aimuscle.data.models.ExerciseTemplate
import com.example.aimuscle.presentation.viewmodels.TemplateViewModel

/**
 * TemplateScreen - Template CRUD operations
 *
 * Allows users to view, create, and delete exercise templates.
 * Templates are predefined workout routines that users can quickly load.
 *
 * Features:
 * - LazyColumn list of templates
 * - Delete button for each template
 * - Create new template dialog
 * - Loading and error states
 * - Empty state message
 *
 * @param templateViewModel The ViewModel managing template state
 * @param onBack Callback when user presses back
 * @param onSelectTemplate Callback when user selects a template to view
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateScreen(
    templateViewModel: TemplateViewModel,
    onBack: () -> Unit,
    onSelectTemplate: (Long) -> Unit = {}
) {
    val templates = templateViewModel.allTemplates.collectAsState()
    val isLoading = templateViewModel.isLoading.collectAsState()
    val error = templateViewModel.error.collectAsState()
    val deleteSuccess = templateViewModel.deleteSuccess.collectAsState()
    val isDeleting = templateViewModel.isDeleting.collectAsState()

    val showCreateDialog = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Templates") },
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog.value = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Create Template"
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
                        onRetry = { templateViewModel.loadAllTemplates() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                templates.value.isEmpty() -> {
                    EmptyStateMessage(
                        message = "No templates",
                        subMessage = "Create your first template by tapping the + button",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    TemplatesListView(
                        templates = templates.value,
                        isDeleting = isDeleting.value,
                        onSelectTemplate = onSelectTemplate,
                        onDeleteTemplate = { templateViewModel.deleteTemplate(it) }
                    )
                }
            }
        }

        if (showCreateDialog.value) {
            CreateTemplateDialog(
                onDismiss = { showCreateDialog.value = false },
                onCreate = {
                    showCreateDialog.value = false
                    // Note: Actual creation would use parsed exercises
                    // This is a placeholder for the template creation flow
                }
            )
        }

        if (deleteSuccess.value) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Template deleted successfully",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun TemplatesListView(
    templates: List<ExerciseTemplate>,
    isDeleting: Boolean,
    onSelectTemplate: (Long) -> Unit,
    onDeleteTemplate: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(templates) { template ->
            TemplateItemCard(
                template = template,
                isDeleting = isDeleting,
                onSelect = { onSelectTemplate(template.id) },
                onDelete = { onDeleteTemplate(template.id) }
            )
        }
    }
}

@Composable
private fun TemplateItemCard(
    template: ExerciseTemplate,
    isDeleting: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
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
                    text = template.templateName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Created: ${template.createdAt.toLocalDate()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row {
                Button(
                    onClick = onSelect,
                    enabled = !isDeleting
                ) {
                    Text("View")
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

/**
 * Dialog for creating a new template
 *
 * @param onDismiss Callback when user dismisses dialog
 * @param onCreate Callback with template name when user creates
 */
@Composable
private fun CreateTemplateDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    val templateName = remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Create New Template",
                    style = MaterialTheme.typography.headlineSmall
                )

                OutlinedTextField(
                    value = templateName.value,
                    onValueChange = { templateName.value = it },
                    label = { Text("Template Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { onCreate(templateName.value) },
                        enabled = templateName.value.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
}
