package com.yadyar.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

enum class ScheduleType { NONE, DAILY, WEEKLY, MONTHLY }

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = Member::class,
            parentColumns = ["id"],
            childColumns = ["memberId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("memberId")]
)
data class Task(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val memberId: String,
    val title: String,
    val description: String = "",
    val done: Boolean = false,
    val doneAt: Long? = null,

    // ---- تنظیمات یادآور (اسکیما از مرحله ۱ ثابت است) ----
    val reminderEnabled: Boolean = false,
    val scheduleType: String = ScheduleType.NONE.name,
    val timeHHMM: String = "08:00",
    val weekDays: List<Int> = emptyList(),
    val dayOfMonth: Int = 1,
    val reminderNotes: String = "",

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val scheduleTypeEnum: ScheduleType
        get() = try {
            ScheduleType.valueOf(scheduleType)
        } catch (e: Exception) {
            ScheduleType.NONE
        }

    companion object {
        /**
         * قرارداد ثابت هفته (۰ تا ۶) — شنبه‌محور:
         * 0=شنبه، 1=یکشنبه، 2=دوشنبه، 3=سه‌شنبه، 4=چهارشنبه، 5=پنجشنبه، 6=جمعه
         */
        const val DAY_SAT = 0
        const val DAY_SUN = 1
        const val DAY_MON = 2
        const val DAY_TUE = 3
        const val DAY_WED = 4
        const val DAY_THU = 5
        const val DAY_FRI = 6

        val WEEK_DAY_FA = listOf("شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه")
    }
}
