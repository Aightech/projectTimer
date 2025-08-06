
// util/AnalyticsUtil.kt
package com.aightech.projecttimer.util

import com.aightech.projecttimer.model.Session
import java.time.LocalDate
import java.time.YearMonth
import java.util.*
import java.time.temporal.WeekFields

object AnalyticsUtil {
    private val sessions: List<Session> get() = PersistenceManager.loadSessions()

    fun calculateWeeklyHours(): Float {
        val now = LocalDate.now()
        val weekNo = now.get(WeekFields.of(Locale.getDefault()).weekOfYear())
        return (sessions
            .filter { it.date.get(WeekFields.of(Locale.getDefault()).weekOfYear()) == weekNo }
            .sumOf { it.durationMinutes.toDouble() } / 60f).toFloat()
    }

    fun calculateMonthlyHours(): Float {
        val now = YearMonth.now()
        return (sessions
            .filter { YearMonth.from(it.date) == now }
            .sumOf { it.durationMinutes.toDouble() } / 60f).toFloat()
    }
}

