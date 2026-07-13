package com.expensetracker.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val dateMillis: Long,
    val category: String = "General"
)
