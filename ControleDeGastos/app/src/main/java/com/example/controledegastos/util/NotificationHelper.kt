package com.example.controledegastos.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.controledegastos.MainActivity
import java.text.NumberFormat
import java.util.Locale
import kotlin.random.Random

object NotificationHelper {

    private const val CHANNEL_ID = "controle_gastos_notifications"
    private const val CHANNEL_NAME = "Alertas de Gastos Pessoais"
    private const val CHANNEL_DESC = "Notificações sobre novos gastos, metas e sincronização"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESC
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showExpenseSavedNotification(
        context: Context,
        description: String,
        value: Double,
        category: String,
        isUpdate: Boolean = false
    ) {
        val currency = NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(value)
        val title = if (isUpdate) "✏️ Despesa Atualizada!" else "✅ Nova Despesa Salva!"
        val text = "$description ($category) — $currency registrado com sucesso."
        sendNotification(context, title, text)
    }

    fun showSyncNotification(context: Context, title: String, message: String) {
        sendNotification(context, title, message)
    }

    fun showBudgetReminderNotification(context: Context, totalSpent: Double, count: Int) {
        val currency = NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(totalSpent)
        val title = "📊 Resumo Financeiro Diário"
        val message = "Você possui $count despesa(s) acumulando $currency. Mantenha seu controle em dia!"
        sendNotification(context, title, message)
    }

    private fun sendNotification(context: Context, title: String, content: String) {
        createNotificationChannel(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(Random.nextInt(1000, 9999), notification)
    }
}
