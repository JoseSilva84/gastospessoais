package com.example.controledegastos

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.controledegastos.data.local.ExpenseDao
import com.example.controledegastos.ui.screens.CloudSyncScreen
import com.example.controledegastos.ui.screens.ExpenseAddScreen
import com.example.controledegastos.ui.screens.ExpenseDetailScreen
import com.example.controledegastos.ui.screens.ExpenseListScreen
import com.example.controledegastos.ui.viewmodel.ExpenseViewModel
import com.example.controledegastos.ui.viewmodel.ExpenseViewModelFactory
import com.example.controledegastos.util.NotificationHelper

@Composable
fun MainNavigation(expenseDao: ExpenseDao) {
    val context = LocalContext.current
    val navController = rememberNavController()

    val viewModel: ExpenseViewModel = viewModel(
        factory = ExpenseViewModelFactory(expenseDao)
    )

    LaunchedEffect(Unit) {
        NotificationHelper.createNotificationChannel(context)
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentRoute == "expense_list" || currentRoute?.startsWith("expense_detail") == true,
                    onClick = {
                        navController.navigate("expense_list") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Meus Gastos") },
                    label = { Text("Gastos") }
                )

                NavigationBarItem(
                    selected = currentRoute == "expense_add" || currentRoute?.startsWith("expense_edit") == true,
                    onClick = {
                        navController.navigate("expense_add") {
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.AddCircle, contentDescription = "Adicionar Gasto") },
                    label = { Text("Novo Gasto") }
                )

                NavigationBarItem(
                    selected = currentRoute == "cloud_api",
                    onClick = {
                        navController.navigate("cloud_api") {
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.CloudSync, contentDescription = "API & Nuvem") },
                    label = { Text("API & Cotações") }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "expense_list",
            modifier = Modifier.padding(innerPadding)
        ) {
            // Tela 1: Lista de Gastos + Resumo + Filtros
            composable("expense_list") {
                ExpenseListScreen(
                    viewModel = viewModel,
                    onNavigateToAdd = { navController.navigate("expense_add") },
                    onNavigateToEdit = { id -> navController.navigate("expense_edit/$id") },
                    onNavigateToDetail = { id -> navController.navigate("expense_detail/$id") },
                    onNavigateToCloudApi = { navController.navigate("cloud_api") }
                )
            }

            // Tela 2: Adicionar Novo Gasto (com Câmera, Galeria, GPS e Notificação)
            composable("expense_add") {
                ExpenseAddScreen(
                    viewModel = viewModel,
                    expenseId = null,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Rota de Edição reutilizando a Tela de Formulário
            composable(
                route = "expense_edit/{expenseId}",
                arguments = listOf(navArgument("expenseId") { type = NavType.IntType })
            ) { backStackEntry ->
                val expenseId = backStackEntry.arguments?.getInt("expenseId")
                ExpenseAddScreen(
                    viewModel = viewModel,
                    expenseId = expenseId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Tela 3: Detalhes da Despesa (Visualização Completa, Comprovante, Conversão USD/EUR, PUT/DELETE)
            composable(
                route = "expense_detail/{expenseId}",
                arguments = listOf(navArgument("expenseId") { type = NavType.IntType })
            ) { backStackEntry ->
                val expenseId = backStackEntry.arguments?.getInt("expenseId") ?: 0
                ExpenseDetailScreen(
                    expenseId = expenseId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { id -> navController.navigate("expense_edit/$id") }
                )
            }

            // Tela 4: Consumo de API Retrofit (GET, POST, PUT, DELETE + Cotações em Tempo Real)
            composable("cloud_api") {
                CloudSyncScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
