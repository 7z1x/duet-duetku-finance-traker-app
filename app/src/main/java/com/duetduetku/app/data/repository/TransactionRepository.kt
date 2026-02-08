package com.duetduetku.app.data.repository

import com.duetduetku.app.data.local.dao.TransactionDao
import com.duetduetku.app.data.local.entity.Transaction
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface TransactionRepository {
    fun getAllTransactions(): Flow<List<Transaction>>
    fun getRecentTransactions(): Flow<List<Transaction>>
    fun getTotalExpenseByDateRange(startDate: Long, endDate: Long): Flow<Double?>
    fun getTransactionsBetween(startDate: Long, endDate: Long): Flow<List<Transaction>>
    suspend fun getTransactionById(id: Long): Transaction?
    suspend fun insertTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transaction: Transaction)
    suspend fun deleteAllTransactions()
}

class TransactionRepositoryImpl @Inject constructor(
    private val dao: TransactionDao
) : TransactionRepository {
    override fun getAllTransactions(): Flow<List<Transaction>> = dao.getAllTransactions()
    
    override fun getRecentTransactions(): Flow<List<Transaction>> = dao.getRecentTransactions()
    
    override fun getTotalExpenseByDateRange(startDate: Long, endDate: Long): Flow<Double?> = 
        dao.getTotalExpenseByDateRange(startDate, endDate)

    override fun getTransactionsBetween(startDate: Long, endDate: Long): Flow<List<Transaction>> =
        dao.getTransactionsBetween(startDate, endDate)

    override suspend fun getTransactionById(id: Long): Transaction? = dao.getTransactionById(id)
        
    override suspend fun insertTransaction(transaction: Transaction) = dao.insert(transaction)
    
    override suspend fun deleteTransaction(transaction: Transaction) = dao.delete(transaction)
    
    override suspend fun deleteAllTransactions() = dao.deleteAll()
}
