package com.expensetracker.app

import com.expensetracker.app.data.Expense
import com.expensetracker.app.data.ExpenseRepository
import com.expensetracker.app.ui.viewmodel.ExpenseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/**
 * An in-memory fake, so these tests exercise the ViewModel's validation and
 * state logic without touching Room or needing an Android runtime.
 */
private class FakeExpenseRepository : ExpenseRepository {
    val savedExpenses = mutableListOf<Expense>()
    private val expensesFlow = MutableStateFlow<List<Expense>>(emptyList())
    private val totalFlow = MutableStateFlow(0.0)

    override fun getAllExpenses() = expensesFlow
    override fun getTotalAmount() = totalFlow

    override suspend fun addExpense(expense: Expense) {
        savedExpenses.add(expense)
        expensesFlow.value = savedExpenses.toList()
        totalFlow.value = savedExpenses.sumOf { it.amount }
    }

    override suspend fun deleteExpense(expense: Expense) {
        savedExpenses.remove(expense)
        expensesFlow.value = savedExpenses.toList()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ExpenseViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `save with blank title sets titleError and does not save`() = runTest {
        val repository = FakeExpenseRepository()
        val viewModel = ExpenseViewModel(repository)

        viewModel.onTitleChanged("")
        viewModel.onAmountChanged("12.50")
        viewModel.saveExpense()

        assertEquals("Give this expense a title", viewModel.addUiState.value.titleError)
        assertEquals(0, repository.savedExpenses.size)
    }

    @Test
    fun `save with non numeric amount sets amountError`() = runTest {
        val repository = FakeExpenseRepository()
        val viewModel = ExpenseViewModel(repository)

        viewModel.onTitleChanged("Coffee")
        viewModel.onAmountChanged("free")
        viewModel.saveExpense()

        assertEquals("Enter a valid number", viewModel.addUiState.value.amountError)
    }

    @Test
    fun `save with zero amount is rejected`() = runTest {
        val repository = FakeExpenseRepository()
        val viewModel = ExpenseViewModel(repository)

        viewModel.onTitleChanged("Coffee")
        viewModel.onAmountChanged("0")
        viewModel.saveExpense()

        assertEquals("Amount must be greater than zero", viewModel.addUiState.value.amountError)
    }

    @Test
    fun `valid input clears errors and saves`() = runTest {
        val repository = FakeExpenseRepository()
        val viewModel = ExpenseViewModel(repository)

        viewModel.onTitleChanged("Coffee")
        viewModel.onAmountChanged("4.50")
        viewModel.saveExpense()
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.addUiState.value.titleError)
        assertNull(viewModel.addUiState.value.amountError)
        assertEquals(1, repository.savedExpenses.size)
        assertEquals("Coffee", repository.savedExpenses.first().title)
    }
}

