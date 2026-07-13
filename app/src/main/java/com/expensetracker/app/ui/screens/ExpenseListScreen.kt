package com.expensetracker.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.expensetracker.app.data.Expense
import com.expensetracker.app.ui.theme.ExpenseTrackerTheme
import com.expensetracker.app.ui.viewmodel.ExpenseViewModel
import com.expensetracker.app.util.myrCurrencyFormat
import com.expensetracker.app.util.toDisplayDate
import java.text.NumberFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    viewModel: ExpenseViewModel,
    onAddExpenseClick: () -> Unit
) {
    val uiState by viewModel.listUiState.collectAsState()
    val currencyFormat = remember { myrCurrencyFormat() }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("My Expenses") })
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddExpenseClick,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add expense") },
                elevation = FloatingActionButtonDefaults.elevation()
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // The filter row stays visible whenever there's at least one
            // expense in the DB — including when the *current* filter has
            // zero matches — so the user can always get back to "All".
            if (!uiState.isLoading && uiState.errorMessage == null && uiState.totalUnfilteredCount > 0) {
                CategoryFilterRow(
                    categories = uiState.availableCategories,
                    selectedCategory = uiState.selectedCategory,
                    onCategorySelected = viewModel::onCategoryFilterSelected
                )
            }

            // AnimatedContent crossfades between loading/error/empty/content
            // instead of the screen just snapping between them.
            val displayKey = when {
                uiState.isLoading -> "loading"
                uiState.errorMessage != null -> "error"
                uiState.isEmpty -> "empty"
                else -> "content"
            }
            AnimatedContent(
                targetState = displayKey,
                transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(150)) },
                label = "expense_list_state",
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) { key ->
                when (key) {
                    "loading" -> LoadingState()
                    "error" -> ErrorState(message = uiState.errorMessage!!)
                    "empty" -> EmptyState(
                        filteredCategory = uiState.selectedCategory,
                        onClearFilter = { viewModel.onCategoryFilterSelected(null) }
                    )
                    else -> ExpenseListContent(
                        expenses = uiState.expenses,
                        total = uiState.total,
                        totalCount = uiState.totalUnfilteredCount,
                        currencyFormat = currencyFormat,
                        onDelete = viewModel::deleteExpense
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryFilterRow(
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { Text("All") }
            )
        }
        items(categories) { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = { Text(category) }
            )
        }
    }
}

@Composable
private fun ExpenseListContent(
    expenses: List<Expense>,
    total: Double,
    totalCount: Int,
    currencyFormat: NumberFormat,
    onDelete: (Expense) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TotalSummaryCard(total = total, currencyFormat = currencyFormat, count = totalCount)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(expenses, key = { it.id }) { expense ->
                ExpenseRow(
                    expense = expense,
                    currencyFormat = currencyFormat,
                    onDelete = { onDelete(expense) },
                    // Handles the insert/remove/reorder animation for free —
                    // items fade + slide into place instead of just popping.
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}

@Composable
private fun TotalSummaryCard(total: Double, currencyFormat: NumberFormat, count: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Total spent",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = currencyFormat.format(total),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold

            )
            Text(
                text = "$count expense${if (count == 1) "" else "s"} logged",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ExpenseRow(
    expense: Expense,
    currencyFormat: NumberFormat,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = expense.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${expense.category} • ${expense.dateMillis.toDisplayDate()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = currencyFormat.format(expense.amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete ${expense.title}",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}


@Composable
private fun EmptyState(filteredCategory: String? = null, onClearFilter: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.ReceiptLong,
            contentDescription = null,
            modifier = Modifier.padding(bottom = 16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (filteredCategory != null) {
            Text(
                text = "No expenses in \"$filteredCategory\"",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Try a different category, or clear the filter.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onClearFilter, modifier = Modifier.padding(top = 8.dp)) {
                Text("Show all expenses")
            }
        } else {
            Text(
                text = "No expenses yet",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Tap \"Add expense\" to log your first one.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize()) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
private fun ErrorState(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Something went wrong",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
    }
}

// ---- Previews ----
// These render the stateless inner composables directly with fake data,
// since ExpenseListScreen itself needs a real ViewModel (and therefore a
// real Room database) that the preview sandbox can't provide.

private val sampleExpenses = listOf(
    Expense(id = 1, title = "Nasi lemak", amount = 8.50, dateMillis = System.currentTimeMillis(), category = "Food"),
    Expense(id = 2, title = "Grab ride", amount = 15.00, dateMillis = System.currentTimeMillis(), category = "Transport"),
    Expense(id = 3, title = "Electricity bill", amount = 120.30, dateMillis = System.currentTimeMillis(), category = "Bills")
)

@Preview(showBackground = true)
@Composable
private fun ExpenseListContentPreview() {
    ExpenseTrackerTheme {
        ExpenseListContent(
            expenses = sampleExpenses,
            total = sampleExpenses.sumOf { it.amount },
            totalCount = sampleExpenses.size,
            currencyFormat = myrCurrencyFormat(),
            onDelete = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CategoryFilterRowPreview() {
    ExpenseTrackerTheme {
        CategoryFilterRow(
            categories = listOf("Bills", "Food", "Transport"),
            selectedCategory = "Food",
            onCategorySelected = {}
        )
    }
}

@Preview(showBackground = true, name = "Empty (no data)")
@Composable
private fun EmptyStatePreview() {
    ExpenseTrackerTheme { EmptyState() }
}

@Preview(showBackground = true, name = "Empty (filtered)")
@Composable
private fun EmptyFilteredStatePreview() {
    ExpenseTrackerTheme { EmptyState(filteredCategory = "Health") }
}

@Preview(showBackground = true)
@Composable
private fun LoadingStatePreview() {
    ExpenseTrackerTheme { LoadingState() }
}

@Preview(showBackground = true)
@Composable
private fun ErrorStatePreview() {
    ExpenseTrackerTheme { ErrorState(message = "Couldn't load your expenses. Check your connection and try again.") }
}
