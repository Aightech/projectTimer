package com.aightech.projecttimer.model

import android.util.Log
import androidx.lifecycle.ViewModel
import com.aightech.projecttimer.util.PersistenceManager // Already imported
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.*

// Data class for combined export/import
data class AppExportData(
    val projects: List<Project>,
    val sessions: List<Session>,
    val appVersion: String? = null,
    val exportDate: String? = null
)

class SettingsViewModel : ViewModel() {
    private val _isDarkTheme = MutableStateFlow(false) // Default to light
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun toggleTheme(isDark: Boolean) {
        _isDarkTheme.value = isDark
        // TODO: Persist this theme setting (e.g., using DataStore)
    }

    private val _projectVM = MutableStateFlow<ProjectViewModel?>(null)
    val projectVM: StateFlow<ProjectViewModel?> get() = _projectVM

    private val _sessionVM = MutableStateFlow<SessionViewModel?>(null)
    val sessionVM: StateFlow<SessionViewModel?> get() = _sessionVM

    fun setProjectViewModel(vm: ProjectViewModel) {
        _projectVM.value = vm
    }



    private val _startDayOfWeek = MutableStateFlow(DayOfWeek.MONDAY) // Default to Monday
    val startDayOfWeek: StateFlow<DayOfWeek> = _startDayOfWeek.asStateFlow()


    fun setSessionViewModel(vm: SessionViewModel) {
        Log.d("SettingsModel", "Setting SessionViewModel hash: ${vm.hashCode()}")
        _sessionVM.value = vm
        // Correct way: Update the value of the existing MutableStateFlow
        _startDayOfWeek.value = vm.startingDayOfTheWeek.value
        Log.d("SettingsModel", "Updated start day of week to ${_startDayOfWeek.value} from SessionViewModel")
    }

    fun setStartDayOfWeek(day: DayOfWeek) {
        _startDayOfWeek.value = day
        Log.d("SettingsModel", "Setting start day of week to $day via UI")
        _sessionVM.value?.updateStartingDayOfTheWeek(day)
    }



    // now you can reference VERSION_NAME
    val version: String = "0.1" // Replace with actual app version if available, e.g., BuildConfig.VERSION_NAME


    /**
     * Exports the current list of projects and sessions to a JSON string.
     * Returns null if ViewModels are not set or data is unavailable.
     */
    suspend fun exportJson(): String? {
        val currentProjects = projectVM.value?.projects?.first()
        val currentSessions = sessionVM.value?.sessions?.first()

        if (currentProjects == null || currentSessions == null) {
            Log.e("SettingsModel", "Cannot export: ProjectViewModel or SessionViewModel not set or data is empty.")
            return null
        }

        val exportData = AppExportData(
            projects = currentProjects,
            sessions = currentSessions,
            appVersion = version,
            exportDate = LocalDateTime.now().toString()
        )

        return try {
            // PersistenceManager uses its own Gson instance.
            // If PersistenceManager's Gson is not public, you might need to
            // recreate a similar Gson instance here or expose it from PersistenceManager.
            // For now, assuming PersistenceManager.gson can be made accessible or recreated:
            val gson = PersistenceManager.getGsonInstance() // You'd need to add this method to PersistenceManager
            // OR if PersistenceManager.gson is internal/private, recreate it:
            // val gson = GsonBuilder()
            //    .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter()) // Make sure these adapters are accessible
            //    .registerTypeAdapter(LocalTime::class.java, LocalTimeAdapter())
            //    .setPrettyPrinting()
            //    .create()
            gson.toJson(exportData)
        } catch (e: Exception) {
            Log.e("SettingsModel", "Error during JSON export serialization", e)
            null
        }
    }

