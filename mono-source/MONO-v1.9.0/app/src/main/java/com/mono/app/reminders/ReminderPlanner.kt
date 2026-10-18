package com.mono.app.reminders

import com.mono.app.data.model.ScheduleType
import com.mono.app.data.model.Task
import com.mono.app.util.parseHour
import com.mono.app.util.parseMinute
import java.time.DayOfWeek
import java.time.YearMonth
import java.time.ZonedDateTime

/**
 * محاسبه زمان دقیق نوبت بعدی یادآور.
 *
 * قرارداد weekDays شماره‌گذاری شده‌ی شنبه‌محور:
 * 0=شنبه … 6=جمعه  →  تبدیل به DayOfWeek جاوا (شنبه=SATURDAY, یکشنبه=SUNDAY, … جمعه=FRIDAY)
 *
 * قرارداد ماهانه: اگر dayOfMonth در ماه جاری وجود نداشت → آخر همان ماه.
 */
object ReminderPlanner {

    /** تبدیل شماره روز هفته (۰..۶ شنبه‌محور) به DayOfWeek جاوا */
    fun toDayOfWeek(persianDay: Int): DayOfWeek = when (persianDay.coerceIn(0, 6)) {
        0 -> DayOfWeek.SATURDAY
        1 -> DayOfWeek.SUNDAY
        2 -> DayOfWeek.MONDAY
        3 -> DayOfWeek.TUESDAY
        4 -> DayOfWeek.WEDNESDAY
        5 -> DayOfWeek.THURSDAY
        else -> DayOfWeek.FRIDAY
    }

    /**
     * نوبت بعدی اجرا را برمی‌گرداند یا اگر زمان‌بندی معنی نداشته باشد null.
     */
    fun nextTrigger(task: Task, from: ZonedDateTime = ZonedDateTime.now()): ZonedDateTime? {
        if (!task.reminderEnabled) return null
        val hour = parseHour(task.timeHHMM)
        val minute = parseMinute(task.timeHHMM)

        return when (task.scheduleTypeEnum) {
            ScheduleType.NONE -> null

            ScheduleType.DAILY -> {
                val todayAt = from.toLocalDate().atTime(hour, minute).atZone(from.zone)
                if (todayAt.isAfter(from)) todayAt else todayAt.plusDays(1)
            }

            ScheduleType.WEEKLY -> {
                if (task.weekDays.isEmpty()) return null
                val wanted: Set<DayOfWeek> = task.weekDays.map { toDayOfWeek(it) }.toSet()
                // از امروز تا ۷ روز جلوتر را بررسی می‌کنیم
                (0L..7L).map { offset ->
                    val day = from.toLocalDate().plusDays(offset)
                    day.atTime(hour, minute).atZone(from.zone)
                }.firstOrNull { cand ->
                    cand.isAfter(from) && cand.dayOfWeek in wanted
                }
            }

            ScheduleType.MONTHLY -> {
                val targetDay = task.dayOfMonth.coerceIn(1, 31)
                // ابتدا ماه جاری، سپس ماه بعد
                val ym = YearMonth.from(from.toLocalDate())
                val candidates = listOf(ym, ym.plusMonths(1)).map { month ->
                    val day = minOf(targetDay, month.lengthOfMonth())
                    month.atDay(day).atTime(hour, minute).atZone(from.zone)
                }
                candidates.firstOrNull { it.isAfter(from) }
            }
        }
    }
}
