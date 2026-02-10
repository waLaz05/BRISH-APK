package com.katchy.focuslive.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import kotlinx.coroutines.delay
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material3.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.hilt.navigation.compose.hiltViewModel
import com.katchy.focuslive.data.model.Task
import com.katchy.focuslive.ui.theme.*
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale
import com.katchy.focuslive.ui.components.BrishMascotWithBubble
import com.katchy.focuslive.ui.components.BrishPose

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    isGuest: Boolean = false,
    onNavigateToLogin: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToFocus: () -> Unit = {},
    onNavigateToMascot: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.homeState.collectAsState()
    val timeLeft by viewModel.timeLeft.collectAsState()
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()

    
    val scrollState = rememberScrollState()
    var showAddTaskSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val selectedMascot by viewModel.selectedMascot.collectAsState()
    val activeTaskId by viewModel.activeTaskId.collectAsState()
    val activeTask by remember(state.tasks, activeTaskId) {
        derivedStateOf { state.tasks.find { it.id == activeTaskId } }
    }
    var showTaskPicker by remember { mutableStateOf(false) }
    
    val isPlannerEnabled by viewModel.isPlannerEnabled.collectAsState()
    val isNotesEnabled by viewModel.isNotesEnabled.collectAsState()
    val isFinanceEnabled by viewModel.isFinanceEnabled.collectAsState()
    val isHabitsEnabled by viewModel.isHabitsEnabled.collectAsState()
    val isGamificationEnabled by viewModel.isGamificationEnabled.collectAsState()

    val minutes = (timeLeft / 60).toString().padStart(2, '0')
    val seconds = (timeLeft % 60).toString().padStart(2, '0')

    val context = androidx.compose.ui.platform.LocalContext.current
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
        onResult = { hasNotificationPermission = it }
    )

    LaunchedEffect(Unit) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
             if (!hasNotificationPermission) {
                 launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
             }
        }
    }
    
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Buenos días"
            in 12..18 -> "Buenas tardes"
            else -> "Buenas noches"
        }
    }


    // Removed expensive blur and duplicated animations
    
    // Persistent state for header to avoid re-animation on tab switch
    val showHeader = remember { mutableStateOf(true) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Confetti Overlay (Z-Index top)
            var showConfetti by remember { mutableStateOf(false) }
            
            // Effect to trigger confetti
            fun triggerConfetti() {
                showConfetti = true
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(scrollState)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Static header for instant, fluid feel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = state.userName ?: "Enfocador",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                
                                if (isGamificationEnabled) {
                                    val level = state.userStats.currentLevel
                                    val levelColor = com.katchy.focuslive.ui.util.GamificationUtils.getLevelColor(level)
                                    
                                    var showBadges by remember { mutableStateOf(false) }
                                    if (showBadges) {
                                        com.katchy.focuslive.ui.components.BadgesSheet(
                                            badges = state.userStats.badges,
                                            onDismiss = { showBadges = false }
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(100.dp),
                                        color = levelColor.copy(alpha = 0.1f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, levelColor.copy(alpha = 0.2f)),
                                        modifier = Modifier.clickable { showBadges = true }
                                    ) {
                                        Text(
                                            text = "Nivel $level",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = levelColor,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            
                            if (isGamificationEnabled) {
                                val level = state.userStats.currentLevel
                                val levelTitle = com.katchy.focuslive.ui.util.GamificationUtils.getLevelTitle(level)
                                Text(
                                    text = "$levelTitle • ${state.userStats.totalFocusMinutes} min",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                )
                            }
                        }
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                             IconButton(
                                onClick = { onNavigateToMascot() },
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f), CircleShape)
                            ) {
                                Icon(Icons.Rounded.Face, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            IconButton(
                                onClick = { onNavigateToSettings() },
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f), CircleShape)
                            ) {
                                Icon(Icons.Rounded.Settings, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Timer Chip - High Performance & Interactivity
                val timerInfiniteTransition = rememberInfiniteTransition(label = "timerBreathing")
                val timerScale by timerInfiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = if (isTimerRunning) 1.015f else 1f, // Very subtle breathing
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, easing = LinearEasing), // Smooth consistent breathing
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "timerBreathingScale"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .graphicsLayer {
                            scaleX = timerScale
                            scaleY = timerScale
                        }
                        .shadow(40.dp, RoundedCornerShape(24.dp), spotColor = Color.Black.copy(alpha = 0.5f))
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF0F172A),
                                    Color(0xFF020617)
                                )
                            )
                        )
                        .clickable { onNavigateToFocus() }
                ) {
                    com.katchy.focuslive.ui.home.AnimatedFocusBackground(modifier = Modifier.fillMaxSize())


                    AnimatedContent(
                        targetState = activeTask?.title ?: "Elegir Objetivo",
                        transitionSpec = { 
                            (fadeIn(spring()) + slideInVertically(spring()) { -it / 2 })
                                .togetherWith(fadeOut(spring()) + slideOutVertically(spring()) { it / 2 })
                        },
                        modifier = Modifier.align(Alignment.TopStart).padding(16.dp),
                        label = "taskSelector"
                    ) { targetTitle ->
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.1f))
                                .clickable { showTaskPicker = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.RadioButtonChecked, null, tint = AntiPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = targetTitle, style = MaterialTheme.typography.labelMedium, color = Color.White)
                        }
                    }

                    Icon(
                        imageVector = Icons.Rounded.OpenInFull,
                        contentDescription = "Pantalla completa",
                        tint = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(20.dp)
                            .size(18.dp)
                    )

                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        AnimatedContent(
                            targetState = "$minutes:$seconds",
                            transitionSpec = { 
                                fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) togetherWith 
                                fadeOut(spring(stiffness = Spring.StiffnessMediumLow)) 
                            },
                            label = "timerText"
                        ) { timeText ->
                            Text(
                                text = timeText,
                                fontSize = 80.sp, // Slightly larger
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = (-2).sp
                            )
                        }
                        
                        Text(
                            text = if(isTimerRunning) "CONCENTRACIÓN PROFUNDA" else "LISTO PARA EMPEZAR",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                        
                        Spacer(modifier = Modifier.height(28.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val buttonScale by animateFloatAsState(
                                targetValue = 1f,
                                animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium)
                            )

                            Button(
                                onClick = { viewModel.toggleTimer() },
                                modifier = Modifier.height(56.dp).width(160.dp).scale(buttonScale),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                shape = RoundedCornerShape(100.dp),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 12.dp)
                            ) {
                                AnimatedContent(
                                    targetState = isTimerRunning, 
                                    label = "buttonLabel",
                                    transitionSpec = { scaleIn(spring()) + fadeIn() togetherWith scaleOut(spring()) + fadeOut() }
                                ) { running ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(if(running) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, null, tint = AntiPrimary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(if (running) "PAUSA" else "INICIAR", fontWeight = FontWeight.Bold, color = AntiPrimary)
                                    }
                                }
                            }
                            
                            AnimatedVisibility(
                                visible = !isTimerRunning && timeLeft < 25 * 60,
                                enter = scaleIn(spring()) + fadeIn(),
                                exit = scaleOut(spring()) + fadeOut()
                            ) {
                                Row {
                                    Spacer(modifier = Modifier.width(16.dp))
                                    IconButton(
                                        onClick = { viewModel.resetTimer() },
                                        modifier = Modifier.size(56.dp).background(Color.White.copy(alpha = 0.15f), CircleShape)
                                    ) {
                                        Icon(Icons.Rounded.Refresh, null, tint = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                BrishMascotWithBubble(mascotType = selectedMascot, level = state.userStats.currentLevel)
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Dashboard Grid
                val activeWidgets = remember(isPlannerEnabled, isHabitsEnabled, isFinanceEnabled, isNotesEnabled, state) {
                    mutableListOf<@Composable (Modifier) -> Unit>().apply {
                        if (isPlannerEnabled) {
                            add { mod ->
                                DashboardWidget(
                                    modifier = mod,
                                    title = "Tareas Hoy",
                                    value = "${state.tasks.filter { !it.isCompleted }.size}",
                                    subtext = "Pendientes",
                                    icon = Icons.Rounded.Event,
                                    iconTint = Color(0xFF3B82F6),
                                    animationType = AnimationType.SWING
                                )
                            }
                        }
                        if (isHabitsEnabled) {
                            add { mod ->
                                DashboardWidget(
                                    modifier = mod,
                                    title = "Hábitos",
                                    value = "${state.activeHabits}",
                                    subtext = "Racha ${state.bestStreak}",
                                    icon = Icons.Rounded.Bolt,
                                    iconTint = Color(0xFFF59E0B),
                                    animationType = AnimationType.PULSE
                                )
                            }
                        }
                        if (isFinanceEnabled) {
                            add { mod ->
                                val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)
                                DashboardWidget(
                                    modifier = mod,
                                    title = "Balance",
                                    value = currencyFormatter.format(state.totalBalance).substringBefore("."), 
                                    subtext = "Disponible",
                                    icon = Icons.Rounded.AccountBalanceWallet,
                                    iconTint = Color(0xFF10B981),
                                    animationType = AnimationType.FLOAT
                                )
                            }
                        }
                        if (isNotesEnabled) {
                            add { mod ->
                                DashboardWidget(
                                    modifier = mod,
                                    title = "Notas",
                                    value = "${state.notesCount}",
                                    subtext = "Ideas",
                                    icon = Icons.Rounded.Lightbulb,
                                    iconTint = Color(0xFF8B5CF6),
                                    animationType = AnimationType.GLOW
                                )
                            }
                        }
                    }
                }

                if (activeWidgets.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        activeWidgets.chunked(2).forEachIndexed { rowIndex, rowWidgets ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                rowWidgets.forEachIndexed { colIndex, widget ->
                                    var isVisible by remember { mutableStateOf(false) }
                                    LaunchedEffect(Unit) {
                                        delay(20L + (rowIndex * 30L) + (colIndex * 15L))
                                        isVisible = true
                                    }
                                    

                                    AnimatedVisibility(
                                        visible = isVisible,
                                        enter = fadeIn(spring()) + scaleIn(spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMediumLow), initialScale = 0.9f),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        widget(Modifier.fillMaxWidth())
                                    }
                                }
                                if (rowWidgets.size == 1 && activeWidgets.size > 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = "Agenda Rápida",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (state.tasks.isEmpty()) {
                    TaskEmptyState()
                } else {
                    state.tasks.take(3).forEachIndexed { index, task ->
                        var isVisible by remember(task.id) { mutableStateOf(false) }
                        LaunchedEffect(task.id) {
                            delay(150L + (index * 30L))
                            isVisible = true
                        }
                        
                        AnimatedVisibility(
                            visible = isVisible,
                            enter = fadeIn(spring()) + slideInHorizontally(spring()) { 50 },
                            label = "taskItem"
                        ) {
                            PremiumTaskItem(
                                task = task, 
                                onToggle = { 
                                    if (!task.isCompleted) {
                                       triggerConfetti() 
                                    }
                                    viewModel.toggleTask(task) 
                                },
                                onDelete = { viewModel.deleteTask(task) }
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(100.dp))
            }
            
            if (showConfetti) {
                com.katchy.focuslive.ui.components.ConfettiAnimation(
                    modifier = Modifier.fillMaxSize().align(Alignment.Center),
                    play = showConfetti,
                    onFinished = { showConfetti = false }
                )
            }
        }

        if (showTaskPicker) {
            TaskPickerDialog(
                tasks = state.tasks.filter { !it.isCompleted },
                onDismiss = { showTaskPicker = false },
                onTaskSelected = { task ->
                    viewModel.setActiveTask(task?.id)
                    showTaskPicker = false
                }
            )
        }
    }
}

@Composable
fun TaskPickerDialog(
    tasks: List<Task>,
    onDismiss: () -> Unit,
    onTaskSelected: (Task?) -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "¿En qué te enfocarás?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                if (tasks.isEmpty()) {
                    Text("No hay tareas pendientes", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Surface(
                                onClick = { onTaskSelected(null) },
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "Ninguna (Solo temporizador)",
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        items(tasks.size) { index ->
                            val task = tasks[index]
                            Surface(
                                onClick = { onTaskSelected(task) },
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    task.title,
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                TextButton(onClick = onDismiss) {
                    Text("CANCELAR")
                }
            }
        }
    }
}

enum class AnimationType {
    SWING, PULSE, FLOAT, GLOW
}

@Composable
fun DashboardWidget(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtext: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    animationType: AnimationType = AnimationType.PULSE
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "widgetScale"
    )
    
    // Premium Colors
    val cardBackground = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF1F2937).copy(alpha = 0.9f), // Slate 800
                Color(0xFF111827).copy(alpha = 0.95f) // Slate 900
            ) 
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color.White, Color(0xFFF9FAFB)) // White to Gray 50
        )
    }
    
    val borderColor = if (isDark) {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.10f),
                Color.White.copy(alpha = 0.02f)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color.White,
                Color(0xFFE5E7EB) // Gray 200
            )
        )
    }
    
    val shadowColor = if (isDark) Color(0xFF000000).copy(alpha = 0.5f) else Color(0xFF9CA3AF).copy(alpha = 0.2f)

    Box(
        modifier = modifier
            .height(160.dp) // Slightly taller
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                shadowElevation = if(isDark) 0f else 8.dp.toPx()
                shape = RoundedCornerShape(28.dp)
                clip = true
            }
            .background(cardBackground, RoundedCornerShape(28.dp))
            .border(1.dp, borderColor, RoundedCornerShape(28.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = androidx.compose.material3.ripple(bounded = true, color = iconTint),
                onClick = { }
            )
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        when (event.type) {
                            androidx.compose.ui.input.pointer.PointerEventType.Press -> isPressed = true
                            androidx.compose.ui.input.pointer.PointerEventType.Release -> isPressed = false
                            androidx.compose.ui.input.pointer.PointerEventType.Exit -> isPressed = false
                        }
                    }
                }
            }
    ) {
        // Inner Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Row: Icon + Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Premium Icon Container
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp)) // Squircle-ish
                        .background(iconTint.copy(alpha = if (isDark) 0.2f else 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedIcon(
                        icon = icon,
                        tint = iconTint,
                        modifier = Modifier.size(26.dp),
                        type = animationType
                    )
                }
                
                // Optional decorative dot
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if(isDark) Color.White.copy(alpha=0.1f) else Color.Black.copy(alpha=0.05f))
                )
            }
            
            // Value and Title
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-1.5).sp
                    ),
                    color = if (isDark) Color.White else Color(0xFF111827) // Gray 900
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280) // Gray 400/500
                )
                
                Text(
                    text = subtext,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = iconTint.copy(alpha = 0.8f) // Subtle tint link
                )
            }
        }
        
        // Decorative Shine Effect (Dark Mode only)
        if (isDark) {
             Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(100.dp)
                    .offset(x = 30.dp, y = (-30).dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.05f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}

@Composable
fun AnimatedIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    type: AnimationType
) {

    val infiniteTransition = rememberInfiniteTransition()
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (type == AnimationType.PULSE) 1.2f else if (type == AnimationType.GLOW) 1.1f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(if(type == AnimationType.GLOW) 2000 else 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), 
        label = "iconScale"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconAlpha"
    )

    val floatingOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (type == AnimationType.FLOAT) -3f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconFloat"
    )
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = if (type == AnimationType.SWING) 10f else -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconRotate"
    )

    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if(type == AnimationType.GLOW) tint.copy(alpha = alpha) else tint,
        modifier = modifier
            .graphicsLayer {
                val s = if(type == AnimationType.PULSE || type == AnimationType.GLOW) scale else 1f
                scaleX = s
                scaleY = s
                this.translationY = if(type == AnimationType.FLOAT) floatingOffset.dp.toPx() else 0f
                rotationZ = if(type == AnimationType.SWING) rotation else 0f
            }
    )
}

