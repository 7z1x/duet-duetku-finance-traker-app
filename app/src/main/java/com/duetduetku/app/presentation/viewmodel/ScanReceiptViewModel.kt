package com.duetduetku.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duetduetku.app.data.local.entity.Transaction
import com.duetduetku.app.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScanReceiptViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _oneTimeEvent = MutableSharedFlow<ScanEvent>()
    val oneTimeEvent: SharedFlow<ScanEvent> = _oneTimeEvent.asSharedFlow()

    fun saveScannedTransaction(merchant: String?, amount: Double?, date: Long?) {
        viewModelScope.launch {
            if (amount == null) {
                _oneTimeEvent.emit(ScanEvent.Error("Could not detect Total Amount. Please try again."))
                return@launch
            }

            try {
                val transaction = Transaction(
                    amount = amount,
                    category = "Shopping", // Default category for receipts
                    note = merchant ?: "Scanned Receipt",
                    date = date ?: System.currentTimeMillis(),
                    type = "Expense"
                )
                repository.insertTransaction(transaction)
                val dateStr = com.duetduetku.app.util.DateUtil.formatTransactionDate(transaction.date)
                _oneTimeEvent.emit(ScanEvent.Saved("Saved: ${transaction.amount} at ${transaction.note} ($dateStr)"))
            } catch (e: Exception) {
                _oneTimeEvent.emit(ScanEvent.Error("Failed to save: ${e.message}"))
            }
        }
    }

    sealed class ScanEvent {
        data class Saved(val message: String) : ScanEvent()
        data class Error(val message: String) : ScanEvent()
    }
}
