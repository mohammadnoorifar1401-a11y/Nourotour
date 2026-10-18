package com.mono.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mono.app.data.model.MemberDetails
import com.mono.app.data.model.ScheduleType
import com.mono.app.data.model.Task
import com.mono.app.ui.components.ConfirmDialog
import com.mono.app.ui.components.GlassCard
import com.mono.app.ui.components.GradientBackground
import com.mono.app.ui.components.MemberAvatar
import com.mono.app.ui.components.SectionTitle
import com.mono.app.ui.components.TypeBadge
import com.mono.app.ui.task.TaskEditorSheet
import com.mono.app.ui.theme.GoldAccent
import com.mono.app.ui.theme.SuccessGreen
import com.mono.app.ui.theme.TextPrimary
import com.mono.app.ui.theme.TextSecondary
import com.mono.app.util.faDigits
import com.mono.app.util.formatDate
import com.mono.app.util.formatDateTime

@Composable
fun ProfileScreen(
    memberId: String,
    onBack: () -> Unit,
    onEditMember: (String) -> Unit,
    viewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.factory(memberId))
) {
    val member by viewModel.member.collectAsStateWithLifecycle(initialValue = null)
    val tasks by viewModel.tasks.collectAsStateWithLifecycle(initialValue = emptyList())
    val details by viewModel.details.collectAsStateWithLifecycle(initialValue = null)

    var editingTask by remember { mutableStateOf<Task?>(null) }
    var showNewTaskSheet by remember { mutableStateOf(false) }
    var showDisableAllConfirm by remember { mutableStateOf(false) }

    GradientBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = 18.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 6.dp, bottom = 12.dp)
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "بازگشت",
                            tint = TextPrimary
                        )
                    }
                    Text(
                        text = "پروفایل",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { onEditMember(memberId) }) {
                        Icon(
                            Icons.Rounded.Edit,
                            contentDescription = "ویرایش مشخصات",
                            tint = GoldAccent
                        )
                    }
                }

                val current = member
                if (current == null) {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text("عضو یافت نشد", color = TextSecondary)
                    }
                    return@Column
                }

                // ---- کارت سربرگ ----
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MemberAvatar(
                            letter = current.avatarLetter.ifBlank { current.name.take(1) },
                            colorHex = current.avatarColor,
                            size = 72.dp,
                            fontSize = 28.sp
                        )
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = current.name,
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TypeBadge(current.typeEnum)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "ثبت: ${formatDate(current.createdAt)}",
                                    color = TextSecondary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                // ==== مشخصات فرم (همان فرم مادر) ====
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SectionTitle("مشخصات", modifier = Modifier.weight(1f))
                    TextButton(onClick = { onEditMember(memberId) }) {
                        Icon(
                            Icons.Rounded.Edit,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text("ویرایش", color = GoldAccent, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Spacer(Modifier.height(10.dp))

                val d = details
                if (d == null) {
                    GlassCard(modifier = Modifier.fillMaxWidth(), fillSoft = true) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "مشخصات فرم هنوز ثبت نشده است",
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "با دکمه ویرایش، فرم مشخصات را تکمیل کنید",
                                color = TextSecondary.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    DetailsForm(d)
                }

                // ==== وظایف ====
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SectionTitle(
                        "وظایف (${tasks.size.toString().faDigits()})",
                        modifier = Modifier.weight(1f)
                    )
                    if (tasks.any { it.reminderEnabled }) {
                        TextButton(onClick = { showDisableAllConfirm = true }) {
                            Icon(
                                Icons.Rounded.NotificationsOff,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                text = "خاموش کردن همه یادآوری‌ها",
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))

                if (tasks.isEmpty()) {
                    GlassCard(modifier = Modifier.fillMaxWidth(), fillSoft = true) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("هنوز وظیفه‌ای ثبت نشده — دکمه «+» را بزنید", color = TextSecondary)
                        }
                    }
                } else {
                    GlassCard(modifier = Modifier.fillMaxWidth(), fillSoft = true) {
                        tasks.forEachIndexed { index, task ->
                            if (index > 0) ThinDivider()
                            TaskRow(
                                task = task,
                                onToggleDone = { viewModel.toggleDone(task) },
                                onEdit = { editingTask = task }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(110.dp))
            }

            FloatingActionButton(
                onClick = { showNewTaskSheet = true },
                containerColor = GoldAccent,
                contentColor = Color(0xFF2B2203),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(20.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "وظیفه جدید")
            }
        }
    }

    if (showNewTaskSheet) {
        TaskEditorSheet(
            memberId = memberId,
            existing = null,
            onSave = {
                viewModel.saveTask(it)
                showNewTaskSheet = false
            },
            onDelete = null,
            onDismiss = { showNewTaskSheet = false }
        )
    }

    editingTask?.let { task ->
        TaskEditorSheet(
            memberId = memberId,
            existing = task,
            onSave = {
                viewModel.saveTask(it)
                editingTask = null
            },
            onDelete = {
                viewModel.deleteTask(task)
                editingTask = null
            },
            onDismiss = { editingTask = null }
        )
    }

    if (showDisableAllConfirm) {
        ConfirmDialog(
            title = "خاموش کردن همه یادآوری‌ها",
            text = "تمام یادآوری‌های فعال این عضو خاموش می‌شوند. وظایف دست‌نخورده می‌مانند.",
            confirmLabel = "خاموش کن",
            onConfirm = {
                showDisableAllConfirm = false
                viewModel.disableAllReminders()
            },
            onDismiss = { showDisableAllConfirm = false }
        )
    }
}

/** نمایش فرم مشخصات به تفکیک بخش‌ها */
@Composable
private fun DetailsForm(d: MemberDetails) {
    DetailSection("مشخصات هویتی") {
        DetailRow("نام پدر", d.fatherName)
        DetailRow("کد ملی", d.nationalId)
        DetailRow("سریال کد ملی / کد رهگیری", d.nationalIdSerial)
        DetailRow("تاریخ تولد", d.birthDate)
        DetailRow("محل تولد", d.birthPlace)
        DetailRow("شماره شناسنامه", d.certNo)
        DetailRow("سریال شناسنامه", d.certSerial)
    }
    DetailSection("تماس و سیم‌کارت") {
        DetailRow("شماره تماس", d.phone)
        DetailRow("اپراتور", d.operatorName)
        DetailRow("پوکه", d.simPuk)
    }
    DetailSection("اطلاعات بانکی و کارت") {
        DetailRow("شماره حساب", d.accountNo)
        DetailRow("شماره کارت", d.cardNo)
        DetailRow("Exp", d.cardExp)
        DetailRow("Cvv2", d.cardCvv2)
        DetailRow("Pass card", d.cardPassword)
        DetailRow("نام کاربری", d.username)
        DetailRow("رمز عبور", d.password)
    }
    DetailSection("وضعیت اعتباری و تسهیلات") {
        DetailRow("رتبه اعتباری", d.creditScore)
        DetailRow("وضعیت چک", d.checkStatus)
        DetailRow("ظرفیت خالی", d.freeCapacity)
        DetailRow("وام‌های در جریان", d.activeLoans)
        DetailRow("وام‌های گرفته‌شده", d.takenLoans)
        DetailRow("ضمانت", d.collateral)
        DetailRow("خطاها", d.errors)
    }
}

@Composable
private fun DetailSection(
    title: String,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    Text(
        text = title,
        color = GoldAccent,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
    )
    GlassCard(modifier = Modifier.fillMaxWidth(), fillSoft = true, content = content)
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = TextSecondary,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(130.dp)
        )
        Text(
            text = value.ifBlank { "—" },
            color = if (value.isBlank()) TextSecondary.copy(alpha = 0.6f) else TextPrimary,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = TextSecondary,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(120.dp)
        )
        Text(
            text = value,
            color = TextPrimary,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ThinDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color.White.copy(alpha = 0.10f))
    )
}

