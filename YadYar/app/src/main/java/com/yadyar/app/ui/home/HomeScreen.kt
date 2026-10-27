package com.yadyar.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yadyar.app.YadyarApp
import com.yadyar.app.data.model.MemberWithStats
import com.yadyar.app.data.model.Task
import com.yadyar.app.ui.components.*
import com.yadyar.app.ui.theme.*
import com.yadyar.app.util.faDigits
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// ===== Home ViewModel =====
class HomeViewModel(
    val memberRepository: com.yadyar.app.data.repository.MemberRepository,
    private val taskRepository: com.yadyar.app.data.repository.TaskRepository,
    private val folderDao: com.yadyar.app.data.db.FolderDao
) : ViewModel() {
    val members = memberRepository.observeActiveWithStats()
    val archived = memberRepository.observeArchived()
    val allTasksFlow: Flow<List<Task>> = kotlinx.coroutines.flow.flow {
        emit(taskRepository.getAll())
    }
}

@Composable
fun HomeScreen(
    onMemberClick: (String) -> Unit,
    onAddMember: () -> Unit,
    onOpenMembers: () -> Unit,
    onOpenTasks: () -> Unit,
    onOpenFolders: () -> Unit,
    onOpenDocuments: () -> Unit,
    onOpenSettings: () -> Unit,
    onEditMember: (String) -> Unit,
    onAddTask: () -> Unit,
    vm: HomeViewModel = viewModel(factory = HomeViewModelFactory)
) {
    val members by vm.members.collectAsStateWithLifecycle(initialValue = emptyList())
    val archived by vm.archived.collectAsStateWithLifecycle(initialValue = emptyList())
    val openTasks = members.sumOf { it.openTaskCount }
    val totalMembers = members.size
    val archivedCount = archived.size

    // Quick stats derived
    val todayTasksCount = remember(members) { openTasks } // simplified

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(horizontal = MonoDimens.ContentPadding)) {
                Spacer(Modifier.height(12.dp))
                MonoHomeHeader(
                    onCalendarClick = onOpenTasks,
                    onNotificationClick = onOpenSettings
                )
                Spacer(Modifier.height(14.dp))
                PrivacyBanner()
            }
        }

        // Summary cards row
        item {
            Spacer(Modifier.height(16.dp))
            Column(modifier = Modifier.padding(horizontal = MonoDimens.ContentPadding)) {
                Text(
                    text = "خلاصه امروز",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    SummaryCard(
                        title = "اعضای فعال",
                        value = totalMembers.toString().faDigits(),
                        subtitle = "${archivedCount.toString().faDigits()} بایگانی‌شده",
                        icon = Icons.Outlined.People,
                        container = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenMembers
                    )
                    SummaryCard(
                        title = "وظایف باز",
                        value = openTasks.toString().faDigits(),
                        subtitle = "از ${ (members.sumOf { it.totalTaskCount }).toString().faDigits()} کل",
                        icon = Icons.Outlined.Checklist,
                        container = Color(0xFFFFE9C8),
                        contentColor = Color(0xFF6A3B00),
                        modifier = Modifier.weight(1f),
                        onClick = onOpenTasks
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    SummaryCard(
                        title = "پرونده‌ها",
                        value = "۲",
                        subtitle = "امور کاری / شخصی",
                        icon = Icons.Outlined.Folder,
                        container = Color(0xFFE8F0F0),
                        contentColor = MonoDeepTeal,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenFolders
                    )
                    SummaryCard(
                        title = "اسناد",
                        value = "پیش‌نویس",
                        subtitle = "اشتغال و فیش",
                        icon = Icons.Outlined.Description,
                        container = Color(0xFFF0E6F6),
                        contentColor = Color(0xFF4A2D7A),
                        modifier = Modifier.weight(1f),
                        onClick = onOpenDocuments
                    )
                }
            }
        }

        // Quick actions
        item {
            Spacer(Modifier.height(20.dp))
            Column(modifier = Modifier.padding(horizontal = MonoDimens.ContentPadding)) {
                MonoSectionHeader(title = "دسترسی سریع")
                Spacer(Modifier.height(10.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(end = 0.dp)
                ) {
                    item { QuickActionChip("افزودن عضو", Icons.Rounded.PersonAdd, MonoDeepTeal, onClick = onAddMember) }
                    item { QuickActionChip("افزودن وظیفه", Icons.Rounded.AddTask, MonoAmber, onClick = onAddTask) }
                    item { QuickActionChip("ایجاد پوشه", Icons.Rounded.CreateNewFolder, MonoSecondary, onClick = onOpenFolders) }
                    item { QuickActionChip("سند جدید", Icons.Rounded.NoteAdd, Color(0xFF6B5CE7), onClick = onOpenDocuments) }
                }
            }
        }

        // Today's program
        item {
            Spacer(Modifier.height(20.dp))
            Column(modifier = Modifier.padding(horizontal = MonoDimens.ContentPadding)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    MonoSectionHeader(title = "برنامه امروز", modifier = Modifier.weight(1f))
                    TextButton(onClick = onOpenTasks) { Text("مشاهده همه", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge) }
                }
                Spacer(Modifier.height(10.dp))
                if (members.isEmpty()) {
                    MonoCard(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                                contentAlignment = Alignment.Center
                            ) { Icon(Icons.Outlined.EventNote, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("هنوز وظیفه‌ای برای امروز ثبت نشده", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                                Text("با «افزودن وظیفه» برنامه‌ات را بساز", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                } else {
                    // Show first 2 members as today's tasks placeholder
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        members.take(2).forEach { item ->
                            MonoCard(modifier = Modifier.fillMaxWidth(), onClick = { onMemberClick(item.member.id) }) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    MemberDot(letter = item.member.avatarLetter.ifBlank { item.member.name.take(1) }, color = item.member.avatarColor)
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.member.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Icon(Icons.Outlined.Schedule, null, Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("ساعت ۱۴:۳۰  •  یادآور فعال", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    StatusDot(done = false)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Upcoming reminders
        item {
            Spacer(Modifier.height(20.dp))
            Column(modifier = Modifier.padding(horizontal = MonoDimens.ContentPadding)) {
                MonoSectionHeader(title = "یادآورهای نزدیک", subtitle = "۳ روز آینده")
                Spacer(Modifier.height(10.dp))
                val reminders = listOf(
                    Triple("پیگیری سفارش", "علی رضایی", Icons.Outlined.Notifications),
                    Triple("ارسال گزارش ماهانه", "سارا احمدی", Icons.Outlined.EventNote),
                )
                if (members.isEmpty()) {
                    MonoEmptyState(
                        title = "یادآوری فعالی وجود ندارد",
                        subtitle = "یادآورهای روزانه، هفتگی یا ماهانه را برای هر عضو فعال کن تا اینجا نمایش داده شوند.",
                        icon = Icons.Outlined.NotificationsActive,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        reminders.forEach { (title, member, icon) ->
                            MonoCard(modifier = Modifier.fillMaxWidth()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(38.dp)) {
                                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                            Icon(icon, null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        Text(member, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    MonoBadge(text = "فردا", container = MonoWarningContainer, contentColor = MonoWarning)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Secondary access grid: تقویم، تمرکز، هدف‌ها، عادت‌ها، یادداشت‌ها، گزارش‌ها
        item {
            Spacer(Modifier.height(20.dp))
            Column(modifier = Modifier.padding(horizontal = MonoDimens.ContentPadding)) {
                MonoSectionHeader(title = "ابزارها")
                Spacer(Modifier.height(10.dp))
                val tools = listOf(
                    Quadruple("تقویم", "برنامه‌ریز", Icons.Outlined.CalendarMonth, Color(0xFF1B7A4B)),
                    Quadruple("تمرکز", "جلسه کاری", Icons.Outlined.Timer, Color(0xFF6B5CE7)),
                    Quadruple("هدف‌ها", "اهداف ماهانه", Icons.Outlined.Flag, MonoAmber),
                    Quadruple("عادت‌ها", "پیگیری روزانه", Icons.Outlined.Loop, Color(0xFF155E7A)),
                    Quadruple("یادداشت‌ها", "ایده‌ها و یادداشت", Icons.Outlined.StickyNote2, MonoSecondary),
                    Quadruple("گزارش‌ها", "آمار و نمودار", Icons.Outlined.BarChart, MonoDeepTeal),
                )
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    tools.chunked(2).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            row.forEach { (title, sub, icon, color) ->
                                MonoCard(
                                    modifier = Modifier.weight(1f),
                                    background = Color.White,
                                    onClick = onOpenTasks
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(color.copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) { Icon(icon, null, tint = color, modifier = Modifier.size(18.dp)) }
                                        Spacer(Modifier.width(10.dp))
                                        Column {
                                            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                            Text(sub, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onOpenFolders,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(ButtonRadius)
                ) {
                    Icon(Icons.Outlined.FolderOpen, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("مشاهده پوشه‌ها", fontWeight = FontWeight.Bold)
                }
            }
        }

        item { Spacer(Modifier.height(12.dp)) }
    }
}

private data class Quadruple(val a: String, val b: String, val c: ImageVector, val d: Color)

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    container: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    MonoCard(
        modifier = modifier,
        background = Color.White,
        contentPadding = PaddingValues(14.dp),
        onClick = onClick
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(container),
                contentAlignment = Alignment.Center
            ) { Icon(icon, null, tint = contentColor, modifier = Modifier.size(18.dp)) }
            Spacer(Modifier.weight(1f))
            Icon(Icons.Rounded.MoreHoriz, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.height(10.dp))
        Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(2.dp))
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
        Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun QuickActionChip(
    label: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        modifier = Modifier
            .heightIn(min = 42.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.13f)),
                contentAlignment = Alignment.Center
            ) { Icon(icon, null, tint = color, modifier = Modifier.size(16.dp)) }
            Spacer(Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun MemberDot(letter: String, color: String) {
    val c = try { Color(android.graphics.Color.parseColor(color)) } catch (_: Exception) { MonoDeepTeal }
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(c),
        contentAlignment = Alignment.Center
    ) {
        Text(letter.take(1).ifBlank { "؟" }, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
private fun StatusDot(done: Boolean) {
    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(if (done) MonoSuccess else MonoWarning)
    )
}

// Proper factory using YadyarApp
val HomeViewModelFactory: ViewModelProvider.Factory = viewModelFactory {
    initializer {
        val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as YadyarApp
        HomeViewModel(app.container.memberRepository, app.container.taskRepository, app.container.database.folderDao())
    }
}
