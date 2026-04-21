package com.yortch.confirmationsaints.util

import com.yortch.confirmationsaints.localization.AppLanguage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/** Port of iOS `Saint.formattedFeastDay` + `SaintDateFormatter`. */
object DateFormatting {

    /** Converts "MM-DD" → "October 1" / "1 de octubre". Returns input on parse failure. */
    fun formatFeastDay(feastDay: String, language: AppLanguage): String {
        val parts = feastDay.split("-")
        if (parts.size != 2) return feastDay
        val month = parts[0].toIntOrNull() ?: return feastDay
        val day = parts[1].toIntOrNull() ?: return feastDay
        val locale = language.locale
        val calendar = Calendar.getInstance(locale).apply {
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
        }
        val pattern = if (language == AppLanguage.ES) "d 'de' MMMM" else "MMMM d"
        return SimpleDateFormat(pattern, locale).format(calendar.time)
    }

    /** Formats an ISO date ("1873-01-02") as a readable year / month-year string. */
    fun formatIsoDate(iso: String, language: AppLanguage): String {
        // Accept YYYY, YYYY-MM, YYYY-MM-DD.
        val parts = iso.split("-")
        val year = parts.getOrNull(0)?.toIntOrNull() ?: return iso
        val month = parts.getOrNull(1)?.toIntOrNull()
        val day = parts.getOrNull(2)?.toIntOrNull()
        val locale = language.locale
        val cal = Calendar.getInstance(locale).apply {
            set(Calendar.YEAR, year)
            if (month != null) set(Calendar.MONTH, month - 1)
            if (day != null) set(Calendar.DAY_OF_MONTH, day)
        }
        val pattern = when {
            day != null && month != null ->
                if (language == AppLanguage.ES) "d 'de' MMMM, yyyy" else "MMMM d, yyyy"
            month != null ->
                if (language == AppLanguage.ES) "MMMM yyyy" else "MMMM yyyy"
            else -> "yyyy"
        }
        return SimpleDateFormat(pattern, locale).format(cal.time)
    }

    private val AppLanguage.locale: Locale
        get() = if (this == AppLanguage.ES) Locale("es") else Locale.ENGLISH
}
