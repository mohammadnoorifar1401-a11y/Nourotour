package com.yadyar.app.backup

import android.content.Context
import android.net.Uri
import com.yadyar.app.data.db.AppDatabase
import com.yadyar.app.data.model.Member
import com.yadyar.app.data.model.MemberDetails
import com.yadyar.app.data.model.Task
import com.yadyar.app.reminders.ReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader

@Serializable
data class BackupTask(
    val id: String,
    val memberId: String,
    val title: String,
    val description: String = "",
    val done: Boolean = false,
    val doneAt: Long? = null,
    val reminderEnabled: Boolean = false,
    val scheduleType: String = "NONE",
    val timeHHMM: String = "08:00",
    val weekDays: List<Int> = emptyList(),
    val dayOfMonth: Int = 1,
    val reminderNotes: String = "",
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
data class BackupDetails(
    val id: String,
    val memberId: String,
    val fatherName: String = "",
    val nationalId: String = "",
    val nationalIdSerial: String = "",
    val birthDate: String = "",
    val birthPlace: String = "",
    val certNo: String = "",
    val certSerial: String = "",
    val phone: String = "",
    val operatorName: String = "",
    val simPuk: String = "",
    val accountNo: String = "",
    val cardNo: String = "",
    val cardExp: String = "",
    val cardCvv2: String = "",
    val cardPassword: String = "",
    val username: String = "",
    val password: String = "",
    val creditScore: String = "",
    val checkStatus: String = "",
    val errors: String = "",
    val freeCapacity: String = "",
    val activeLoans: String = "",
    val takenLoans: String = "",
    val collateral: String = "",
    val updatedAt: Long
)

@Serializable
data class BackupMember(
    val id: String,
    val type: String,
    val name: String,
    val avatarLetter: String = "",
    val avatarColor: String = "",
    val avatarImageBase64: String? = null,
    val notes: String = "",
    val archived: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
data class BackupFile(
    val format: String = "yadyar-backup",
    val version: Int = 1,
    val exportedAt: Long,
    val members: List<BackupMember>,
    val details: List<BackupDetails>,
    val tasks: List<BackupTask>
)

/**
 * Export/Import بکاپ JSON کامل (اعضا + مشخصات + وظایف).
 * کاملاً محلی — از طریق Storage Access Framework فایل ذخیره/خوانده می‌شود.
 */
object BackupManager {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    suspend fun exportToJson(context: Context): String = withContext(Dispatchers.IO) {
        val db = AppDatabase.get(context)
        val backup = BackupFile(
            exportedAt = System.currentTimeMillis(),
            members = db.memberDao().getAll().map { it.toBackup() },
            details = db.memberDetailsDao().getAll().map { it.toBackup() },
            tasks = db.taskDao().getAll().map { it.toBackup() }
        )
        json.encodeToString(backup)
    }

    suspend fun writeToFile(context: Context, uri: Uri, content: String) =
        withContext(Dispatchers.IO) {
            context.contentResolver.openOutputStream(uri, "wt")?.use { out ->
                out.write(content.toByteArray(Charsets.UTF_8))
                out.flush()
            } ?: throw IllegalStateException("دسترسی به فایل ممکن نشد")
        }

    suspend fun readFromFile(context: Context, uri: Uri): String =
        withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(uri)?.use { input ->
                input.bufferedReader(Charsets.UTF_8).use(BufferedReader::readText)
            } ?: throw IllegalStateException("خواندن فایل ممکن نشد")
        }

    /**
     * بازیابی بکاپ — جایگزینی کامل داده‌های فعلی.
     * بعد از بازیابی، یادآورها دوباره زمان‌بندی می‌شوند.
     */
    suspend fun restoreFromJson(context: Context, content: String) = withContext(Dispatchers.IO) {
        val backup = json.decodeFromString<BackupFile>(content)
        val db = AppDatabase.get(context)

        db.taskDao().deleteAll()
        db.memberDetailsDao().deleteAll()
        db.memberDao().deleteAll()

        backup.members.forEach { db.memberDao().upsert(it.toEntity()) }
        backup.details.forEach { db.memberDetailsDao().upsert(it.toEntity()) }
        backup.tasks.forEach { db.taskDao().upsert(it.toEntity()) }

        // زمان‌بندی مجدد یادآورها
        val tasks = db.taskDao().getAllWithReminders()
        ReminderScheduler.rescheduleAll(context, tasks)
        backup
    }

    // ---- Mapping ----

    private fun Member.toBackup() = BackupMember(
        id = id, type = type, name = name,
        avatarLetter = avatarLetter, avatarColor = avatarColor,
        avatarImageBase64 = avatarImageBase64, notes = notes,
        archived = archived, createdAt = createdAt, updatedAt = updatedAt
    )

    private fun BackupMember.toEntity() = Member(
        id = id, type = type, name = name,
        avatarLetter = avatarLetter, avatarColor = avatarColor,
        avatarImageBase64 = avatarImageBase64, notes = notes,
        archived = archived, createdAt = createdAt, updatedAt = updatedAt
    )

    private fun MemberDetails.toBackup() = BackupDetails(
        id = id, memberId = memberId,
        fatherName = fatherName, nationalId = nationalId, nationalIdSerial = nationalIdSerial,
        birthDate = birthDate, birthPlace = birthPlace, certNo = certNo, certSerial = certSerial,
        phone = phone, operatorName = operatorName, simPuk = simPuk,
        accountNo = accountNo, cardNo = cardNo, cardExp = cardExp, cardCvv2 = cardCvv2,
        cardPassword = cardPassword, username = username, password = password,
        creditScore = creditScore, checkStatus = checkStatus, errors = errors,
        freeCapacity = freeCapacity, activeLoans = activeLoans, takenLoans = takenLoans,
        collateral = collateral, updatedAt = updatedAt
    )

    private fun BackupDetails.toEntity() = MemberDetails(
        id = id, memberId = memberId,
        fatherName = fatherName, nationalId = nationalId, nationalIdSerial = nationalIdSerial,
        birthDate = birthDate, birthPlace = birthPlace, certNo = certNo, certSerial = certSerial,
        phone = phone, operatorName = operatorName, simPuk = simPuk,
        accountNo = accountNo, cardNo = cardNo, cardExp = cardExp, cardCvv2 = cardCvv2,
        cardPassword = cardPassword, username = username, password = password,
        creditScore = creditScore, checkStatus = checkStatus, errors = errors,
        freeCapacity = freeCapacity, activeLoans = activeLoans, takenLoans = takenLoans,
        collateral = collateral, updatedAt = updatedAt
    )

    private fun Task.toBackup() = BackupTask(
        id = id, memberId = memberId, title = title, description = description,
        done = done, doneAt = doneAt, reminderEnabled = reminderEnabled,
        scheduleType = scheduleType, timeHHMM = timeHHMM, weekDays = weekDays,
        dayOfMonth = dayOfMonth, reminderNotes = reminderNotes,
        createdAt = createdAt, updatedAt = updatedAt
    )

    private fun BackupTask.toEntity() = Task(
        id = id, memberId = memberId, title = title, description = description,
        done = done, doneAt = doneAt, reminderEnabled = reminderEnabled,
        scheduleType = scheduleType, timeHHMM = timeHHMM, weekDays = weekDays,
        dayOfMonth = dayOfMonth, reminderNotes = reminderNotes,
        createdAt = createdAt, updatedAt = updatedAt
    )
}
