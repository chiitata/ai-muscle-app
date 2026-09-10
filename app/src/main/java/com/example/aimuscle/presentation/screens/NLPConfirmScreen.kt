package com.example.aimuscle.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import com.example.aimuscle.domain.models.ParsedExercise
import com.example.aimuscle.presentation.viewmodels.NLPConfirmViewModel

/**
 * NLPConfirmScreen - Exercise list with edit and save
 *
 * Displays parsed exercises from NLP with options to edit or remove them.
 * Users can review parsed data, make corrections, and save as a session.
 *
 * Features:
 * - LazyColumn list of parsed exercises
 * - Edit/Delete buttons for each exercise
 * - Inline edit dialog
 * - Session name and notes input
 * - Save button with loading state
 * - Error and success messages
 *
 * @param nlpConfirmViewModel The ViewModel managing parse state
 * @param onBack Callback when user presses back
 * @param onSaveSuccess Callback after successful save
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NLPConfirmScreen(
    nlpConfirmViewModel: NLPConfirmViewModel,
    onBack: () -> Unit,
    onSaveSuccess: () -> Unit = {}
) {
    val parsedExercises = nlpConfirmViewModel.parsedExercises.collectAsState()
    val isParsing = nlpConfirmViewModel.isParsing.collectAsState()
    val parseError = nlpConfirmViewModel.parseError.collectAsState()
    val isSaving = nlpConfirmViewModel.isSaving.collectAsState()
    val saveError = nlpConfirmViewModel.saveError.collectAsState()
    val saveSuccess = nlpConfirmViewModel.saveSuccess.collectAsState()

    val sessionName = remember { mutableStateOf("") }
    val sessionNotes = remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val editingIndex = remember { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Confirm Exercises") },
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isParsing.value -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                parseError.value != null -> {
                    ErrorMessageWithRetry(
                        message = parseError.value ?: "Parse error",
                        onRetry = { nlpConfirmViewModel.clearErrors() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                parsedExercises.value.isEmpty() -> {
                    EmptyStateMessage(
                        message = "No exercises parsed",
                        subMessage = "Go back and enter your workout details",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(parsedExercises.value) { index, exercise ->
                                ExerciseEditCard(
                                    exercise = exercise,
                                    index = index,
                                    isEditing = editingIndex.value == index,
                                    onEdit = { editingIndex.value = index },
                                    onDelete = { nlpConfirmViewModel.removeExercise(index) },
                                    onSaveEdit = { updatedEx ->
                                        nlpConfirmViewModel.updateExercise(
                                            index,
                                            updatedEx.name,
                                            updatedEx.sets,
                                            updatedEx.reps,
                                            updatedEx.weight
                                        )
                                        editingIndex.value = null
                                    }
                                )
                            }
                        }

                        // Session Info Section
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = sessionName.value,
                                onValueChange = { sessionName.value = it },
                                label = { Text("Session Name") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = sessionNotes.value,
                                onValueChange = { sessionNotes.value = it },
                                label = { Text("Notes (Optional)") },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 3
                            )

                            if (saveError.value != null) {
                                Text(
                                    text = saveError.value ?: "",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            Button(
                                onClick = {
                                    nlpConfirmViewModel.saveSession(
                                        sessionName.value.ifBlank { "Workout Session" },
                                        sessionNotes.value.ifBlank { null }
                                    )
                                },
                                enabled = !isSaving.value && sessionName.value.isNotBlank(),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (isSaving.value) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.padding(end = 8.dp),
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                                Text("Save Session")
                            }
                        }
                    }
                }
            }

            if (saveSuccess.value) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Session saved successfully!",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun ExerciseEditCard(
    exercise: ParsedExercise,
    index: Int,
    isEditing: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSaveEdit: (ParsedExercise) -> Unit
) {
    val editName = remember { mutableStateOf(exercise.name) }
    val editSets = remember { mutableStateOf(exercise.sets.toString()) }
    val editReps = remember { mutableStateOf(exercise.reps.toString()) }
    val editWeight = remember { mutableStateOf(exercise.weight?.toString() ?: "") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        if (!isEditing) {
            // Display View
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exercise.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${exercise.sets}x${exercise.reps}" +
                                (exercise.weight?.let { " @ ${it}kg" } ?: ""),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit"
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        } else {
            // Edit View
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = editName.value,
                    onValueChange = { editName.value = it },
                    label = { Text("Exercise Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = editSets.value,
                        onValueChange = { editSets.value = it },
                        label = { Text("Sets") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editReps.value,
                        onValueChange = { editReps.value = it },
                        label = { Text("Reps") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editWeight.value,
                        onValueChange = { editWeight.value = it },
                        label = { Text("Weight (kg)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            onSaveEdit(
                                exercise.copy(
                                    name = editName.value,
                                    sets = editSets.value.toIntOrNull() ?: 1,
                                    reps = editReps.value.toIntOrNull() ?: 1,
                                    weight = editWeight.value.toDoubleOrNull()
                                )
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save")
                    }
                    Button(
                        onClick = { /* Exit edit mode */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Cancel")
                    }
                }
            }
        }
    }
}
