package com.katchy.focuslive.ui.habits

import kotlinx.coroutines.delay

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.IntOffset
import androidx.hilt.navigation.compose.hiltViewModel
import com.katchy.focuslive.data.model.Habit
import com.katchy.focuslive.ui.components.BrishMascotWithBubble
import com.katchy.focuslive.ui.components.BrishPose
import com.katchy.focuslive.ui.theme.*
import java.util.Calendar
import java.util.Locale
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.animation.core.LinearEasing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen(
    viewModel: HabitViewModel = hiltViewModel()
) {
    val habits by viewModel.habits.collectAsState()
    val monthlyStats by viewModel.monthlyStats.collectAsState()
    val selectedMascot by viewModel.selectedMascot.collectAsState()

    val scrollState = rememberScrollState()
    
    var showAddHabitSheet by remember { mutableStateOf(false) }
    var habitToEdit by remember { mutableStateOf<Habit?>(null) }
    val sheetState = rememberModalBottomSheetState()

    val context = androidx.compose.ui.platform.LocalContext.current
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    
    // Fix: Dynamic date that updates when app resumes (OnResume)
    // This solves the bug where "Today" stays as "Yesterday" if app runs overnight
    var todayDateParams by remember {
        val c = Calendar.getInstance()
        mutableStateOf(Pair(c.get(Calendar.YEAR), c.get(Calendar.DAY_OF_YEAR)))
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // Refresh date check when user comes back to app
                val c = Calendar.getInstance()
                todayDateParams = Pair(c.get(Calendar.YEAR), c.get(Calendar.DAY_OF_YEAR))
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(300)) + slideInVertically(initialOffsetY = { 20 }, animationSpec = tween(300)),
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding(),
                contentPadding = PaddingValues(
                    start = 24.dp, 
                    end = 24.dp, 
                    top = 24.dp, 
                    bottom = 140.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- Header ---
                item {
                     Column {
                        Text(
                            text = "HÁBITOS",
                            style = MaterialTheme.typography.labelSmall,
                            color = AntiPrimary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Construyendo\nConsistencia",
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            lineHeight = 36.sp
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // --- Stats Summary (Mini Cards) ---
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatMiniCard(
                                title = "Hábitos",
                                value = habits.size.toString(),
                                icon = Icons.Rounded.Bolt,
                                modifier = Modifier.weight(1f)
                            )
                            StatMiniCard(
                                title = "Mejor Racha",
                                value = (habits.maxOfOrNull { it.currentStreak } ?: 0).toString(),
                                icon = Icons.Rounded.Whatshot,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))

                        // Brish Mascot (Habits)
                        BrishMascotWithBubble(pose = BrishPose.HABITS, mascotType = selectedMascot)
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                         // --- Habits List Header ---
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tus Rutinas",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { 
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                showAddHabitSheet = true 
                            }) {
                                Icon(Icons.Rounded.AddCircle, null, tint = AntiPrimary, modifier = Modifier.size(32.dp))
                            }
                        }
                     }
                }

            if (habits.isEmpty()) {
                item { HabitEmptyState() }
            } else {
                itemsIndexed(habits, key = { _, habit -> habit.id }) { index, habit ->
                    DailyHabitCard(
                        habit = habit, 
                        onToggleToday = { 
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            viewModel.toggleHabitDate(habit.id, System.currentTimeMillis()) 
                        },
                        onEdit = { habitToEdit = habit },
                        todayYear = todayDateParams.first,
                        todayDayOfYear = todayDateParams.second
                    )
                }
            }
            
            item { Spacer(modifier = Modifier.height(40.dp)) }

            // --- Monthly Calendar View ---
            item {
                 val monthName by viewModel.currentMonthName.collectAsState()
                 MonthlyProgressCalendar(
                    monthlyStats = monthlyStats,
                    monthName = monthName,
                    onPrevMonth = { viewModel.prevMonth() },
                    onNextMonth = { viewModel.nextMonth() }
                )
            }
            
            item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
    }

