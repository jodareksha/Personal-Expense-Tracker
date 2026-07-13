package com.expensetracker.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.expensetracker.app.data.ExpenseDatabase
import com.expensetracker.app.data.RoomExpenseRepository
import com.expensetracker.app.ui.navigation.AppNavigation
import com.expensetracker.app.ui.theme.ExpenseTrackerTheme
import com.expensetracker.app.ui.viewmodel.ExpenseViewModel
import com.expensetracker.app.ui.viewmodel.ExpenseViewModelFactory

class MainActivity : ComponentActivity() {

    private val repository by lazy {
        RoomExpenseRepository(ExpenseDatabase.getInstance(applicationContext).expenseDao())
    }

    private val viewModel: ExpenseViewModel by viewModels {
        ExpenseViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ExpenseTrackerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(viewModel = viewModel)
                }
            }
        }
    }
}
