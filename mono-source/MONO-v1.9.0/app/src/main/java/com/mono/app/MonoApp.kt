package com.mono.app

import android.app.Application
import android.content.Context
import com.mono.app.data.db.AppDatabase
import com.mono.app.data.repository.MemberDetailsRepository
import com.mono.app.data.repository.MemberRepository
import com.mono.app.data.repository.TaskRepository
import com.mono.app.data.seed.SampleData
import com.mono.app.reminders.MissedReminderWorker
import com.mono.app.reminders.ReminderNotifications
import com.mono.app.reminders.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AppContainer(context: Context) {
    private val database = AppDatabase.get(context)
    val memberRepository = MemberRepository(database.memberDao())
    val taskRepository = TaskRepository(database.taskDao())
    val memberDetailsRepository = MemberDetailsRepository(database.memberDetailsDao())
}

class MonoApp : Application() {

    lateinit var container: AppContainer
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

        ReminderNotifications.ensureChannel(this)
        MissedReminderWorker.enqueue(this)

        applicationScope.launch {
            SampleData.seedIfEmpty(
                container.memberRepository,
                container.taskRepository,
                container.memberDetailsRepository
            )
            // ثبت/تمدید یادآورهای فعال هنگام شروع
            val tasks = container.taskRepository.getAllWithReminders()
            ReminderScheduler.rescheduleAll(this@MonoApp, tasks)
        }
    }
}
