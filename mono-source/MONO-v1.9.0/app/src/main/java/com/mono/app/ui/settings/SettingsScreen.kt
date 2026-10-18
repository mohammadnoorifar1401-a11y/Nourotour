package com.mono.app.ui.settings

import android.Manifest
import android.app.AlarmManager
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mono.app.backup.BackupManager
import com.mono.app.reminders.ReminderScheduler
import com.mono.app.ui.components.ConfirmDialog
import com.mono.app.ui.components.GlassCard
import com.mono.app.ui.components.GradientBackground
import com.mono.app.ui.components.SectionTitle
import com.mono.app.ui.theme.DangerRed
import com.mono.app.ui.theme.GoldAccent
import com.mono.app.ui.theme.SuccessGreen
import com.mono.app.ui.theme.TextPrimary
import com.mono.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showRestoreConfirm by remember { mutableStateOf(false) }
    var pendingRestoreJson by remember { mutableStateOf<String?>(null) }

    // ---- مجوز نوتیفیکیشن ----
    var notifGranted by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> notifGranted = granted }

    // ---- خروجی بکاپ ----
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            try {
                val json = BackupManager.exportToJson(context)
                BackupManager.writeToFile(context, uri, json)
                toast(context, "بکاپ با موفقیت ذخیره شد")
            } catch (e: Exception) {
                toast(context, "خطا در ذخیره بکاپ: ${e.message}")
            }
        }
    }

    // ---- ورودی بکاپ ----
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            try {
                val text = BackupManager.readFromFile(context, uri)
                pendingRestoreJson = text
                showRestoreConfirm = true
            } catch (e: Exception) {
                toast(context, "خطا در خواندن فایل: ${e.message}")
            }
        }
    }

    val exactOk = ReminderScheduler.canScheduleExact(context)

    GradientBackground {
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
                    text = "تنظیمات",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            // ===== بکاپ =====
            SectionTitle("بکاپ و بازیابی")
            Spacer(Modifier.height(10.dp))
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.Backup,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "خروجی بکاپ (JSON)",
                            color = TextPrimary,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "همه اعضا، مشخصات و وظایف در یک فایل ذخیره می‌شود",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GlassAction(
                        text = "ذخیره خروجی",
                        icon = Icons.Rounded.FileUpload,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val stamp = SimpleDateFormat("yyyyMMdd-HHmm", Locale.US).format(Date())
                            exportLauncher.launch("yadyar-backup-$stamp.json")
                        }
                    )
                    GlassAction(
                        text = "بازیابی از فایل",
                        icon = Icons.Rounded.FileDownload,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            importLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                        }
                    )
                }
            }

            // ===== مجوزها =====
            Spacer(Modifier.height(16.dp))
            SectionTitle("مجوزهای یادآوری")
            Spacer(Modifier.height(10.dp))
            GlassCard(modifier = Modifier.fillMaxWidth(), fillSoft = true) {
                PermissionRow(
                    title = "اعلان‌ها (نوتیفیکیشن)",
                    subtitle = "برای نمایش یادآوری‌ها لازم است",
                    ok = notifGranted,
                    actionLabel = "درخواست اجازه",
                    onAction = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                )
                Spacer(Modifier.height(8.dp))
                PermissionRow(
                    title = "یادآوری دقیق (Exact Alarm)",
                    subtitle = if (exactOk) {
                        "فعال — یادآورها سرِ ساعت دقیق اجرا می‌شوند"
                    } else {
                        "غیرفعال — برای دقت بالا در تنظیمات اندروید فعالش کنید"
                    },
                    ok = exactOk,
                    actionLabel = "باز کردن تنظیمات",
                    onAction = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            runCatching { context.startActivity(intent) }
                        }
                    }
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "اگر مجوزها داده نشوند، فقط نوتیف نمایش داده نمی‌شود؛ " +
                        "ثبت و ویرایش اعضا و وظایف بدون مشکل کار می‌کند.",
                    color = TextSecondary.copy(alpha = 0.75f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // ===== درباره =====
            Spacer(Modifier.height(16.dp))
            SectionTitle("درباره برنامه")
            Spacer(Modifier.height(10.dp))
            GlassCard(modifier = Modifier.fillMaxWidth(), fillSoft = true) {
                AboutRow("نام", "یادیار")
                AboutRow("نسخه", "1.0.0")
                AboutRow("ذخیره‌سازی", "کاملاً محلی (SQLite) — بدون اینترنت")
            }

            Spacer(Modifier.height(30.dp))
        }
    }

    if (showRestoreConfirm && pendingRestoreJson != null) {
        ConfirmDialog(
            title = "بازیابی بکاپ",
            text = "تمام داده‌های فعلی جایگزین محتوای فایل بکاپ می‌شوند. این عمل قابل بازگشت نیست. ادامه می‌دهید؟",
            confirmLabel = "بازیابی",
            destructive = true,
            onConfirm = {
                showRestoreConfirm = false
                scope.launch {
                    try {
                        val backup = BackupManager.restoreFromJson(context, pendingRestoreJson!!)
                        toast(
                            context,
                            "بازیابی انجام شد: ${backup.members.size} عضو، ${backup.tasks.size} وظیفه"
                        )
                    } catch (e: Exception) {
                        toast(context, "خطا در بازیابی: ${e.message}")
                    }
                    pendingRestoreJson = null
                }
            },
            onDismiss = {
                showRestoreConfirm = false
                pendingRestoreJson = null
            }
        )
    }
}

@Composable
private fun GlassAction(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    androidx.compose.material3.TextButton(
        onClick = onClick,
        modifier = modifier
            .height(52.dp)
            .background(
                Color.White.copy(alpha = 0.12f),
                RoundedCornerShape(14.dp)
            )
    ) {
        Icon(icon, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, color = TextPrimary, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PermissionRow(
    title: String,
    subtitle: String,
    ok: Boolean,
    actionLabel: String,
    onAction: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            if (ok) Icons.Rounded.CheckCircle else Icons.Rounded.ErrorOutline,
            contentDescription = null,
            tint = if (ok) SuccessGreen else DangerRed,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                title,
                color = TextPrimary,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                subtitle,
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        if (!ok) {
            androidx.compose.material3.TextButton(onClick = onAction) {
                Text(actionLabel, color = GoldAccent, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun AboutRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            label,
            color = TextSecondary,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(100.dp)
        )
        Text(
            value,
            color = TextPrimary,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
    }
}

private fun toast(context: Context, message: String) {
    android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
}
