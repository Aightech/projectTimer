package com.aightech.projecttimer.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// Make sure this is YOUR Project data class
// import com.aightech.projecttimer.model.Project
import com.aightech.projecttimer.util.PersistenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.WeekFields
import java.util.UUID

// Your Session Data Class - Ensure this is the one being used everywhere
data class Session(
    val id: String = UUID.randomUUID().toString(),
    val projectId: String,
    val title: String,
    var note: String? = null,
    val date: LocalDate,
    val startTime: LocalTime,
    var endTime: LocalTime? = null,
    var durationMinutes: Float = 0.0f
)

class SessionViewModel : ViewModel() {
    // CRITICAL: Ensure this uses your model.Session
    private val _sessions = MutableStateFlow<List<Session>>(emptyList())
    val sessions: StateFlow<List<Session>> get() = _sessions.asStateFlow()

    private var _prjVM: ProjectViewModel? = null

    private val _startingDayOfTheWeek = MutableStateFlow(DayOfWeek.MONDAY)
    val startingDayOfTheWeek: StateFlow<DayOfWeek> = _startingDayOfTheWeek.asStateFlow()

    init {
        Log.d("SessionViewModel", "SessionViewModel instance initialized: ${this.hashCode()}")
        loadStartingDayOfTheWeek()
        loadSessionsInternal()
    }

    private fun loadStartingDayOfTheWeek() {
        _startingDayOfTheWeek.value = PersistenceManager.loadStartingDayOfWeek()
        Log.d("SessionViewModel", "Loaded starting day of week: ${_startingDayOfTheWeek.value}")
    }

    fun updateStartingDayOfTheWeek(day: DayOfWeek) {
        Log.d("SessionViewModel", "Updating starting day of week to: $day")
        if (_startingDayOfTheWeek.value == day) return

        _startingDayOfTheWeek.value = day
        PersistenceManager.saveStartingDayOfWeek(day)
        Log.d("SessionViewModel", "Updated starting day of week to: $day")
        recalculateProjectHoursForAllProjectsCurrentWeek()
    }

    private fun recalculateProjectHoursForAllProjectsCurrentWeek() {
        viewModelScope.launch {
            val currentProjects = _prjVM?.projects?.value ?: return@launch
            val allSessions: List<Session> = _sessions.value // Explicit type

            Log.d("SessionViewModel", "Recalculating weekly hours for all projects due to week start change.")

            currentProjects.forEach { project ->
                var weeklyDurationMinutes = 0.0f
                allSessions.filter { session: Session -> session.projectId == project.id } // Explicit type for session
                    .filter { session: Session -> // Explicit type
                        areInSameWeek(
                            session.date.atTime(session.startTime),
                            LocalDateTime.now(),
                            _startingDayOfTheWeek.value
                        )
                    }
                    .forEach { session: Session -> weeklyDurationMinutes += session.durationMinutes } // Explicit type

                val hoursDoneForWeek = weeklyDurationMinutes / 60.0f
                if (project.hoursDone != hoursDoneForWeek) {
                    val updatedProject = project.copy(hoursDone = hoursDoneForWeek)
                    _prjVM?.updateProject(updatedProject)
                    Log.d("SessionViewModel", "Project ${project.name} weekly hours updated to: $hoursDoneForWeek")
                }
            }
        }
    }

    private fun loadSessionsInternal() {
        val loadedSessions: List<Session> = PersistenceManager.loadSessions() // Ensure this returns List<Session>
        val comparator = compareByDescending<Session> { it.date }
            .thenByDescending { it.startTime }
        _sessions.value = loadedSessions.sortedWith(comparator)
        Log.d("SessionViewModel", "Sessions loaded. Count: ${loadedSessions.size}")
    }

    fun forceReloadSessions() {
        loadSessionsInternal()
        recalculateProjectHoursForAllProjectsCurrentWeek()
    }

    private fun saveSessions() {
        PersistenceManager.saveSessions(_sessions.value)
        Log.d("SessionViewModel", "Sessions saved. Count: ${_sessions.value.size}")
    }

    fun setProjectViewModel(projectViewModel: ProjectViewModel) {
        _prjVM = projectViewModel
        recalculateProjectHoursForAllProjectsCurrentWeek()
    }

    fun areInSameWeek(dt1: LocalDateTime, dt2: LocalDateTime, firstDayOfWeek: DayOfWeek): Boolean {
        val weekFields = WeekFields.of(firstDayOfWeek, 1)
        val wby1 = dt1.get(weekFields.weekBasedYear())
        val woy1 = dt1.get(weekFields.weekOfWeekBasedYear())
        val wby2 = dt2.get(weekFields.weekBasedYear())
        val woy2 = dt2.get(weekFields.weekOfWeekBasedYear())
        return wby1 == wby2 && woy1 == woy2
    }

