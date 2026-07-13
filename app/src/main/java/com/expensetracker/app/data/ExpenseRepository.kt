package com.expensetracker.app.data

import kotlinx.coroutines.flow.Flow


interface ExpenseRepository {
    fun getAllExpenses(): Flow<List<Expense>>
    fun getTotalAmount(): Flow<Double>
    suspend fun addExpense(expense: Expense)
    suspend fun deleteExpense(expense: Expense)
}


class RoomExpenseRepository(private val expenseDao: ExpenseDao) : ExpenseRepository {

    override fun getAllExpenses(): Flow<List<Expense>> = expenseDao.getAllExpenses()

    override fun getTotalAmount(): Flow<Double> = expenseDao.getTotalAmount()

    override suspend fun addExpense(expense: Expense) {
        expenseDao.insertExpense(expense)
    }

    override suspend fun deleteExpense(expense: Expense) {
        expenseDao.deleteExpense(expense)
    }
}