// Sheets (Add/Edit)
    if (showAddHabitSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddHabitSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            AddHabitContent(onAdd = { title, reminder, days ->
                viewModel.addHabit(title, reminder, days)
                showAddHabitSheet = false
            })
        }
    }

    if (habitToEdit != null) {
        ModalBottomSheet(
            onDismissRequest = { habitToEdit = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            EditHabitContent(
                initialTitle = habitToEdit!!.title,
                initialReminderTime = habitToEdit!!.reminderTime,
                initialDays = habitToEdit!!.daysOfWeek,
                onUpdate = { newTitle, newReminder, newDays ->
                    viewModel.updateHabitDetails(habitToEdit!!.id, newTitle, newReminder, newDays)
                    habitToEdit = null
                },
                onDelete = {
                    viewModel.deleteHabit(habitToEdit!!)
                    habitToEdit = null
                }
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MonthlyProgressCalendar(
    monthlyStats: List<DayStats>, 
    monthName: String,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Transparent, minimal header
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevMonth) {
                Icon(Icons.Rounded.ChevronLeft, null, tint = MaterialTheme.colorScheme.onSurface)
            }
            
            Text(
                text = monthName.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = 1.sp
            )
            
            IconButton(onClick = onNextMonth) {
                Icon(Icons.Rounded.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurface)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Card Container for Grid
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 0.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant) // Subtle border
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // Days Header (L M X J V S D)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf("L", "M", "M", "J", "V", "S", "D").forEach { day ->
                        Text(
                            text = day,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF9CA3AF), // Tailwind Gray 400
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontSize = 10.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                // Animated Month Content - iOS Spring Slide
                AnimatedContent(
                    targetState = monthName,
                    transitionSpec = {
                        val springSpec = spring<IntOffset>(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                        if (targetState > initialState) { 
                             (fadeIn(animationSpec = tween(300)) + slideInHorizontally(animationSpec = springSpec) { it / 2 })
                                 .togetherWith(fadeOut(animationSpec = tween(200)) + slideOutHorizontally(animationSpec = springSpec) { -it / 2 })
                        } else {
                             (fadeIn(animationSpec = tween(300)) + slideInHorizontally(animationSpec = springSpec) { -it / 2 })
                                 .togetherWith(fadeOut(animationSpec = tween(200)) + slideOutHorizontally(animationSpec = springSpec) { it / 2 })
                        }
                    }, label = "monthTransition"
                ) { _ ->
                    // Re-capture monthlyStats from the parent scope or use a calculated one
                    // Since we are inside HabitsScreen, we can just use monthlyStats directly
                   val firstDayOfWeek = remember(monthlyStats) {
                        if (monthlyStats.isNotEmpty()) {
                            val c = Calendar.getInstance()
                            c.timeInMillis = monthlyStats[0].timestamp
                            val dow = c.get(Calendar.DAY_OF_WEEK)
                            if (dow == Calendar.SUNDAY) 6 else dow - 2
                        } else 0
                   }
                   
                   val totalSlots = firstDayOfWeek + monthlyStats.size
                   val rows = (totalSlots + 6) / 7
                   
                   Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                       for (row in 0 until rows) {
                           Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                               for (col in 0..6) {
                                   val slotIndex = row * 7 + col
                                   val cellModifier = Modifier.weight(1f).aspectRatio(1f)
                                   
                                   if (slotIndex < firstDayOfWeek || slotIndex >= totalSlots) {
                                       Spacer(modifier = cellModifier)
                                   } else {
                                       val dayStat = monthlyStats.getOrNull(slotIndex - firstDayOfWeek)
                                       if (dayStat != null) {
                                           DayCell(
                                               dayStat = dayStat,
                                               modifier = cellModifier
                                           )
                                       } else {
                                           Spacer(modifier = cellModifier)
                                       }
                                   }
                               }
                           }
                       }
                   }
                }
            }
        }
    }
}

