package com.expensetracker.app.ui.viewmodel

import com.expensetracker.app.data.Expense


data class ExpenseListUiState(
    val expenses: List<Expense> = emptyList(),
    val total: Double = 0.0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
) {
    val isEmpty: Boolean get() = !isLoading && expenses.isEmpty() && errorMessage == null
}


data class AddExpenseUiState(
    val title: String = "",
    val amount: String = "",
    val category: String = "General",
    val titleError: String? = null,
    val amountError: String? = null,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val didSaveSuccessfully: Boolean = false
)
