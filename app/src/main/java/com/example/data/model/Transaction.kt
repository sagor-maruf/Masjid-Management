package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val description: String,
    val type: String, // "INCOME" or "EXPENSE"
    val amount: Double,
    val dateString: String,
    val timestamp: Long = System.currentTimeMillis()
)