@Composable
fun DayCell(dayStat: DayStats, modifier: Modifier = Modifier) {
    val isToday = remember(dayStat.timestamp) { isToday(dayStat.timestamp) }
    
    // Smooth Color Animation for the background - iOS Spring
    val targetColor = when (dayStat.status) {
        DayStatus.COMPLETED -> AntiPrimary
        DayStatus.PARTIAL -> Color(0xFFFBBF24)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    val backgroundColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow),
        label = "cellColor"
    )

    // Squircle shape for a premium feel
    val shape = RoundedCornerShape(10.dp)

    // Optimized rendering for minimalism
    val isCompleted = dayStat.status == DayStatus.COMPLETED
    
    Box(
        modifier = modifier
            .background(backgroundColor, shape) // Use animated color
            .then(if (isToday && !isCompleted) Modifier.border(2.dp, AntiPrimary.copy(alpha = 0.5f), shape) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.animation.AnimatedVisibility(
            visible = isCompleted,
            enter = scaleIn(spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium)) + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            Icon(
                Icons.Rounded.Check, 
                null, 
                tint = Color.White, 
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

private fun isToday(timestamp: Long): Boolean {
    val c1 = Calendar.getInstance().apply { timeInMillis = timestamp }
    val c2 = Calendar.getInstance()
    return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
           c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun DailyHabitCard(
    habit: Habit, 
    onToggleToday: () -> Unit, 
    onEdit: () -> Unit,
    todayYear: Int,
    todayDayOfYear: Int
) {
    val isCompletedToday = remember(habit.completedDates, todayYear, todayDayOfYear) {
        habit.completedDates.any { timestamp ->
             val c = Calendar.getInstance().apply { timeInMillis = timestamp }
             c.get(Calendar.YEAR) == todayYear && c.get(Calendar.DAY_OF_YEAR) == todayDayOfYear
        }
    }
    
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    
    // Animate scale on press - Responsive
    var isPressed by remember { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) } 
    
    // iOS Spring Spec
    val springSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )
    
    val scale by animateFloatAsState(
        targetValue = if(isPressed) 0.98f else 1f, 
        animationSpec = springSpec,
        label = "cardScale"
    )
    
    val cardColor by animateColorAsState(
        targetValue = if (isCompletedToday) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "cardBg"
    )
    
    // Checkbox specific debounce
    val lastClickTime = remember { mutableStateOf(0L) }
    val debounceTime = 500L

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = { 
                    // Card BODY click -> Edit (like Planner expands/edits)
                    // This prevents accidental toggles when trying to scroll or touch the card
                    onEdit()
                },
                onLongClick = {
                     haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                     onEdit()
                },
                interactionSource = remember { MutableInteractionSource() }
                    .also { source ->
                        LaunchedEffect(source) {
                            source.interactions.collect {
                                when(it) {
                                    is androidx.compose.foundation.interaction.PressInteraction.Press -> isPressed = true
                                    else -> isPressed = false
                                }
                            }
                        }
                    },
                indication = null 
            ),
        shape = RoundedCornerShape(16.dp),
        color = cardColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, if(isCompletedToday) AntiPrimary.copy(alpha=0.1f) else Color(0xFFF1F5F9)),
        shadowElevation = if(isPressed) 1.dp else 2.dp,
        tonalElevation = 1.dp
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                 // Checkbox Indicator (Premium Ring) - Distinct Clickable
                 val checkScale by animateFloatAsState(if(isCompletedToday) 1f else 0.9f, springSpec)
                 
                 Box(
                    modifier = Modifier
                        .size(32.dp) // Larger touch target
                        .scale(checkScale)
                        .clip(CircleShape)
                        .background(if (isCompletedToday) AntiPrimary else Color.Transparent)
                        .border(
                            width = 3.dp, 
                            color = if (isCompletedToday) AntiPrimary else Color(0xFFCBD5E1), 
                            shape = CircleShape
                        )
                        .clickable {
                             val currentTime = System.currentTimeMillis()
                             if (currentTime - lastClickTime.value > debounceTime) {
                                 lastClickTime.value = currentTime
                                 if (!isCompletedToday) {
                                     showConfetti = true 
                                     haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                 } else {
                                     haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove) 
                                 }
                                 onToggleToday() 
                             }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = isCompletedToday,
                        enter = scaleIn(spring(dampingRatio = 0.5f)) + fadeIn(),
                        exit = fadeOut()
                    ) {
                         Icon(
                            Icons.Rounded.Check, 
                            null, 
                            tint = Color.White, 
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
    
                // Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = habit.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isCompletedToday) FontWeight.Medium else FontWeight.SemiBold, 
                        color = if(isCompletedToday) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface, 
                        textDecoration = if(isCompletedToday) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Streak Chip
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val streak = habit.currentStreak
                        val isFire = streak > 0
                        
                        val fireColor = when {
                            streak > 10 -> Color(0xFFA855F7)
                            streak > 3 -> Color(0xFFEF4444) 
                            streak > 0 -> Color(0xFFF59E0B) 
                            else -> Color(0xFFCBD5E1)
                        }
                        
                        Icon(
                            Icons.Rounded.LocalFireDepartment, 
                            null, 
                            tint = fireColor, 
                            modifier = Modifier
                                .size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$streak días",
                            style = MaterialTheme.typography.labelSmall,
                            color = if(isFire) fireColor else Color(0xFF94A3B8),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Edit Icon (Subtle)
                IconButton(onClick = onEdit) {
                    Icon(Icons.Rounded.MoreHoriz, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.5f)) 
                }
            }
            
            // Confetti Overlay
            if (showConfetti) {
                ConfettiExplosion(modifier = Modifier.matchParentSize())
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(1000)
                    showConfetti = false
                }
            }
        }
    }
}

