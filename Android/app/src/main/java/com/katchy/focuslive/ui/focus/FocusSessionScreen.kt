package com.katchy.focuslive.ui.focus

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import android.provider.Settings
import android.content.Intent
import androidx.compose.material.icons.rounded.DoNotDisturbOn
import androidx.compose.material.icons.rounded.DoNotDisturbOff
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.katchy.focuslive.ui.home.HomeViewModel
import com.katchy.focuslive.ui.home.AnimatedFocusBackground
import com.katchy.focuslive.ui.components.BrishMascotWithBubble
import com.katchy.focuslive.ui.components.BrishPose
import kotlinx.coroutines.delay

@Composable
fun FocusSessionScreen(
    onNavigateBack: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val timeLeft by viewModel.timeLeft.collectAsState()
    val isRunning by viewModel.isTimerRunning.collectAsState()
    val selectedMascot by viewModel.selectedMascot.collectAsState()
    val isZenMode by viewModel.isZenModeEnabled.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Sync DND state on resume
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onResume()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    // Handle DND Permission Request
    var showDndPermissionDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        viewModel.zenModeEffect.collect { effect ->
            when(effect) {
                HomeViewModel.ZenModeEffect.RequestDndPermission -> {
                    showDndPermissionDialog = true
                }
            }
        }
    }
    
    if (showDndPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showDndPermissionDialog = false },
            title = { Text("Modo Concentración Total") },
            text = { Text("Para bloquear notificaciones y evitar distracciones, Brish necesita permiso para acceder a 'No Molestar'.") },
            confirmButton = {
                TextButton(onClick = {
                    showDndPermissionDialog = false
                    val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                    context.startActivity(intent)
                }) {
                    Text("CONCEDER PERMISO")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDndPermissionDialog = false }) {
                    Text("CANCELAR")
                }
            }
        )
    }

    
    val timerState by viewModel.timerState.collectAsState()
    val workDuration by viewModel.workDuration.collectAsState(initial = 25L)
    val breakDuration by viewModel.breakDuration.collectAsState(initial = 5L)
    
    val activeTaskId by viewModel.activeTaskId.collectAsState()
    val homeState by viewModel.homeState.collectAsState()
    val activeTask = homeState.tasks.find { it.id == activeTaskId }
    
    val activeSound by viewModel.ambientSound.collectAsState()
    var showSoundPicker by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    
    val minutes = String.format(java.util.Locale.US, "%02d", timeLeft / 60)
    val seconds = String.format(java.util.Locale.US, "%02d", timeLeft % 60)
    
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val isWarning = timeLeft in 1..59 && timerState == com.katchy.focuslive.data.manager.TimerManager.TimerState.WORK
    val isBreak = timerState == com.katchy.focuslive.data.manager.TimerManager.TimerState.BREAK
    
    val animatedBg by animateColorAsState(
        targetValue = when { 
            isBreak -> Color(0xFF064E3B)
            isWarning -> Color(0xFF991B1B)
            else -> Color(0xFF000000) 
        }, 
        animationSpec = tween(2000),
        label = "bgColorTransition"
    )

    if (showSettingsDialog) {
        Dialog(onDismissRequest = { showSettingsDialog = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.width(320.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Configuración",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Enfoque",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "$workDuration min",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Slider(
                            value = workDuration.toFloat(),
                            onValueChange = { viewModel.setWorkDuration(it.toLong()) },
                            valueRange = 5f..60f,
                            steps = 10
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Column(modifier = Modifier.fillMaxWidth()) {
                         Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Descanso",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "$breakDuration min",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Slider(
                            value = breakDuration.toFloat(),
                            onValueChange = { viewModel.setBreakDuration(it.toLong()) },
                            valueRange = 1f..30f,
                            steps = 28
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Button(
                        onClick = { showSettingsDialog = false },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Listo")
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(animatedBg.copy(alpha=0.8f), animatedBg)))
    ) {
        AnimatedFocusBackground(modifier = Modifier.fillMaxSize())

        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(24.dp)
                .background(Color.White.copy(alpha = 0.1f), CircleShape)
        ) {
            Icon(Icons.Rounded.CloseFullscreen, null, tint = Color.White)
        }
        
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(
                onClick = { viewModel.toggleZenMode() },
                modifier = Modifier
                    .background(if(isZenMode) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(
                    imageVector = if (isZenMode) Icons.Rounded.DoNotDisturbOn else Icons.Rounded.DoNotDisturbOff,
                    contentDescription = "Modo Zen",
                    tint = Color.White.copy(alpha = if(isZenMode) 1f else 0.6f)
                )
            }

            IconButton(
                onClick = { showSoundPicker = true },
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(
                    imageVector = if (activeSound == com.katchy.focuslive.data.manager.TimerManager.AmbientSound.NONE) 
                        Icons.Rounded.MusicOff else Icons.Rounded.MusicNote, 
                    null, 
                    tint = if (activeSound == com.katchy.focuslive.data.manager.TimerManager.AmbientSound.NONE) 
                        Color.White.copy(alpha = 0.6f) else Color.White
                )
            }

            IconButton(
                onClick = { showSettingsDialog = true },
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(Icons.Rounded.Settings, null, tint = Color.White)
            }
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isBreak) "MODO DESCANSO" else "MODO ENFOQUE",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.7f),
                letterSpacing = 4.sp
            )
            
            if (!isBreak && activeTask != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Icon(
                        Icons.Rounded.RadioButtonChecked, 
                        null, 
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = activeTask.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Box(contentAlignment = Alignment.Center) {
                if (isRunning) {
                     val orbitalRotation by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(10000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "orbitalRotation"
                    )

                    Box(
                        modifier = Modifier
                            .size(280.dp)
                            .scale(breathingScale * 1.1f)
                            .alpha(glowAlpha * 0.1f)
                    )

                    Canvas(modifier = Modifier.size(260.dp).rotate(orbitalRotation)) {
                        drawArc(
                            color = Color.White.copy(alpha = 0.15f),
                            startAngle = 0f,
                            sweepAngle = 120f,
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                        )
                    }

                    Canvas(modifier = Modifier.size(240.dp).rotate(-orbitalRotation * 1.5f)) {
                         drawArc(
                            color = Color.White.copy(alpha = 0.1f),
                            startAngle = 180f,
                            sweepAngle = 90f,
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                        )
                    }
                }

                Text(
                    text = "$minutes:$seconds",
                    fontSize = 100.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = (-4).sp,
                    modifier = Modifier.scale(if (isRunning) breathingScale else 1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isBreak) {
                if (isRunning) {
                     Text("Relájate y recarga energías", color = Color.White, fontWeight = FontWeight.Bold)
                } else {
                     Text("¿Listo para el descanso?", color = Color.White, fontWeight = FontWeight.Bold)
                }
            } else if (isWarning) {
                Text("¡Último esfuerzo!", color = Color.White, fontWeight = FontWeight.Bold)
            } else {
                 if (isRunning) {
                    Text("Sumérgete en el trabajo", color = Color.White.copy(alpha=0.5f))
                 } else {
                    Text("¡Hora de enfocar!", color = Color.White, fontWeight = FontWeight.Bold)
                 }
            }
            
            Spacer(modifier = Modifier.height(32.dp))

            BrishMascotWithBubble(pose = BrishPose.FOCUS, modifier = Modifier.padding(horizontal = 32.dp), mascotType = selectedMascot)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                   onClick = { 
                       haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                       viewModel.resetTimer() 
                   },
                   modifier = Modifier
                       .size(64.dp)
                       .background(Color.White.copy(alpha = 0.1f), CircleShape)
               ) {
                   Icon(Icons.Rounded.Refresh, null, tint = Color.White, modifier = Modifier.size(32.dp))
               }

                Button(
                    onClick = { 
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        viewModel.toggleTimer() 
                    },
                    modifier = Modifier
                        .size(96.dp)
                        .scale(if (isRunning) 1f + (breathingScale-1f)*0.5f else 1f)
                        .shadow(if (isRunning) (glowAlpha * 20).dp else 0.dp, CircleShape, spotColor = Color.White),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                    contentPadding = PaddingValues(0.dp)
                ) {
                   Column(horizontalAlignment = Alignment.CenterHorizontally) {
                       Icon(
                           imageVector = if (isRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                           contentDescription = null,
                           modifier = Modifier.size(32.dp)
                       )
                       if (!isRunning) {
                           Text(
                               text = if (isBreak) "Descanso" else "Iniciar",
                               style = MaterialTheme.typography.labelSmall,
                               fontWeight = FontWeight.Bold,
                               fontSize = 10.sp
                           )
                       }
                   }
                }
                
                 IconButton(
                   onClick = { 
                       haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                       viewModel.skipSession()
                   },
                   modifier = Modifier
                       .size(64.dp)
                       .background(Color.White.copy(alpha = 0.1f), CircleShape)
               ) {
                   Icon(Icons.Rounded.SkipNext, null, tint = Color.White, modifier = Modifier.size(32.dp))
               }
            }
        }

        if (showSoundPicker) {
            SoundPickerDialog(
                activeSound = activeSound,
                onDismiss = { showSoundPicker = false },
                onSoundSelected = { sound ->
                    viewModel.setAmbientSound(sound)
                    showSoundPicker = false
                }
            )
        }
    }
}

@Composable
fun SoundPickerDialog(
    activeSound: com.katchy.focuslive.data.manager.TimerManager.AmbientSound,
    onDismiss: () -> Unit,
    onSoundSelected: (com.katchy.focuslive.data.manager.TimerManager.AmbientSound) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.width(300.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Sonidos Ambientales", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                val sounds = com.katchy.focuslive.data.manager.TimerManager.AmbientSound.entries
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    sounds.forEach { sound ->
                        val isSelected = activeSound == sound
                        Surface(
                            onClick = { onSoundSelected(sound) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    when(sound) {
                                        com.katchy.focuslive.data.manager.TimerManager.AmbientSound.NONE -> Icons.AutoMirrored.Rounded.VolumeOff
                                        com.katchy.focuslive.data.manager.TimerManager.AmbientSound.RAIN -> Icons.Rounded.Cloud
                                        com.katchy.focuslive.data.manager.TimerManager.AmbientSound.FIRE -> Icons.Rounded.LocalFireDepartment
                                        com.katchy.focuslive.data.manager.TimerManager.AmbientSound.CAFE -> Icons.Rounded.Coffee
                                        com.katchy.focuslive.data.manager.TimerManager.AmbientSound.AMBIENT -> Icons.Rounded.Nature
                                    },
                                    null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    when(sound) {
                                        com.katchy.focuslive.data.manager.TimerManager.AmbientSound.NONE -> "Silencio"
                                        com.katchy.focuslive.data.manager.TimerManager.AmbientSound.RAIN -> "Lluvia Calma"
                                        com.katchy.focuslive.data.manager.TimerManager.AmbientSound.FIRE -> "Fogata Lejana"
                                        com.katchy.focuslive.data.manager.TimerManager.AmbientSound.CAFE -> "Cafetería Jazz"
                                        com.katchy.focuslive.data.manager.TimerManager.AmbientSound.AMBIENT -> "Naturaleza"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                TextButton(onClick = onDismiss) {
                    Text("CERRAR")
                }
            }
        }
    }
}

