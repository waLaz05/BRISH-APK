package com.katchy.focuslive.ui.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.katchy.focuslive.data.model.Transaction
import com.katchy.focuslive.data.repository.FinanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class FinanceViewModel @Inject constructor(
    private val financeRepository: FinanceRepository,
    private val goalRepository: com.katchy.focuslive.data.repository.GoalRepository,
    private val mascotRepository: com.katchy.focuslive.data.repository.MascotRepository,
    private val appPreferencesRepository: com.katchy.focuslive.data.repository.AppPreferencesRepository
) : ViewModel() {

    val selectedMascot = mascotRepository.selectedMascot


    val transactions: StateFlow<List<Transaction>> = financeRepository.getTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expensesByCategory: StateFlow<List<com.katchy.focuslive.ui.components.ChartData>> = transactions.map { list ->
        getChartData(list, "EXPENSE")
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val incomeByCategory: StateFlow<List<com.katchy.focuslive.ui.components.ChartData>> = transactions.map { list ->
        getChartData(list, "INCOME")
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // Budgets (Placeholder for future implementation)
    private val _budgets = kotlinx.coroutines.flow.MutableStateFlow<Map<String, Double>>(emptyMap())
    val budgets = _budgets.asStateFlow()

    private fun getChartData(transactions: List<Transaction>, type: String): List<com.katchy.focuslive.ui.components.ChartData> {
        val filtered = transactions.filter { it.type == type }
        val grouped = filtered.groupBy { it.category }
        
        return grouped.map { (category, txs) ->
            val sum = txs.sumOf { it.amount }
            com.katchy.focuslive.ui.components.ChartData(
                value = sum.toFloat(),
                color = getCategoryColor(category),
                label = category
            )
        }.sortedByDescending { it.value }
    }

    private fun getCategoryColor(category: String): androidx.compose.ui.graphics.Color {
        return when(category) {
            // Expenses
            "Food" -> androidx.compose.ui.graphics.Color(0xFFF87171) // Red
            "Transport" -> androidx.compose.ui.graphics.Color(0xFF60A5FA) // Blue
            "Shopping" -> androidx.compose.ui.graphics.Color(0xFFFBBF24) // Amber
            "Bills" -> androidx.compose.ui.graphics.Color(0xFF34D399) // Emerald
            "Entertainment" -> androidx.compose.ui.graphics.Color(0xFFA78BFA) // Purple
            "Health" -> androidx.compose.ui.graphics.Color(0xFFF472B6) // Pink
            // Income
            "Salary" -> androidx.compose.ui.graphics.Color(0xFF10B981) // Emerald Green
            "Freelance" -> androidx.compose.ui.graphics.Color(0xFF3B82F6) // Royal Blue
            "Investment" -> androidx.compose.ui.graphics.Color(0xFF8B5CF6) // Violet
            "Gift" -> androidx.compose.ui.graphics.Color(0xFFEC4899) // Pink
            else -> androidx.compose.ui.graphics.Color(0xFF9CA3AF) // Gray
        }
    }

    fun addTransaction(title: String, amount: Double, type: String, category: String, isRecurring: Boolean = false, recurrenceInterval: String = "NONE") {
        if (title.isBlank() || amount <= 0) return
        viewModelScope.launch {
            val transaction = Transaction(
                title = title,
                amount = amount,
                type = type,
                category = category,
                isRecurring = isRecurring,
                recurrenceInterval = recurrenceInterval,
                timestamp = System.currentTimeMillis()
            )
            financeRepository.addTransaction(transaction)
        }
    }

    fun updateTransaction(transaction: Transaction, newTitle: String, newAmount: Double, newCategory: String, newRecurrence: String) {
        viewModelScope.launch {
            val updatedTransaction = transaction.copy(
                title = newTitle, 
                amount = newAmount, 
                category = newCategory,
                isRecurring = newRecurrence != "NONE",
                recurrenceInterval = newRecurrence
            )
            financeRepository.updateTransaction(updatedTransaction)
        }
    }

    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            financeRepository.deleteTransaction(id)
        }
    }

    // --- Savings Goals (Persistent via GoalRepository) ---
    
    val savingsGoals: StateFlow<List<com.katchy.focuslive.data.model.Goal>> = goalRepository.getGoals()
        .map { goals -> goals.filter { it.type == "SAVINGS" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addSavingsGoal(title: String, targetAmount: Double, icon: String, colorHex: Long) {
        if (title.isBlank() || targetAmount <= 0) return
        viewModelScope.launch {
            val goal = com.katchy.focuslive.data.model.Goal(
                title = title,
                targetAmount = targetAmount,
                currentAmount = 0.0,
                type = "SAVINGS",
                icon = icon,
                colorHex = String.format("#%06X", (0xFFFFFF and colorHex.toInt()))
            )
            goalRepository.addGoal(goal)
        }
    }

    fun depositToGoal(goal: com.katchy.focuslive.data.model.Goal, amount: Double) {
        viewModelScope.launch {
            val updatedAmount = (goal.currentAmount + amount).coerceAtMost(goal.targetAmount)
            goalRepository.updateGoal(goal.copy(currentAmount = updatedAmount))
        }
    }

    fun updateSavingsGoal(goal: com.katchy.focuslive.data.model.Goal, newTitle: String, newTarget: Double, newIcon: String, newColor: Long) {
        viewModelScope.launch {
            val updatedGoal = goal.copy(
                title = newTitle,
                targetAmount = newTarget,
                icon = newIcon,
                colorHex = String.format("#%06X", (0xFFFFFF and newColor.toInt()))
            )
            goalRepository.updateGoal(updatedGoal)
        }
    }

    fun deleteSavingsGoal(id: String) {
        viewModelScope.launch {
            goalRepository.deleteGoal(id)
        }
    }
}
