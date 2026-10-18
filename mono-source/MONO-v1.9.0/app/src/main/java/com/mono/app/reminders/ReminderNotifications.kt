package com.mono.app.reminders

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.mono.app.MainActivity
import com.mono.app.R

/**
 * ساخت و نمایش نوتیفیکیشن محلی یادآور.
 * اگر POST_NOTIFICATIONS داده نشده باشد، بی‌صدا کار نمی‌کند (CRUD خراب نمی‌شود).
 */
object ReminderNotifications {

    const val CHANNEL_ID = "yadyar_reminders"
    const val CHANNEL_NAME = "یادآوری‌ها"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val existing = manager.getNotificationChannel(CHANNEL_ID)
            if (existing == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "یادآوری وظایف ثبت‌شده"
                    enableVibration(true)
                }
                manager.createNotificationChannel(channel)
            }
        }
    }

    fun show(context: Context, taskId: String, title: String, subtitle: String) {
        ensureChannel(context)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // اگر اجازه نوتیف نیست، فقط رد می‌شویم
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            context,
            taskId.hashCode(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(subtitle)
            .setStyle(NotificationCompat.BigTextStyle().bigText(subtitle))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()

        manager.notify(taskId.hashCode(), notification)
    }
}
