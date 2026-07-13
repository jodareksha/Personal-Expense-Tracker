package com.expensetracker.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {


    @Query("SELECT * FROM expenses ORDER BY dateMillis DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses")
    fun getTotalAmount(): Flow<Double>

    @Insert
    suspend fun insertExpense(expense: Expense): Long

    @Delete
    suspend fun deleteExpense(expense: Expense)
}