@Composable
fun PremiumTaskItem(
    task: Task, 
    onToggle: () -> Unit, 
    onDelete: () -> Unit
) {
    val haptics = androidx.compose.ui.platform.LocalHapticFeedback.current
    
    val lastClickTime = remember { mutableStateOf(0L) }
    val debounceTime = 500L

    // Animate checkbox scale - Spring physics
    val checkboxScale by animateFloatAsState(
        targetValue = if (task.isCompleted) 1.2f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium),
        label = "checkboxScale"
    )
    val checkboxColor by animateColorAsState(
        targetValue = if (task.isCompleted) AntiSuccess else MaterialTheme.colorScheme.outline.copy(alpha=0.5f),
        animationSpec = spring(),
        label = "checkboxColor"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { 
                haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
            },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 0.dp,
        border = if(task.isCompleted) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Animated Checkbox
             IconButton(
                onClick = onToggle, 
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer {
                        scaleX = checkboxScale
                        scaleY = checkboxScale
                    }
            ) {
                Icon(
                    if (task.isCompleted) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                    null,
                    tint = checkboxColor
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    task.title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if(task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                    color = if(task.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha=0.5f) else MaterialTheme.colorScheme.onSurface
                )
                if(task.description.isNotBlank()) {
                     Text(
                        task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Rounded.DeleteOutline, null, tint = MaterialTheme.colorScheme.error.copy(alpha=0.4f))
            }
        }
    }
}

@Composable
fun TaskEmptyState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.AutoMirrored.Rounded.Assignment, null, tint = AntiTextSecondary.copy(alpha=0.3f), modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("No hay tareas pendientes", color = AntiTextSecondary)
    }
}

