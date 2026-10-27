package com.yadyar.app.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yadyar.app.data.model.Member
import com.yadyar.app.data.model.MemberWithStats
import com.yadyar.app.ui.components.ConfirmDialog
import com.yadyar.app.ui.components.EmptyGlass
import com.yadyar.app.ui.components.GlassCard
import com.yadyar.app.ui.components.GradientBackground
import com.yadyar.app.ui.components.MemberAvatar
import com.yadyar.app.ui.components.TypeBadge
import com.yadyar.app.ui.theme.BgMid
import com.yadyar.app.ui.theme.DangerRed
import com.yadyar.app.ui.theme.GoldAccent
import com.yadyar.app.ui.theme.SuccessGreen
import com.yadyar.app.ui.theme.TextPrimary
import com.yadyar.app.ui.theme.TextSecondary
import com.yadyar.app.util.faDigits

@Composable
fun DashboardScreen(
    onMemberClick: (String) -> Unit,
    onAddMember: () -> Unit,
    onEditMember: (String) -> Unit,
    onSettings: () -> Unit,
    folderId: String? = null,
    viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.factory(folderId))
) {
    val members by viewModel.members.collectAsStateWithLifecycle(initialValue = emptyList())
    val archived by viewModel.archived.collectAsStateWithLifecycle(initialValue = emptyList())
    val openTasks = members.sumOf { it.openTaskCount }

    var showArchivedDialog by remember { mutableStateOf(false) }

    GradientBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 18.dp)
            ) {
                Spacer(Modifier.height(14.dp))

                // ---- سربرگ ----
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "MONO",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "آزادسازی ذهن از اطلاعات و وظایف",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                    GlassCard(
                        cornerRadius = 18.dp,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Rounded.People,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "${members.size.toString().faDigits()} عضو",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Text(
                                    text = "${openTasks.toString().faDigits()} وظیفه باز",
                                    color = TextSecondary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = onSettings) {
                        Icon(
                            Icons.Rounded.Settings,
                            contentDescription = "تنظیمات",
                            tint = TextPrimary
                        )
                    }
                }

                // ---- دکمه بایگانی‌شده‌ها ----
                if (archived.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    TextButton(onClick = { showArchivedDialog = true }) {
                        Icon(
                            Icons.Rounded.Archive,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "بایگانی‌شده‌ها (${archived.size.toString().faDigits()})",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                if (members.isEmpty()) {
                    EmptyGlass("هنوز عضوی ثبت نشده — دکمه «+» را بزنید")
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 96.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(members, key = { it.member.id }) { item ->
                            MemberCard(
                                item = item,
                                onClick = { onMemberClick(item.member.id) },
                                onEdit = { onEditMember(item.member.id) },
                                onArchive = { viewModel.archiveMember(item.member) },
                                onDelete = { viewModel.deleteMember(item.member) }
                            )
                        }
                    }
                }
            }

            // ---- FAB افزودن عضو ----
            FloatingActionButton(
                onClick = onAddMember,
                containerColor = GoldAccent,
                contentColor = Color(0xFF2B2203),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(20.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "عضو جدید")
            }
        }
    }

    // ---- دیالوگ بایگانی‌شده‌ها ----
    if (showArchivedDialog) {
        AlertDialog(
            onDismissRequest = { showArchivedDialog = false },
            containerColor = BgMid,
            title = {
                Text("اعضای بایگانی‌شده", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    if (archived.isEmpty()) {
                        Text("لیست خالی است", color = TextSecondary)
                    } else {
                        archived.forEach { member ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {
                                MemberAvatar(
                                    letter = member.avatarLetter.ifBlank { member.name.take(1) },
                                    colorHex = member.avatarColor,
                                    size = 36.dp,
                                    fontSize = 14.sp,
                                    imageUri = member.avatarImageUri
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = member.name,
                                    color = TextPrimary,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                TextButton(onClick = { viewModel.unarchiveMember(member) }) {
                                    Icon(
                                        Icons.Rounded.Refresh,
                                        contentDescription = null,
                                        tint = SuccessGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text("بازگردانی", color = SuccessGreen)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showArchivedDialog = false }) {
                    Text("بستن", color = GoldAccent, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun MemberCard(
    item: MemberWithStats,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit
) {
    val member = item.member
    var menuOpen by remember { mutableStateOf(false) }
    var showArchiveConfirm by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    GlassCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MemberAvatar(
                letter = member.avatarLetter.ifBlank { member.name.take(1) },
                colorHex = member.avatarColor,
                size = 52.dp,
                fontSize = 20.sp,
                imageUri = member.avatarImageUri
            )
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = member.name,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(5.dp))
                TypeBadge(member.typeEnum)
            }
            Box {
                IconButton(onClick = { menuOpen = true }, modifier = Modifier.size(30.dp)) {
                    Icon(
                        Icons.Rounded.MoreVert,
                        contentDescription = "گزینه‌ها",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text("ویرایش") },
                        leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null) },
                        onClick = {
                            menuOpen = false
                            onEdit()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("بایگانی") },
                        leadingIcon = { Icon(Icons.Rounded.Archive, contentDescription = null) },
                        onClick = {
                            menuOpen = false
                            showArchiveConfirm = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("حذف", color = DangerRed) },
                        leadingIcon = {
                            Icon(Icons.Rounded.Delete, contentDescription = null, tint = DangerRed)
                        },
                        onClick = {
                            menuOpen = false
                            showDeleteConfirm = true
                        }
                    )
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = if (item.openTaskCount > 0) SuccessGreen else TextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = if (item.openTaskCount > 0) {
                    "${item.openTaskCount.toString().faDigits()} وظیفه باز"
                } else {
                    "بدون وظیفه باز"
                },
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    if (showArchiveConfirm) {
        ConfirmDialog(
            title = "بایگانی ${member.name}",
            text = "عضو از لیست اصلی پنهان می‌شود و وظایف او حفظ می‌شوند.",
            confirmLabel = "بایگانی",
            onConfirm = {
                showArchiveConfirm = false
                onArchive()
            },
            onDismiss = { showArchiveConfirm = false }
        )
    }

    if (showDeleteConfirm) {
        ConfirmDialog(
            title = "حذف ${member.name}",
            text = "تمام اطلاعات و وظایف این عضو برای همیشه حذف می‌شوند. این عمل قابل بازگشت نیست.",
            confirmLabel = "حذف",
            destructive = true,
            onConfirm = {
                showDeleteConfirm = false
                onDelete()
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}
