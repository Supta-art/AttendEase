package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class MonthItem(
    val yearMonthKey: String, // "2026-08"
    val displayName: String,  // "August 2026"
    val year: Int,
    val month: Int,
    val totalDaysInMonth: Int,
    val estimatedWorkingDays: Int
)

object DateUtils {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    fun getTodayDateString(): String {
        return dateFormat.format(Date())
    }

    fun getFormattedTime(timeMillis: Long?): String {
        if (timeMillis == null) return "--:--"
        return timeFormat.format(Date(timeMillis))
    }

    fun getFormattedDate(dateString: String): String {
        return try {
            val parsed = dateFormat.parse(dateString)
            if (parsed != null) displayDateFormat.format(parsed) else dateString
        } catch (e: Exception) {
            dateString
        }
    }

    fun getPast6Months(): List<MonthItem> {
        val list = mutableListOf<MonthItem>()
        val cal = Calendar.getInstance()

        for (i in 0 until 6) {
            val year = cal.get(Calendar.YEAR)
            val month = cal.get(Calendar.MONTH) + 1
            val key = String.format(Locale.US, "%04d-%02d", year, month)
            val name = monthYearFormat.format(cal.time)
            val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

            // Calculate working days (Mon-Fri)
            var workDays = 0
            val tempCal = cal.clone() as Calendar
            for (d in 1..maxDays) {
                tempCal.set(Calendar.DAY_OF_MONTH, d)
                val dayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK)
                if (dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY) {
                    workDays++
                }
            }

            list.add(MonthItem(key, name, year, month, maxDays, workDays))
            cal.add(Calendar.MONTH, -1)
        }
        return list
    }

    fun calculateWorkHours(checkIn: Long?, checkOut: Long?): String {
        if (checkIn == null || checkOut == null || checkOut <= checkIn) return "--"
        val diffMillis = checkOut - checkIn
        val hours = diffMillis / (1000.0 * 60 * 60)
        return String.format(Locale.US, "%.1f hrs", hours)
    }

    fun getTimeOfDayGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 4..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }
}
