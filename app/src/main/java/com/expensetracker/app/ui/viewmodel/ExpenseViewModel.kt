package com.expensetracker.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.app.data.Expense
import com.expensetracker.app.data.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

class ExpenseViewModel(private val repository: ExpenseRepository) : ViewModel() {


    private val _selectedCategory = MutableStateFlow<String?>(null)


    val listUiState: StateFlow<ExpenseListUiState> =
        combine(
            repository.getAllExpenses(),
            repository.getTotalAmount(),
            _selectedCategory
        ) { allExpenses, total, selectedCategory ->
            val filtered = if (selectedCategory == null) {
                allExpenses
            } else {
                allExpenses.filter { it.category == selectedCategory }
            }
            ExpenseListUiState(
                expenses = filtered,
                total = total,
                isLoading = false,
                availableCategories = allExpenses.map { it.category }.distinct().sorted(),
                selectedCategory = selectedCategory,
                totalUnfilteredCount = allExpenses.size
            )
        }
            .catch { throwable ->
                emit(
                    ExpenseListUiState(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Something went wrong loading your expenses."
                    )
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = ExpenseListUiState(isLoading = true)
            )

    fun onCategoryFilterSelected(category: String?) {
        _selectedCategory.value = category
    }

    private val _addUiState = MutableStateFlow(AddExpenseUiState())
    val addUiState: StateFlow<AddExpenseUiState> = _addUiState

    fun onTitleChanged(newTitle: String) {
        _addUiState.update { it.copy(title = newTitle, titleError = null) }
    }

    fun onAmountChanged(newAmount: String) {
        _addUiState.update { it.copy(amount = newAmount, amountError = null) }
    }

    fun onCategoryChanged(newCategory: String) {
        _addUiState.update { it.copy(category = newCategory) }
    }

    fun resetAddForm() {
        _addUiState.value = AddExpenseUiState()
    }


    fun saveExpense() {
        val current = _addUiState.value
        val trimmedTitle = current.title.trim()
        val parsedAmount = current.amount.trim().toDoubleOrNull()

        val titleError = if (trimmedTitle.isEmpty()) "Give this expense a title" else null
        val amountError = when {
            current.amount.isBlank() -> "Enter an amount"
            parsedAmount == null -> "Enter a valid number"
            parsedAmount <= 0.0 -> "Amount must be greater than zero"
            else -> null
        }

        if (titleError != null || amountError != null) {
            _addUiState.update { it.copy(titleError = titleError, amountError = amountError) }
            return
        }

        _addUiState.update { it.copy(isSaving = true, saveError = null) }

        viewModelScope.launch {
            try {
                repository.addExpense(
                    Expense(
                        title = trimmedTitle,
                        amount = parsedAmount!!,
                        dateMillis = Calendar.getInstance().timeInMillis,
                        category = current.category
                    )
                )
                _addUiState.update { it.copy(isSaving = false, didSaveSuccessfully = true) }
            } catch (t: Throwable) {
                _addUiState.update {
                    it.copy(
                        isSaving = false,
                        saveError = t.message ?: "Couldn't save this expense. Please try again."
                    )
                }
            }
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }
}