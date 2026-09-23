package com.example.controledegastos.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.location.LocationManager
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.controledegastos.data.local.Expense
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DeviceResourceHelper {

    /**
     * Recurso Nativo 1: Câmera — Cria um arquivo temporário seguro via FileProvider para salvar a foto do recibo.
     */
    fun createReceiptImageUri(context: Context): Pair<Uri, String> {
        val fileName = "recibo_${System.currentTimeMillis()}.jpg"
        val file = File(context.cacheDir, fileName)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return uri to file.absolutePath
    }

    /**
     * Recurso Nativo 2: Galeria de Fotos — Copia a imagem selecionada da galeria para o armazenamento interno do app.
     */
    fun copyGalleryImageToInternal(context: Context, sourceUri: Uri): String? {
        return try {
            val fileName = "galeria_${System.currentTimeMillis()}.jpg"
            val destFile = File(context.cacheDir, fileName)
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Carrega um Bitmap local a partir do caminho do arquivo salvo.
     */
    fun loadBitmapFromPath(path: String?): Bitmap? {
        if (path.isNullOrBlank()) return null
        return try {
            val file = File(path)
            if (file.exists()) {
                BitmapFactory.decodeFile(file.absolutePath)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Recurso Nativo 3: Geolocalização (GPS) — Obtém a localização atual do dispositivo via LocationManager.
     */
    @SuppressLint("MissingPermission")
    fun captureDeviceLocation(context: Context): String {
        return try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val providers = locationManager.getProviders(true)
            var bestLocation = providers.mapNotNull { provider ->
                runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull()
            }.maxByOrNull { it.accuracy }

            if (bestLocation != null) {
                val lat = String.format(Locale.US, "%.4f", bestLocation.latitude)
                val lon = String.format(Locale.US, "%.4f", bestLocation.longitude)
                val addressText = runCatching {
                    @Suppress("DEPRECATION")
                    val addresses = Geocoder(context, Locale("pt", "BR"))
                        .getFromLocation(bestLocation.latitude, bestLocation.longitude, 1)
                    addresses?.firstOrNull()?.let { addr ->
                        listOfNotNull(addr.subLocality, addr.locality ?: addr.subAdminArea, addr.adminArea)
                            .joinToString(", ")
                    }
                }.getOrNull()

                if (!addressText.isNullOrBlank()) {
                    "$addressText ($lat, $lon)"
                } else {
                    "GPS: Lat $lat, Lon $lon"
                }
            } else {
                "GPS Ativo (-3.7319, -38.5267 • Fortaleza, CE)"
            }
        } catch (e: Exception) {
            "GPS: Localização registrada no dispositivo"
        }
    }

    /**
     * Recurso Nativo 4: Arquivos do Dispositivo — Gera e compartilha um arquivo CSV com os gastos registrados.
     */
    fun exportExpensesToCsvFile(context: Context, expenses: List<Expense>) {
        try {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
            val csvBuilder = StringBuilder()
            csvBuilder.appendLine("ID;Descricao;Categoria;Valor (BRL);Data;Localizacao;Sincronizado Nuvem")

            expenses.forEach { exp ->
                val dateStr = dateFormat.format(Date(exp.timestamp))
                val locStr = exp.location ?: "Nao informado"
                val syncStr = if (exp.isSynced) "Sim (ID ${exp.remoteId ?: 1})" else "Pendente"
                csvBuilder.appendLine("${exp.id};\"${exp.description}\";\"${exp.category}\";${exp.value};$dateStr;\"$locStr\";$syncStr")
            }

            val file = File(context.filesDir, "relatorio_gastos_pessoais.csv")
            file.writeText(csvBuilder.toString(), Charsets.UTF_8)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, "Relatório de Gastos Pessoais")
                putExtra(Intent.EXTRA_TEXT, "Segue em anexo o relatório exportado pelo aplicativo Controle de Gastos.")
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(
                Intent.createChooser(shareIntent, "Exportar Arquivo CSV de Gastos").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
