package com.mono.app.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mono.app.data.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * گیرنده هشدار: نوتیفیکیشن را نشان می‌دهد و نوبت بعدی را ثبت می‌کند.
 */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ReminderScheduler.ACTION_REMIND) return
        val taskId = intent.getStringExtra(ReminderScheduler.EXTRA_TASK_ID) ?: return

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val db = AppDatabase.get(context)
                val task = db.taskDao().getById(taskId) ?: return@launch
                val member = db.memberDao().getById(task.memberId)

                val subtitle = buildString {
                    member?.name?.let { append(it) }
                    if (task.description.isNotBlank()) {
                        if (isNotEmpty()) append(" — ")
                        append(task.description)
                    }
                    if (task.reminderNotes.isNotBlank()) {
                        if (isNotEmpty()) append("\n")
                        append(task.reminderNotes)
                    }
                }

                ReminderNotifications.show(
                    context = context,
                    taskId = task.id,
                    title = task.title,
                    subtitle = subtitle.ifBlank { "یادآوری" }
                )

                // ثبت نوبت بعدی
                ReminderScheduler.schedule(context, task)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
