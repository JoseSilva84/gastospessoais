package com.example.controledegastos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.controledegastos.data.local.Expense
import com.example.controledegastos.ui.viewmodel.ApiOperationLog
import com.example.controledegastos.ui.viewmodel.ExpenseViewModel
import com.example.controledegastos.util.NotificationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudSyncScreen(
    viewModel: ExpenseViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val expenses by viewModel.expenses.collectAsState()
    val currencyRates by viewModel.currencyRates.collectAsState()
    val apiLogs by viewModel.apiLogs.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statusMessage) {
        statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("API Retrofit & Nuvem", fontWeight = FontWeight.Bold)
                        Text(
                            text = "Requisições GET, POST, PUT e DELETE",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.fetchExchangeRates() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Atualizar Cotações (GET)")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card 1: Cotação de Moedas em Tempo Real via GET (AwesomeAPI)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CurrencyExchange, contentDescription = null)
                            Text(
                                text = "Cotações em Tempo Real (Retrofit @GET)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("🇺🇸 Dólar (USD-BRL)", style = MaterialTheme.typography.labelMedium)
                                Text(
                                    text = "R$ ${String.format("%.2f", currencyRates.usdRate)} (${currencyRates.usdVariation})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("🇪🇺 Euro (EUR-BRL)", style = MaterialTheme.typography.labelMedium)
                                Text(
                                    text = "R$ ${String.format("%.2f", currencyRates.eurRate)} (${currencyRates.eurVariation})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = currencyRates.lastUpdated,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f)
                        )
                    }
                }
            }

            // Card 2: Painel de Operações HTTP Retrofit (GET, POST, PUT, DELETE)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Painel de Requisições Retrofit",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Demonstração direta das 4 operações HTTP na API externa (JSONPlaceholder + AwesomeAPI):",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.fetchExchangeRates()
                                    viewModel.fetchRemoteExpensesFromApi()
                                },
                                modifier = Modifier.weight(1f),
                                enabled = !isSyncing
                            ) {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("GET")
                            }

                            Button(
                                onClick = {
                                    val target = expenses.firstOrNull() ?: Expense(
                                        id = 1,
                                        description = "Despesa Teste Cloud",
                                        value = 99.90,
                                        category = "Outros"
                                    )
                                    viewModel.postExpenseToApi(target)
                                    NotificationHelper.showSyncNotification(
                                        context = context,
                                        title = "🚀 Requisição POST Concluída",
                                        message = "Despesa '${target.description}' enviada via POST para a API!"
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                enabled = !isSyncing
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("POST")
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledTonalButton(
                                onClick = {
                                    val target = expenses.firstOrNull() ?: Expense(
                                        id = 1,
                                        description = "Despesa Atualizada Cloud",
                                        value = 149.90,
                                        category = "Alimentação",
                                        remoteId = 1
                                    )
                                    viewModel.putExpenseToApi(target)
                                },
                                modifier = Modifier.weight(1f),
                                enabled = !isSyncing
                            ) {
                                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("PUT")
                            }

                            OutlinedButton(
                                onClick = {
                                    val target = expenses.firstOrNull() ?: Expense(
                                        id = 1,
                                        description = "Registro Remoto #1",
                                        value = 50.0,
                                        category = "Outros",
                                        remoteId = 1
                                    )
                                    viewModel.deleteExpenseFromApi(target)
                                },
                                modifier = Modifier.weight(1f),
                                enabled = !isSyncing
                            ) {
                                Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("DELETE")
                            }
                        }

                        Button(
                            onClick = { viewModel.syncAllLocalExpenses() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isSyncing
                        ) {
                            Text("Sincronizar Todos os Gastos Locais (${expenses.size}) com a Nuvem")
                        }
                    }
                }
            }

            // Lista de Logs das Requisições Retrofit realizadas
            item {
                Text(
                    text = "Histórico de Comunicação HTTP (Retrofit)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            if (apiLogs.isEmpty()) {
                item {
                    Text(
                        text = "Nenhuma requisição registrada ainda.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } else {
                items(apiLogs, key = { it.id }) { log ->
                    ApiLogItemCard(log)
                }
            }
        }
    }
}

@Composable
fun ApiLogItemCard(log: ApiOperationLog) {
    val badgeColor = when (log.method) {
        "GET" -> MaterialTheme.colorScheme.primary
        "POST" -> MaterialTheme.colorScheme.tertiary
        "PUT" -> MaterialTheme.colorScheme.secondary
        "DELETE" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.outline
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = badgeColor,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = log.method,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = log.status,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = log.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = log.endpoint,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = log.summary,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
