package com.example.controledegastos.ui.screens

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.controledegastos.data.local.Expense
import com.example.controledegastos.ui.viewmodel.ExpenseViewModel
import com.example.controledegastos.util.DeviceResourceHelper
import com.example.controledegastos.util.NotificationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseAddScreen(
    viewModel: ExpenseViewModel,
    expenseId: Int? = null,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val existingExpense by remember(expenseId) {
        if (expenseId != null && expenseId > 0) {
            viewModel.getExpenseById(expenseId)
        } else {
            kotlinx.coroutines.flow.flowOf(null)
        }
    }.collectAsState(initial = null)

    var description by remember { mutableStateOf("") }
    var valueStr by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Alimentação") }
    var receiptPath by remember { mutableStateOf<String?>(null) }
    var locationText by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    var pendingCameraPath by remember { mutableStateOf<String?>(null) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val categories = listOf("Alimentação", "Transporte", "Moradia", "Saúde", "Lazer", "Educação", "Outros")

    LaunchedEffect(existingExpense) {
        existingExpense?.let { exp ->
            description = exp.description
            valueStr = exp.value.toString()
            category = exp.category
            receiptPath = exp.receiptUri
            locationText = exp.location
        }
    }

    // Launcher para Câmera Nativa (TakePicture)
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && pendingCameraPath != null) {
            receiptPath = pendingCameraPath
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val (uri, path) = DeviceResourceHelper.createReceiptImageUri(context)
            pendingCameraUri = uri
            pendingCameraPath = path
            takePictureLauncher.launch(uri)
        }
    }

    // Launcher para Galeria Nativa (GetContent)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val copiedPath = DeviceResourceHelper.copyGalleryImageToInternal(context, uri)
            if (copiedPath != null) {
                receiptPath = copiedPath
            }
        }
    }

    // Launcher para Permissão de Localização GPS
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        locationText = DeviceResourceHelper.captureDeviceLocation(context)
    }

    // Launcher para Permissão de Notificações (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val isEditing = existingExpense != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Gasto" else "Adicionar Gasto") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descrição da Despesa") },
                placeholder = { Text("Ex: Almoço executivo, Supermercado, Uber...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = isError && description.isBlank()
            )

            OutlinedTextField(
                value = valueStr,
                onValueChange = { valueStr = it },
                label = { Text("Valor (R$)") },
                placeholder = { Text("Ex: 45.90") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                isError = isError && valueStr.replace(",", ".").toDoubleOrNull() == null
            )

            Text(
                text = "Categoria",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { item ->
                    FilterChip(
                        selected = category.equals(item, ignoreCase = true),
                        onClick = { category = item },
                        label = { Text(item) },
                        leadingIcon = if (category.equals(item, ignoreCase = true)) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }

            HorizontalDivider()

            // Seção de Recursos Nativos do Dispositivo (Câmera, Galeria e GPS)
            Text(
                text = "Recursos Nativos do Dispositivo (Comprovante & GPS)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Câmera", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Câmera")
                }

                OutlinedButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Image, contentDescription = "Galeria", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Galeria")
                }

                OutlinedButton(
                    onClick = {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "GPS", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("GPS")
                }
            }

            // Exibir localização capturada via GPS
            if (!locationText.isNullOrBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = null)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Localização Registrada (GPS)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = locationText ?: "",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // Exibir miniatura da foto do comprovante capturada pela Câmera/Galeria
            val receiptBitmap = remember(receiptPath) {
                DeviceResourceHelper.loadBitmapFromPath(receiptPath)
            }
            if (receiptBitmap != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "📷 Comprovante Anexado",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Image(
                            bitmap = receiptBitmap.asImageBitmap(),
                            contentDescription = "Foto do Comprovante",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            if (isError) {
                Text(
                    text = "Preencha a descrição e um valor numérico válido.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val value = valueStr.replace(",", ".").toDoubleOrNull()
                    if (description.isNotBlank() && value != null && category.isNotBlank()) {
                        val currentExpense = existingExpense
                        if (currentExpense != null) {
                            viewModel.updateExpense(
                                currentExpense.copy(
                                    description = description.trim(),
                                    value = value,
                                    category = category.trim(),
                                    receiptUri = receiptPath,
                                    location = locationText
                                )
                            )
                            NotificationHelper.showExpenseSavedNotification(
                                context = context,
                                description = description.trim(),
                                value = value,
                                category = category.trim(),
                                isUpdate = true
                            )
                        } else {
                            viewModel.addExpense(
                                description = description.trim(),
                                value = value,
                                category = category.trim(),
                                receiptUri = receiptPath,
                                location = locationText
                            )
                            NotificationHelper.showExpenseSavedNotification(
                                context = context,
                                description = description.trim(),
                                value = value,
                                category = category.trim(),
                                isUpdate = false
                            )
                        }
                        onNavigateBack()
                    } else {
                        isError = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = if (isEditing) "Atualizar Gasto (Room + API PUT)" else "Salvar Gasto (Room + API POST)",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
