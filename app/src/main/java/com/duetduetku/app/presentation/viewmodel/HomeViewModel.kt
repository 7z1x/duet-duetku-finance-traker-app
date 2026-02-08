package com.duetduetku.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duetduetku.app.data.datastore.UserPreferences
import com.duetduetku.app.data.local.entity.Transaction
import com.duetduetku.app.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    // Combine multiple flows into a single UI State
    val uiState: StateFlow<HomeUiState> = combine(
        combine(
            userPreferences.userName,
            userPreferences.dailyLimit,
            userPreferences.profilePhotoUri
        ) { name, limit, photoUri -> Triple(name, limit, photoUri) },
        repository.getRecentTransactions(),
        repository.getTotalExpenseByDateRange(getStartOfDay(), getEndOfDay())
    ) { (name, limit, photoUri), recentTransactions, todayExpense ->
        HomeUiState(
            userName = name,
            profilePhotoUri = photoUri,
            recentTransactions = recentTransactions,
            todayExpense = todayExpense ?: 0.0,
            dailyLimit = limit
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    private fun getStartOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getEndOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    fun updateDailyLimit(newLimit: Double) {
        viewModelScope.launch {
            userPreferences.setDailyLimit(newLimit)
        }
    }
}

data class HomeUiState(
    val userName: String = "User",
    val dailyLimit: Double = 100000.0,
    val profilePhotoUri: String? = null,
    val recentTransactions: List<Transaction> = emptyList(),
    val todayExpense: Double = 0.0
)


