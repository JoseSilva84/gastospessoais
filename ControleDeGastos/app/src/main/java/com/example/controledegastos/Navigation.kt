package com.example.controledegastos

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.controledegastos.data.local.ExpenseDao
import com.example.controledegastos.ui.screens.ExpenseAddScreen
import com.example.controledegastos.ui.screens.ExpenseListScreen
import com.example.controledegastos.ui.viewmodel.ExpenseViewModel
import com.example.controledegastos.ui.viewmodel.ExpenseViewModelFactory

@Composable
fun MainNavigation(expenseDao: ExpenseDao) {
    val navController = rememberNavController()

    val viewModel: ExpenseViewModel = viewModel(
        factory = ExpenseViewModelFactory(expenseDao)
    )

    NavHost(navController = navController, startDestination = "expense_list") {
        composable("expense_list") {
            ExpenseListScreen(
                viewModel = viewModel,
                onNavigateToAdd = { navController.navigate("expense_add") }
            )
        }
        composable("expense_add") {
            ExpenseAddScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
