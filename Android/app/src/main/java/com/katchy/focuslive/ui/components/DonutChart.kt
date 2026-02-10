package com.katchy.focuslive.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.katchy.focuslive.ui.theme.AntiPrimary

data class ChartData(
    val value: Float,
    val color: Color,
    val label: String
)

@Composable
fun DonutChart(
    data: List<ChartData>,
    modifier: Modifier = Modifier,
    chartSize: Dp = 200.dp,
    strokeWidth: Dp = 20.dp,
    centerContent: @Composable () -> Unit = {}
) {
    val total = data.sumOf { it.value.toDouble() }.toFloat()
    
    // Animation for drawing the chart
    val transition = updateTransition(targetState = data, label = "chartTransition")
    val progress by transition.animateFloat(
        label = "progress",
        transitionSpec = { tween(durationMillis = 1500, easing = FastOutSlowInEasing) }
    ) { _ -> 1f }

    Box(
        modifier = modifier.size(chartSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            val radius = size.minDimension / 2 - strokeWidth.toPx() / 2
            val center = Offset(size.width / 2, size.height / 2)
            
            var startAngle = -90f
            
            if(total == 0f) {
                // Empty state ring
                 drawArc(
                    color = Color(0xFFE5E7EB).copy(alpha = 0.3f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = stroke
                )
            } else {
                data.forEach { item ->
                    val sweepAngle = (item.value / total) * 360f * progress
                    
                    drawArc(
                        color = item.color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false, // Donut style
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = stroke
                    )
                    
                    startAngle += sweepAngle
                }
            }
        }
        
        // Center Content (e.g., Total Amount)
        centerContent()
    }
}

@Composable
fun ChartLegend(
    data: List<ChartData>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        data.forEach { item ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(item.color, androidx.compose.foundation.shape.CircleShape)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
