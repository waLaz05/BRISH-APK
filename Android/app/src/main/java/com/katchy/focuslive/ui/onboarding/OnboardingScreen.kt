package com.katchy.focuslive.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.katchy.focuslive.R
import com.katchy.focuslive.ui.theme.AntiPrimary
import com.katchy.focuslive.ui.theme.AntiTextPrimary
import com.katchy.focuslive.ui.theme.AntiTextSecondary
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import android.graphics.Paint
import android.graphics.BlurMaskFilter
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import kotlinx.coroutines.delay
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    var currentStep by remember { mutableStateOf(OnboardingStep.WELCOME) }
    var userName by remember { mutableStateOf("") }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF060B18), Color(0xFF0F172A))
                )
            )
    ) { 
        // Premium Background
        com.katchy.focuslive.ui.home.AnimatedFocusBackground(modifier = Modifier.fillMaxSize())
        
        // Polished Particles
        OnboardingParticles()

        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                if (targetState.ordinal > initialState.ordinal) {
                    (fadeIn(animationSpec = tween(800, easing = LinearOutSlowInEasing)) + slideInHorizontally(animationSpec = tween(800)) { it / 2 })
                        .togetherWith(fadeOut(animationSpec = tween(500)) + slideOutHorizontally(animationSpec = tween(500)) { -it / 2 })
                } else {
                    (fadeIn(animationSpec = tween(800, easing = LinearOutSlowInEasing)) + slideInHorizontally(animationSpec = tween(800)) { -it / 2 })
                        .togetherWith(fadeOut(animationSpec = tween(500)) + slideOutHorizontally(animationSpec = tween(500)) { it / 2 })
                }
            },
            label = "onboardingTransition",
            modifier = Modifier.fillMaxSize()
        ) { step ->
            when (step) {
                OnboardingStep.WELCOME -> WelcomeStep { currentStep = OnboardingStep.NAME }
                OnboardingStep.NAME -> NameStep(onNext = { name -> 
                    userName = name
                    viewModel.saveUserName(name)
                    currentStep = OnboardingStep.FINALE 
                })
                OnboardingStep.FINALE -> FinaleStep(onFinish)
            }
        }
    }
}

@Composable
fun OnboardingParticles() {
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // Pre-calculate random data for particles
    val particles = remember {
        List(20) {
            Particle(
                x = Math.random().toFloat(),
                y = Math.random().toFloat(),
                radius = (1..3).random().toFloat(),
                speed = (0.2f + Math.random().toFloat() * 0.8f) // Speed multiplier
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        
        particles.forEach { p ->
            // Move particles upward
            val offsetY = (p.y + time * p.speed) % 1f
            val yPos = h * (1f - offsetY) // Move up
            val xPos = w * p.x
            
            // Fade in/out based on position
            val alpha = 0.2f + 0.6f * kotlin.math.sin(offsetY * Math.PI).toFloat()

            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = p.radius.dp.toPx(),
                center = Offset(xPos, yPos)
            )
        }
    }
}

private data class Particle(
    val x: Float,
    val y: Float,
    val radius: Float,
    val speed: Float
)

enum class OnboardingStep { WELCOME, NAME, FINALE }

@Composable
private fun WelcomeStep(onNext: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "logoPulse")
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse),
            label = "scale"
        )

        Box(modifier = Modifier.size(160.dp).scale(scale), contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(id = R.drawable.ic_app_logo_vector),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "BIENVENIDO A\nBRISH",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            color = Color.White,
            letterSpacing = 4.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Tu sistema operativo de vida. Simple, potente y diseñado para ti.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = Color.White.copy(alpha = 0.7f),
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(64.dp))

        PremiumButton(text = "EMPEZAR", onClick = onNext)
    }
}

@Composable
private fun NameStep(onNext: (String) -> Unit) {
    var nameInput by remember { mutableStateOf("") }
    val fullText = "¡Hola! Soy Brish.\n¿Cómo te gustaría que te llame?"
    var displayedText by remember { mutableStateOf("") }
    var showCursor by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        fullText.forEachIndexed { index, _ ->
            displayedText = fullText.substring(0, index + 1)
            delay(40)
        }
    }
    
    LaunchedEffect(Unit) {
        while(true) {
            showCursor = !showCursor
            delay(500)
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp).imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
             Column(horizontalAlignment = Alignment.CenterHorizontally) {
                 com.katchy.focuslive.ui.components.BrishMascotAnimation(
                    modifier = Modifier.size(160.dp),
                    pose = com.katchy.focuslive.ui.components.BrishPose.DEFAULT
                )
                Spacer(modifier = Modifier.height(32.dp))
                
                // Chat Bubble Style
                Surface(
                    color = Color.White.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = buildAnnotatedString {
                            append(displayedText)
                            if (showCursor) {
                                withStyle(SpanStyle(color = AntiPrimary)) { append("|") }
                            }
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(20.dp)
                    )
                }
             }
        }

        OutlinedTextField(
            value = nameInput,
            onValueChange = { nameInput = it },
            placeholder = { Text("Tu nombre...", color = Color.White.copy(alpha = 0.5f)) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = AntiPrimary,
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                cursorColor = AntiPrimary
            ),
            singleLine = true
        )

        PremiumButton(
            text = "CONTINUAR", 
            enabled = nameInput.isNotBlank(), 
            onClick = { onNext(nameInput) }
        )
    }
}

@Composable
private fun FinaleStep(onFinish: () -> Unit) {
    var showBadge by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(500)
        showBadge = true
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        com.katchy.focuslive.ui.components.BrishMascotAnimation(
            modifier = Modifier.size(160.dp),
            pose = com.katchy.focuslive.ui.components.BrishPose.PLANNER
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "TODO LISTO",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Ahora vamos a configurar tu cuenta para sincronizar tus progresos.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(64.dp))

        PremiumButton(text = "IR AL LOGIN", onClick = onFinish)
    }
}

@Composable
fun PremiumButton(
    text: String, 
    onClick: () -> Unit, 
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val glowColor = AntiPrimary.copy(alpha = 0.4f)

    Button(
        onClick = { 
            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
            onClick() 
        },
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .shadow(
                elevation = if (enabled) 12.dp else 0.dp,
                shape = RoundedCornerShape(20.dp),
                clip = false,
                spotColor = AntiPrimary
            )
            .drawBehind {
                if (enabled) {
                    drawIntoCanvas { canvas ->
                        val paint = Paint().apply {
                            color = glowColor.toArgb()
                            maskFilter = BlurMaskFilter(40f, BlurMaskFilter.Blur.NORMAL)
                        }
                        canvas.nativeCanvas.drawRoundRect(
                            0f, 0f, size.width, size.height, 20.dp.toPx(), 20.dp.toPx(), paint
                        )
                    }
                }
            },
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AntiPrimary,
            contentColor = Color.White,
            disabledContainerColor = Color.White.copy(alpha = 0.05f),
            disabledContentColor = Color.White.copy(alpha = 0.2f)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp)
    ) {
        Text(
            text = text, 
            style = MaterialTheme.typography.labelLarge, 
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
    }
}
