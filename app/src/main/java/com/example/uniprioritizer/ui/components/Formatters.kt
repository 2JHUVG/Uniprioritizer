package com.example.uniprioritizer.ui.components

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val SpanishLocale: Locale = Locale.forLanguageTag("es-GT")
private val ShortDateTimeFormatter = DateTimeFormatter.ofPattern("EEE d MMM, HH:mm", SpanishLocale)
private val LongDateFormatter = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", SpanishLocale)

fun LocalDateTime.toShortLabel(): String = format(ShortDateTimeFormatter)

fun LocalDate.toLongLabel(): String = format(LongDateFormatter).replaceFirstChar { it.uppercase() }

fun LocalDate.dayInitial(): String =
    dayOfWeek.getDisplayName(TextStyle.NARROW, SpanishLocale).uppercase()

fun Double.toHoursLabel(): String {
    val totalMinutes = (this * 60).toInt()
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours == 0 -> "$minutes min"
        minutes == 0 -> "$hours h"
        else -> "$hours h $minutes min"
    }
}
