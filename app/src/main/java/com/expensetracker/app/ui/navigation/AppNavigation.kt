package com.expensetracker.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.expensetracker.app.ui.screens.AddExpenseScreen
import com.expensetracker.app.ui.screens.ExpenseListScreen
import com.expensetracker.app.ui.viewmodel.ExpenseViewModel

private object Routes {
    const val LIST = "expense_list"
    const val ADD = "add_expense"
}

@Composable
fun AppNavigation(
    viewModel: ExpenseViewModel,
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = Routes.LIST) {
        composable(Routes.LIST) {
            ExpenseListScreen(
                viewModel = viewModel,
                onAddExpenseClick = { navController.navigate(Routes.ADD) }
            )
        }
        composable(Routes.ADD) {
            AddExpenseScreen(
                viewModel = viewModel,
                onSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