@Composable
fun ConfettiExplosion(modifier: Modifier = Modifier) {
    // Note: Implementing full physics confetti in one go is risky. 
    // Let's just use a simple Lottie-like "Scale Up + Fade Out" indicator or just haptics.
    // The user asked for "Confetti".
    // I will implement a minimal "Exploding Circles" effect.
    
    val transition = rememberInfiniteTransition() // Actually we want one-shot, but infinite is easier to setup quickly? No.
    // Let's use a launched effect state.
    var progress by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        animate(0f, 1f, animationSpec = tween(600, easing = LinearEasing)) { value, _ ->
            progress = value
        }
    }
    
    Canvas(modifier = modifier) {
        val center = center + Offset(-30f, 0f) // Offset near checkbox
        val radius = size.minDimension / 3 * progress
        
        // Draw 8 particles
        for (i in 0 until 8) {
            val angle = i * (360f / 8) * (Math.PI / 180f)
            val dist = radius * 2
            val x = center.x + (dist * Math.cos(angle)).toFloat()
            val y = center.y + (dist * Math.sin(angle)).toFloat()
            
            drawCircle(
                color = when(i % 3) { 
                    0 -> Color(0xFFEF4444) // Red
                    1 -> Color(0xFF3B82F6) // Blue
                    else -> Color(0xFFEAB308) // Yellow
                }.copy(alpha = 1f - progress),
                radius = 10f * (1f - progress),
                center = Offset(x, y)
            )
        }
    }
}
// Removed unused class ConfettiParticle

