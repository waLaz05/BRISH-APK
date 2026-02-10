package com.katchy.focuslive.ui.finance

import kotlinx.coroutines.delay

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush // Added for Gradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.TabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.hilt.navigation.compose.hiltViewModel
import com.katchy.focuslive.data.model.Transaction
import com.katchy.focuslive.ui.theme.*
import java.text.NumberFormat
import java.util.Locale
import com.katchy.focuslive.ui.components.SimpleFlowRow

import com.katchy.focuslive.ui.components.BrishMascotWithBubble
import com.katchy.focuslive.ui.components.BrishPose

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(
    viewModel: FinanceViewModel = hiltViewModel()
) {
    val transactions by viewModel.transactions.collectAsState()
    val selectedMascot by viewModel.selectedMascot.collectAsState()

    var showAddSheet by remember { mutableStateOf(false) }
    var transactionToEdit by remember { mutableStateOf<Transaction?>(null) }
    
    // Savings Goals State
    var showAddGoalSheet by remember { mutableStateOf(false) }
    var goalToEdit by remember { mutableStateOf<com.katchy.focuslive.data.model.Goal?>(null) }
    
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val totalIncome = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    val totalExpense = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    val balance = totalIncome - totalExpense

    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)

    val expensesByCategory by viewModel.expensesByCategory.collectAsState()
    val incomeByCategory by viewModel.incomeByCategory.collectAsState()
    var chartType by remember { mutableStateOf("EXPENSE") } // EXPENSE or INCOME
    
    val budgets by viewModel.budgets.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0: Transactions, 1: Budgets

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
            .systemBarsPadding()
    ) {
        // --- Header ---
        Column(modifier = Modifier.padding(24.dp).padding(bottom = 0.dp)) {
            Text(
                text = "FINANZAS",
                style = MaterialTheme.typography.labelSmall,
                color = AntiPrimary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Text(
                text = "Control de Capital",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Black
            )
        }
        
        // Content Scrollable
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                // --- Balance Card ---
                val animatedBalance = remember { Animatable(0f) }
                LaunchedEffect(balance) {
                        animatedBalance.animateTo(
                            targetValue = balance.toFloat(),
                            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
                        )
                    }


                Surface(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),

                    contentColor = Color.White,
                    shadowElevation = 0.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Box(modifier = Modifier.background(
                        Brush.linearGradient(colors = listOf(Color(0xFF1F2937), Color(0xFF000000)))
                    )) {
                        Column(modifier = Modifier.padding(28.dp)) {
                            Text("Balance Total", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.6f))
                            Text(
                                text = currencyFormatter.format(animatedBalance.value),
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-1).sp
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Rounded.ArrowUpward, null, tint = Color(0xFF4ADE80), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Ingresos", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                                    }
                                    Text(currencyFormatter.format(totalIncome), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Rounded.ArrowDownward, null, tint = Color(0xFFFB7185), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Gastos", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                                    }
                                    Text(currencyFormatter.format(totalExpense), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                }
                            }
                        }
                    }
                }
            }
            
            // --- Chart Section ---
            val currentChartData = if (chartType == "EXPENSE") expensesByCategory else incomeByCategory
            val currentTotal = if (chartType == "EXPENSE") totalExpense else totalIncome
            
            if (expensesByCategory.isNotEmpty() || incomeByCategory.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Chart Toggle
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(if (chartType == "EXPENSE") MaterialTheme.colorScheme.surface else Color.Transparent)
                                    .clickable { chartType = "EXPENSE" }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    "Gastos", 
                                    style = MaterialTheme.typography.labelSmall, 
                                    fontWeight = FontWeight.Bold,
                                    color = if (chartType == "EXPENSE") MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(if (chartType == "INCOME") MaterialTheme.colorScheme.surface else Color.Transparent)
                                    .clickable { chartType = "INCOME" }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    "Ingresos", 
                                    style = MaterialTheme.typography.labelSmall, 
                                    fontWeight = FontWeight.Bold,
                                    color = if (chartType == "INCOME") MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    
                        if (currentChartData.isNotEmpty()) {
                            com.katchy.focuslive.ui.components.DonutChart(
                                data = currentChartData,
                                chartSize = 220.dp,
                                strokeWidth = 24.dp,
                                modifier = Modifier.padding(vertical = 16.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = if (chartType == "EXPENSE") "Gastos" else "Ingresos",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = currencyFormatter.format(currentTotal),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            
                            com.katchy.focuslive.ui.components.ChartLegend(
                                data = currentChartData.take(4), // Top 4
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        } else {
                            Text(
                                text = "Sin datos de ${if (chartType == "EXPENSE") "gastos" else "ingresos"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(32.dp)
                            )
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(24.dp))
                BrishMascotWithBubble(pose = BrishPose.FINANCE, mascotType = selectedMascot, modifier = Modifier.padding(horizontal = 24.dp))
                Spacer(modifier = Modifier.height(24.dp))
            }

            // --- Tabs ---
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = AntiPrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = AntiPrimary
                        )
                    },
                    divider = { HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("MOVIMIENTOS", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("PRESUPUESTOS", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("METAS", fontWeight = FontWeight.Bold) }
                    )
                }
            }
            
            item {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { width -> width } + fadeIn()).togetherWith(slideOutHorizontally { width -> -width } + fadeOut())
                        } else {
                            (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(slideOutHorizontally { width -> width } + fadeOut())
                        }
                    },
                    label = "financeTabs"
                ) { targetTab ->
                    when (targetTab) {
                        0 -> {
                            // Transactions List
                            Column(
                                modifier = Modifier.padding(horizontal = 24.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Header and Add Button
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Recientes",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(
                                        onClick = { showAddSheet = true },
                                        modifier = Modifier.background(AntiPrimary.copy(alpha = 0.1f), CircleShape)
                                    ) {
                                        Icon(Icons.Rounded.Add, null, tint = AntiPrimary)
                                    }
                                }
                                
                                if (transactions.isEmpty()) {
                                    FinanceEmptyState()
                                } else {
                                    // Use a column for the items since we are inside an AnimatedContent item
                                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                         transactions.forEachIndexed { index, transaction ->
                                            var isVisible by remember(transaction.id) { mutableStateOf(false) }
                                            LaunchedEffect(transaction.id) {
                                                delay(index * 60L)
                                                isVisible = true
                                            }

                                            androidx.compose.animation.AnimatedVisibility(
                                                visible = isVisible,
                                                enter = fadeIn(tween(400)) + slideInVertically(initialOffsetY = { it / 4 }),
                                                exit = fadeOut()
                                            ) {
                                                TransactionItem(
                                                    transaction = transaction, 
                                                    formatter = currencyFormatter,
                                                    onEdit = { transactionToEdit = transaction }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        1 -> {
                            // Budgets List
                            Column(
                                modifier = Modifier.padding(horizontal = 24.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                if (budgets.isEmpty()) {
                                    Text("No hay presupuestos definidos", style = MaterialTheme.typography.bodyMedium)
                                } else {
                                    budgets.forEach { (category, limit) ->
                                        val spent = expensesByCategory.find { it.label == category }?.value?.toDouble() ?: 0.0
                                        val color = expensesByCategory.find { it.label == category }?.color ?: Color.Gray
                                        
                                        BudgetCard(
                                            category = category, 
                                            spent = spent, 
                                            limit = limit,
                                            color = color
                                        )
                                    }
                                }
                            }
                        }
                        2 -> {
                            // Savings Goals (METAS)
                            // We need to access savingsGoals here. Since this is a composable lambda, we can access outer scope.
                            // But viewModel.savingsGoals call is outside.
                            // We captured `transactions`. We need to define `savingsGoals` outside or call it here.
                            // `savingsGoals` was defined line 361 in original code.
                            // We moved the `item` block, so we need to ensure variables are available.
                            // Let's hoist the savingsGoals collection before this item.
                            
                            val localGoals = viewModel.savingsGoals.collectAsState().value
                            
                            Column(
                                modifier = Modifier.padding(horizontal = 24.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Mis Objetivos",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(
                                        onClick = { showAddGoalSheet = true }, 
                                        modifier = Modifier.background(AntiPrimary.copy(alpha = 0.1f), CircleShape)
                                    ) {
                                        Icon(Icons.Rounded.Add, null, tint = AntiPrimary)
                                    }
                                }

                                localGoals.forEach { goal ->
                                    SavingsGoalCard(
                                        goal = goal,
                                        formatter = currencyFormatter,
                                        onDeposit = { viewModel.depositToGoal(goal, 50.0) },
                                        onClick = { goalToEdit = goal }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            AddTransactionContent(onAdd = { title, amount, type, category, recurrence ->
                viewModel.addTransaction(title, amount, type, category, recurrence != "NONE", recurrence)
                showAddSheet = false
            })
        }
    }

    if (transactionToEdit != null) {
        ModalBottomSheet(
            onDismissRequest = { transactionToEdit = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            EditTransactionContent(
                transaction = transactionToEdit!!,
                onUpdate = { title, amount, category, recurrence ->
                    viewModel.updateTransaction(transactionToEdit!!, title, amount, category, recurrence)
                    transactionToEdit = null
                },
                onDelete = {
                    viewModel.deleteTransaction(transactionToEdit!!.id)
                    transactionToEdit = null
                }
            )
        }
    }


    // --- Goal Sheets ---

    if (showAddGoalSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddGoalSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            AddSavingsGoalContent(onAdd = { title, target, icon, color ->
                viewModel.addSavingsGoal(title, target, icon, color)
                showAddGoalSheet = false
            })
        }
    }

    if (goalToEdit != null) {
        ModalBottomSheet(
            onDismissRequest = { goalToEdit = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            EditSavingsGoalContent(
                goal = goalToEdit!!,
                onUpdate = { title, target, icon, color ->
                    viewModel.updateSavingsGoal(goalToEdit!!, title, target, icon, color)
                    goalToEdit = null
                },
                onDelete = {
                    viewModel.deleteSavingsGoal(goalToEdit!!.id)
                    goalToEdit = null
                }
            )
        }
    }
}

@Composable
fun SavingsGoalCard(
    goal: com.katchy.focuslive.data.model.Goal, 
    formatter: NumberFormat,
    onDeposit: () -> Unit,
    onClick: () -> Unit
) {
    val progress = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f) else 0f
    val color = try {
        Color(android.graphics.Color.parseColor(goal.colorHex))
    } catch (e: Exception) {
        Color(0xFF4ADE80)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(goal.icon, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(goal.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("${(progress * 100).toInt()}% completado", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                IconButton(
                    onClick = onDeposit,
                    modifier = Modifier
                        .size(36.dp)
                        .background(color.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(Icons.Rounded.Add, null, tint = color, modifier = Modifier.size(20.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(6.dp))
                        .background(color)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Amount
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatter.format(goal.currentAmount), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text("Meta: ${formatter.format(goal.targetAmount)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun TransactionItem(
    transaction: Transaction, 
    formatter: NumberFormat, 
    onEdit: () -> Unit
) {
    val isExpense = transaction.type == "EXPENSE"
    val color = if (isExpense) Color(0xFFFB7185) else Color(0xFF4ADE80)
    val icon = if (isExpense) Icons.Rounded.Remove else Icons.Rounded.Add
    val haptics = androidx.compose.ui.platform.LocalHapticFeedback.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .combinedClickable(
                onClick = {},
                onLongClick = {
                    haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    onEdit()
                }
            ),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(transaction.title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(transaction.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            Text(
                text = (if (isExpense) "-" else "+") + formatter.format(transaction.amount),
                fontWeight = FontWeight.Black,
                color = if (isExpense) MaterialTheme.colorScheme.onSurface else Color(0xFF22C55E)
            )
        }
    }
}

@Composable
fun FinanceEmptyState() {
    val infiniteTransition = rememberInfiniteTransition(label = "emptyState")
    val animOffsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "floatingBank"
    )
    
    val offsetY = animOffsetY
    
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.offset(y = offsetY.dp)) {
             Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f)),
                contentAlignment = Alignment.Center
            ) {
                 Icon(Icons.Rounded.AccountBalance, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.5f), modifier = Modifier.size(40.dp))
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("No hay movimientos aún", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("Tus transacciones aparecerán aquí", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
fun AddTransactionContent(onAdd: (String, Double, String, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("EXPENSE") }
    var category by remember { mutableStateOf("Food") }
    var recurrence by remember { mutableStateOf("NONE") }

    val expenseCategories = listOf("Food", "Transport", "Shopping", "Bills", "Health", "Entertainment", "Other")
    val incomeCategories = listOf("Salary", "Freelance", "Investment", "Gift", "Other")
    val currentCategories = if (type == "EXPENSE") expenseCategories else incomeCategories

    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp, top = 24.dp)
            .fillMaxWidth()
    ) {
        // Drag Handle Removed

        // Minimal Title Input (Like Notes)
        Text(
            text = "NUEVO MOVIMIENTO", 
            style = MaterialTheme.typography.labelSmall, 
            color = AntiPrimary, 
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        TextField(
            value = title,
            onValueChange = { title = it },
            placeholder = { 
                Text(
                    "Concepto (ej. Almuerzo)", 
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ) 
            },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent, 
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Medium),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)
        
        Spacer(modifier = Modifier.height(24.dp))

        // Amount Input (Large & clean)
        Row(
            modifier = Modifier.fillMaxWidth(), 
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("$", style = MaterialTheme.typography.displayMedium, color = AntiTextSecondary.copy(alpha=0.5f), fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(4.dp))
            TextField(
                value = amount,
                onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) amount = it },
                placeholder = { Text("0.00", style = MaterialTheme.typography.displayMedium, color = Color(0xFFD1D5DB)) },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent, 
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                textStyle = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                singleLine = true
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Type Selection
        Text("Tipo", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            val isExpense = type == "EXPENSE"
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isExpense) Color(0xFFFB7185) else MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { 
                        type = "EXPENSE"
                        category = expenseCategories.first()
                    }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Gasto", 
                    color = if (isExpense) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (type == "INCOME") Color(0xFF4ADE80) else MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { 
                        type = "INCOME"
                        category = incomeCategories.first()
                    }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Ingreso", 
                    color = if (type == "INCOME") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Categories - Flow Layout
        Text("Categoría", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(16.dp))
        SimpleFlowRow(
            verticalGap = 10.dp,
            horizontalGap = 10.dp,
            alignment = Alignment.Start
        ) {
            currentCategories.forEach { cat ->
                val isSelected = category == cat
                 Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) AntiPrimary else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { category = cat }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = cat,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        // Recurrence
        Text("Recurrencia", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
             listOf("NONE" to "No", "MONTHLY" to "Mensual", "YEARLY" to "Anual").forEach { (key, label) ->
                val isSelected = recurrence == key
                 Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) Color.Black else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { recurrence = key }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
             }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        val isValid = title.isNotBlank() && (amount.toDoubleOrNull() ?: 0.0) > 0.0

        Button(
            onClick = { if (isValid) onAdd(title, amount.toDoubleOrNull() ?: 0.0, type, category, recurrence) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isValid) MaterialTheme.colorScheme.inverseSurface else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (isValid) MaterialTheme.colorScheme.inverseOnSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.5f)
            ),
            elevation = ButtonDefaults.buttonElevation(0.dp),
            enabled = isValid
        ) {
            Text("REGISTRAR", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
    }
}

@Composable
fun EditTransactionContent(
    transaction: Transaction,
    onUpdate: (String, Double, String, String) -> Unit,
    onDelete: () -> Unit
) {
    var title by remember { mutableStateOf(transaction.title) }
    var amount by remember { mutableStateOf(transaction.amount.toString()) }
    var category by remember { mutableStateOf(transaction.category) }
    var recurrence by remember { mutableStateOf(transaction.recurrenceInterval) }
    
    val expenseCategories = listOf("Food", "Transport", "Shopping", "Bills", "Health", "Entertainment", "Other")
    val incomeCategories = listOf("Salary", "Freelance", "Investment", "Gift", "Other")
    val currentCategories = if (transaction.type == "EXPENSE") expenseCategories else incomeCategories

    Column(modifier = Modifier.padding(24.dp).padding(bottom = 32.dp)) {
        // Drag Handle Removed 
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("EDITAR MOVIMIENTO", style = MaterialTheme.typography.labelSmall, color = AntiPrimary, fontWeight = FontWeight.Bold)
            IconButton(onClick = onDelete) {
                Icon(Icons.Rounded.DeleteOutline, null, tint = Color(0xFFEF4444))
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        TextField(
            value = title,
            onValueChange = { title = it },
            placeholder = { Text("Concepto") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TextField(
            value = amount,
             onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) amount = it },
            placeholder = { Text("Monto") },
            modifier = Modifier.fillMaxWidth(),
             colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("Categoría", style = MaterialTheme.typography.labelMedium)
        SimpleFlowRow(
            verticalGap = 8.dp,
            horizontalGap = 8.dp,
            alignment = Alignment.Start
        ) {
            currentCategories.forEach { cat ->
                FilterChip(selected = category == cat, onClick = { category = cat }, label = { Text(cat, color = if (category == cat) Color.White else MaterialTheme.colorScheme.onSurface) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AntiPrimary, containerColor = MaterialTheme.colorScheme.surfaceVariant))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("Recurrencia", style = MaterialTheme.typography.labelMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
             listOf("NONE" to "No", "MONTHLY" to "Mensual", "YEARLY" to "Anual").forEach { (key, label) ->
                FilterChip(selected = recurrence == key, onClick = { recurrence = key }, label = { Text(label, color = if (recurrence == key) Color.White else MaterialTheme.colorScheme.onSurface) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color.Black, containerColor = MaterialTheme.colorScheme.surfaceVariant))
             }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        val isValid = title.isNotBlank() && (amount.toDoubleOrNull() ?: 0.0) > 0.0

        Button(
            onClick = { if (isValid) onUpdate(title, amount.toDoubleOrNull() ?: 0.0, category, recurrence) },
             modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if(isValid) MaterialTheme.colorScheme.inverseSurface else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if(isValid) MaterialTheme.colorScheme.inverseOnSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.5f)
            ),
            enabled = isValid
        ) {
            Text("GUARDAR CAMBIOS", fontWeight = FontWeight.Bold)
        }
    }
    }



@Composable
fun AddSavingsGoalContent(onAdd: (String, Double, String, Long) -> Unit) {
    var title by remember { mutableStateOf("") }
    var targetAmount by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("💰") }
    var selectedColor by remember { mutableStateOf(0xFF4ADE80) } // Green

    val icons = listOf("💰", "🚗", "✈️", "🏠", "💻", "🎮", "🎓", "🏥", "💍", "👶")
    val colors = listOf(0xFF4ADE80, 0xFF3B82F6, 0xFFF59E0B, 0xFFEC4899, 0xFFA78BFA, 0xFFF87171, 0xFF22D3EE)

    Column(modifier = Modifier.padding(24.dp).padding(bottom = 32.dp)) {
        Text("NUEVA META", style = MaterialTheme.typography.labelSmall, color = AntiPrimary, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        
        // Icon Picker
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(icons) { icon ->
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (selectedIcon == icon) AntiPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                        .border(if (selectedIcon == icon) 2.dp else 0.dp, if (selectedIcon == icon) AntiPrimary else Color.Transparent, CircleShape)
                        .clickable { selectedIcon = icon },
                    contentAlignment = Alignment.Center
                ) {
                    Text(icon, fontSize = 20.sp)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        TextField(
            value = title,
            onValueChange = { title = it },
            placeholder = { Text("Nombre de la meta (ej. Coche)") },
            modifier = Modifier.fillMaxWidth(),
             colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TextField(
            value = targetAmount,
            onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) targetAmount = it },
            placeholder = { Text("Objetivo ($)") },
            modifier = Modifier.fillMaxWidth(),
             colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Color Picker
         Text("Color", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
         Spacer(modifier = Modifier.height(12.dp))
         LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(colors) { colorHex ->
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(colorHex))
                        .border(if (selectedColor == colorHex) 2.dp else 0.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                        .clickable { selectedColor = colorHex }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        val isValid = title.isNotBlank() && (targetAmount.toDoubleOrNull() ?: 0.0) > 0.0

        Button(
            onClick = { if (isValid) onAdd(title, targetAmount.toDoubleOrNull() ?: 0.0, selectedIcon, selectedColor) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isValid) MaterialTheme.colorScheme.inverseSurface else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (isValid) MaterialTheme.colorScheme.inverseOnSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.5f)
            ),
            enabled = isValid
        ) {
            Text("CREAR META", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun EditSavingsGoalContent(
    goal: com.katchy.focuslive.data.model.Goal,
    onUpdate: (String, Double, String, Long) -> Unit,
    onDelete: () -> Unit
) {
    var title by remember { mutableStateOf(goal.title) }
    var targetAmount by remember { mutableStateOf(goal.targetAmount.toString()) }
    var selectedIcon by remember { mutableStateOf(goal.icon) }
    var selectedColor by remember { 
        mutableStateOf(
            try {
                android.graphics.Color.parseColor(goal.colorHex).toLong() and 0xFFFFFFFFL
            } catch (e: Exception) {
                0xFF4ADE80
            }
        ) 
    }

    val icons = listOf("💰", "🚗", "✈️", "🏠", "💻", "🎮", "🎓", "🏥", "💍", "👶")
    val colors = listOf(0xFF4ADE80, 0xFF3B82F6, 0xFFF59E0B, 0xFFEC4899, 0xFFA78BFA, 0xFFF87171, 0xFF22D3EE)

    Column(modifier = Modifier.padding(24.dp).padding(bottom = 32.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("EDITAR META", style = MaterialTheme.typography.labelSmall, color = AntiPrimary, fontWeight = FontWeight.Bold)
            IconButton(onClick = onDelete) {
                 Icon(Icons.Rounded.DeleteOutline, null, tint = Color(0xFFEF4444))
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Icon Picker
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(icons) { icon ->
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (selectedIcon == icon) AntiPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                        .border(if (selectedIcon == icon) 2.dp else 0.dp, if (selectedIcon == icon) AntiPrimary else Color.Transparent, CircleShape)
                        .clickable { selectedIcon = icon },
                    contentAlignment = Alignment.Center
                ) {
                    Text(icon, fontSize = 20.sp)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        TextField(
            value = title,
            onValueChange = { title = it },
            placeholder = { Text("Nombre de la meta") },
            modifier = Modifier.fillMaxWidth(),
             colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TextField(
            value = targetAmount,
            onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) targetAmount = it },
            placeholder = { Text("Objetivo ($)") },
            modifier = Modifier.fillMaxWidth(),
             colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Color Picker
         Text("Color", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
         Spacer(modifier = Modifier.height(12.dp))
         LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(colors) { colorHex ->
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(colorHex))
                        .border(if (selectedColor == colorHex) 2.dp else 0.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                        .clickable { selectedColor = colorHex }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        val isValid = title.isNotBlank() && (targetAmount.toDoubleOrNull() ?: 0.0) > 0.0
        
        Button(
            onClick = { if (isValid) onUpdate(title, targetAmount.toDoubleOrNull() ?: 0.0, selectedIcon, selectedColor) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
             colors = ButtonDefaults.buttonColors(
                containerColor = if (isValid) MaterialTheme.colorScheme.inverseSurface else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (isValid) MaterialTheme.colorScheme.inverseOnSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.5f)
            ),
            enabled = isValid
        ) {
            Text("GUARDAR CAMBIOS", fontWeight = FontWeight.Bold)
        }
    }
}


