@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.katchy.focuslive.ui.planner

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.katchy.focuslive.data.model.Task
import com.katchy.focuslive.data.model.SubtaskItem
import com.katchy.focuslive.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import android.app.TimePickerDialog as AndroidTimePickerDialog
import com.katchy.focuslive.ui.components.SimpleFlowRow
import com.katchy.focuslive.ui.components.BrishMascotWithBubble
import com.katchy.focuslive.ui.components.BrishPose
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.Canvas
import kotlinx.coroutines.delay

@Composable
fun PlannerScreen(
    viewModel: PlannerViewModel = hiltViewModel()
) {

    val selectedDate by viewModel.selectedDate.collectAsState()
    val tasks by viewModel.tasksForSelectedDate.collectAsState()
    val selectedMascot by viewModel.selectedMascot.collectAsState()

    
    var showAddTaskSheet by remember { mutableStateOf(false) }
    var showBrainDumpDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    val isBrainDumping by viewModel.isBrainDumping.collectAsState()
    val brainDumpError by viewModel.brainDumpError.collectAsState()
    
    // Auto-close dialog if successful (not loading, no error, and was showing)
    LaunchedEffect(isBrainDumping, brainDumpError) {
        if (!isBrainDumping && brainDumpError == null && showBrainDumpDialog) {
             // We can check if we actually submitted something, but usually loading->not loading means done.
             // However, `showBrainDumpDialog` is initially true.
             // We might want to close only if we were loading before. 
             // Simplified: keep open if error, otherwise user closes or we close on success.
             // Since we track `isBrainDumping`, we can just close if it goes false AND error is null.
             // But valid "idle" state is also !isBrainDumping.
             // Let's rely on the user closing it or ViewModel signaling success.
             // ViewModel emits UI message on success.
        }
    }
    
    // Consume UI messages
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.uiMessage.collect { message ->
            if (message.contains("¡Listo!")) showBrainDumpDialog = false 
            snackbarHostState.showSnackbar(message)
        }
    }
    
    val calendar = Calendar.getInstance()
    val days = (0..6).map {
        val cal = calendar.clone() as Calendar
        cal.add(Calendar.DAY_OF_YEAR, it)
        cal.timeInMillis
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTaskSheet = true },
                containerColor = AntiPrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 80.dp)
            ) {
                Icon(Icons.Rounded.Add, "Añadir Tarea")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PLANIFICADOR",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "Mi Agenda",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Black
                    )
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { showBrainDumpDialog = true },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    ) {
                        Icon(Icons.Rounded.Psychology, "Brain Dump", tint = MaterialTheme.colorScheme.primary)
                    }

                    IconButton(
                        onClick = { viewModel.autoPrioritizeTasks() },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    ) {
                        Icon(Icons.Rounded.AutoAwesome, "Auto Priorizar")
                    }
                }
            }

            // Horizontal Date Picker
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                items(days) { dateMillis ->
                    val isSelected = isSameDay(dateMillis, selectedDate)
                    val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
                    val dayNumFormat = SimpleDateFormat("d", Locale.getDefault())
                    
                    Surface(
                        onClick = { viewModel.selectDate(dateMillis) },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) AntiPrimary else MaterialTheme.colorScheme.surface,
                        contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier
                                .width(64.dp)
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(dayFormat.format(Date(dateMillis)).uppercase(), style = MaterialTheme.typography.labelSmall)
                            Text(dayNumFormat.format(Date(dateMillis)), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Mascot Greeting
            Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                BrishMascotWithBubble(mascotType = selectedMascot)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Task List
            if (tasks.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    PlannerEmptyState()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(tasks, key = { _, task -> task.id }) { index, task ->
                        val loadingTaskIds by viewModel.loadingTaskIds.collectAsState()
                        
                        var isVisible by remember(task.id) { mutableStateOf(false) }
                        LaunchedEffect(task.id) {
                            delay(20L * index)
                            isVisible = true
                        }

                        AnimatedVisibility(
                            visible = isVisible,
                            enter = fadeIn(tween(250)) + scaleIn(tween(250, easing = androidx.compose.animation.core.EaseOutQuart)),
                            exit = fadeOut(tween(150))
                        ) {
                            PlannerTaskItem(
                                task = task, 
                                onToggle = { viewModel.toggleTask(task) },
                                onDelete = { viewModel.deleteTask(task) },
                                onEdit = { taskToEdit = task },
                                onBreakdown = { viewModel.autoBreakdownTask(task) },
                                onToggleSubtask = { subtaskId -> viewModel.toggleSubtask(task, subtaskId) },
                                onAddSubtask = { title -> viewModel.addSubtask(task, title) },
                                onUpdateSubtask = { subtaskId, newTitle -> viewModel.updateSubtask(task, subtaskId, newTitle) },
                                onDeleteSubtask = { subtaskId -> viewModel.deleteSubtask(task, subtaskId) },
                                isBreakingDown = loadingTaskIds.contains(task.id)
                            )
                        }
                    }
                    item { Spacer(modifier = Modifier.height(100.dp)) }
                }
            }
        }
    }
    
    if (showAddTaskSheet) {
        ModalBottomSheet(onDismissRequest = { showAddTaskSheet = false }, sheetState = sheetState) {
            AddPlannerTaskContent { title, repeatMode, repeatDays, time, endTime, category, priority, subtasks ->
                viewModel.addTask(title, repeatMode, repeatDays, time, endTime, category, priority, subtasks)
                showAddTaskSheet = false
            }
        }
    }
    
    taskToEdit?.let { task ->
        ModalBottomSheet(onDismissRequest = { taskToEdit = null }) {
            AddPlannerTaskContent(
                initialTask = task,
                onTaskAdded = { title, repeatMode, repeatDays, time, endTime, category, priority, subtasks ->
                    // For edit, we actully ignore subtasks from this callback if we assume subtasks are edited inline 
                    // OR we replace them. 
                    // Currently `updateTask` doesn't take subtasks argument easily unless we modify it.
                    // But `AddPlannerTaskContent` now manages subtasks state locally.
                    // If we want to allow rewriting subtasks here, we should update the task with these subtasks.
                    // However, `subtasks` param here is just a list of Strings.
                    // If we blindly replace, we lose completion status of existing subtasks!
                    // So for "Edit Task", we should probably mostly edit title/time.
                    // BUT, if we want to allow editing subtasks here too... checking implementation plan.
                    // Plan says "Allow editing/deleting subtasks existing" -> in PlannerTaskItem.
                    // So here in Edit Sheet we can just preserve existing subtasks or try to merge.
                    // Simplest safe approach: Preserve existing subtasks (ignore AddPlannerTaskContent's subtasks output for EDIT, 
                    // or better: map strings back to existing if possible, but that's complex).
                    // Let's assume for now Edit Sheet is mostly for Title/Time and Inline is for Subtasks.
                    
                    viewModel.updateTask(task.copy(
                        title = title,
                        repeatMode = repeatMode,
                        repeatDays = repeatDays,
                        time = time,
                        endTime = endTime,
                        category = category,
                        priority = priority
                        // Not updating subtasks here to avoid losing IDs and completed state
                    ))
                    taskToEdit = null
                }
            )
        }
    }

    if (showBrainDumpDialog) {
        BrainDumpDialog(
            onDismiss = { 
                showBrainDumpDialog = false
                viewModel.clearBrainDumpError()
            },
            onConfirm = { text -> viewModel.processBrainDump(text) },
            isLoading = isBrainDumping,
            errorMessage = brainDumpError
        )
    }
}