@Composable
private fun TaskRow(
    task: Task,
    onToggleDone: () -> Unit,
    onEdit: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.Top
    ) {
        IconButton(onClick = onToggleDone, modifier = Modifier.size(26.dp)) {
            Icon(
                imageVector = if (task.done) {
                    Icons.Rounded.CheckCircle
                } else {
                    Icons.Rounded.RadioButtonUnchecked
                },
                contentDescription = if (task.done) "برگرداندن" else "انجام شد",
                tint = if (task.done) SuccessGreen else TextSecondary,
                modifier = Modifier.size(21.dp)
            )
        }
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = task.title,
                color = if (task.done) TextSecondary else TextPrimary,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                textDecoration = if (task.done) TextDecoration.LineThrough else null
            )
            if (task.description.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = task.description,
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = reminderSummary(task),
                color = if (task.reminderEnabled) GoldAccent else TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        IconButton(onClick = onEdit, modifier = Modifier.size(30.dp)) {
            Icon(
                Icons.Rounded.Edit,
                contentDescription = "ویرایش",
                tint = TextSecondary,
                modifier = Modifier.size(17.dp)
            )
        }
    }
}

private fun reminderSummary(task: Task): String {
    if (!task.reminderEnabled) return "یادآور خاموش"
    return when (task.scheduleTypeEnum) {
        ScheduleType.NONE -> "یادآور خاموش"
        ScheduleType.DAILY -> "یادآور روزانه ساعت ${task.timeHHMM}"
        ScheduleType.WEEKLY -> {
            val days = task.weekDays
                .sorted()
                .joinToString("، ") { Task.WEEK_DAY_FA.getOrElse(it) { "؟" } }
            "یادآور هفتگی — $days ساعت ${task.timeHHMM}"
        }
        ScheduleType.MONTHLY -> "یادآور ماهانه — روز ${task.dayOfMonth} ساعت ${task.timeHHMM}"
    }
}
