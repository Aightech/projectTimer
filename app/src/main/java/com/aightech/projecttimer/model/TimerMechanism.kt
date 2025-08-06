package com.aightech.projecttimer.model

import android.app.Application
// import android.content.Intent // Temporarily commented out
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// import com.aightech.projecttimer.services.TimerService // Temporarily commented out
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import android.content.Context
import com.aightech.projecttimer.util.NotificationHelper


class TimerMechanism(
    private val application: Application,
    private val sessionViewModel: SessionViewModel,
    private val context: Context
) : ViewModel() {
    private var timerJob: Job? = null

    // private val context = context // Redundant, context is now a constructor property
    private var startTime: LocalDateTime? = null // This is LocalDateTime

    private val _currentProject = MutableStateFlow<Project?>(null)
    val currentProject: StateFlow<Project?> = _currentProject.asStateFlow()

    private val _currentTime = MutableStateFlow("00:00:00")
    val currentTime: StateFlow<String> = _currentTime.asStateFlow()
    private val _currentProjectState = MutableStateFlow(0)
    val currentProjectState: StateFlow<Int> = _currentProjectState.asStateFlow()


    private var currentSession: Session? = null
    //state of the timer : 0 = not running, 1 = running, 2: paused

    init {
        Log.d("TimerMechanism", "ViewModel instance ${this.hashCode()} initialized with SessionViewModel hash: ${sessionViewModel.hashCode()}")
    }

    fun start(p: Project) {
        if (_currentProject.value == p && timerJob?.isActive == true) return

        // Stop existing timer and clear notification before starting a new one or restarting
        stop(clearCurrentProject = false) // Stop first, but keep project info for a moment

        _currentProject.value = p
        startTime = LocalDateTime.now() // startTime is LocalDateTime
        _currentTime.value = "00:00:00" // Reset display time
        _currentProjectState.value = 1

        currentSession = Session(
            projectId = p.id,
            title = p.name, // Provide title
            date = startTime!!.toLocalDate(), // Convert LocalDateTime to LocalDate for Session's date
            startTime = startTime!!.toLocalTime() // Convert LocalDateTime to LocalTime for Session's startTime
        )
        Log.d("TimerMechanism", "Started session ID: ${currentSession?.id} for project ${p.name}")

        NotificationHelper.showOngoing(context, p.name, _currentTime.value)


        /* // Temporarily commented out TimerService logic
        val serviceIntent = Intent(application, TimerService::class.java).apply {
            putExtra("PROJECT_NAME", p.name)
        }
        application.startService(serviceIntent)
        */

        timerJob = viewModelScope.launch {
            Log.d("TimerMechanism", "Starting timer UI for project: ${p.name}")
            while (isActive) {
                val now = LocalDateTime.now()
                startTime?.let {
                    val duration = Duration.between(it, now)
                    _currentTime.value = formatDuration(duration)
                    // Update notification with the new time
                    p.let { project -> NotificationHelper.update(context, project.name, _currentTime.value) }
                }
                delay(1000)
            }
        }
    }

    fun pause() {

    }

    fun stop(note: String = ""
        ,clearCurrentProject: Boolean = true) {
        timerJob?.cancel()
        timerJob = null
        Log.d("TimerMechanism", "Timer job stopped. clearCurrentProject: $clearCurrentProject")

        NotificationHelper.cancel(context) // Cancel notification when timer stops

        // application.stopService(Intent(application, TimerService::class.java)) // Temporarily commented out

        if (startTime != null && _currentProject.value != null && currentSession != null) {
            val localEndTime = LocalDateTime.now()
            currentSession?.endTime = localEndTime.toLocalTime() // Convert LocalDateTime to LocalTime for Session's endTime
            currentSession?.durationMinutes = Duration.between(startTime!!, localEndTime).seconds / 60.0f
            currentSession?.note = note
            Log.d("TimerMechanism", "Current session ID: ${currentSession?.id}")
            Log.d("TimerMechanism", "Project ID: ${currentSession?.projectId}")
            Log.d("TimerMechanism", "Title: ${currentSession?.title}")
            Log.d("TimerMechanism", "Date: ${currentSession?.date}")
            Log.d("TimerMechanism", "Start Time: ${currentSession?.startTime}")
            Log.d("TimerMechanism", "End Time: ${currentSession?.endTime}")
            Log.d("TimerMechanism", "Duration Minutes: ${currentSession?.durationMinutes}")

            sessionViewModel.addSession(currentSession!!)
        }

        if (clearCurrentProject) {
            _currentProject.value = null
            _currentTime.value = "00:00:00"
            _currentProjectState.value = 0
            startTime = null
            currentSession = null
            Log.d("TimerMechanism", "Current project and time cleared.")
        } else {
            // If not clearing the project (e.g., switching), keep the project info
            // but reset the displayed time as the timer is stopped.
            // The notification is already cancelled. If we are immediately starting another timer,
            // it will show its own notification.
            _currentTime.value = "00:00:00"
            Log.d("TimerMechanism", "Timer stopped (e.g. for switching), time reset, project kept: ${_currentProject.value?.name}")
        }
    }

    private fun formatDuration(duration: Duration): String {
        val seconds = duration.seconds
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, secs)
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("TimerMechanism", "ViewModel instance ${this.hashCode()} cleared. Stopping timer job if active.")
        stop(clearCurrentProject = true) // This will also cancel the notification
    }
}