private fun isSameDay(m1: Long, m2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = m1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = m2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}


@Composable
fun PlannerTaskItem(
    task: Task,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onBreakdown: () -> Unit,
    onToggleSubtask: (String) -> Unit, 
    onAddSubtask: (String) -> Unit,
    onUpdateSubtask: (String, String) -> Unit,
    onDeleteSubtask: (String) -> Unit,
    onDelete: () -> Unit,
    isBreakingDown: Boolean
) {
    var isExpanded by remember { mutableStateOf(false) }
    val haptics = androidx.compose.ui.platform.LocalHapticFeedback.current
    
    // Subtask Edit Dialog State
    var subtaskToEdit by remember { mutableStateOf<Pair<String, String>?>(null) } // ID, Title

    if (subtaskToEdit != null) {
        Dialog(onDismissRequest = { subtaskToEdit = null }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Editar paso", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    var text by remember(subtaskToEdit) { mutableStateOf(subtaskToEdit?.second ?: "") }
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = { subtaskToEdit = null }) { Text("Cancelar") }
                        TextButton(onClick = { 
                            if (text.isNotBlank()) {
                                onUpdateSubtask(subtaskToEdit!!.first, text)
                                subtaskToEdit = null
                            }
                        }) { Text("Guardar") }
                    }
                }
            }
        }
    }
    
    // iOS-style Spring Specs
    val layoutSpring = spring<IntSize>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )
    
    val touchSpring = spring<Float>(
        dampingRatio = 0.7f, 
        stiffness = Spring.StiffnessMedium
    )

    val checkboxScale by animateFloatAsState(
        targetValue = if (task.isCompleted) 1.1f else 1f,
        animationSpec = touchSpring,
        label = "checkbox"
    )
    
    val checkboxColor by animateColorAsState(
        targetValue = if (task.isCompleted) AntiSuccess else MaterialTheme.colorScheme.outline.copy(alpha=0.4f),
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "checkboxColor"
    )
    
    val borderColor by animateColorAsState(
        targetValue = if (isBreakingDown) AntiPrimary else Color.Transparent,
        label = "border"
    )

    // Debounce handled in ViewModel now, but visual debounce is also good
    val lastClickTime = remember { mutableStateOf(0L) }
    val debounceTime = 300L 

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = layoutSpring)
            .clip(RoundedCornerShape(16.dp)) 
            .combinedClickable(
                onClick = {
                    isExpanded = !isExpanded
                },
                onLongClick = {
                    haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    onEdit()
                }
            ),
        shape = RoundedCornerShape(16.dp), 
        color = MaterialTheme.colorScheme.surface,
        border = if (isBreakingDown) BorderStroke(1.5.dp, borderColor) else null,
        shadowElevation = if(isExpanded) 4.dp else 1.dp,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) { 
            Row(verticalAlignment = Alignment.Top) {
                // Checkbox Touch Target
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .offset(y = (-2).dp)
                        .graphicsLayer {
                            scaleX = checkboxScale
                            scaleY = checkboxScale
                        }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null 
                        ) { 
                            // UI Debounce
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastClickTime.value > debounceTime) {
                                lastClickTime.value = currentTime
                                if (!task.isCompleted) {
                                   haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                }
                                onToggle()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = checkboxColor,
                            style = if (task.isCompleted) androidx.compose.ui.graphics.drawscope.Fill else androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                        )
                        if (task.isCompleted) {
                            val path = androidx.compose.ui.graphics.Path().apply {
                                moveTo(size.width * 0.25f, size.height * 0.5f)
                                lineTo(size.width * 0.45f, size.height * 0.7f)
                                lineTo(size.width * 0.75f, size.height * 0.35f)
                            }
                            drawPath(path, Color.White, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (task.isCompleted) FontWeight.Medium else FontWeight.SemiBold,
                        color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.6f) else MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                        maxLines = if(isExpanded) Int.MAX_VALUE else 2
                    )
                    
                    if (task.time != null) {
                         Spacer(modifier = Modifier.height(4.dp))
                         Row(verticalAlignment = Alignment.CenterVertically) {
                             Text(
                                text = "${task.time}${if(task.endTime != null) " - ${task.endTime}" else ""}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                             )
                         }
                    }
                }
                
                IconButton(
                    onClick = { isExpanded = !isExpanded }, 
                    modifier = Modifier
                        .size(24.dp)
                        .offset(y = (-2).dp)
                ) {
                    Icon(
                        if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore, 
                        null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.5f)
                    )
                }
            }
            
            // Expansion Content
            if (isExpanded) { 
                val fadeAlpha by animateFloatAsState(targetValue = if(isExpanded) 1f else 0f, animationSpec = tween(200))

                Column(
                    modifier = Modifier
                        .padding(top = 16.dp, start = 44.dp) // Align with text start
                        .alpha(fadeAlpha)
                ) { 
                    
                     if (task.subtasks.isNotEmpty()) {
                         task.subtasks.forEach { subtask ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp), // Less padding
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Subtask bullet / check
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .border(1.dp, if(subtask.isCompleted) AntiSuccess else MaterialTheme.colorScheme.outline.copy(alpha=0.3f), CircleShape)
                                        .background(if(subtask.isCompleted) AntiSuccess else Color.Transparent, CircleShape)
                                        .clickable { onToggleSubtask(subtask.id) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if(subtask.isCompleted) {
                                        Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(10.dp))
                                    }
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                // Text (Clickable to Edit)
                                Text(
                                    text = subtask.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (subtask.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                    textDecoration = if (subtask.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                                    modifier = Modifier.weight(1f).clickable { 
                                         subtaskToEdit = Pair(subtask.id, subtask.title)
                                    }
                                )
                                
                                // Delete Subtask
                                IconButton(
                                    onClick = { onDeleteSubtask(subtask.id) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Rounded.Close, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.3f), modifier = Modifier.size(16.dp))
                                }
                            }
                         }
                         Spacer(modifier = Modifier.height(12.dp))
                     }

                    if (!task.isCompleted) {
                        // Quick Add Subtask
                        var newSubtaskTitle by remember { mutableStateOf("") }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                             androidx.compose.foundation.text.BasicTextField(
                                value = newSubtaskTitle,
                                onValueChange = { newSubtaskTitle = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 8.dp),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                                singleLine = true,
                                decorationBox = { innerTextField ->
                                    Box(contentAlignment = Alignment.CenterStart) {
                                        if (newSubtaskTitle.isEmpty()) {
                                            Text("+ Añadir paso", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary.copy(alpha=0.7f))
                                        }
                                        innerTextField()
                                    }
                                }
                             )
                             
                             if (newSubtaskTitle.isNotBlank()) {
                                 IconButton(
                                    onClick = { 
                                        onAddSubtask(newSubtaskTitle)
                                        newSubtaskTitle = ""
                                    },
                                    modifier = Modifier.size(24.dp)
                                 ) {
                                    Icon(Icons.Rounded.ArrowUpward, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                 }
                             }
                        }
                        
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.3f))
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Actions (AI Breakdown / Delete)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                             if (task.subtasks.isEmpty()) {
                                TextButton(
                                    onClick = onBreakdown,
                                    enabled = !isBreakingDown,
                                    contentPadding = PaddingValues(0.dp) 
                                ) {
                                    if (isBreakingDown) {
                                        CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.dp)
                                    } else {
                                        Icon(Icons.Rounded.AutoAwesome, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Desglosar con IA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                             } else {
                                 Spacer(modifier = Modifier.width(1.dp)) 
                             }

                            TextButton(
                                onClick = onDelete, 
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error.copy(alpha=0.7f)),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Eliminar", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            } 
        }
    }
}

