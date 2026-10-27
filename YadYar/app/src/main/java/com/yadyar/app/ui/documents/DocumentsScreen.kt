package com.yadyar.app.ui.documents

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yadyar.app.ui.components.*
import com.yadyar.app.ui.theme.*

@Composable
fun DocumentsScreen() {
    var orgName by remember { mutableStateOf("لاوان صنعت نوین شمس") }
    var employeeName by remember { mutableStateOf("کاربر نمونه") }
    var title by remember { mutableStateOf("کارشناس عملیات") }
    var startDate by remember { mutableStateOf("۱۴۰۲/۰۱/۱۵") }
    var salary by remember { mutableStateOf("۸۵٬۰۰۰٬۰۰۰ ریال") }
    var showPreview by remember { mutableStateOf(false) }
    var docType by remember { mutableStateOf("employment") } // employment | payslip
    var maskSensitive by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding().verticalScroll(rememberScrollState())
    ) {
        Column(modifier = Modifier.padding(horizontal = MonoDimens.ContentPadding)) {
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text("اسناد", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
                    Text("گواهی اشتغال و فیش حقوقی — فقط پیش‌نویس قابل ویرایش", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                OfflineBadge()
            }
            Spacer(Modifier.height(12.dp))
            PrivacyBanner()
            Spacer(Modifier.height(16.dp))

            // Doc type selector
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                FilterChip(
                    selected = docType == "employment",
                    onClick = { docType = "employment" },
                    label = { Text("گواهی اشتغال") },
                    leadingIcon = { if (docType == "employment") Icon(Icons.Rounded.Check, null, Modifier.size(14.dp)) }
                )
                FilterChip(
                    selected = docType == "payslip",
                    onClick = { docType = "payslip" },
                    label = { Text("فیش حقوقی") },
                    leadingIcon = { if (docType == "payslip") Icon(Icons.Rounded.Check, null, Modifier.size(14.dp)) }
                )
                Spacer(Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("ماسک اطلاعات", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(4.dp))
                    Switch(checked = maskSensitive, onCheckedChange = { maskSensitive = it })
                }
            }

            Spacer(Modifier.height(16.dp))
            MonoSectionHeader(title = if (docType == "employment") "فرم گواهی اشتغال" else "فرم فیش حقوقی")
            Spacer(Modifier.height(10.dp))

            // Editable form
            MonoCard(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = orgName,
                    onValueChange = { orgName = it },
                    label = { Text("نام سازمان") },
                    supportingText = { Text("قابل ویرایش — پیش‌فرض «لاوان صنعت نوین شمس»") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(value = employeeName, onValueChange = { employeeName = it }, label = { Text("نام کارمند") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("سمت") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(value = startDate, onValueChange = { startDate = it }, label = { Text("تاریخ شروع") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(
                        value = if (maskSensitive) "•••• ••••" else salary,
                        onValueChange = { if (!maskSensitive) salary = it },
                        label = { Text("مبلغ / حقوق ${if (maskSensitive) "(ماسک‌شده)" else ""}") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        readOnly = maskSensitive
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Info, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(6.dp))
                    Text("خروجی‌ها فقط پیش‌نویس هستند و محاسبه حقوقی/قانونی انجام نمی‌دهند.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                MonoPrimaryButton(text = "پیش‌نمایش", onClick = { showPreview = true }, icon = Icons.Outlined.Visibility, modifier = Modifier.weight(1f))
                MonoSecondaryButton(text = "خروجی PDF", onClick = { showPreview = true }, icon = Icons.Outlined.PictureAsPdf, modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                MonoSecondaryButton(text = "خروجی JPG", onClick = { showPreview = true }, icon = Icons.Outlined.Image, modifier = Modifier.weight(1f))
                MonoSecondaryButton(text = "خروجی DOCX", onClick = { showPreview = true }, icon = Icons.Outlined.Description, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Text("هر خروجی شامل واترمارک «نمونه / نامعتبر / غیرقابل ارائه» است؛ مهر و امضا خالی می‌ماند.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())

            if (showPreview) {
                Spacer(Modifier.height(16.dp))
                MonoSectionHeader(title = "پیش‌نمایش سند")
                Spacer(Modifier.height(10.dp))
                DocumentPreviewCard(
                    orgName = orgName,
                    employeeName = employeeName,
                    title = title,
                    startDate = startDate,
                    salary = if (maskSensitive) "•••• ••••" else salary,
                    docType = docType,
                    onClose = { showPreview = false }
                )
            }

            Spacer(Modifier.height(12.dp))
            MonoCard(modifier = Modifier.fillMaxWidth(), background = MonoWarningContainer) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Outlined.WarningAmber, null, tint = MonoWarning, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("توجه", fontWeight = FontWeight.Bold, color = MonoWarning, style = MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.height(4.dp))
                        Text("این سند نمونه و غیرقابل ارائه است؛ برای صدور رسمی با واحد منابع انسانی هماهنگ کنید. اطلاعات حساس به‌صورت پیش‌فرض ماسک شده و محل مهر/امضا خالی است.", style = MaterialTheme.typography.bodySmall, color = MonoWarning.copy(alpha = 0.9f))
                    }
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun DocumentPreviewCard(
    orgName: String,
    employeeName: String,
    title: String,
    startDate: String,
    salary: String,
    docType: String,
    onClose: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White).border(1.dp, MonoLineLight, RoundedCornerShape(16.dp))) {
        // Watermark layer
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    MonoBrandMark(size = 32)
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text(orgName, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium, color = MonoDeepTeal)
                        Text(if (docType == "employment") "گواهی اشتغال — پیش‌نویس" else "فیش حقوقی — پیش‌نویس", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) { Icon(Icons.Rounded.Close, "بستن", Modifier.size(18.dp)) }
                }
                Spacer(Modifier.height(14.dp))
                MonoDivider()
                Spacer(Modifier.height(16.dp))
                Text(
                    if (docType == "employment") "به استحضار می‌رساند آقای/خانم $employeeName با سمت $title از تاریخ $startDate در این مجموعه مشغول به کار می‌باشد. این گواهی بنا به درخواست نامبرده صادر شده و فاقد اعتبار حقوقی برای ارائه به مراجع رسمی است."
                    else "فیش حقوقی مربوط به $employeeName — سمت: $title — دوره: $startDate — مبلغ ناخالص: $salary — این فیش صرفاً نمونه نمایشی است و فاقد اعتبار مالی/قانونی می‌باشد.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF132223),
                    textAlign = TextAlign.Justify
                )
                Spacer(Modifier.height(18.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("محل مهر سازمان", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        Box(modifier = Modifier.size(86.dp).clip(RoundedCornerShape(10.dp)).border(1.dp, MonoLineLight.copy(alpha = 0.6f), RoundedCornerShape(10.dp)).background(Color(0xFFF7F8F6)), contentAlignment = Alignment.Center) {
                            Text("خالی", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("امضا و تاریخ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        Box(modifier = Modifier.width(120.dp).height(46.dp).clip(RoundedCornerShape(10.dp)).border(1.dp, MonoLineLight.copy(alpha = 0.6f), RoundedCornerShape(10.dp)).background(Color(0xFFF7F8F6)), contentAlignment = Alignment.Center) {
                            Text("خالی", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), style = MaterialTheme.typography.labelSmall)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text("۱۴۰۴/۰۸/۰۵", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Surface(shape = RoundedCornerShape(10.dp), color = MonoErrorContainer, modifier = Modifier.fillMaxWidth()) {
                    Text("نمونه / نامعتبر / غیرقابل ارائه — فقط برای پیش‌نمایش", color = MonoError, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp))
                }
            }
            // Diagonal watermark overlay
            Box(modifier = Modifier.fillMaxWidth().height(260.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = "نمونه  •  نامعتبر",
                    color = MonoError.copy(alpha = 0.08f),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 34.sp,
                    modifier = Modifier.rotate(-18f)
                )
            }
        }
    }
}


