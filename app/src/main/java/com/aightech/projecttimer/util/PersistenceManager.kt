package com.aightech.projecttimer.util

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.input.key.type
import com.aightech.projecttimer.model.Project
import com.aightech.projecttimer.model.Session
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object PersistenceManager {
    private lateinit var prefs: SharedPreferences
    private val gson = Gson()

    private const val PREFS_NAME = "ProjectTimerPrefs"
    private const val KEY_PROJECTS = "projects"
    private const val KEY_SESSIONS = "sessions"
    private const val KEY_ACTIVE_PROJECT_ID = "active_project_id"
    private const val KEY_ACTIVE_PROJECT_NAME = "active_project_name" // Storing name for convenience
    private const val KEY_ACTIVE_ELAPSED_TIME = "active_elapsed_time"

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // --- Project Persistence ---

    fun saveProjects(projects: List<Project>) {
        val json = gson.toJson(projects)
        prefs.edit().putString(KEY_PROJECTS, json).apply()
    }

    fun loadProjects(): List<Project> {
        val json = prefs.getString(KEY_PROJECTS, null)
        return if (json != null) {
            val type = object : TypeToken<List<Project>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }

    // --- Session Persistence ---

    fun saveSessions(sessions: List<Session>) {
        val json = gson.toJson(sessions)
        prefs.edit().putString(KEY_SESSIONS, json).apply()
    }

    fun loadSessions(): List<Session> {
        val json = prefs.getString(KEY_SESSIONS, null)
        return if (json != null) {
            val type = object : TypeToken<List<Session>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }

    /**
     * Adds a new session to the existing list of sessions and saves it.
     */
    fun addSession(session: Session) {
        val currentSessions = loadSessions().toMutableList()
        currentSessions.add(session)
        saveSessions(currentSessions)
    }

    // --- Active Session Persistence ---

    /**
     * Saves the currently active project and its elapsed time.
     * If the project is null, it clears the active session.
     */
    fun saveActiveSession(pair: Pair<Project?, Long>) {
        val project = pair.first
        val elapsedTime = pair.second
        val editor = prefs.edit()

        if (project != null) {
            editor.putString(KEY_ACTIVE_PROJECT_ID, project.id) // Assuming Project has an 'id'
            editor.putString(KEY_ACTIVE_PROJECT_NAME, project.name)
            editor.putLong(KEY_ACTIVE_ELAPSED_TIME, elapsedTime)
        } else {
            editor.remove(KEY_ACTIVE_PROJECT_ID)
            editor.remove(KEY_ACTIVE_PROJECT_NAME)
            editor.remove(KEY_ACTIVE_ELAPSED_TIME)
        }
        editor.apply()
    }

    /**
     * Loads the currently active project and its elapsed time.
     * Returns null if no active session is saved.
     */
    fun loadActiveSession(): Pair<Project, Long>? {
        val projectId = prefs.getString(KEY_ACTIVE_PROJECT_ID, null)
        val projectName = prefs.getString(KEY_ACTIVE_PROJECT_NAME, null) // Retrieve name
        val elapsedTime = prefs.getLong(KEY_ACTIVE_ELAPSED_TIME, 0L)

        return if (projectId != null && projectName != null) {
            // Reconstruct the project object.
            // Ensure your Project data class has appropriate fields (e.g., id, name)
            val project = Project(id = projectId, name = projectName /*, add other properties if needed */)
            project to elapsedTime
        } else {
            null
        }
    }

    /**
     * Overload to load active session details for a specific project.
     * This is useful if you want to resume a specific project and need its last elapsed time.
     * Note: This implementation assumes the active session IS for the given project.
     * You might need more sophisticated logic if multiple projects could have "active" states
     * or if you're looking for a specific project's last *paused* state rather than the globally active one.
     */
    fun loadActiveSession(projectToLoad: Project): Pair<Project, Long>? {
        val activeSession = loadActiveSession()
        return if (activeSession?.first?.id == projectToLoad.id) {
            activeSession
        } else {
            // If the currently saved active session is not for projectToLoad,
            // it means projectToLoad was not the last active one, or it's a fresh start for it.
            // You might return null or projectToLoad to 0L depending on desired behavior.
            null // Or: projectToLoad to 0L
        }
    }

    /**
     * Clears all saved data. Useful for development or a reset feature.
     */
    fun clearAllData() {
        prefs.edit().clear().apply()
    }
}

