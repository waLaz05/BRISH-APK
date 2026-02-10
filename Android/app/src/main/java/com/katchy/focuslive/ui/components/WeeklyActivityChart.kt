package com.katchy.focuslive.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WeeklyActivityChart(
    data: List<Float>, // Values from 0f to 1f (normalized)
    labels: List<String> = listOf("L", "M", "M", "J", "V", "S", "D"),
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    title: String = "Actividad Semanal",
    onDetailClick: () -> Unit = {}
) {
    var animationPlayed by remember { mutableStateOf(false) }
    
    // Animate bars on load
    val animateHeight by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "barHeight"
    )

    LaunchedEffect(Unit) {
        animationPlayed = true
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp // Subtle shadow
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = onDetailClick) {
                    Text(
                        text = "Ver detalles",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chart Area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                data.forEachIndexed { index, value ->
                    val label = labels.getOrElse(index) { "" }
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Bar Canvas
                        Canvas(
                            modifier = Modifier
                                .width(24.dp) // Width of the pill
                                .weight(1f) // Takes up remaining vertical space
                        ) {
                            val barWidth = size.width
                            val barHeight = size.height
                            val cornerRadius = CornerRadius(barWidth / 2) // Fully rounded

                            // 1. Draw Track (Background Pill)
                            drawRoundRect(
                                color = trackColor,
                                topLeft = Offset(0f, 0f),
                                size = size,
                                cornerRadius = cornerRadius
                            )

                            // 2. Draw Active Bar (Foreground)
                            // Calculate height based on value (0..1)
                            val filledHeight = barHeight * value * animateHeight
                            val topOffset = barHeight - filledHeight
                            
                            // Prevent drawing if value is 0 (or very small) to avoid artifacts
                            if (value > 0.05f) {
                                drawRoundRect(
                                    color = barColor,
                                    topLeft = Offset(0f, topOffset),
                                    size = Size(barWidth, filledHeight),
                                    cornerRadius = cornerRadius
                                )
                            } else if (value > 0f) {
                                // Draw a small circle at bottom if value is tiny but > 0
                                drawCircle(
                                    color = barColor,
                                    radius = barWidth/2,
                                    center = Offset(barWidth/2, barHeight - barWidth/2)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
