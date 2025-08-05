package com.aightech.projecttimer.util

import android.content.Context
import com.aightech.projecttimer.model.Project
import com.aightech.projecttimer.model.Session

object PersistenceManager {
    private lateinit var ctx: Context

    fun init(context: Context) {
        ctx = context
    }

    fun loadProjects(): List<Project> = emptyList()  // TODO: read JSON
    fun saveProjects(list: List<Project>) {}      // TODO: write JSON
    fun loadSessions(): List<Session> = emptyList()// TODO
    fun saveSessions(list: List<Session>) {}      // TODO
    fun saveActiveSession(pair: Pair<Project?, Long>) {} // TODO
}