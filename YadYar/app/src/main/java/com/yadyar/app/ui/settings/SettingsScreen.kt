package com.yadyar.app.ui.settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yadyar.app.backup.BackupManager
import com.yadyar.app.data.preferences.ThemePreference
import com.yadyar.app.security.AppLock
import com.yadyar.app.reminders.ReminderScheduler
import com.yadyar.app.ui.components.*
import com.yadyar.app.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val themePref = remember { ThemePreference.get(context) }
    val isDark by themePref.isDark.collectAsState(initial = false)

    var showRestoreConfirm by remember { mutableStateOf(false) }
    var pendingRestoreJson by remember { mutableStateOf<String?>(null) }
    var showPinDialog by remember { mutableStateOf(false) }
    var showPatternDialog by remember { mutableStateOf(false) }
    var patternValue by remember { mutableStateOf("") }
    var pinValue by remember { mutableStateOf("") }
    var pinEnabled by remember { mutableStateOf(AppLock.isEnabled(context)) }

    var notifGranted by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        )
    }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            try {
                val json = BackupManager.exportToJson(context)
                BackupManager.writeToFile(context, uri, json)
                toast(context, "بکاپ با موفقیت ذخیره شد")
            } catch (e: Exception) { toast(context, "خطا در ذخیره بکاپ: ${e.message}") }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            try {
                val text = BackupManager.readFromFile(context, uri)
                pendingRestoreJson = text
                showRestoreConfirm = true
            } catch (e: Exception) { toast(context, "خطا در خواندن فایل: ${e.message}") }
        }
    }
    val exactOk = ReminderScheduler.canScheduleExact(context)

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding().verticalScroll(rememberScrollState())
    ) {
        // AppBar
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = MonoDimens.ContentPadding, vertical = 8.dp)) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "بازگشت", tint = MaterialTheme.colorScheme.onBackground) }
            Column(Modifier.weight(1f)) {
                Text("تنظیمات", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.ExtraBold)
                Text("حریم خصوصی، ظاهر و پشتیبان", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            OfflineBadge()
        }

        Column(modifier = Modifier.padding(horizontal = MonoDimens.ContentPadding)) {
            PrivacyBanner()
            Spacer(Modifier.height(18.dp))

            // Appearance
            MonoSectionHeader(title = "ظاهر")
            Spacer(Modifier.height(10.dp))
            MonoCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(44.dp)) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) { Icon(Icons.Outlined.DarkMode, null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(22.dp)) }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("تم تاریک", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleSmall)
                        Text("برای استفاده در شب و کاهش مصرف باتری", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = isDark, onCheckedChange = { scope.launch { themePref.setDark(it) } })
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.weight(1f).height(64.dp), shape = RoundedCornerShape(12.dp), border = androidx.compose.foundation.BorderStroke(1.dp, MonoLineLight)) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) { Text("روشن", fontWeight = FontWeight.Bold, color = MonoDeepTeal) }
                    }
                    Card(colors = CardDefaults.cardColors(containerColor = MonoCardDark), modifier = Modifier.weight(1f).height(64.dp), shape = RoundedCornerShape(12.dp)) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) { Text("تیره", fontWeight = FontWeight.Bold, color = Color.White) }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            MonoSectionHeader(title = "حریم خصوصی و قفل")
            Spacer(Modifier.height(10.dp))
            MonoCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Lock, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (pinEnabled) "قفل برنامه فعال است" else "قفل برنامه", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                    if (pinEnabled) MonoBadge(text = "فعال", container = MonoSuccessContainer, contentColor = MonoSuccess)
                }
                Spacer(Modifier.height(6.dp))
                Text("با PIN یا الگو، ورود به MONO را محدود کنید. داده‌ها روی دستگاه می‌مانند و به اینترنت ارسال نمی‌شوند.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MonoPrimaryButton(text = if (pinEnabled) "تغییر PIN" else "تنظیم PIN", onClick = { pinValue = ""; showPinDialog = true }, modifier = Modifier.weight(1f))
                    OutlinedButton(onClick = { patternValue = ""; showPatternDialog = true }, shape = RoundedCornerShape(ButtonRadius), modifier = Modifier.weight(1f).heightIn(min = MonoDimens.TouchMin)) { Text("الگو", fontWeight = FontWeight.Bold) }
                }
                if (pinEnabled) {
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { AppLock.removePin(context); pinEnabled = false; toast(context, "قفل غیرفعال شد") },
                        shape = RoundedCornerShape(ButtonRadius),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MonoError),
                        modifier = Modifier.fillMaxWidth()
                    ) { Icon(Icons.Outlined.LockOpen, null, Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text("غیرفعال کردن قفل") }
                }
            }

            Spacer(Modifier.height(18.dp))
            MonoSectionHeader(title = "بکاپ محلی")
            Spacer(Modifier.height(10.dp))
            MonoCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(44.dp)) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) { Icon(Icons.Outlined.Backup, null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(22.dp)) }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("پشتیبان‌گیری JSON", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleSmall)
                        Text("همه اعضا، مشخصات و وظایف در یک فایل امن", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MonoPrimaryButton(text = "خروجی JSON", onClick = {
                        val stamp = SimpleDateFormat("yyyyMMdd-HHmm", Locale.US).format(Date())
                        exportLauncher.launch("yadyar-backup-$stamp.json")
                    }, icon = Icons.Rounded.FileUpload, modifier = Modifier.weight(1f))
                    MonoSecondaryButton(text = "بازیابی", onClick = { importLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) }, modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
                Surface(shape = RoundedCornerShape(10.dp), color = MonoWarningContainer, modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.Top) {
                        Icon(Icons.Outlined.WarningAmber, null, tint = MonoWarning, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("بازیابی، تمام داده‌های فعلی را جایگزین می‌کند. قبل از ادامه تأیید صریح گرفته می‌شود.", style = MaterialTheme.typography.bodySmall, color = MonoWarning)
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            MonoSectionHeader(title = "اعلان‌ها و یادآورها")
            Spacer(Modifier.height(10.dp))
            MonoCard(modifier = Modifier.fillMaxWidth()) {
                PermissionRow(title = "اعلان‌ها (نوتیفیکیشن)", subtitle = "برای نمایش یادآوری‌های محلی لازم است", ok = notifGranted, actionLabel = "تنظیمات اعلان", onAction = {
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply { putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName) }
                    runCatching { context.startActivity(intent) }
                })
                Spacer(Modifier.height(12.dp))
                MonoDivider()
                Spacer(Modifier.height(12.dp))
                PermissionRow(title = "یادآوری دقیق (Exact Alarm)", subtitle = if (exactOk) "فعال — یادآورها سرِ ساعت دقیق اجرا می‌شوند" else "غیرفعال — از تنظیمات اندروید فعال کنید", ok = exactOk, actionLabel = "باز کردن تنظیمات", onAction = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply { data = Uri.parse("package:${context.packageName}") }
                        runCatching { context.startActivity(intent) }
                    }
                })
                Spacer(Modifier.height(12.dp))
                Text("اگر مجوزها داده نشوند، فقط نوتیف نمایش داده نمی‌شود؛ ثبت و ویرایش اعضا و وظایف بدون مشکل کار می‌کند.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f))
                if (!notifGranted) {
                    Spacer(Modifier.height(10.dp))
                    MonoCard(background = MonoErrorContainer) {
                        Text("مجوز اعلان رد شده — برای دریافت یادآورها آن را فعال کنید.", color = MonoError, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            MonoSectionHeader(title = "وضعیت آفلاین و اطلاعات")
            Spacer(Modifier.height(10.dp))
            MonoCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(12.dp), color = MonoSuccessContainer, modifier = Modifier.size(44.dp)) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) { Icon(Icons.Outlined.CloudOff, null, tint = MonoSuccess, modifier = Modifier.size(22.dp)) }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("کاملاً آفلاین", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleSmall)
                        Text("بدون نیاز به اینترنت — داده‌ها روی دستگاه شما", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Rounded.CheckCircle, null, tint = MonoSuccess, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.height(12.dp))
                MonoDivider()
                Spacer(Modifier.height(12.dp))
                AboutRow("نام", "MONO — یادیار")
                AboutRow("نسخه", "1.8.0-preview (debug/test)")
                AboutRow("شناسه برنامه", "com.yadyar.app — نسخه preview با .preview")
                AboutRow("ذخیره‌سازی", "SQLite محلی + DataStore — بدون اینترنت")
                AboutRow("سازمان پیش‌فرض", "لاوان صنعت نوین شمس (قابل ویرایش)")
            }

            Spacer(Modifier.height(80.dp))
        }
    }

    if (showRestoreConfirm && pendingRestoreJson != null) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirm = false; pendingRestoreJson = null },
            title = { Text("تأیید بازیابی", fontWeight = FontWeight.Bold) },
            text = { Text("تمام داده‌های فعلی جایگزین محتوای فایل بکاپ می‌شوند. این عمل قابل بازگشت نیست. ادامه می‌دهید؟") },
            confirmButton = {
                TextButton(onClick = {
                    showRestoreConfirm = false
                    scope.launch {
                        try {
                            val backup = BackupManager.restoreFromJson(context, pendingRestoreJson!!)
                            toast(context, "بازیابی انجام شد: ${backup.members.size} عضو، ${backup.tasks.size} وظیفه")
                        } catch (e: Exception) { toast(context, "خطا در بازیابی: ${e.message}") }
                        pendingRestoreJson = null
                    }
                }) { Text("بازیابی", color = MonoError, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showRestoreConfirm = false; pendingRestoreJson = null }) { Text("انصراف") } }
        )
    }
    if (showPatternDialog) {
        AlertDialog(
            onDismissRequest = { showPatternDialog = false },
            title = { Text("تنظیم الگوی قفل", fontWeight = FontWeight.Bold) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("حداقل چهار نقطه را انتخاب کنید: ${patternValue.map { it.digitToInt() }.joinToString(" → ")}")
                    Spacer(Modifier.height(10.dp))
                    (0..2).forEach { row ->
                        Row {
                            (1..3).forEach { col ->
                                val n = row * 3 + col
                                OutlinedButton(onClick = { if (!patternValue.contains(n.toString())) patternValue += n.toString() }, modifier = Modifier.size(58.dp).padding(3.dp)) { Text(n.toString()) }
                            }
                        }
                    }
                    TextButton(onClick = { patternValue = "" }) { Text("پاک‌کردن") }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (patternValue.length >= 4) {
                        AppLock.setPattern(context, patternValue); pinEnabled = true; showPatternDialog = false; toast(context, "الگوی قفل ذخیره شد")
                    } else toast(context, "الگو باید دست‌کم چهار نقطه داشته باشد")
                }) { Text("ذخیره", fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showPatternDialog = false }) { Text("انصراف") } }
        )
    }
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("تنظیم قفل PIN", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = pinValue,
                    onValueChange = { pinValue = it.filter(Char::isDigit).take(12) },
                    label = { Text("رمز ۴ تا ۱۲ رقمی") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (pinValue.length >= 4) { AppLock.setPin(context, pinValue); pinEnabled = true; showPinDialog = false; toast(context, "قفل PIN فعال شد") }
                    else toast(context, "رمز باید دست‌کم ۴ رقم باشد")
                }) { Text("ذخیره", fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showPinDialog = false }) { Text("انصراف") } }
        )
    }
}

@Composable
private fun PermissionRow(title: String, subtitle: String, ok: Boolean, actionLabel: String, onAction: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(if (ok) Icons.Rounded.CheckCircle else Icons.Rounded.ErrorOutline, null, tint = if (ok) MonoSuccess else MonoError, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
        if (!ok) TextButton(onClick = onAction) { Text(actionLabel, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall) }
    }
}

@Composable
private fun AboutRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(110.dp))
        Text(value, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
    }
}

private fun toast(context: Context, message: String) { Toast.makeText(context, message, Toast.LENGTH_LONG).show() }
