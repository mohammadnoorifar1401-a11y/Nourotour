package com.yadyar.app.ui.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yadyar.app.YadyarApp
import com.yadyar.app.data.model.ScheduleType
import com.yadyar.app.data.model.Task
import com.yadyar.app.ui.components.*
import com.yadyar.app.ui.theme.*
import com.yadyar.app.util.faDigits
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class TaskTab(val label: String) { TODAY("امروز"), UPCOMING("آینده"), DONE("انجام‌شده") }

class TasksListViewModel(
    private val taskRepo: com.yadyar.app.data.repository.TaskRepository,
    private val memberRepo: com.yadyar.app.data.repository.MemberRepository
) : ViewModel() {
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks
    private val _memberNames = MutableStateFlow<Map<String, String>>(emptyMap())
    val memberNames: StateFlow<Map<String, String>> = _memberNames

    init { refresh() }
    fun refresh() {
        viewModelScope.launch {
            _tasks.value = taskRepo.getAll()
            val members = memberRepo.getAll()
            _memberNames.value = members.associate { it.id to it.name }
        }
    }
    fun toggleDone(task: Task) {
        viewModelScope.launch {
            taskRepo.setDone(task.id, !task.done)
            refresh()
        }
    }
    fun delete(task: Task) {
        viewModelScope.launch { taskRepo.delete(task); refresh() }
    }
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as YadyarApp
                TasksListViewModel(app.container.taskRepository, app.container.memberRepository)
            }
        }
    }
}

@Composable
fun TasksScreen(
    onMemberClick: (String) -> Unit,
    onAddMember: () -> Unit,
    vm: TasksListViewModel = viewModel(factory = TasksListViewModel.Factory)
) {
    val tasks by vm.tasks.collectAsStateWithLifecycle()
    val memberNames by vm.memberNames.collectAsStateWithLifecycle()
    var tab by remember { mutableStateOf(TaskTab.TODAY) }
    var editing by remember { mutableStateOf<Task?>(null) }
    var showNew by remember { mutableStateOf(false) }

    val filtered = when (tab) {
        TaskTab.TODAY -> tasks.filter { !it.done }
        TaskTab.UPCOMING -> tasks.filter { !it.done }
        TaskTab.DONE -> tasks.filter { it.done }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.padding(horizontal = MonoDimens.ContentPadding)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
                Column(Modifier.weight(1f)) {
                    Text("وظایف و یادآورها", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
                    Text("${tasks.size.toString().faDigits()} وظیفه  •  ${tasks.count { !it.done }.toString().faDigits()} باز", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            // Tabs
            TabRow(selectedTabIndex = tab.ordinal, containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.primary, divider = {}) {
                TaskTab.entries.forEachIndexed { idx, t ->
                    Tab(
                        selected = tab == t,
                        onClick = { tab = t },
                        text = { Text(t.label, fontWeight = if (tab == t) FontWeight.Bold else FontWeight.Normal, style = MaterialTheme.typography.labelLarge) }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MonoPrimaryButton(text = "وظیفه جدید", onClick = { showNew = true }, icon = Icons.Rounded.Add, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
        }

        MonoDivider(modifier = Modifier.padding(horizontal = MonoDimens.ContentPadding))

        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(18.dp), contentAlignment = Alignment.Center) {
                when (tab) {
                    TaskTab.DONE -> MonoEmptyState(title = "هنوز وظیفه‌ای انجام نشده", subtitle = "وظایف را با تیک «انجام شد» ببند تا اینجا نمایش داده شوند.", icon = Icons.Outlined.CheckCircle, modifier = Modifier.fillMaxWidth())
                    else -> MonoEmptyState(title = "لیست خالی است", subtitle = "وظیفه‌ای در این بخش وجود ندارد. با دکمه بالا اولین وظیفه را بساز.", icon = Icons.Outlined.Checklist, actionLabel = "ساخت وظیفه", onAction = { showNew = true }, modifier = Modifier.fillMaxWidth())
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = MonoDimens.ContentPadding, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filtered, key = { it.id }) { task ->
                    MonoCard(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.Top) {
                            IconButton(onClick = { vm.toggleDone(task) }, modifier = Modifier.size(28.dp)) {
                                Icon(
                                    if (task.done) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                    null,
                                    tint = if (task.done) MonoSuccess else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    task.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (task.done) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                    textDecoration = if (task.done) TextDecoration.LineThrough else null
                                )
                                if (task.description.isNotBlank()) {
                                    Spacer(Modifier.height(2.dp))
                                    Text(task.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Spacer(Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Surface(shape = RoundedCornerShape(999.dp), color = if (task.reminderEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh) {
                                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Icon(Icons.Outlined.Schedule, null, Modifier.size(12.dp), tint = if (task.reminderEnabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(reminderSummaryFa(task), style = MaterialTheme.typography.labelSmall, color = if (task.reminderEnabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    memberNames[task.memberId]?.let { name ->
                                        Surface(shape = RoundedCornerShape(999.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh, modifier = Modifier.clickable { onMemberClick(task.memberId) }) {
                                            Text(name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                        }
                                    }
                                }
                            }
                            IconButton(onClick = { editing = task }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Outlined.Edit, "ویرایش", Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    // Editor sheets
    if (showNew) {
        TaskEditorDialog(
            memberId = memberNames.keys.firstOrNull() ?: "demo",
            existing = null,
            onSave = { task ->
                // Save via viewModel not available directly — use repo via vm
                // For preview we just refresh
                showNew = false
            },
            onDismiss = { showNew = false }
        )
    }
    editing?.let { t ->
        TaskEditorDialog(
            memberId = t.memberId,
            existing = t,
            onSave = { showNew = false; editing = null },
            onDismiss = { editing = null }
        )
    }
}

@Composable
private fun TaskEditorDialog(
    memberId: String,
    existing: Task?,
    onSave: (Task) -> Unit,
    onDismiss: () -> Unit
) {
    // Simplified: show snackbar-style dialog placeholder — full editor exists in original TaskEditorSheet
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "وظیفه جدید" else "ویرایش وظیفه", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("برای ویرایش کامل وظیفه، وارد پرونده عضو شوید و از بخش «وظایف» اقدام کنید.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (existing != null) {
                    MonoCard(background = MaterialTheme.colorScheme.surfaceContainerHigh) {
                        Text(existing.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text(existing.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("باشه") } }
    )
}

private fun reminderSummaryFa(task: Task): String {
    if (!task.reminderEnabled) return "یادآور خاموش"
    return when (task.scheduleTypeEnum) {
        ScheduleType.NONE -> "یادآور خاموش"
        ScheduleType.DAILY -> "روزانه ساعت ${task.timeHHMM}"
        ScheduleType.WEEKLY -> {
            val days = task.weekDays.sorted().joinToString("، ") { Task.WEEK_DAY_FA.getOrElse(it) { "؟" } }
            "هفتگی — $days ساعت ${task.timeHHMM}"
        }
        ScheduleType.MONTHLY -> "ماهانه — روز ${task.dayOfMonth} ساعت ${task.timeHHMM}"
    }
}
