package com.example.controledegastos.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.controledegastos.data.local.Expense
import com.example.controledegastos.ui.viewmodel.ExpenseViewModel
import com.example.controledegastos.util.DeviceResourceHelper
import com.example.controledegastos.util.NotificationHelper
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    viewModel: ExpenseViewModel,
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (Int) -> Unit,
    onNavigateToDetail: (Int) -> Unit,
    onNavigateToCloudApi: () -> Unit
) {
    val context = LocalContext.current
    val expenses by viewModel.expenses.collectAsState()
    val currencyRates by viewModel.currencyRates.collectAsState()
    var selectedCategory by remember { mutableStateOf("Todas") }

    val categories = listOf("Todas", "Alimentação", "Transporte", "Moradia", "Saúde", "Lazer", "Outros")

    val filteredExpenses = remember(expenses, selectedCategory) {
        if (selectedCategory == "Todas") expenses
        else expenses.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    val totalSpent = remember(filteredExpenses) {
        filteredExpenses.sumOf { it.value }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        NotificationHelper.showBudgetReminderNotification(
            context = context,
            totalSpent = totalSpent,
            count = filteredExpenses.size
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Controle de Gastos", fontWeight = FontWeight.Bold)
                        Text(
                            text = "Room Database + Retrofit API",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                NotificationHelper.showBudgetReminderNotification(
                                    context = context,
                                    totalSpent = totalSpent,
                                    count = filteredExpenses.size
                                )
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.NotificationsActive,
                            contentDescription = "Disparar Notificação de Resumo"
                        )
                    }
                    IconButton(
                        onClick = {
                            DeviceResourceHelper.exportExpensesToCsvFile(context, expenses)
                        }
                    ) {
                        Icon(
                            Icons.Default.FileDownload,
                            contentDescription = "Exportar Relatório CSV"
                        )
                    }
                    IconButton(onClick = onNavigateToCloudApi) {
                        Icon(
                            Icons.Default.CloudSync,
                            contentDescription = "Sincronização e Cotações API"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAdd,
                icon = { Icon(Icons.Default.Add, contentDescription = "Adicionar Gasto") },
                text = { Text("Novo Gasto") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Card de Resumo Geral + Conversão de Moedas via API (Retrofit)
            SummaryDashboardCard(
                totalBrl = totalSpent,
                usdRate = currencyRates.usdRate,
                eurRate = currencyRates.eurRate,
                expenseCount = filteredExpenses.size,
                onOpenCloudApi = onNavigateToCloudApi
            )

            // Filtro de Categorias
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) }
                    )
                }
            }

            if (filteredExpenses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Nenhum gasto registrado nesta categoria.",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Toque em 'Novo Gasto' para registrar com foto, GPS e sincronização na nuvem.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 88.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredExpenses, key = { it.id }) { expense ->
                        ExpenseItem(
                            expense = expense,
                            onClick = { onNavigateToDetail(expense.id) },
                            onEdit = { onNavigateToEdit(expense.id) },
                            onDelete = { viewModel.deleteExpense(expense) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryDashboardCard(
    totalBrl: Double,
    usdRate: Double,
    eurRate: Double,
    expenseCount: Int,
    onOpenCloudApi: () -> Unit
) {
    val brlFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    val usdFormat = NumberFormat.getCurrencyInstance(Locale.US)
    val eurFormat = NumberFormat.getCurrencyInstance(Locale.GERMANY)

    val totalUsd = if (usdRate > 0) totalBrl / usdRate else 0.0
    val totalEur = if (eurRate > 0) totalBrl / eurRate else 0.0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onOpenCloudApi() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Acumulado ($expenseCount itens)",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                )
                AssistChip(
                    onClick = onOpenCloudApi,
                    label = {
                        Text(
                            "API Retrofit • USD R$ ${String.format("%.2f", usdRate)}",
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = brlFormat.format(totalBrl),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "🇺🇸 ${usdFormat.format(totalUsd)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                )
                Text(
                    text = "🇪🇺 ${eurFormat.format(totalEur)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
fun ExpenseItem(
    expense: Expense,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = expense.description,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (expense.isSynced) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = "Sincronizado na API",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = expense.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    if (!expense.receiptUri.isNullOrBlank()) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Possui foto de comprovante",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    if (!expense.location.isNullOrBlank()) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Possui localização GPS",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dateFormat.format(Date(expense.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = currencyFormat.format(expense.value),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Excluir",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
