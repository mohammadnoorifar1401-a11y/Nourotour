package com.yadyar.app.reminders

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.yadyar.app.data.db.AppDatabase
import java.util.concurrent.TimeUnit

/**
 * تور ایمنی: هر ۱۵ دقیقه بررسی می‌کند یادآوری جا نمانده باشد
 * (مثلاً بعد از خاموشی طولانی یا لغو شدن آلارم).
 * این worker دقیق نیست؛ زمان‌بند اصلی AlarmManager است.
 */
class MissedReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.get(applicationContext)
        val tasks = db.taskDao().getAllWithReminders()
        val now = System.currentTimeMillis()

        tasks.forEach { task ->
            val next = ReminderPlanner.nextTrigger(task)?.toInstant()?.toEpochMilli()
            // اگر نوبت بعدی خیلی دور است یا گذشته، دوباره ثبت کن
            if (next == null || next - now > ReminderScheduler.MAX_TRIGGER_MILLIS) {
                ReminderScheduler.schedule(applicationContext, task)
            }
        }
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "yadyar_missed_reminders"

        fun enqueue(context: Context) {
            val request = PeriodicWorkRequestBuilder<MissedReminderWorker>(15, TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
