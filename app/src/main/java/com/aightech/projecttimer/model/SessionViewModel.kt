// model/SessionViewModel.kt
package com.aightech.projecttimer.model

import android.util.Log
import androidx.activity.result.launch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aightech.projecttimer.util.PersistenceManager // Ensure this import is correct
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch // Added this import
import java.time.Duration // Added
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

// Updated Session data class
data class Session(
    val id: String = UUID.randomUUID().toString(),
    val projectId: String,
    val title: String,
    var note: String? = null,
    val date: LocalDate,
    val startTime: LocalTime,
    var endTime: LocalTime? = null,
    var durationMinutes: Long = 0 // Changed to Long, default 0
)

// Renamed from SessionModel to SessionViewModel for consistency if desired,
// or keep as SessionModel if that's the project convention.
// For this example, I'll use SessionViewModel to align with typical Android naming.
class SessionViewModel : ViewModel() { // Changed class name
    private val _sessions = MutableStateFlow<List<Session>>(emptyList())
    val sessions: StateFlow<List<Session>> get() = _sessions // No change here

    init {
        Log.d("SessionViewModel", "SessionViewModel instance initialized: ${this.hashCode()}")
        loadSessions() // Renamed from load() to loadSessions() for clarity
    }

    // Renamed from load()
    private fun loadSessions() {
        val loadedSessions = PersistenceManager.loadSessions()
        val comparator = compareByDescending<Session> { it.date }.thenByDescending { it.startTime }
        _sessions.value = loadedSessions.sortedWith(comparator)
    }

    // Renamed from save() and made private as it's an internal implementation detail
    private fun saveSessions() {
        PersistenceManager.saveSessions(_sessions.value)
    }

    fun addSession(session: Session) { // <<< Confirmed: addSession(session: Session) exists
        Log.d("SessionViewModel", "addSession called with Session ID: ${session.id}")
        viewModelScope.launch {
            val currentSessions = _sessions.value.toMutableList()
            currentSessions.add(0, session)
            _sessions.value = currentSessions
            PersistenceManager.saveSessions(currentSessions)
            Log.d("SessionViewModel", "Session saved and list updated. New size: ${currentSessions.size}")
        }
    }

    // New function to start a session
    fun startNewSessionForProject(project: Project): Session {
        val newSession = Session(
            projectId = project.id,
            title = project.name, // Using project name as session title
            date = LocalDate.now(),
            startTime = LocalTime.now()
            // endTime and durationMinutes will be set when the timer stops
        )
        val comparator = compareByDescending<Session> { it.date }.thenByDescending { it.startTime }
        _sessions.value = (_sessions.value + newSession).sortedWith(comparator)
        saveSessions()
        return newSession
    }

    // New function to update session end time and duration
    fun updateSessionEndTime(sessionId: String, endTimeValue: LocalTime, calculatedDurationMinutes: Long) {
        val comparator = compareByDescending<Session> { it.date }.thenByDescending { it.startTime }
        _sessions.value = _sessions.value.map {
            if (it.id == sessionId) {
                it.copy(endTime = endTimeValue, durationMinutes = calculatedDurationMinutes)
            } else {
                it
            }
        }.sortedWith(comparator)
        saveSessions()
    }

    fun getSessionById(sessionId: String): Session? {
        return _sessions.value.find { it.id == sessionId }
    }

    // edit() and addNew() can be implemented based on future manual editing needs
    fun addNewBlankSession() { /* TODO: For manual session creation */ }
    fun editSessionDetails(session: Session) { /* TODO: For manual session editing */ }

    fun deleteSession(session: Session) {
        _sessions.value = _sessions.value.filterNot { it.id == session.id }
        saveSessions() // Ensure sessions are saved after deletion and list remains sorted (filter maintains order)
    }
}

