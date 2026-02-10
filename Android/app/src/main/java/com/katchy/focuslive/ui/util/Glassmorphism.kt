package com.katchy.focuslive.ui.util

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A safe, high-performance "Glossy" effect that simulates glass
 * without using expensive or risky RenderEffect/Blur APIs.
 * This guarantees 0 crashes and a consistent Premium look on all devices.
 */
fun Modifier.glassmorphism(
    shape: Shape = RoundedCornerShape(16.dp),
    backgroundColor: Color? = null, // Defaults to NeoSecondaryBase
    borderColor: Color? = null,     // Defaults to NeoBorder
    borderWidth: Dp = 1.dp,
    blurRadius: Dp = 0.dp // Ignored in this safe version, kept for API compatibility
): Modifier = composed {
    // Default to a dark, rich glass base if not provided
    val baseColor = backgroundColor ?: Color(0xFF1E1E1E).copy(alpha = 0.90f)
    val borderCol = borderColor ?: Color(0xFF333333)

    this
        .clip(shape)
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    baseColor.copy(alpha = 0.85f), // Top: slightly more transparent
                    baseColor.copy(alpha = 0.98f)  // Bottom: almost solid
                )
            ),
            shape = shape
        )
        // Add a "Glossy Reflection" overlay
        .background(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.05f), // Subtle shine at top-left
                    Color.Transparent,
                    Color.Transparent
                )
            ),
            shape = shape
        )
        .border(
            width = borderWidth,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.15f), // Top border highlight (Light catch)
                    borderCol.copy(alpha = 0.5f)     // Bottom border shadow
                )
            ),
            shape = shape
        )
}
