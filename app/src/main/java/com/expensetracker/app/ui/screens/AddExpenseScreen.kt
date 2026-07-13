package com.expensetracker.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.expensetracker.app.ui.theme.ExpenseTrackerTheme
import com.expensetracker.app.ui.viewmodel.AddExpenseUiState
import com.expensetracker.app.ui.viewmodel.ExpenseViewModel

private val categories = listOf("General", "Food", "Transport", "Bills", "Shopping", "Health", "Other")

/** Stateful wrapper: owns the ViewModel connection and the save→navigate side effect. */
@Composable
fun AddExpenseScreen(
    viewModel: ExpenseViewModel,
    onSaved: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.addUiState.collectAsState()

    LaunchedEffect(uiState.didSaveSuccessfully) {
        if (uiState.didSaveSuccessfully) {
            onSaved()
            viewModel.resetAddForm()
        }
    }

    AddExpenseContent(
        uiState = uiState,
        onTitleChanged = viewModel::onTitleChanged,
        onAmountChanged = viewModel::onAmountChanged,
        onCategoryChanged = viewModel::onCategoryChanged,
        onSave = viewModel::saveExpense,
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddExpenseContent(
    uiState: AddExpenseUiState,
    onTitleChanged: (String) -> Unit,
    onAmountChanged: (String) -> Unit,
    onCategoryChanged: (String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Expense") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.title,
                onValueChange = onTitleChanged,
                label = { Text("Title") },
                placeholder = { Text("e.g. Groceries") },
                isError = uiState.titleError != null,
                supportingText = { uiState.titleError?.let { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.amount,
                onValueChange = onAmountChanged,
                label = { Text("Amount") },
                placeholder = { Text("0.00") },
                prefix = { Text("RM ") },
                isError = uiState.amountError != null,
                supportingText = { uiState.amountError?.let { Text(it) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            )

            Text(
                text = "Category",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { category ->
                    FilterChip(
                        selected = uiState.category == category,
                        onClick = { onCategoryChanged(category) },
                        label = { Text(category) }
                    )
                }
            }

            AnimatedVisibility(
                visible = uiState.saveError != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Text(
                    text = uiState.saveError.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Button(onClick = onSave, enabled = !uiState.isSaving) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(16.dp)
                                .padding(end = 8.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Text(if (uiState.isSaving) "Saving..." else "Save expense")
                }
            }
        }
    }
}

// ---- Previews ----

@Preview(showBackground = true, name = "Empty form")
@Composable
private fun AddExpenseContentPreview() {
    ExpenseTrackerTheme {
        AddExpenseContent(
            uiState = AddExpenseUiState(),
            onTitleChanged = {},
            onAmountChanged = {},
            onCategoryChanged = {},
            onSave = {},
            onBack = {}
        )
    }
}

@Preview(showBackground = true, name = "Validation errors")
@Composable
private fun AddExpenseContentErrorPreview() {
    ExpenseTrackerTheme {
        AddExpenseContent(
            uiState = AddExpenseUiState(
                title = "",
                amount = "free",
                titleError = "Give this expense a title",
                amountError = "Enter a valid number"
            ),
            onTitleChanged = {},
            onAmountChanged = {},
            onCategoryChanged = {},
            onSave = {},
            onBack = {}
        )
    }
}

@Preview(showBackground = true, name = "Saving")
@Composable
private fun AddExpenseContentSavingPreview() {
    ExpenseTrackerTheme {
        AddExpenseContent(
            uiState = AddExpenseUiState(title = "Coffee", amount = "4.50", isSaving = true),
            onTitleChanged = {},
            onAmountChanged = {},
            onCategoryChanged = {},
            onSave = {},
            onBack = {}
        )
    }
}
