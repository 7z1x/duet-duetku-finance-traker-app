package com.duetduetku.app.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtil {
    fun formatTransactionDate(timestamp: Long): String {
        val calendar = java.util.Calendar.getInstance()
        val now = calendar.timeInMillis
        val daysDiff = (now - timestamp) / (1000 * 60 * 60 * 24)

        return when {
            android.text.format.DateUtils.isToday(timestamp) -> "Today"
            android.text.format.DateUtils.isToday(timestamp + 86400000) -> "Yesterday" // Check if it was yesterday
            else -> {
                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                sdf.format(Date(timestamp))
            }
        }
    }


    fun stripTime(date: Date): Date {
        val calendar = java.util.Calendar.getInstance()
        calendar.time = date
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.time
    }

    fun endOfDay(date: Date): Date {
        val calendar = java.util.Calendar.getInstance()
        calendar.time = date
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
        calendar.set(java.util.Calendar.MINUTE, 59)
        calendar.set(java.util.Calendar.SECOND, 59)
        calendar.set(java.util.Calendar.MILLISECOND, 999)
        return calendar.time
    }

    fun parseDateString(dateString: String): Long? {
        val formats = listOf(
            "dd/MM/yyyy", "dd-MM-yyyy", "dd.MM.yyyy",
            "yyyy-MM-dd", "yyyy/MM/dd", "dd MMM yyyy"
        )
        
        for (format in formats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.getDefault())
                val date = sdf.parse(dateString)
                if (date != null) return date.time
            } catch (e: Exception) {
                // Continue to next format
            }
        }
        return null
    }
}