    /**
     * Imports projects and sessions from a JSON string.
     * This will overwrite existing projects and sessions.
     *
     * @param jsonString The JSON string containing AppExportData.
     * @return True if import was successful, false otherwise.
     */
    fun importJson(jsonString: String): Boolean {
        val pVM = projectVM.value
        val sVM = sessionVM.value

        if (pVM == null || sVM == null) {
            Log.e("SettingsModel", "Cannot import: ProjectViewModel or SessionViewModel not set.")
            return false
        }

        return try {
            val gson = PersistenceManager.getGsonInstance() // Again, assuming this method exists or recreate Gson
            // OR:
            // val gson = GsonBuilder()
            //    .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
            //    .registerTypeAdapter(LocalTime::class.java, LocalTimeAdapter())
            //    .create()

            val type = object : TypeToken<AppExportData>() {}.type
            val importedData: AppExportData = gson.fromJson(jsonString, type)

            // Save the imported data using PersistenceManager
            PersistenceManager.saveProjects(importedData.projects)
            PersistenceManager.saveSessions(importedData.sessions)

            // Trigger a reload in the ViewModels.
            // ProjectViewModel already reloads in its init block if PersistenceManager is updated.
            // For SessionViewModel, you'll need a public reload method or similar trigger.
            // For ProjectViewModel, to be explicit or if init is not enough:
            pVM.forceReloadProjects() // You'll need to add this method to ProjectViewModel
            sVM.forceReloadSessions() // You'll need to add this method to SessionViewModel

            Log.i("SettingsModel", "Data imported successfully. Projects: ${importedData.projects.size}, Sessions: ${importedData.sessions.size}")
            true
        } catch (e: Exception) {
            Log.e("SettingsModel", "Error during JSON import deserialization or processing", e)
            false
        }
    }

    // Helper function to escape special characters for ICS
    private fun escapeICSString(text: String?): String {
        if (text == null) return ""
        return text
            .replace("\\", "\\\\") // Must be first
            .replace(";", "\\;")
            .replace(",", "\\,")
            .replace("\r\n", "\\n") // Windows newline
            .replace("\n", "\\n")   // Unix newline
            .replace("\r", "\\n")   // Mac (old) newline
    }

    /*
        export the sessions to a calendar ICS file. only export session within start and end dates
     */

    fun exportICS(start: LocalDate, end: LocalDate): String? {
        val currentSessionVM = _sessionVM.value ?: return null
        val sessions = currentSessionVM.sessions.value ?: return null
        if (sessions.isEmpty()) return buildString {
            appendLine("BEGIN:VCALENDAR")
            appendLine("VERSION:2.0")
            appendLine("PRODID:-//YourApp//SessionCalendar//EN")
            appendLine("END:VCALENDAR")
        }

        val localZone = ZoneId.systemDefault()
        val utc = ZoneOffset.UTC
        val dtf = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
        val nowUTC = Instant.now().atZone(utc).format(dtf)

        fun escapeICS(text: String) = text
            .replace("\\", "\\\\")
            .replace(";", "\\;")
            .replace(",", "\\,")
            .replace("\n", "\\n")

        return buildString {
            appendLine("BEGIN:VCALENDAR")
            appendLine("VERSION:2.0")
            appendLine("PRODID:-//YourApp//SessionCalendar//EN")

            sessions
                .asSequence()
                .filter { it.endTime != null && !it.date.isBefore(start) && !it.date.isAfter(end) }
                .forEach { s ->
                    val endTime = s.endTime ?: s.startTime.plusMinutes(s.durationMinutes.toLong())
                    val startZdt = ZonedDateTime.of(s.date, s.startTime, localZone)
                        .withZoneSameInstant(utc)
                    val endZdt = ZonedDateTime.of(s.date, endTime, localZone)
                        .withZoneSameInstant(utc)

                    appendLine("BEGIN:VEVENT")
                    appendLine("UID:${s.id}@yourapp.com")
                    appendLine("DTSTAMP:$nowUTC")
                    appendLine("DTSTART:${startZdt.format(dtf)}")
                    appendLine("DTEND:${endZdt.format(dtf)}")
                    appendLine("SUMMARY:${escapeICS(s.title)}")
                    s.note?.takeIf { it.isNotBlank() }?.let {
                        appendLine("DESCRIPTION:${escapeICS(it)}")
                    }
                    appendLine("END:VEVENT")
                }

            appendLine("END:VCALENDAR")
        }
    }
}
