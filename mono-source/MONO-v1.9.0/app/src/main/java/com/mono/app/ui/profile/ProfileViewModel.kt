package com.mono.app.ui.profile

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mono.app.MonoApp
import com.mono.app.data.model.Member
import com.mono.app.data.model.MemberDetails
import com.mono.app.data.model.Task
import com.mono.app.data.repository.MemberDetailsRepository
import com.mono.app.data.repository.MemberRepository
import com.mono.app.data.repository.TaskRepository
import com.mono.app.reminders.ReminderScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val app: Application,
    val memberId: String,
    memberRepository: MemberRepository,
    private val taskRepository: TaskRepository,
    memberDetailsRepository: MemberDetailsRepository
) : ViewModel() {

    val member: Flow<Member?> = memberRepository.observeById(memberId)

    val tasks: Flow<List<Task>> = taskRepository.observeByMember(memberId)

    val details: Flow<MemberDetails?> = memberDetailsRepository.observeByMember(memberId)

    fun saveTask(task: Task) {
        viewModelScope.launch {
            taskRepository.upsert(task)
            ReminderScheduler.schedule(app, task)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskRepository.delete(task)
            ReminderScheduler.cancel(app, task.id)
        }
    }

    fun toggleDone(task: Task) {
        viewModelScope.launch {
            taskRepository.setDone(task.id, !task.done)
        }
    }

    fun disableAllReminders() {
        viewModelScope.launch {
            val tasks = taskRepository.getByMember(memberId)
            taskRepository.disableAllReminders(memberId)
            tasks.forEach { ReminderScheduler.cancel(app, it.id) }
        }
    }

    companion object {
        fun factory(memberId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MonoApp
                ProfileViewModel(
                    app = application,
                    memberId = memberId,
                    memberRepository = application.container.memberRepository,
                    taskRepository = application.container.taskRepository,
                    memberDetailsRepository = application.container.memberDetailsRepository
                )
            }
        }
    }
}
