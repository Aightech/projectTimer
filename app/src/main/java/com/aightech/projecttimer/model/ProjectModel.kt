// model/ProjectModel.kt
package com.aightech.projecttimer.model

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.aightech.projecttimer.util.PersistenceManager

data class Project(
    val id: String,
    var name: String,
    var color: Long,
    var expectedHours: Float,
    var hoursDone: Float,
    var workedHours: Float = 0f
)

class ProjectModel : ViewModel() {
    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> get() = _projects

    init { load() }

    fun addNew() { /* TODO */ }
    fun edit(project: Project) { /* TODO */ }
    fun reorder(from: Int, to: Int) { /* swap then: */ save() }

    private fun load() {
        _projects.value = PersistenceManager.loadProjects()
    }

    fun save() {
        PersistenceManager.saveProjects(_projects.value)
    }
}
