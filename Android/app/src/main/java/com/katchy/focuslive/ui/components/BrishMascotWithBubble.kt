package com.katchy.focuslive.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.katchy.focuslive.ui.viewmodel.BrishViewModel
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun BrishMascotWithBubble(
    modifier: Modifier = Modifier,
    viewModel: BrishViewModel = hiltViewModel(),
    pose: BrishPose = BrishPose.DEFAULT,
    mascotType: com.katchy.focuslive.data.model.MascotType = com.katchy.focuslive.data.model.MascotType.POPPIN,
    showBubble: Boolean = true,
    level: Int = 1
) {
    val quote by viewModel.currentQuote.collectAsState()
    val isVisible by viewModel.isMascotVisible.collectAsState()
    
    val floatAnim = run {
        val infiniteTransition = rememberInfiniteTransition(label = "floating")
        val animValue by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 8f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "float" 
        )
        animValue
    }


    // Notify ViewModel of the current context/pose when this composable enters composition
    LaunchedEffect(pose) {
        viewModel.updateContext(pose)
    }

    if (isVisible) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp).graphicsLayer { translationY = floatAnim },
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center
        ) {
            // Speech Bubble (Left side)
            if (showBubble) {
                // Dynamic Colors for Dark/Light Mode
                // Detect Dark Mode from Theme (not only system) using luminance
                val isDark = MaterialTheme.colorScheme.background.let { (it.red * 0.2126f + it.green * 0.7152f + it.blue * 0.0722f) < 0.5f }
                val bubbleColor = if (isDark) Color(0xFF1F2937).copy(alpha = 0.95f) else Color.White
                val textColor = if (isDark) Color(0xFFE5E7EB) else Color.Black
                val shadowElevation = if (isDark) 0.dp else 4.dp 
                val borderStroke = if (isDark) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF374151)) else null

                val shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 0.dp)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                        .shadow(shadowElevation, shape)
                        .clip(shape)
                        .graphicsLayer {
                            // Any frame-by-frame anim here
                        }
                        .then(if (borderStroke != null) Modifier.border(borderStroke, shape) else Modifier)
                        .background(bubbleColor, shape)
                        .clickable { viewModel.refreshQuote() }
                        .padding(16.dp)
                ) {
                    Text(
                        text = quote,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = textColor
                    )
                }
            } else {
                 Spacer(modifier = Modifier.weight(1f))
            }

            // Mascot (Right side)
            BrishMascotAnimation(
                modifier = Modifier.size(80.dp, 100.dp),
                pose = pose,
                mascotType = mascotType,
                level = level
            )
        }
    }
}
