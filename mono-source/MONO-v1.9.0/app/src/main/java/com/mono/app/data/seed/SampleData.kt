package com.mono.app.data.seed

import com.mono.app.data.model.Member
import com.mono.app.data.model.MemberDetails
import com.mono.app.data.model.MemberType
import com.mono.app.data.model.ScheduleType
import com.mono.app.data.model.Task
import com.mono.app.data.repository.MemberDetailsRepository
import com.mono.app.data.repository.MemberRepository
import com.mono.app.data.repository.TaskRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * دیتای نمونه برای شروع و تست سریع.
 * دو یادآور نمونه، ۲ و ۴ دقیقه پس از «اولین اجرا» تنظیم می‌شوند تا موتور یادآوری
 * بدون صبر کردن قابل تست باشد.
 */
object SampleData {

    private val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    suspend fun seedIfEmpty(
        memberRepository: MemberRepository,
        taskRepository: TaskRepository,
        detailsRepository: MemberDetailsRepository? = null
    ) {
        if (memberRepository.count() > 0) return

        val now = LocalDateTime.now()
        fun at(minutes: Long): String = now.plusMinutes(minutes).format(TIME_FORMAT)

        val ali = Member(
            type = MemberType.CUSTOMER.key,
            name = "علی رضایی",
            avatarLetter = "ع",
            avatarColor = "#5B8DEF",
            notes = "مشتری نمونه — برای تست اولیه"
        )
        val sara = Member(
            type = MemberType.STAFF.key,
            name = "سارا احمدی",
            avatarLetter = "س",
            avatarColor = "#F2994A",
            notes = "پرسنل نمونه — برای تست اولیه"
        )
        val maryam = Member(
            type = MemberType.CUSTOMER.key,
            name = "مریم محمدی",
            avatarLetter = "م",
            avatarColor = "#9B51E0",
            notes = "مشتری نمونه"
        )

        memberRepository.upsert(ali)
        memberRepository.upsert(sara)
        memberRepository.upsert(maryam)

        // مشخصات نمونه (فرم) برای علی — بقیه خالی می‌مانند تا تست افزودن راحت باشد
        detailsRepository?.upsert(
            MemberDetails(
                memberId = ali.id,
                fatherName = "حسین",
                nationalId = "0012345678",
                nationalIdSerial = "1234/567",
                birthDate = "1370/05/12",
                birthPlace = "تهران",
                certNo = "45879632",
                certSerial = "الف/۷۸/۱۲۳۴",
                phone = "09121234567",
                operatorName = "ایرانسل",
                simPuk = "9876543210",
                accountNo = "123456789012",
                cardNo = "6037-99**-****-1234",
                creditScore = "A+",
                checkStatus = "بدون چک برگشتی",
                freeCapacity = "500,000,000 ریال",
                activeLoans = "وام خودرو — قسط ۵ از ۲۴",
                takenLoans = "وام شخصی ۱۴۰۲",
                collateral = "سفته + چک ضمانت",
                errors = "—"
            )
        )

        taskRepository.upsert(
            Task(
                memberId = ali.id,
                title = "پیگیری سفارش",
                description = "یادآور روزانه — زمان تست: ۲ دقیقه پس از اولین اجرا",
                reminderEnabled = true,
                scheduleType = ScheduleType.DAILY.name,
                timeHHMM = at(2)
            )
        )
        taskRepository.upsert(
            Task(
                memberId = ali.id,
                title = "تماس هفتگی",
                description = "شنبه و سه‌شنبه — فعلاً یادآور خاموش",
                reminderEnabled = false,
                scheduleType = ScheduleType.WEEKLY.name,
                timeHHMM = "10:00",
                weekDays = listOf(Task.DAY_SAT, Task.DAY_TUE)
            )
        )
        taskRepository.upsert(
            Task(
                memberId = sara.id,
                title = "ارسال گزارش ماهانه",
                description = "یادآور ماهانه — زمان تست: ۴ دقیقه پس از اولین اجرا",
                reminderEnabled = true,
                scheduleType = ScheduleType.MONTHLY.name,
                timeHHMM = at(4),
                dayOfMonth = now.dayOfMonth.coerceIn(1, 31)
            )
        )
        taskRepository.upsert(
            Task(
                memberId = sara.id,
                title = "بررسی موجودی انبار",
                description = "بدون یادآور",
                reminderEnabled = false,
                scheduleType = ScheduleType.NONE.name
            )
        )
        taskRepository.upsert(
            Task(
                memberId = maryam.id,
                title = "پیگیری فاکتور",
                description = "",
                reminderEnabled = false,
                scheduleType = ScheduleType.NONE.name
            )
        )
    }
}
