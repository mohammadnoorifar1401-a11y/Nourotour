package com.yadyar.app.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.yadyar.app.data.model.Task
import java.util.concurrent.TimeUnit

/**
 * زمان‌بندی یادآورها:
 * - نوبت بعدی را با ReminderPlanner حساب می‌کند.
 * - با AlarmManager دقیق (setAlarmClock) ثبت می‌کند؛ اگر SCHEDULE_EXACT_ALARM در دسترس نبود
 *   به setExactAndAllowWhileIdle یا setAndAllowWhileIdle تنزل می‌کند.
 * - گیرنده ReminderReceiver بعد از هر اجرا، نوبت بعدی را دوباره ثبت می‌کند.
 */
object ReminderScheduler {

    const val ACTION_REMIND = "com.yadyar.app.action.REMIND"
    const val EXTRA_TASK_ID = "extra_task_id"

    /** حداکثر فاصله‌ای که AlarmManager تضمین می‌کند (برای پشتیبان WorkManager) */
    val MAX_TRIGGER_MILLIS = TimeUnit.DAYS.toMillis(28)

    private fun alarmManager(context: Context): AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private fun operationIntent(context: Context, taskId: String): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_REMIND
            putExtra(EXTRA_TASK_ID, taskId)
        }
        return PendingIntent.getBroadcast(
            context,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun canScheduleExact(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager(context).canScheduleExactAlarms()
        } else {
            true
        }
    }

    /** لغو یادآور یک وظیفه */
    fun cancel(context: Context, taskId: String) {
        alarmManager(context).cancel(operationIntent(context, taskId))
    }

    /** ثبت یادآور بعدیِ یک وظیفه (خواندن از دیتابیس در ReminderReceiver انجام می‌شود) */
    fun schedule(context: Context, task: Task) {
        cancel(context, task.id)
        if (!task.reminderEnabled) return

        val next = ReminderPlanner.nextTrigger(task) ?: return
        val triggerAtMillis = next.toInstant().toEpochMilli()
        val op = operationIntent(context, task.id)

        val am = alarmManager(context)
        try {
            if (canScheduleExact(context)) {
                val showIntent = PendingIntent.getActivity(
                    context, 0, Intent(context, com.yadyar.app.MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE
                )
                am.setAlarmClock(AlarmManager.AlarmClockInfo(triggerAtMillis, showIntent), op)
            } else {
                // Fallback must be inexact: exact APIs can throw SecurityException when
                // the user has not granted exact-alarm access (common on Android 12+).
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, op)
                } else {
                    am.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, op)
                }
            }
        } catch (_: SecurityException) {
            // Scheduling reminders must never crash app startup or the UI.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                runCatching { am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, op) }
            } else {
                runCatching { am.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, op) }
            }
        }
    }

    /** ثبت مجدد همه یادآورهای فعال (مثلاً بعد از بوت یا بازیابی بکاپ) */
    fun rescheduleAll(context: Context, tasks: List<Task>) {
        tasks.forEach { schedule(context, it) }
    }
}
