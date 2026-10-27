package com.yadyar.app.ui.task

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yadyar.app.data.model.ScheduleType
import com.yadyar.app.data.model.Task
import com.yadyar.app.ui.components.ConfirmDialog
import com.yadyar.app.ui.components.DangerOutlineButton
import com.yadyar.app.ui.components.GlassTextField
import com.yadyar.app.ui.components.GoldButton
import com.yadyar.app.ui.components.SelectablePill
import com.yadyar.app.ui.theme.BgMid
import com.yadyar.app.ui.theme.DangerRed
import com.yadyar.app.ui.theme.GoldAccent
import com.yadyar.app.ui.theme.TextPrimary
import com.yadyar.app.ui.theme.TextSecondary
import com.yadyar.app.util.faDigits
import com.yadyar.app.util.formatHHMM
import com.yadyar.app.util.parseHour
import com.yadyar.app.util.parseMinute

/**
 * شیت افزودن/ویرایش وظیفه + تنظیمات یادآور.
 * [existing] == null یعنی وظیفه جدید.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditorSheet(
    memberId: String,
    existing: Task?,
    onSave: (Task) -> Unit,
    onDelete: (() -> Unit)?,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var title by remember { mutableStateOf(existing?.title ?: "") }
    var description by remember { mutableStateOf(existing?.description ?: "") }
    var reminderEnabled by remember { mutableStateOf(existing?.reminderEnabled ?: false) }
    var scheduleType by remember { mutableStateOf(existing?.scheduleTypeEnum ?: ScheduleType.NONE) }
    var timeHour by remember { mutableStateOf(parseHour(existing?.timeHHMM ?: "08:00")) }
    var timeMinute by remember { mutableStateOf(parseMinute(existing?.timeHHMM ?: "08:00")) }
    var weekDays by remember {
        mutableStateOf((existing?.weekDays ?: emptyList()).toSet())
    }
    var dayOfMonth by remember {
        mutableStateOf((existing?.dayOfMonth ?: 1).coerceIn(1, 31))
    }
    var reminderNotes by remember { mutableStateOf(existing?.reminderNotes ?: "") }

    var titleError by remember { mutableStateOf(false) }
    var weekDaysError by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BgMid
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(max = 620.dp)
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = if (existing == null) "وظیفه جدید" else "ویرایش وظیفه",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(Modifier.height(16.dp))

            GlassTextField(
                value = title,
                onValueChange = {
                    title = it
                    if (it.isNotBlank()) titleError = false
                },
                label = "عنوان وظیفه *",
                placeholder = "مثال: پیگیری سفارش",
                isError = titleError,
                supportingText = if (titleError) "عنوان الزامی است" else null,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            GlassTextField(
                value = description,
                onValueChange = { description = it },
                label = "توضیحات",
                placeholder = "جزئیات اختیاری…",
                singleLine = false,
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(18.dp))

            // ---- یادآور ----
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "یادآوری",
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "اعلان تکرارشونده برای این وظیفه",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Switch(
                    checked = reminderEnabled,
                    onCheckedChange = { reminderEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = GoldAccent,
                        checkedTrackColor = GoldAccent.copy(alpha = 0.40f),
                        checkedBorderColor = GoldAccent,
                        uncheckedThumbColor = Color.White.copy(alpha = 0.75f),
                        uncheckedTrackColor = Color.White.copy(alpha = 0.14f),
                        uncheckedBorderColor = Color.White.copy(alpha = 0.25f)
                    )
                )
            }

            if (reminderEnabled) {
                Spacer(Modifier.height(14.dp))

                Text(
                    text = "نوع تکرار",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SelectablePill("روزانه", scheduleType == ScheduleType.DAILY, onClick = {
                        scheduleType = ScheduleType.DAILY
                    })
                    SelectablePill("هفتگی", scheduleType == ScheduleType.WEEKLY, onClick = {
                        scheduleType = ScheduleType.WEEKLY
                    })
                    SelectablePill("ماهانه", scheduleType == ScheduleType.MONTHLY, onClick = {
                        scheduleType = ScheduleType.MONTHLY
                    })
                }

                Spacer(Modifier.height(14.dp))

                // زمان
                Text(
                    text = "زمان یادآوری",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.10f))
                        .padding(horizontal = 4.dp)
                ) {
                    TextButton(onClick = { showTimePicker = true }) {
                        Text(
                            text = "ساعت ${formatHHMM(timeHour, timeMinute).faDigits()}",
                            color = GoldAccent,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                if (scheduleType == ScheduleType.WEEKLY) {
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "روزهای هفته",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Task.WEEK_DAY_FA.forEachIndexed { index, label ->
                            SelectablePill(
                                text = label,
                                selected = weekDays.contains(index),
                                onClick = {
                                    weekDays = if (weekDays.contains(index)) {
                                        weekDays - index
                                    } else {
                                        weekDays + index
                                    }
                                    weekDaysError = false
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    if (weekDaysError) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "حداقل یک روز را انتخاب کنید",
                            color = DangerRed,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                if (scheduleType == ScheduleType.MONTHLY) {
                    Spacer(Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "روز ماه",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = dayOfMonth.toString().faDigits(),
                            color = GoldAccent,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = " (اگر روز وجود نداشت، آخر ماه)",
                            color = TextSecondary.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Slider(
                        value = dayOfMonth.toFloat(),
                        onValueChange = { dayOfMonth = it.toInt().coerceIn(1, 31) },
                        valueRange = 1f..31f,
                        steps = 29,
                        colors = SliderDefaults.colors(
                            thumbColor = GoldAccent,
                            activeTrackColor = GoldAccent,
                            inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                        )
                    )
                }

                Spacer(Modifier.height(14.dp))

                GlassTextField(
                    value = reminderNotes,
                    onValueChange = { reminderNotes = it },
                    label = "یادداشت یادآور (اختیاری)",
                    placeholder = "متن همراه اعلان…",
                    singleLine = false,
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(22.dp))

            GoldButton(
                text = "ذخیره وظیفه",
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                        return@GoldButton
                    }
                    if (reminderEnabled && scheduleType == ScheduleType.WEEKLY && weekDays.isEmpty()) {
                        weekDaysError = true
                        return@GoldButton
                    }
                    val finalType =
                        if (reminderEnabled) scheduleType else ScheduleType.NONE
                    val base = existing ?: Task(memberId = memberId, title = title.trim())
                    val task = base.copy(
                        title = title.trim(),
                        description = description.trim(),
                        reminderEnabled = reminderEnabled,
                        scheduleType = finalType.name,
                        timeHHMM = formatHHMM(timeHour, timeMinute),
                        weekDays = if (finalType == ScheduleType.WEEKLY) weekDays.sorted() else emptyList(),
                        dayOfMonth = dayOfMonth.coerceIn(1, 31),
                        reminderNotes = reminderNotes.trim(),
                        updatedAt = System.currentTimeMillis()
                    )
                    onSave(task)
                },
                modifier = Modifier.fillMaxWidth()
            )

            if (existing != null && onDelete != null) {
                Spacer(Modifier.height(12.dp))
                DangerOutlineButton(
                    text = "حذف وظیفه",
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(10.dp))
        }
    }

    // ---- انتخابگر زمان ----
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = timeHour,
            initialMinute = timeMinute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            containerColor = BgMid,
            confirmButton = {
                TextButton(onClick = {
                    timeHour = timePickerState.hour
                    timeMinute = timePickerState.minute
                    showTimePicker = false
                }) {
                    Text("تأیید", color = GoldAccent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("انصراف", color = TextSecondary)
                }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }

    if (showDeleteConfirm) {
        ConfirmDialog(
            title = "حذف وظیفه",
            text = "این وظیفه و یادآور آن برای همیشه حذف می‌شود.",
            confirmLabel = "حذف",
            destructive = true,
            onConfirm = {
                showDeleteConfirm = false
                onDelete?.invoke()
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}
