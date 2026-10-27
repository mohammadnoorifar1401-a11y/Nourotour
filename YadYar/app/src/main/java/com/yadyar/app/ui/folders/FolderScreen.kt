package com.yadyar.app.ui.folders

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.yadyar.app.YadyarApp
import com.yadyar.app.data.db.FolderDao
import com.yadyar.app.data.model.Folder
import com.yadyar.app.ui.components.*
import com.yadyar.app.ui.theme.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class FolderViewModel(private val dao: FolderDao, private val members: com.yadyar.app.data.db.MemberDao) : ViewModel() {
    fun observe(parent: String?): Flow<List<Folder>> = dao.observeByParent(parent)
    fun save(folder: Folder) { viewModelScope.launch { dao.upsert(folder) } }
    fun delete(folder: Folder) { viewModelScope.launch { deleteTree(folder.id) } }
    private suspend fun deleteTree(id: String) {
        dao.getByParent(id).forEach { deleteTree(it.id) }
        members.clearFolder(id, System.currentTimeMillis())
        dao.deleteById(id)
    }
    suspend fun children(id: String) = dao.getByParent(id)
    fun duplicate(source: Folder, parentId: String?) { viewModelScope.launch { copyTree(source, parentId, "${source.name} - کپی") } }
    private suspend fun copyTree(source: Folder, parentId: String?, name: String) {
        val copy = Folder(parentId=parentId, name=name, avatarLetter=source.avatarLetter, avatarColor=source.avatarColor, avatarImageUri=source.avatarImageUri, sortOrder=source.sortOrder)
        dao.upsert(copy)
        dao.getByParent(source.id).forEach { child -> copyTree(child, copy.id, child.name) }
    }
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as YadyarApp
                FolderViewModel(app.container.database.folderDao(), app.container.database.memberDao())
            }
        }
    }
}

@Composable
fun FolderScreen(onOpenMembers: (String?) -> Unit, onSettings: () -> Unit, vm: FolderViewModel = viewModel(factory = FolderViewModel.Factory)) {
    var parentId by remember { mutableStateOf<String?>(null) }
    var path by remember { mutableStateOf(listOf<Pair<String?, String>>(null to "MONO")) }
    val folders by vm.observe(parentId).collectAsStateWithLifecycle(initialValue = emptyList())
    var editFolder by remember { mutableStateOf<Folder?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("📁") }
    var menuFolder by remember { mutableStateOf<Folder?>(null) }
    var clipboard by remember { mutableStateOf<Folder?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val dao = (context.applicationContext as YadyarApp).container.database.folderDao()
        if (dao.count() == 0) {
            val work = Folder(name = "امورکاری", avatarLetter = "ک", sortOrder = 0)
            val personal = Folder(name = "امورشخصی", avatarLetter = "ش", sortOrder = 1)
            dao.upsert(work); dao.upsert(personal)
            val bankMeh = Folder(parentId = work.id, name = "بانک مهر", avatarLetter = "🏦", sortOrder = 0)
            dao.upsert(bankMeh)
            dao.upsert(Folder(parentId = work.id, name = "بانک ملی", avatarLetter = "🏦", sortOrder = 1))
        }
        val roots = dao.getRoots()
        val workFolder = roots.firstOrNull { it.name == "امورکاری" }
        val bankMeh = workFolder?.let { dao.getByParent(it.id).firstOrNull { child -> child.name == "بانک مهر" } }
        if (bankMeh != null) (context.applicationContext as YadyarApp).container.database.memberDao().assignUnfiledMembers(bankMeh.id)
    }

    GradientBackground {
        Column(Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 18.dp)) {
            Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                if (path.size > 1) IconButton(onClick = { path = path.dropLast(1); parentId = path.last().first }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "بازگشت", tint = TextPrimary) }
                Column(Modifier.weight(1f)) {
                    Text(path.last().second, color = TextPrimary, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("مدیریت امور کاری آقای نوری فر", color = TextSecondary)
                }
                TextButton(onClick = onSettings) { Text("تنظیمات", color = GoldAccent) }
            }
            if (folders.isEmpty()) EmptyGlass("این پوشه خالی است — + برای افزودن")
            else LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 90.dp)) {
                items(folders, key = { it.id }) { folder ->
                    GlassCard(modifier = Modifier.fillMaxWidth().clickable {
                        scope.launch { if (vm.children(folder.id).isEmpty()) onOpenMembers(folder.id) else { parentId = folder.id; path = path + (folder.id to folder.name) } }
                    }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(folder.avatarLetter.ifBlank { "📁" }, fontSize = MaterialTheme.typography.headlineSmall.fontSize)
                            Spacer(Modifier.width(14.dp))
                            Text(folder.name, color = TextPrimary, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                            IconButton(onClick = { menuFolder = folder }) { Icon(Icons.Rounded.MoreVert, "گزینه‌ها", tint = TextSecondary) }
                            DropdownMenu(expanded = menuFolder?.id == folder.id, onDismissRequest = { menuFolder = null }) {
                                DropdownMenuItem(text = { Text("باز کردن") }, onClick = { menuFolder = null; scope.launch { if (vm.children(folder.id).isEmpty()) onOpenMembers(folder.id) else { parentId=folder.id; path=path+(folder.id to folder.name) } } })
                                DropdownMenuItem(text = { Text("تغییر نام") }, onClick = { menuFolder=null; editFolder=folder; name=folder.name; selectedIcon=folder.avatarLetter.ifBlank { "📁" }; showDialog=true })
                                DropdownMenuItem(text = { Text("تغییر نماد") }, onClick = { menuFolder=null; editFolder=folder; name=folder.name; selectedIcon=folder.avatarLetter.ifBlank { "📁" }; showDialog=true })
                                DropdownMenuItem(text = { Text("کپی") }, onClick = { clipboard=folder; menuFolder=null })
                                if (clipboard != null) DropdownMenuItem(text = { Text("جای‌گذاری کپی") }, onClick = { vm.duplicate(clipboard!!, parentId); menuFolder=null })
                                DropdownMenuItem(text = { Text("حذف", color= DangerRed) }, onClick = { vm.delete(folder); menuFolder=null })
                            }
                        }
                    }
                }
            }
        }
        if (parentId != null || folders.size < 2) FloatingActionButton(onClick={ editFolder=null; name=""; selectedIcon="📁"; showDialog=true }, modifier=Modifier.align(Alignment.BottomEnd).navigationBarsPadding().padding(20.dp), containerColor=GoldAccent, contentColor=BgDeep) { Icon(Icons.Rounded.Add, "افزودن پوشه") }
    }
    if (showDialog) AlertDialog(onDismissRequest={showDialog=false}, title={Text(if(editFolder==null) "پوشهٔ جدید" else "نام و نماد پوشه")}, text={
        Column {
            OutlinedTextField(value=name,onValueChange={name=it},label={Text("نام پوشه")})
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                listOf("📁", "🏦", "💼", "🏠", "📂", "⭐", "🗂️").forEach { icon ->
                    TextButton(onClick={selectedIcon=icon}) { Text(if(selectedIcon==icon) "✓$icon" else icon, fontSize=MaterialTheme.typography.titleLarge.fontSize) }
                }
            }
        }
    }, confirmButton={TextButton(onClick={if(name.isNotBlank()){ vm.save((editFolder ?: Folder(parentId=parentId,name=name)).copy(name=name, avatarLetter=selectedIcon, parentId=parentId, updatedAt=System.currentTimeMillis())); showDialog=false }} ){Text("ذخیره")}}, dismissButton={TextButton(onClick={showDialog=false}){Text("انصراف")}})
}
