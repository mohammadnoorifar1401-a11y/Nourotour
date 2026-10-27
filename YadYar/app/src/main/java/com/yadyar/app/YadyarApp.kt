package com.yadyar.app

import android.app.Application
import android.content.Context
import com.yadyar.app.data.db.AppDatabase
import com.yadyar.app.data.repository.MemberDetailsRepository
import com.yadyar.app.data.repository.MemberRepository
import com.yadyar.app.data.repository.TaskRepository
import com.yadyar.app.data.seed.SampleData
import com.yadyar.app.reminders.MissedReminderWorker
import com.yadyar.app.reminders.ReminderNotifications
import com.yadyar.app.reminders.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AppContainer(context: Context) {
    // For preview flavor we use isolated DB name to avoid overwriting real data
    private val dbName = if (context.packageName.endsWith(".preview") || context.packageName.endsWith(".preview.debug")) "yadyar_preview.db" else "yadyar.db"
    val database = AppDatabase.get(context, dbName)
    val memberRepository = MemberRepository(database.memberDao())
    val taskRepository = TaskRepository(database.taskDao())
    val memberDetailsRepository = MemberDetailsRepository(database.memberDetailsDao())
}

class YadyarApp : Application() {

    lateinit var container: AppContainer
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

        runCatching { ReminderNotifications.ensureChannel(this) }
        runCatching { MissedReminderWorker.enqueue(this) }

        applicationScope.launch {
            try {
                SampleData.seedIfEmpty(
                    container.memberRepository,
                    container.taskRepository,
                    container.memberDetailsRepository
                )
                val tasks = container.taskRepository.getAllWithReminders()
                ReminderScheduler.rescheduleAll(this@YadyarApp, tasks)
            } catch (error: Exception) {
                android.util.Log.e("MONO", "Startup data/reminder initialization failed", error)
            }
        }
    }
}
