package com.katchy.focuslive.ui.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.katchy.focuslive.R

object NotificationHelper {
    const val CHANNEL_ID_COUPLE = "couple_updates"
    const val CHANNEL_ID_TIMER = "timer_notifications"
    const val NOTIFICATION_ID_PARTNER_TASK = 1001
    const val NOTIFICATION_ID_POMODORO = 1002

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Couple Channel
            val coupleName = "Couple Updates"
            val coupleDesc = "Notifications when your partner completes tasks"
            val coupleChannel = NotificationChannel(CHANNEL_ID_COUPLE, coupleName, NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = coupleDesc
            }
            notificationManager.createNotificationChannel(coupleChannel)

            // Timer Channel
            val timerName = "Protocolo de Enfoque"
            val timerDesc = "Alertas cuando terminas un bloque de Deep Work"
            val timerChannel = NotificationChannel(CHANNEL_ID_TIMER, timerName, NotificationManager.IMPORTANCE_HIGH).apply {
                description = timerDesc
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(timerChannel)
        }
    }

    fun showPomodoroFinishedNotification(context: Context) {
        try {
            val builder = NotificationCompat.Builder(context, CHANNEL_ID_TIMER)
                .setSmallIcon(com.katchy.focuslive.R.drawable.ic_app_logo_vector)
                .setContentTitle("¡Deep Work Terminado!")
                .setContentText("Has completado un protocolo de concentración. Tómate un respiro.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (androidx.core.content.ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.POST_NOTIFICATIONS
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                ) {
                    NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_POMODORO, builder.build())
                }
            } else {
                NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_POMODORO, builder.build())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showPartnerTaskNotification(context: Context, partnerName: String) {
        // Permission check should be handled by caller or assumed granted for standard notifications in older versions
        // For Android 13+, POST_NOTIFICATIONS permission request is needed in MainActivity.
        
        try {
            val builder = NotificationCompat.Builder(context, CHANNEL_ID_COUPLE)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Fallback, better to use a heart or app icon
                .setContentTitle("Couple Update ❤️")
                .setContentText("$partnerName just completed a task!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (androidx.core.content.ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.POST_NOTIFICATIONS
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                ) {
                    NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_PARTNER_TASK, builder.build())
                }
            } else {
                NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_PARTNER_TASK, builder.build())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
