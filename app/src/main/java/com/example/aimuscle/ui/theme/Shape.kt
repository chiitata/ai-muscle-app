package com.example.aimuscle.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Material 3 Shape Configuration for AIMuscle
 *
 * Defines rounded corner radius for different component sizes:
 * - Small: 4dp - Buttons, small components
 * - Medium: 8dp - Cards, chips
 * - Large: 12dp - Dialogs, sheets, large components
 */
val AIMuscleShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(12.dp),
    extraLarge = RoundedCornerShape(16.dp)
)
