// ui/ProjectsScreen.kt
package com.aightech.projecttimer.ui

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.aightech.projecttimer.model.ProjectViewModel
import com.aightech.projecttimer.model.SessionViewModel
import com.aightech.projecttimer.model.TimerMechanism
import com.aightech.projecttimer.ui.components.ProjectItem
import com.aightech.projecttimer.util.NotificationHelper


// Factory is no longer needed here if TimerMechanism is hoisted
// class TimerMechanismFactory(
//     private val application: Application,
//     private val sessionViewModel: SessionViewModel
// ) : ViewModelProvider.Factory {
//     override fun <T : ViewModel> create(modelClass: Class<T>): T {
//         Log.d("TimerMechanismFactory", "Creating TimerMechanism with SessionViewModel hash: ${sessionViewModel.hashCode()}")
//         if (modelClass.isAssignableFrom(TimerMechanism::class.java)) {
//             @Suppress("UNCHECKED_CAST")
//             return TimerMechanism(application, sessionViewModel) as T
//         }
//         throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
//     }
// }

@Composable
fun ProjectsScreen(
    navController: NavController,
    projectViewModel: ProjectViewModel,  // Accepts ProjectViewModel
    sessionViewModel: SessionViewModel,  // Accepts SessionViewModel
    timerMechanism: TimerMechanism,     // Accepts TimerMechanism
) {

    // Log instances received from MainActivity
    Log.d("ProjectsScreen", "Using ProjectViewModel hash: ${projectViewModel.hashCode()}")
    Log.d("ProjectsScreen", "Using SessionViewModel hash: ${sessionViewModel.hashCode()}")
    Log.d("ProjectsScreen", "Using TimerMechanism hash: ${timerMechanism.hashCode()}")

    val projects by projectViewModel.projects.collectAsState()
    val currentProject by timerMechanism.currentProject.collectAsState()
    val currentProjectTime by timerMechanism.currentTime.collectAsState()

    Log.d("ProjectsScreen", "Active project: ${currentProject?.name}")

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(projects) { project ->
                val isTimerActiveForProject = currentProject?.id == project.id
                ProjectItem(
                    project = project,
                    isTimerActive = isTimerActiveForProject,
                    onClick = {
                        Log.d("projectsScreen", "click on ${project.name}")
                        if (isTimerActiveForProject) {
                            timerMechanism.stop()
                        } else {
                            timerMechanism.start(project)

                        }
                    },
                    onEdit = { navController.navigate("project_edit/${project.id}") }
                )
            }
        }
        FloatingActionButton(
            onClick = { navController.navigate("project_edit/new") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add Project")
        }
    }
}
