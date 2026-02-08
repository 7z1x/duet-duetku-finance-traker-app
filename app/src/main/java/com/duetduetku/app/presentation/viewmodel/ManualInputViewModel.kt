package com.duetduetku.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duetduetku.app.data.local.entity.Transaction
import com.duetduetku.app.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

data class ManualInputState(
    val amount: String = "",
    val category: String = "Food",
    val note: String = "",
    val date: Date = Date(), // Default today
    val type: String = "Expense", // Expense or Income
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val editingTransactionId: Long? = null
)

@HiltViewModel
class ManualInputViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val userPreferences: com.duetduetku.app.data.datastore.UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManualInputState())
    val uiState: StateFlow<ManualInputState> = _uiState.asStateFlow()

    init {
        // No currency collection needed
    }

    fun onAmountChange(newAmount: String) {
        // Strip non-digits
        val cleanString = newAmount.replace("[^\\d]".toRegex(), "")
        
        if (cleanString.isNotEmpty()) {
            val parsed = cleanString.toDoubleOrNull()
            if (parsed != null) {
                // Format using CurrencyFormatter for just the number part
                val formatted = com.duetduetku.app.util.CurrencyFormatter.formatNumber(parsed)
                _uiState.value = _uiState.value.copy(amount = formatted)
            }
        } else {
            _uiState.value = _uiState.value.copy(amount = "")
        }
    }

    fun onCategoryChange(newCategory: String) {
        _uiState.value = _uiState.value.copy(category = newCategory)
    }

    fun onNoteChange(newNote: String) {
        _uiState.value = _uiState.value.copy(note = newNote)
    }
    
    fun onTypeChange(newType: String) {
        _uiState.value = _uiState.value.copy(type = newType)
    }

    fun onDateChange(newDate: Date) {
        _uiState.value = _uiState.value.copy(date = newDate)
    }

    fun saveTransaction() {
        val currentState = _uiState.value
        // Remove separators before parsing
        // We need to know what separator is used. 
        // Simple heuristic: Remove non-digits and non-decimal-separator (if any)
        // But formatNumber usually uses standard grouping separators.
        // E.g. "10.000" (ID) or "10,000" (US).
        // Let's rely on standard parsing or strip everything but digits if we assume whole numbers for now?
        // User requested "sesuaikan penulisan angka", implying standard formatting which might have decimals.
        // For simplicity with Manual Input which usually types integers:
        val cleanAmount = currentState.amount.replace("[^\\d]".toRegex(), "")
        val amountDouble = cleanAmount.toDoubleOrNull() ?: return

        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true)
            try {
                val transaction = Transaction(
                    id = currentState.editingTransactionId ?: 0L, // 0L means auto-generate new ID
                    amount = amountDouble,
                    date = currentState.date.time,
                    category = currentState.category,
                    note = currentState.note,
                    type = currentState.type
                )
                repository.insertTransaction(transaction)
                _uiState.value = currentState.copy(isLoading = false, isSaved = true)
            } catch (e: Exception) {
                // Handle error
                _uiState.value = currentState.copy(isLoading = false)
            }
        }
    }
    
    fun initializeFromScan(amount: Double?, merchant: String?, date: Long?) {
        // Format amount
        val formattedAmount = if (amount != null) {
             com.duetduetku.app.util.CurrencyFormatter.formatNumber(amount)
        } else ""
        
        val newDate = if (date != null && date > 0) Date(date) else Date()
        val newNote = merchant ?: ""
        
        _uiState.value = _uiState.value.copy(
            amount = formattedAmount,
            note = newNote, // Use merchant as note/description
            date = newDate,
            type = "Expense" // Assume scanning is for expense
        )
    }

    fun loadTransaction(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val transaction = repository.getTransactionById(id)
            if (transaction != null) {
                val formattedAmount = com.duetduetku.app.util.CurrencyFormatter.formatNumber(transaction.amount)
                _uiState.value = _uiState.value.copy(
                    amount = formattedAmount,
                    category = transaction.category,
                    note = transaction.note ?: "",
                    date = Date(transaction.date),
                    type = transaction.type,
                    editingTransactionId = transaction.id,
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun resetState() {
        _uiState.value = ManualInputState()
    }
}
