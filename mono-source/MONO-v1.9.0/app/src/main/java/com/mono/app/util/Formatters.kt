package com.mono.app.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val DATE_TIME_FORMAT = SimpleDateFormat("yyyy/MM/dd - HH:mm", Locale.US)
private val DATE_FORMAT = SimpleDateFormat("yyyy/MM/dd", Locale.US)

fun formatDateTime(millis: Long): String = DATE_TIME_FORMAT.format(Date(millis))

fun formatDate(millis: Long): String = DATE_FORMAT.format(Date(millis))

/** ساخت رشته HH:MM از ساعت و دقیقه */
fun formatHHMM(hour: Int, minute: Int): String =
    String.format(Locale.US, "%02d:%02d", hour, minute)

/** ساعت (۰..۲۳) از رشته HH:MM */
fun parseHour(time: String): Int = time.split(":").getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23) ?: 8

/** دقیقه (۰..۵۹) از رشته HH:MM */
fun parseMinute(time: String): Int = time.split(":").getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59) ?: 0

/** تبدیل ارقام لاتین به فارسی برای نمایش */
fun String.faDigits(): String = buildString {
    for (ch in this@faDigits) {
        append(
            when (ch) {
                '0' -> '۰'; '1' -> '۱'; '2' -> '۲'; '3' -> '۳'; '4' -> '۴'
                '5' -> '۵'; '6' -> '۶'; '7' -> '۷'; '8' -> '۸'; '9' -> '۹'
                else -> ch
            }
        )
    }
}