    fun addSession(session: Session) { // Parameter is already correctly typed
        Log.d("SessionViewModel", "addSession called with Session ID: ${session.id}, Project ID: ${session.projectId}")
        viewModelScope.launch {
            val currentSessions: MutableList<Session> = _sessions.value.toMutableList() // Explicit type
            currentSessions.add(0, session)
            val comparator = compareByDescending<Session> { it.date }.thenByDescending { it.startTime }
            _sessions.value = currentSessions.sortedWith(comparator)
            saveSessions()

            updateProjectHoursForSpecificProject(session.projectId, session.date.atTime(session.startTime))
            Log.d("SessionViewModel", "Session added and list updated. New size: ${_sessions.value.size}")
        }
    }

    fun updateSession(updatedSession: Session) { // Parameter is correctly typed
        Log.d("SessionViewModel", "updateSession called for Session ID: ${updatedSession.id}")
        viewModelScope.launch {
            val currentList: MutableList<Session> = _sessions.value.toMutableList() // Explicit type
            val index = currentList.indexOfFirst { it.id == updatedSession.id }
            if (index != -1) {
                currentList[index] = updatedSession
                val comparator = compareByDescending<Session> { it.date }.thenByDescending { it.startTime }
                _sessions.value = currentList.sortedWith(comparator)
                saveSessions()
                updateProjectHoursForSpecificProject(updatedSession.projectId, updatedSession.date.atTime(updatedSession.startTime))
            } else {
                Log.w("SessionViewModel", "Attempted to update a session that does not exist: ${updatedSession.id}")
            }
        }
    }

    fun updateSessionEndTime(sessionId: String, endTimeValue: LocalTime, calculatedDurationMinutes: Float) {
        Log.d("SessionViewModel", "updateSessionEndTime for Session ID: $sessionId")
        viewModelScope.launch {
            val sessionToUpdate: Session? = _sessions.value.find { it.id == sessionId } // Explicit type for find if needed
            val updatedList = _sessions.value.map { session: Session -> // Explicit type
                if (session.id == sessionId) {
                    // Make sure your Session data class has copy method (it should as a data class)
                    session.copy(endTime = endTimeValue, durationMinutes = calculatedDurationMinutes)
                } else {
                    session
                }
            }
            val comparator = compareByDescending<Session> { it.date }.thenByDescending { it.startTime }
            _sessions.value = updatedList.sortedWith(comparator)
            saveSessions()

            sessionToUpdate?.let { updatedSess: Session -> // Explicit type
                updateProjectHoursForSpecificProject(updatedSess.projectId, updatedSess.date.atTime(updatedSess.startTime))
            }
        }
    }

    private fun updateProjectHoursForSpecificProject(projectId: String, sessionDateTime: LocalDateTime) {
        viewModelScope.launch {
            var weeklyDurationMinutes = 0.0f
            _sessions.value.filter { session: Session -> session.projectId == projectId } // Explicit type
                .filter { session: Session -> // Explicit type
                    areInSameWeek(
                        session.date.atTime(session.startTime),
                        sessionDateTime,
                        _startingDayOfTheWeek.value
                    )
                }
                .forEach { session: Session -> weeklyDurationMinutes += session.durationMinutes } // Explicit type

            val hoursDoneForWeek = weeklyDurationMinutes / 60.0f
            Log.d("SessionViewModel", "Duration for project $projectId in relevant week: $hoursDoneForWeek")

            _prjVM?.projects?.value?.find { project -> project.id == projectId }?.let { projectToUpdate ->
                if (projectToUpdate.hoursDone != hoursDoneForWeek) {
                    val updatedProject = projectToUpdate.copy(hoursDone = hoursDoneForWeek)
                    _prjVM?.updateProject(updatedProject)
                    Log.d("SessionViewModel", "Project ${projectToUpdate.name} hours updated to: $hoursDoneForWeek")
                }
            }
        }
    }

    fun getSessionById(sessionId: String): Session? {
        return _sessions.value.find { session: Session -> session.id == sessionId } // Explicit type
    }

    fun deleteSession(session: Session) { // Parameter correctly typed
        Log.d("SessionViewModel", "deleteSession called for Session ID: ${session.id}")
        val projectId = session.projectId
        val sessionDateTime = session.date.atTime(session.startTime)

        _sessions.value = _sessions.value.filterNot { s: Session -> s.id == session.id } // Explicit type
        saveSessions()
        updateProjectHoursForSpecificProject(projectId, sessionDateTime)
    }

    fun startNewSessionForProject(project: Project): Session { // Ensure Project is your data class
        val newSession = Session( // Calling your Session data class constructor
            projectId = project.id,
            title = project.name,
            date = LocalDate.now(),
            startTime = LocalTime.now()
        )
        // This logic might add an incomplete session; decide if this is what you want.
        // Or if it should only be added upon completion.
        // For now, retaining the logic from previous version.
        // val currentSessions = _sessions.value.toMutableList()
        // currentSessions.add(0, newSession)
        // val comparator = compareByDescending<Session> { it.date }.thenByDescending { it.startTime }
        // _sessions.value = currentSessions.sortedWith(comparator)
        // saveSessions() // Usually save a session when it's complete or explicitly added
        return newSession
    }

    fun addNewBlankSession() { /* TODO: Implement */ }
    fun editSessionDetails(session: Session) { /* TODO: Implement */ }
}
