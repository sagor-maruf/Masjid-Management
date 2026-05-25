package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "salary_records")
data class SalaryRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val year: Int,
    val month: Int, // 1 to 12
    val monthName: String, // e.g. "May 2026", "June 2026"
    val baseSalary: Double,
    val paidAmount: Double,
    val lastUpdated: Long = System.currentTimeMillis()
)
