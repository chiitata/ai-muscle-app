package com.example.aimuscle.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.aimuscle.presentation.viewmodels.InputViewModel

/**
 * InputScreen - Text input and parse button
 *
 * Allows users to enter natural language workout descriptions
 * which will be parsed by the NLP engine.
 *
 * Features:
 * - Large text input field
 * - Input validation (non-empty check)
 * - Parse button (disabled when input is empty)
 * - Back button for navigation
 * - Character counter for user feedback
 *
 * @param inputViewModel The ViewModel managing input state
 * @param onBack Callback when user presses back
 * @param onParse Callback with parsed input text
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputScreen(
    inputViewModel: InputViewModel,
    onBack: () -> Unit,
    onParse: (String) -> Unit
) {
    val inputText = inputViewModel.inputText.collectAsState()
    val isInputValid = inputViewModel.isInputValid.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Enter Workout") },
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Instructions
                Text(
                    text = "Describe your workout in natural language",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Text Input Field
                OutlinedTextField(
                    value = inputText.value,
                    onValueChange = { inputViewModel.updateInputText(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    placeholder = {
                        Text("e.g., 10 pushups, 3x8 bench press 185 kg, 20 squats")
                    },
                    label = { Text("Workout Description") },
                    maxLines = 10
                )

                // Character counter
                Text(
                    text = "${inputText.value.length} characters",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.End)
                )

                // Parse Button
                Button(
                    onClick = {
                        if (isInputValid.value) {
                            onParse(inputText.value)
                        }
                    },
                    enabled = isInputValid.value,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text(
                        text = "Parse Workout",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