// Reuse existing components
@Composable
fun StatMiniCard(
    title: String, 
    value: String, 
    icon: androidx.compose.ui.graphics.vector.ImageVector, 
    modifier: Modifier = Modifier
) {
    val scale = 1f

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        shadowElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AntiPrimary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon, 
                    null, 
                    tint = AntiPrimary, 
                    modifier = Modifier.size(20.dp).scale(scale)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun HabitEmptyState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Rounded.SelfImprovement, null, tint = Color(0xFFE5E7EB), modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Empieza un nuevo hábito", color = AntiTextSecondary, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun DaySelector(
    selectedDays: List<Int>,
    onDaySelected: (Int) -> Unit
) {
    val days = listOf(
        Pair("L", java.util.Calendar.MONDAY),
        Pair("M", java.util.Calendar.TUESDAY),
        Pair("M", java.util.Calendar.WEDNESDAY),
        Pair("J", java.util.Calendar.THURSDAY),
        Pair("V", java.util.Calendar.FRIDAY),
        Pair("S", java.util.Calendar.SATURDAY),
        Pair("D", java.util.Calendar.SUNDAY)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        days.forEach { (label, dayCode) ->
            val isSelected = selectedDays.contains(dayCode)
            val bgColor = if (isSelected) AntiPrimary else MaterialTheme.colorScheme.surfaceVariant
            val contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(bgColor)
                    .clickable { onDaySelected(dayCode) }
                    .border(1.dp, if(isSelected) Color.Transparent else MaterialTheme.colorScheme.outlineVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AddHabitContent(onAdd: (String, String?, List<Int>) -> Unit) {
    var title by remember { mutableStateOf("") }
    var reminderTime by remember { mutableStateOf("") } // HH:mm
    var selectedDays by remember { mutableStateOf(listOf(1, 2, 3, 4, 5, 6, 7)) } // Default all
    
    Column(modifier = Modifier.padding(24.dp).padding(bottom = 32.dp)) {
        Text("NUEVO HÁBITO", style = MaterialTheme.typography.labelSmall, color = AntiPrimary, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = title,
            onValueChange = { title = it },
            placeholder = { Text("Nombre del hábito") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Frecuencia:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        DaySelector(selectedDays = selectedDays) { dayCode ->
            if (selectedDays.contains(dayCode)) {
                if (selectedDays.size > 1) { // Prevent empty selection
                     selectedDays = selectedDays - dayCode
                }
            } else {
                selectedDays = selectedDays + dayCode
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        // Simple Text Input for Time (MVP) - Can be upgraded to TimePicker later
        Text("Recordatorio (Opcional):", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        TextField(
            value = reminderTime,
            onValueChange = { reminderTime = it },
            placeholder = { Text("Ej: 08:00") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { onAdd(title, if(reminderTime.isBlank()) null else reminderTime, selectedDays) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.inverseSurface)
        ) {
            Text("AÑADIR A LA RUTINA")
        }
    }
}

@Composable
fun EditHabitContent(initialTitle: String, initialReminderTime: String?, initialDays: List<Int>, onUpdate: (String, String?, List<Int>) -> Unit, onDelete: () -> Unit) {
    var title by remember { mutableStateOf(initialTitle) }
    var reminderTime by remember { mutableStateOf(initialReminderTime ?: "") }
    var selectedDays by remember { mutableStateOf(initialDays) }

    Column(modifier = Modifier.padding(24.dp).padding(bottom = 32.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("EDITAR HÁBITO", style = MaterialTheme.typography.labelSmall, color = AntiPrimary, fontWeight = FontWeight.Bold)
            IconButton(onClick = onDelete) {
                Icon(Icons.Rounded.DeleteOutline, null, tint = Color(0xFFEF4444))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = title,
            onValueChange = { title = it },
            placeholder = { Text("Nombre del hábito") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
        )

        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Frecuencia:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        DaySelector(selectedDays = selectedDays) { dayCode ->
            if (selectedDays.contains(dayCode)) {
                if (selectedDays.size > 1) { // Prevent empty
                     selectedDays = selectedDays - dayCode
                }
            } else {
                selectedDays = selectedDays + dayCode
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Recordatorio (Opcional):", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        TextField(
            value = reminderTime,
            onValueChange = { reminderTime = it },
            placeholder = { Text("Ej: 08:00") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
        )

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { onUpdate(title, if(reminderTime.isBlank()) null else reminderTime, selectedDays) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.inverseSurface)
        ) {
            Text("ACTUALIZAR")
        }
    }
}
