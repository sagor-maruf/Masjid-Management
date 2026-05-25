package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "member_payments")
data class MemberPayment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val memberId: Int,
    val year: Int,
    val month: Int, // 1 to 12
    val monthName: String, // E.g., "মে"
    val salaryAmountPaid: Double = 0.0,
    val salaryStatus: String = "DUE", // "PAID", "DUE"
    val tarabiAmountPaid: Double = 0.0,
    val lastUpdated: Long = System.currentTimeMillis()
)
