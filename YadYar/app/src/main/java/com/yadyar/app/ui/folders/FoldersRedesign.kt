package com.yadyar.app.ui.folders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yadyar.app.data.model.Folder
import com.yadyar.app.ui.components.*
import com.yadyar.app.ui.theme.*
import com.yadyar.app.util.faDigits

// Enhanced folder card matching spec: amber, blue, gray/teal, name + count, open/create CTA
@Composable
fun FoldersRedesignScreen(
    onOpenMembers: (String?) -> Unit,
    onSettings: () -> Unit,
    vm: FolderViewModel = viewModel(factory = FolderViewModel.Factory)
) {
    // Reuse existing VM logic but present with new design
    val roots by vm.observe(null).collectAsStateWithLifecycle(initialValue = emptyList())
    var newFolderName by remember { mutableStateOf("") }
    var showCreate by remember { mutableStateOf(false) }

    // For preview we also show sample colored folders if DB empty
    val sampleFolders = remember(roots) {
        if (roots.isNotEmpty()) roots else listOf(
            Folder(name = "بانک مهر", avatarLetter = "🏦", avatarColor = "#B86B19"),
            Folder(name = "بانک ملی", avatarLetter = "🏛️", avatarColor = "#2A7A8A"),
            Folder(name = "امور شخصی", avatarLetter = "👤", avatarColor = "#4A5D6A"),
            Folder(name = "پروژه‌ها", avatarLetter = "📁", avatarColor = "#6B5CE7"),
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding()) {
        Column(modifier = Modifier.padding(horizontal = MonoDimens.ContentPadding)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
                Text("پوشه‌ها", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.weight(1f))
                IconButton(onClick = onSettings) { Icon(Icons.Rounded.Settings, "تنظیمات", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            Text("پوشه‌ها را بر اساس بانک، پروژه یا دسته‌بندی دلخواه بسازید. هر پوشه تعداد پرونده‌هایش را نشان می‌دهد.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                MonoPrimaryButton(text = "پوشه جدید", onClick = { showCreate = true }, icon = Icons.Rounded.CreateNewFolder, modifier = Modifier.weight(1f))
                OutlinedButton(onClick = { onOpenMembers(null) }, shape = RoundedCornerShape(ButtonRadius), modifier = Modifier.weight(1f).heightIn(min = MonoDimens.TouchMin)) {
                    Icon(Icons.Outlined.FolderOpen, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("بدون پوشه", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(14.dp))
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = MonoDimens.ContentPadding, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(sampleFolders) { folder ->
                FolderColorCard(folder = folder, onOpen = { onOpenMembers(folder.id) }, onCreate = { showCreate = true })
            }
            item { Spacer(Modifier.height(80.dp)) }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    if (showCreate) {
        AlertDialog(
            onDismissRequest = { showCreate = false },
            title = { Text("پوشه جدید", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = newFolderName, onValueChange = { newFolderName = it }, label = { Text("نام پوشه") }, placeholder = { Text("مثلاً: بانک صادرات") }, singleLine = true, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("#B86B19" to "کهربایی", "#2A7A8A" to "آبی", "#4A5D6A" to "خاکستری").forEach { (hex, name) ->
                            AssistChip(onClick = {}, label = { Text(name, style = MaterialTheme.typography.labelSmall) }, leadingIcon = { Box(Modifier.size(10.dp).clip(CircleShape).background(try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { MonoDeepTeal })) })
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newFolderName.isNotBlank()) {
                        vm.save(Folder(name = newFolderName.trim(), avatarLetter = "📁", avatarColor = "#2A7A8A"))
                        newFolderName = ""
                        showCreate = false
                    }
                }) { Text("ایجاد") }
            },
            dismissButton = { TextButton(onClick = { showCreate = false }) { Text("انصراف") } }
        )
    }
}

@Composable
private fun FolderColorCard(folder: Folder, onOpen: () -> Unit, onCreate: () -> Unit) {
    val accent = try { Color(android.graphics.Color.parseColor(folder.avatarColor)) } catch (_: Exception) { MonoDeepTeal }
    val bg = accent.copy(alpha = 0.10f)
    val iconBg = accent.copy(alpha = 0.16f)
    MonoCard(
        modifier = Modifier.fillMaxWidth(),
        background = Color.White,
        contentPadding = PaddingValues(14.dp),
        onClick = onOpen
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(iconBg), contentAlignment = Alignment.Center) {
                Text(folder.avatarLetter.ifBlank { "📁" }, fontSize = 18.sp)
            }
            Spacer(Modifier.weight(1f))
            Icon(Icons.Outlined.MoreVert, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.height(12.dp))
        Text(folder.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(4.dp))
        Text("۰ پرونده", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(12.dp))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(accent.copy(alpha = 0.18f)))
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Surface(shape = RoundedCornerShape(999.dp), color = accent, contentColor = Color.White, modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.padding(vertical = 8.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.FolderOpen, null, Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("باز کردن", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
