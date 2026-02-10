package com.katchy.focuslive.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.katchy.focuslive.MainActivity
import com.katchy.focuslive.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        const val REMINDER_CHANNEL_ID = "daily_reminders"
        const val REMINDER_CHANNEL_NAME = "Recordatorios Diarios"
        const val STREAK_CHANNEL_ID = "streak_alerts"
        const val STREAK_CHANNEL_NAME = "Rachas de Hábitos"
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val reminderChannel = NotificationChannel(
                REMINDER_CHANNEL_ID,
                REMINDER_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Recordatorios para tus tareas y planificación diaria."
            }

            val streakChannel = NotificationChannel(
                STREAK_CHANNEL_ID,
                STREAK_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertas para no perder tus rachas de hábitos."
            }

            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannels(listOf(reminderChannel, streakChannel))
        }
    }

    fun showNotification(
        id: Int,
        title: String,
        message: String,
        destination: String,
        channelId: String = REMINDER_CHANNEL_ID
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("destination", destination)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher_round) // Use app icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        // Check permission if needed (handled by caller or assumed granted for standard notifs in older android, 
        // for T+ we should request it).
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || 
            androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
             NotificationManagerCompat.from(context).notify(id, notification)
        }
    }
}
