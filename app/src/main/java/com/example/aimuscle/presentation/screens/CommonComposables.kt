package com.example.aimuscle.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Common reusable composables shared across all screens
 */

/**
 * Error message with retry button
 *
 * @param message Error message to display
 * @param onRetry Callback when user taps retry
 * @param modifier Modifier for layout
 */
@Composable
fun ErrorMessageWithRetry(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(0.8f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Error",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

/**
 * Empty state message for when no data is available
 *
 * @param message Main empty state message
 * @param subMessage Optional sub-message for additional context
 * @param modifier Modifier for layout
 */
@Composable
fun EmptyStateMessage(
    message: String,
    subMessage: String = "",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(0.8f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (subMessage.isNotEmpty()) {
            Text(
                text = subMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
