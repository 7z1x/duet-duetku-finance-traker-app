package com.duetduetku.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val type: String, // "EXPENSE" or "INCOME"
    val category: String,
    val note: String? = null,
    val date: Long, // Unix timestamp
    val evidencePath: String? = null // Path to stored receipt image
)