@Composable
fun PlannerEmptyState() {
     Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
         Box(
            modifier = Modifier
                .size(80.dp) // Size consistent with visual hierarchy
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f)),
            contentAlignment = Alignment.Center
        ) {
             Icon(Icons.Rounded.EventAvailable, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.5f), modifier = Modifier.size(40.dp))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "¡Día despejado!", 
            style = MaterialTheme.typography.titleMedium, 
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "No hay tareas programadas para hoy.", 
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "¡Disfruta tu tiempo libre!",
            color = MaterialTheme.colorScheme.primary, // Add a pop of color
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun AddPlannerTaskContent(
    initialTask: Task? = null,
    onTaskAdded: (String, String, List<Int>, String?, String?, String, String, List<String>) -> Unit
) {
    var title by remember { mutableStateOf(initialTask?.title ?: "") }
    var repeatMode by remember { mutableStateOf(initialTask?.repeatMode ?: "NONE") } // NONE, DAILY, CUSTOM
    var repeatDays by remember { mutableStateOf<List<Int>>(initialTask?.repeatDays ?: emptyList()) }
    var time by remember { mutableStateOf(initialTask?.time) }
    var endTime by remember { mutableStateOf(initialTask?.endTime) }
    
    // Subtasks State
    var subtasks by remember { mutableStateOf<List<String>>(initialTask?.subtasks?.map { it.title } ?: emptyList()) }
    var newSubtaskText by remember { mutableStateOf("") }
    
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val showTimePicker = { isStartTime: Boolean ->
        val existingTime = if (isStartTime) time else endTime
        val hour = existingTime?.split(":")?.get(0)?.toIntOrNull() ?: calendar.get(Calendar.HOUR_OF_DAY)
        val minute = existingTime?.split(":")?.get(1)?.toIntOrNull() ?: calendar.get(Calendar.MINUTE)

        AndroidTimePickerDialog(context, { _, h, m ->
            val formatted = String.format("%02d:%02d", h, m)
            if (isStartTime) time = formatted else endTime = formatted
        }, hour, minute, true).show()
    }

    Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
        Text(
            text = if(initialTask==null) "NUEVA TAREA" else "EDITAR TAREA", 
            style = MaterialTheme.typography.labelSmall, 
            fontWeight = FontWeight.Bold, 
            color = AntiPrimary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Title Input
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Actividad") },
            placeholder = { Text("Ej: Ir al gimnasio") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Time Selection
        Text("Horario", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { showTimePicker(true) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Rounded.Schedule, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(time ?: "Hora inicio")
            }
            
            Button(
                onClick = { showTimePicker(false) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(endTime ?: "Hora fin")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (initialTask == null) {
            // Subtasks Section
            Text("Subtareas / Pasos", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            
            // List existing subtasks
            if (subtasks.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    subtasks.forEachIndexed { index, subtaskTitle ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp)).padding(8.dp)
                        ) {
                            Icon(Icons.Rounded.DragHandle, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.5f), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(subtaskTitle, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                            IconButton(
                                onClick = { 
                                    subtasks = subtasks.filterIndexed { i, _ -> i != index }
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Rounded.Close, null, tint = MaterialTheme.colorScheme.error.copy(alpha=0.7f), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Add Subtask Input
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newSubtaskText,
                    onValueChange = { newSubtaskText = it },
                    placeholder = { Text("Añadir paso...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedBorderColor = AntiPrimary
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { 
                        if (newSubtaskText.isNotBlank()) {
                            subtasks = subtasks + newSubtaskText
                            newSubtaskText = ""
                        }
                    },
                    modifier = Modifier.background(AntiPrimary, RoundedCornerShape(8.dp)).size(50.dp) // Match height roughly
                ) {
                    Icon(Icons.Rounded.Add, null, tint = Color.White)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Repetition
        Text("Repetición", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("NONE" to "Una vez", "DAILY" to "Diario", "CUSTOM" to "Personalizado").forEach { (mode, label) ->
                FilterChip(
                    selected = repeatMode == mode,
                    onClick = { repeatMode = mode },
                    label = { Text(label) },
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }
        
        if (repeatMode == "CUSTOM") {
            Spacer(modifier = Modifier.height(12.dp))
            Text("Días activos", style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                val weekDays = listOf("D", "L", "M", "M", "J", "V", "S") // 0=Sun
                weekDays.forEachIndexed { index, dayLabel ->
                    val isSelected = repeatDays.contains(index + 1) // Calendar.SUNDAY = 1
                    
                    val dayValue = index + 1 // 1..7
                    
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) AntiPrimary else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                repeatDays = if (isSelected) repeatDays - dayValue else repeatDays + dayValue
                            }
                    ) {
                        Text(
                            text = dayLabel,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            // Ensure we sort and distinct the days before saving to be absolutely safe
            onClick = { 
                onTaskAdded(
                    title, 
                    repeatMode, 
                    if(repeatMode == "CUSTOM") repeatDays.distinct().sorted() else emptyList(), 
                    time, 
                    endTime, 
                    "General", 
                    "MEDIUM",
                    subtasks
                ) 
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = title.isNotBlank()
        ) {
            Text("GUARDAR TAREA", fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

