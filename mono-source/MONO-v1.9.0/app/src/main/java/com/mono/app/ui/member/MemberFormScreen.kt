package com.mono.app.ui.member

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mono.app.data.model.Member
import com.mono.app.data.model.MemberType
import com.mono.app.ui.components.ColorSwatchRow
import com.mono.app.ui.components.ConfirmDialog
import com.mono.app.ui.components.DangerOutlineButton
import com.mono.app.ui.components.GlassCard
import com.mono.app.ui.components.GlassTextField
import com.mono.app.ui.components.GoldButton
import com.mono.app.ui.components.GradientBackground
import com.mono.app.ui.components.MemberAvatar
import com.mono.app.ui.components.SectionTitle
import com.mono.app.ui.components.SelectablePill
import com.mono.app.ui.theme.GoldAccent
import com.mono.app.ui.theme.TextPrimary
import com.mono.app.ui.theme.TextSecondary

@Composable
fun MemberFormScreen(
    memberId: String?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onRemoved: () -> Unit,
    viewModel: MemberFormViewModel = viewModel(factory = MemberFormViewModel.factory(memberId))
) {
    var showArchiveConfirm by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showClearDetailsConfirm by remember { mutableStateOf(false) }

    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 18.dp)
        ) {
            // ---- نوار بالا ----
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
                    text = if (viewModel.isEdit) "ویرایش عضو" else "عضو جدید",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            // ===== پایه: آواتار و نوع =====
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MemberAvatar(
                        letter = viewModel.name.trim().take(1).ifBlank { "؟" },
                        colorHex = viewModel.colorHex,
                        size = 64.dp,
                        fontSize = 24.sp
                    )
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            text = viewModel.name.trim().ifBlank { "بدون نام" },
                            color = TextPrimary,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "رنگ آواتار را انتخاب کنید",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                ColorSwatchRow(
                    colors = Member.DEFAULT_COLORS,
                    selected = viewModel.colorHex,
                    onSelect = { viewModel.colorHex = it }
                )
            }

            Spacer(Modifier.height(14.dp))

            GlassCard(modifier = Modifier.fillMaxWidth(), fillSoft = true) {
                GlassTextField(
                    value = viewModel.name,
                    onValueChange = {
                        viewModel.name = it
                        if (it.isNotBlank()) viewModel.nameError = false
                    },
                    label = "نام و نام خانوادگی *",
                    placeholder = "مثال: علی رضایی",
                    isError = viewModel.nameError,
                    supportingText = if (viewModel.nameError) "نام الزامی است" else null,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "نوع عضویت",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SelectablePill(
                        text = MemberType.CUSTOMER.fa,
                        selected = viewModel.type == MemberType.CUSTOMER,
                        onClick = { viewModel.type = MemberType.CUSTOMER }
                    )
                    SelectablePill(
                        text = MemberType.STAFF.fa,
                        selected = viewModel.type == MemberType.STAFF,
                        onClick = { viewModel.type = MemberType.STAFF }
                    )
                }
            }

            // ===== مشخصات هویتی =====
            Spacer(Modifier.height(16.dp))
            SectionTitle("مشخصات هویتی")
            Spacer(Modifier.height(10.dp))
            GlassCard(modifier = Modifier.fillMaxWidth(), fillSoft = true) {
                val d = viewModel.details
                fun upd(fn: (MemberDetailsForm) -> Unit) {
                    fn(viewModel.details)
                    viewModel.details = viewModel.details.copy()
                }

                Field("نام پدر", d.fatherName) { upd { f -> f.fatherName = it } }
                Field("کد ملی", d.nationalId) { upd { f -> f.nationalId = it } }
                Field("سریال کد ملی / کد رهگیری", d.nationalIdSerial) { upd { f -> f.nationalIdSerial = it } }
                Field("تاریخ تولد", d.birthDate) { upd { f -> f.birthDate = it } }
                Field("محل تولد", d.birthPlace) { upd { f -> f.birthPlace = it } }
                Field("شماره شناسنامه", d.certNo) { upd { f -> f.certNo = it } }
                Field("سریال شناسنامه", d.certSerial) { upd { f -> f.certSerial = it } }
            }

            // ===== تماس و سیم‌کارت =====
            Spacer(Modifier.height(16.dp))
            SectionTitle("اطلاعات تماس و سیم‌کارت")
            Spacer(Modifier.height(10.dp))
            GlassCard(modifier = Modifier.fillMaxWidth(), fillSoft = true) {
                val d = viewModel.details
                fun upd(fn: (MemberDetailsForm) -> Unit) {
                    fn(viewModel.details)
                    viewModel.details = viewModel.details.copy()
                }
                Field("شماره تماس", d.phone) { upd { f -> f.phone = it } }
                Field("اپراتور", d.operatorName) { upd { f -> f.operatorName = it } }
                Field("پوکه", d.simPuk) { upd { f -> f.simPuk = it } }
            }

            // ===== اطلاعات بانکی و کارت =====
            Spacer(Modifier.height(16.dp))
            SectionTitle("اطلاعات بانکی و کارت")
            Spacer(Modifier.height(10.dp))
            GlassCard(modifier = Modifier.fillMaxWidth(), fillSoft = true) {
                val d = viewModel.details
                fun upd(fn: (MemberDetailsForm) -> Unit) {
                    fn(viewModel.details)
                    viewModel.details = viewModel.details.copy()
                }
                Field("شماره حساب", d.accountNo) { upd { f -> f.accountNo = it } }
                Field("شماره کارت", d.cardNo) { upd { f -> f.cardNo = it } }
                Field("Exp", d.cardExp) { upd { f -> f.cardExp = it } }
                Field("Cvv2", d.cardCvv2) { upd { f -> f.cardCvv2 = it } }
                Field("Pass card", d.cardPassword) { upd { f -> f.cardPassword = it } }
                Field("نام کاربری", d.username) { upd { f -> f.username = it } }
                Field("رمز عبور", d.password) { upd { f -> f.password = it } }
            }

            // ===== وضعیت اعتباری و تسهیلات =====
            Spacer(Modifier.height(16.dp))
            SectionTitle("وضعیت اعتباری و تسهیلات")
            Spacer(Modifier.height(10.dp))
            GlassCard(modifier = Modifier.fillMaxWidth(), fillSoft = true) {
                val d = viewModel.details
                fun upd(fn: (MemberDetailsForm) -> Unit) {
                    fn(viewModel.details)
                    viewModel.details = viewModel.details.copy()
                }
                Field("رتبه اعتباری", d.creditScore) { upd { f -> f.creditScore = it } }
                Field("وضعیت چک", d.checkStatus) { upd { f -> f.checkStatus = it } }
                Field("ظرفیت خالی", d.freeCapacity) { upd { f -> f.freeCapacity = it } }
                Field("وام‌های در جریان", d.activeLoans) { upd { f -> f.activeLoans = it } }
                Field("وام‌های گرفته‌شده", d.takenLoans) { upd { f -> f.takenLoans = it } }
                Field("ضمانت", d.collateral, multiline = true) { upd { f -> f.collateral = it } }
                Field("خطاها", d.errors, multiline = true) { upd { f -> f.errors = it } }
            }

            // ===== یادداشت =====
            Spacer(Modifier.height(16.dp))
            SectionTitle("یادداشت")
            Spacer(Modifier.height(10.dp))
            GlassCard(modifier = Modifier.fillMaxWidth(), fillSoft = true) {
                GlassTextField(
                    value = viewModel.notes,
                    onValueChange = { viewModel.notes = it },
                    label = "یادداشت آزاد",
                    placeholder = "هر توضیحی درباره این فرد…",
                    singleLine = false,
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(20.dp))

            GoldButton(
                text = "ذخیره",
                onClick = { if (viewModel.save()) onSaved() },
                modifier = Modifier.fillMaxWidth()
            )

            if (viewModel.isEdit) {
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DangerOutlineButton(
                        text = "پاک کردن مشخصات فرم",
                        onClick = { showClearDetailsConfirm = true },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DangerOutlineButton(
                        text = "بایگانی",
                        onClick = { showArchiveConfirm = true },
                        modifier = Modifier.weight(1f)
                    )
                    DangerOutlineButton(
                        text = "حذف دائمی عضو",
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(30.dp))
        }
    }

    if (showClearDetailsConfirm) {
        ConfirmDialog(
            title = "پاک کردن مشخصات فرم",
            text = "همه فیلدهای مشخصات (هویتی، تماس، بانکی، اعتباری) این عضو پاک می‌شوند. خود عضو و وظایف باقی می‌مانند.",
            confirmLabel = "پاک کن",
            destructive = true,
            onConfirm = {
                showClearDetailsConfirm = false
                viewModel.clearDetails {}
            },
            onDismiss = { showClearDetailsConfirm = false }
        )
    }

    if (showArchiveConfirm) {
        ConfirmDialog(
            title = "بایگانی عضو",
            text = "این عضو از لیست اصلی پنهان می‌شود و وظایف او حفظ می‌شوند. بعداً از بخش «بایگانی‌شده‌ها» قابل بازگردانی است.",
            confirmLabel = "بایگانی",
            onConfirm = {
                showArchiveConfirm = false
                viewModel.archive(onRemoved)
            },
            onDismiss = { showArchiveConfirm = false }
        )
    }

    if (showDeleteConfirm) {
        ConfirmDialog(
            title = "حذف دائمی عضو",
            text = "تمام اطلاعات، مشخصات فرم و وظایف این عضو برای همیشه حذف می‌شوند. این عمل قابل بازگشت نیست.",
            confirmLabel = "حذف",
            destructive = true,
            onConfirm = {
                showDeleteConfirm = false
                viewModel.delete(onRemoved)
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}

@Composable
private fun Field(
    label: String,
    value: String,
    multiline: Boolean = false,
    onChange: (String) -> Unit
) {
    GlassTextField(
        value = value,
        onValueChange = onChange,
        label = label,
        placeholder = "—",
        singleLine = !multiline,
        minLines = if (multiline) 2 else 1,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    )
}
