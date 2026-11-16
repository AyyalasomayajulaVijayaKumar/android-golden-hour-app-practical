package com.example.practical.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateTimeUtils {

    fun parseUtcIsoString(utcString: String?): Date? {
        if (utcString.isNullOrBlank()) return null
        val patterns = arrayOf(
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            "yyyy-MM-dd'T'HH:mm:ssZ",
            "yyyy-MM-dd'T'HH:mm:ss"
        )
        for (pattern in patterns) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.US)
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                val parsed = sdf.parse(utcString)
                if (parsed != null) return parsed
            } catch (_: Exception) {
            }
        }
        return null
    }

    fun formatToLocalTimeString(date: Date?): String {
        if (date == null) return "N/A"
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(date)
    }

    fun formatToLocalDateString(date: Date?): String {
        if (date == null) return "N/A"
        val sdf = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(date)
    }

    fun formatDayLength(dayLength: String?): String {
        if (dayLength.isNullOrBlank()) return "N/A"
        return try {
            val seconds = dayLength.toLongOrNull()
            if (seconds != null) {
                val hours = seconds / 3600
                val minutes = (seconds % 3600) / 60
                val secs = seconds % 60
                if (hours > 0) "${hours}h ${minutes}m ${secs}s" else "${minutes}m ${secs}s"
            } else {
                val parts = dayLength.split(":")
                if (parts.size >= 2) {
                    val h = parts[0].toIntOrNull() ?: 0
                    val m = parts[1].toIntOrNull() ?: 0
                    "${h}h ${m}m"
                } else {
                    dayLength
                }
            }
        } catch (e: Exception) {
            dayLength
        }
    }

    fun getMorningGoldenHourRange(sunriseDate: Date?): Pair<Date, Date>? {
        if (sunriseDate == null) return null
        val startCal = Calendar.getInstance().apply { time = sunriseDate }
        val endCal = Calendar.getInstance().apply {
            time = sunriseDate
            add(Calendar.HOUR_OF_DAY, 1)
        }
        return Pair(startCal.time, endCal.time)
    }

    fun getEveningGoldenHourRange(sunsetDate: Date?): Pair<Date, Date>? {
        if (sunsetDate == null) return null
        val startCal = Calendar.getInstance().apply {
            time = sunsetDate
            add(Calendar.HOUR_OF_DAY, -1)
        }
        val endCal = Calendar.getInstance().apply { time = sunsetDate }
        return Pair(startCal.time, endCal.time)
    }

    fun calculateCountdown(nowMs: Long, targetMs: Long): String {
        var diffMs = targetMs - nowMs
        if (diffMs <= 0) return "00h 00m 00s"
        val seconds = (diffMs / 1000) % 60
        val minutes = (diffMs / (1000 * 60)) % 60
        val hours = (diffMs / (1000 * 60 * 60)) % 24
        val days = diffMs / (1000 * 60 * 60 * 24)

        return if (days > 0) {
            String.format(Locale.getDefault(), "%dd %02dh %02dm %02ds", days, hours, minutes, seconds)
        } else {
            String.format(Locale.getDefault(), "%02dh %02dm %02ds", hours, minutes, seconds)
        }
    }

    fun getFormattedApiDateStr(timeInMillis: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date(timeInMillis))
    }

    fun getTodayApiDateStr(): String {
        return getFormattedApiDateStr(System.currentTimeMillis())
    }
}
