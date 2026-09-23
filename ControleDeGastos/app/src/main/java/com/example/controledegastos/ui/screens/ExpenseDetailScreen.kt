package com.example.controledegastos.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.controledegastos.ui.viewmodel.ExpenseViewModel
import com.example.controledegastos.util.DeviceResourceHelper
import com.example.controledegastos.util.NotificationHelper
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDetailScreen(
    expenseId: Int,
    viewModel: ExpenseViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Int) -> Unit
) {
    val context = LocalContext.current
    val expense by viewModel.getExpenseById(expenseId).collectAsState(initial = null)
    val currencyRates by viewModel.currencyRates.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    val brlFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    val usdFormat = NumberFormat.getCurrencyInstance(Locale.US)
    val eurFormat = NumberFormat.getCurrencyInstance(Locale.GERMANY)
    val dateFormat = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale("pt", "BR"))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes da Despesa") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    expense?.let { current ->
                        IconButton(onClick = { onNavigateToEdit(current.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar")
                        }
                        IconButton(
                            onClick = {
                                viewModel.deleteExpense(current)
                                onNavigateBack()
                            }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Excluir",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        val current = expense
        if (current == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Despesa não encontrada ou removida.")
            }
        } else {
            val usdValue = if (currencyRates.usdRate > 0) current.value / currencyRates.usdRate else 0.0
            val eurValue = if (currencyRates.eurRate > 0) current.value / currencyRates.eurRate else 0.0

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Card Principal com Valor e Conversão via Retrofit API
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = current.category,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = current.description,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = brlFormat.format(current.value),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Conversão API em Tempo Real: 🇺🇸 ${usdFormat.format(usdValue)}  |  🇪🇺 ${eurFormat.format(eurValue)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Registrado em ${dateFormat.format(Date(current.timestamp))}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                // Status de Sincronização Retrofit
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (current.isSynced) Icons.Default.CloudDone else Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = if (current.isSynced) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                            )
                            Column {
                                Text(
                                    text = if (current.isSynced) "Sincronizado com API Externa" else "Pendente de Sincronização",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (current.isSynced) "ID Remoto na Nuvem: #${current.remoteId ?: current.id}" else "Toque para enviar via POST/PUT",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        FilledTonalButton(
                            onClick = {
                                viewModel.putExpenseToApi(current)
                                NotificationHelper.showSyncNotification(
                                    context = context,
                                    title = "☁️ Sincronização Retrofit Concluída",
                                    message = "A despesa '${current.description}' foi sincronizada com a API externa!"
                                )
                            },
                            enabled = !isSyncing
                        ) {
                            Text(if (current.isSynced) "Atualizar (PUT)" else "Enviar (POST)")
                        }
                    }
                }

                // Localização GPS (se disponível)
                if (!current.location.isNullOrBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null)
                            Column {
                                Text(
                                    text = "Localização do Gasto (GPS Nativo)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = current.location,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                // Foto do Comprovante (se disponível)
                val bitmap = remember(current.receiptUri) {
                    DeviceResourceHelper.loadBitmapFromPath(current.receiptUri)
                }
                if (bitmap != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "📷 Comprovante Anexado (Câmera / Galeria)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Comprovante da Despesa",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { onNavigateToEdit(current.id) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Editar")
                    }

                    Button(
                        onClick = {
                            viewModel.deleteExpense(current)
                            onNavigateBack()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Excluir (DELETE)")
                    }
                }
            }
        }
    }
}
