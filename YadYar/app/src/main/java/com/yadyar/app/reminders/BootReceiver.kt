package com.yadyar.app.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.yadyar.app.data.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * بعد از ری‌استارت دستگاه، همه یادآورها دوباره ثبت می‌شوند.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_MY_PACKAGE_REPLACED &&
            action != Intent.ACTION_LOCKED_BOOT_COMPLETED
        ) return

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val db = AppDatabase.get(context)
                val tasks = db.taskDao().getAllWithReminders()
                ReminderScheduler.rescheduleAll(context, tasks)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
