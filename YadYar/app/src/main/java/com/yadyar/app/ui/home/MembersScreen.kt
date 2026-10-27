package com.yadyar.app.ui.home

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yadyar.app.data.model.MemberType
import com.yadyar.app.ui.components.*
import com.yadyar.app.ui.dashboard.DashboardViewModel
import com.yadyar.app.ui.theme.*
import com.yadyar.app.util.faDigits

@Composable
fun MembersScreen(
    onMemberClick: (String) -> Unit,
    onAddMember: () -> Unit,
    onEditMember: (String) -> Unit,
    onSettings: () -> Unit,
    viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory)
) {
    val members by viewModel.members.collectAsStateWithLifecycle(initialValue = emptyList())
    val archived by viewModel.archived.collectAsStateWithLifecycle(initialValue = emptyList())
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf<MemberType?>(null) }
    var showArchived by remember { mutableStateOf(false) }

    val filtered = members.filter { item ->
        val matchesQuery = query.isBlank() || item.member.name.contains(query, ignoreCase = true)
        val matchesType = filter == null || item.member.typeEnum == filter
        matchesQuery && matchesType
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Header
        Column(modifier = Modifier.padding(horizontal = MonoDimens.ContentPadding)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
                Column(Modifier.weight(1f)) {
                    Text("اعضا", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
                    Text("مدیریت مشتریان و پرسنل", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onSettings) { Icon(Icons.Rounded.Settings, "تنظیمات", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
            }

            // Search
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("جست‌وجو در نام اعضا…", style = MaterialTheme.typography.bodyMedium) },
                leadingIcon = { Icon(Icons.Outlined.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                trailingIcon = { if (query.isNotBlank()) IconButton(onClick = { query = "" }) { Icon(Icons.Rounded.Close, null) } },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = filter == null,
                    onClick = { filter = null },
                    label = { Text("همه") },
                    leadingIcon = { if (filter == null) Icon(Icons.Rounded.Check, null, Modifier.size(14.dp)) }
                )
                FilterChip(
                    selected = filter == MemberType.CUSTOMER,
                    onClick = { filter = if (filter == MemberType.CUSTOMER) null else MemberType.CUSTOMER },
                    label = { Text(MemberType.CUSTOMER.fa) }
                )
                FilterChip(
                    selected = filter == MemberType.STAFF,
                    onClick = { filter = if (filter == MemberType.STAFF) null else MemberType.STAFF },
                    label = { Text(MemberType.STAFF.fa) }
                )
                Spacer(Modifier.weight(1f))
                if (archived.isNotEmpty()) {
                    TextButton(onClick = { showArchived = true }) {
                        Icon(Icons.Outlined.Inventory2, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("بایگانی (${archived.size.toString().faDigits()})", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        MonoDivider(modifier = Modifier.padding(horizontal = MonoDimens.ContentPadding))

        // List
        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(18.dp), contentAlignment = Alignment.Center) {
                if (query.isNotBlank() || filter != null) {
                    MonoEmptyState(
                        title = "نتیجه‌ای یافت نشد",
                        subtitle = "جست‌وجو یا فیلتر را تغییر دهید تا دوباره امتحان کنید.",
                        icon = Icons.Outlined.SearchOff,
                        actionLabel = "پاک کردن فیلتر",
                        onAction = { query = ""; filter = null }
                    )
                } else {
                    MonoEmptyState(
                        title = "هنوز عضوی ثبت نشده",
                        subtitle = "اولین عضو را اضافه کنید تا پرونده، وظایف و یادآورهای او را مدیریت کنید.",
                        icon = Icons.Outlined.People,
                        actionLabel = "افزودن عضو",
                        onAction = onAddMember
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = MonoDimens.ContentPadding, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filtered, key = { it.member.id }) { item ->
                    MonoElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onMemberClick(item.member.id) }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(48.dp).clip(CircleShape)
                                    .background(try { Color(android.graphics.Color.parseColor(item.member.avatarColor)) } catch (_: Exception) { MonoDeepTeal }),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(item.member.avatarLetter.ifBlank { item.member.name.take(1) }, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(item.member.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                                    Surface(shape = RoundedCornerShape(999.dp), color = if (item.member.typeEnum == MemberType.CUSTOMER) Color(0xFFE0ECFF) else Color(0xFFFFE9C8)) {
                                        Text(item.member.typeEnum.fa, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = if (item.member.typeEnum == MemberType.CUSTOMER) Color(0xFF1A3A7A) else Color(0xFF6A3B00), modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.Outlined.CheckCircle, null, Modifier.size(12.dp), tint = if (item.openTaskCount > 0) MonoWarning else MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("${item.openTaskCount.toString().faDigits()} باز", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                                    Text("${item.totalTaskCount.toString().faDigits()} وظیفه", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            IconButton(onClick = { onEditMember(item.member.id) }, modifier = Modifier.size(36.dp)) {
                                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainerHigh, modifier = Modifier.size(36.dp)) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) { Icon(Icons.Outlined.Edit, "ویرایش", Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                                }
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    // FAB
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
        FloatingActionButton(
            onClick = onAddMember,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(18.dp).navigationBarsPadding()
        ) { Icon(Icons.Rounded.Add, "افزودن عضو") }
    }

    if (showArchived) {
        AlertDialog(
            onDismissRequest = { showArchived = false },
            title = { Text("بایگانی‌شده‌ها", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    if (archived.isEmpty()) Text("لیست خالی است", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    else archived.forEach { m ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(m.name, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                            TextButton(onClick = { viewModel.unarchiveMember(m) }) { Text("بازگردانی") }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showArchived = false }) { Text("بستن") } }
        )
    }
}
