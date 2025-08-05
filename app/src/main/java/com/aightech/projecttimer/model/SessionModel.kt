
// model/SessionModel.kt
package com.aightech.projecttimer.model

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.aightech.projecttimer.util.PersistenceManager
import java.time.LocalDate

data class Session(
    val id: String,
    val projectId: String,
    val title: String,
    val note: String?,
    val date: LocalDate,
    val durationMinutes: Int
)

class SessionModel : ViewModel() {
    private val _sessions = MutableStateFlow<List<Session>>(emptyList())
    val sessions: StateFlow<List<Session>> get() = _sessions

    init { load() }

    fun addNew() { /* TODO */ }
    fun edit(session: Session) { /* TODO */ }

    private fun load() {
        _sessions.value = PersistenceManager.loadSessions()
    }

    fun save() {
        PersistenceManager.saveSessions(_sessions.value)
    }
}