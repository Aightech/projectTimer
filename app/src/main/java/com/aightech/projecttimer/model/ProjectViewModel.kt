package com.aightech.projecttimer.model

import androidx.lifecycle.ViewModel
import com.aightech.projecttimer.util.PersistenceManager // Import PersistenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Data model representing a project.
 */
data class Project(
    val id: String,
    val name: String,
    val color: Long=0xffff0000,
    val expectedHours: Float=1f,
    val hoursDone: Float = 0f,
    val workedHours: Float = 0f
)

/**
 * ViewModel for managing a list of projects.
 */
class ProjectViewModel : ViewModel() {
    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    init {
        // Load projects when ViewModel is created
        _projects.value = PersistenceManager.loadProjects()
    }

    /**
     * Add a new project to the list and save.
     */
    fun addProject(project: Project) {
        _projects.value = _projects.value + project
        PersistenceManager.saveProjects(_projects.value) // Save after adding
    }

    /**
     * Update an existing project by matching its id and save.
     */
    fun updateProject(project: Project) {
        _projects.value = _projects.value.map {
            if (it.id == project.id) project else it
        }
        PersistenceManager.saveProjects(_projects.value) // Save after updating
    }

    /**
     * Reorder the list by moving an item from one index to another and save.
     */
    fun reorder(fromIndex: Int, toIndex: Int) {
        val list = _projects.value.toMutableList()
        if (fromIndex in list.indices && toIndex in list.indices) {
            val item = list.removeAt(fromIndex)
            list.add(toIndex, item)
            _projects.value = list
            PersistenceManager.saveProjects(_projects.value) // Save after reordering
        }
    }

    /**
     * Retrieve a project by its unique identifier.
     */
    fun getProjectById(id: String): Project? =
        _projects.value.find { it.id == id }
}
