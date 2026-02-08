package com.duetduetku.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duetduetku.app.data.local.entity.Transaction
import com.duetduetku.app.data.repository.TransactionRepository
import com.duetduetku.app.util.DateUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

enum class StatsPeriod(val label: String) {
    WEEK("This Week"),
    MONTH("Month"),
    YEAR("Years")
}

data class StatsState(
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val balance: Double = 0.0,
    val transactions: List<Transaction> = emptyList(),
    val categoryBreakdown: List<CategoryStat> = emptyList(),
    val dailyStats: List<DailyStat> = emptyList(),
    val selectedPeriod: String = "This Week", // "This Week", "Month", "Years"
    val selectedDate: Date = Date(), // The specific date anchor (e.g., any day in selected week/month/year)
    val isLoading: Boolean = false
)

data class CategoryStat(val category: String, val amount: Double, val percentage: Float, val colorHex: String)
data class DailyStat(val date: Date, val amount: Double, val segments: List<StatSegment> = emptyList())
data class StatSegment(val colorHex: String, val amount: Double)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val userPreferences: com.duetduetku.app.data.datastore.UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsState())
    val uiState: StateFlow<StatsState> = _uiState.asStateFlow()

    init {
        loadStats("This Week", Date())
    }

    fun onPeriodChange(period: String) {
        // When switching period, reset to current date or keep context? 
        // User behavior usually: Switch to "Month" -> Shows current month.
        val newDate = Date()
        _uiState.value = _uiState.value.copy(selectedPeriod = period, selectedDate = newDate)
        loadStats(period, newDate)
    }

    fun onDateSelected(date: Date) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
        loadStats(_uiState.value.selectedPeriod, date)
    }

    private fun loadStats(period: String, date: Date) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val (start, end) = getDateRange(period, date)
            
            // Collect Flow from Repository
            repository.getTransactionsBetween(start.time, end.time).collect { transactions ->
                calculateStats(transactions, start, end, period)
            }
        }
    }

    private fun calculateStats(transactions: List<Transaction>, start: Date, end: Date, period: String) {
        var income = 0.0
        var expense = 0.0
        val categoryMap = mutableMapOf<String, Double>()
        val chartMap = mutableMapOf<Long, Double>()

        // 1. Initialize Chart Map with Zeros
        val calendar = Calendar.getInstance()
        calendar.time = start
        
        if (period == "Years") {
             // Yearly: iterate months until end
             // Use a clone to iterate
             val iterCal = calendar.clone() as Calendar
             while (iterCal.time.time <= end.time) {
                 val key = iterCal.time.time
                 chartMap[key] = 0.0
                 iterCal.add(Calendar.MONTH, 1)
             }
        } else {
             // Daily: Iterate days
             val iterCal = calendar.clone() as Calendar
             while (iterCal.time.time <= end.time) {
                 val key = iterCal.time.time
                 chartMap[key] = 0.0
                 iterCal.add(Calendar.DAY_OF_MONTH, 1)
             }
        }

        transactions.forEach {
            if (it.type == "Income") {
                income += it.amount
            } else {
                expense += it.amount
                // Category breakdown only for expenses
                categoryMap[it.category] = categoryMap.getOrDefault(it.category, 0.0) + it.amount
                
                // Chart Stats
                val key = if (period == "Years") {
                    // Snap to 1st of month
                    val c = Calendar.getInstance()
                    c.time = Date(it.date)
                    c.set(Calendar.DAY_OF_MONTH, 1)
                    c.set(Calendar.HOUR_OF_DAY, 0)
                    c.set(Calendar.MINUTE, 0)
                    c.set(Calendar.SECOND, 0)
                    c.set(Calendar.MILLISECOND, 0)
                    c.time.time
                } else {
                    DateUtil.stripTime(Date(it.date)).time
                }
                
                if (chartMap.containsKey(key)) {
                    chartMap[key] = chartMap[key]!! + it.amount
                }
            }
        }

        // Calculate segments for each day/key
        val chartSegmentsMap = mutableMapOf<Long, List<StatSegment>>()
        chartMap.keys.forEach { key ->
            // Filter transactions for this key (Day or Month)
            val txs = transactions.filter { 
                if (it.type == "Expense") {
                    val txKey = if (period == "Years") {
                        val c = Calendar.getInstance()
                        c.time = Date(it.date)
                        c.set(Calendar.DAY_OF_MONTH, 1)
                        c.set(Calendar.HOUR_OF_DAY, 0)
                        c.set(Calendar.MINUTE, 0)
                        c.set(Calendar.SECOND, 0)
                        c.set(Calendar.MILLISECOND, 0)
                        c.time.time
                    } else {
                        DateUtil.stripTime(Date(it.date)).time
                    }
                    txKey == key
                } else false
            }
            
            if (txs.isNotEmpty()) {
                val segments = txs.groupBy { it.category }
                    .map { (cat, list) -> 
                        StatSegment(getCategoryColor(cat), list.sumOf { it.amount })
                    }
                    .sortedByDescending { it.amount } // Largest segments usually look better at bottom or consistent
                chartSegmentsMap[key] = segments
            } else {
                chartSegmentsMap[key] = emptyList()
            }
        }

        val balance = income - expense

        // Category Stats
        val totalExp = if (expense == 0.0) 1.0 else expense
        val categories = categoryMap.map { (cat, amt) ->
            CategoryStat(
                category = cat,
                amount = amt,
                percentage = (amt / totalExp).toFloat(),
                colorHex = getCategoryColor(cat)
            )
        }.sortedByDescending { it.amount }

        // Daily Stats for Chart
        val dailies = chartMap.map { (dateMillis, amt) ->
            DailyStat(Date(dateMillis), amt, chartSegmentsMap[dateMillis] ?: emptyList())
        }.sortedBy { it.date }

        _uiState.value = _uiState.value.copy(
            totalIncome = income,
            totalExpense = expense,
            balance = balance,
            transactions = transactions,
            categoryBreakdown = categories,
            dailyStats = dailies,
            isLoading = false
        )
    }

    private fun getDateRange(period: String, date: Date): Pair<Date, Date> {
        val calendar = Calendar.getInstance()
        calendar.time = date
        
        val start: Date
        val end: Date

        when (period) {
            "This Week" -> {
                // Set to start of week (Sunday or Monday?) Let's assume Monday as standard in ID/Business
                // Adjust to first day of week
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                
                // If it's Sunday, and we want Monday start, we need to subtract days or handle Locale
                // Simple approach: Calendar.DAY_OF_WEEK. 
                // Let's rely on default Locale for now (usually Sunday start in US, Monday in others)
                // Assuming Monday start for better UX usually.
                var dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                // Convert Sunday (1) to 7, and others to 1-6
                // Mon=2 -> 1, Tue=3 -> 2 ... Sun=1 -> 7
                val daysFromMonday = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - Calendar.MONDAY
                
                calendar.add(Calendar.DAY_OF_MONTH, -daysFromMonday)
                start = calendar.time
                
                // End of week
                calendar.add(Calendar.DAY_OF_MONTH, 6)
                end = DateUtil.endOfDay(calendar.time)
            }
            "Month" -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                start = DateUtil.stripTime(calendar.time)
                
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                end = DateUtil.endOfDay(calendar.time)
            }
            "Years" -> {
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                start = DateUtil.stripTime(calendar.time)
                
                // End of Year
                calendar.set(Calendar.MONTH, Calendar.DECEMBER)
                calendar.set(Calendar.DAY_OF_MONTH, 31)
                end = DateUtil.endOfDay(calendar.time)
            }
            else -> {
                start = DateUtil.stripTime(Date())
                end = DateUtil.endOfDay(Date())
            }
        }
        
        return Pair(start, end)
    }
    
    // Helper to assign fixed colors to known categories
    private fun getCategoryColor(category: String): String {
        return when (category) {
            // Vibrant Distinct Colors
            "Food" -> "#FF5252"       // Material Red A200
            "Transport" -> "#448AFF"  // Material Blue A200
            "Shopping" -> "#FFD740"   // Material Amber A200
            "Health" -> "#69F0AE"     // Material Green A200
            "Education" -> "#E040FB"  // Material Purple A200
            "Bills" -> "#FFAB40"      // Material Orange A200
            "Fun" -> "#FF4081"        // Material Pink A200
            "Investment" -> "#18FFFF" // Material Cyan A200
            "Salary" -> "#64DD17"     // Material Light Green A400
            "Gift" -> "#AA00FF"       // Deep Purple A700
            else -> "#BDBDBD"         // Grey 400
        }
    }
}
