// ui/ProjectsScreen.kt
package com.aightech.projecttimer.ui

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column // Added for layout within AlertDialog
import androidx.compose.foundation.layout.Spacer // Added for spacing
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height // Added for spacing
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField // Import OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.aightech.projecttimer.model.Project
import com.aightech.projecttimer.model.ProjectViewModel
import com.aightech.projecttimer.model.SessionViewModel
import com.aightech.projecttimer.model.TimerMechanism
import com.aightech.projecttimer.ui.components.ProjectItem
import com.aightech.projecttimer.util.NotificationHelper


@Composable
fun ProjectsScreen(
    navController: NavController,
    projectViewModel: ProjectViewModel,
    sessionViewModel: SessionViewModel,
    timerMechanism: TimerMechanism,
) {

    var showTimerActionsDialog by remember { mutableStateOf(false) }
    var selectedProjectForDialog by remember { mutableStateOf<Project?>(null) }
    var sessionNote by remember { mutableStateOf("") } // State for the note text input

    Log.d("ProjectsScreen", "Using ProjectViewModel hash: ${projectViewModel.hashCode()}")
    Log.d("ProjectsScreen", "Using SessionViewModel hash: ${sessionViewModel.hashCode()}")
    Log.d("ProjectsScreen", "Using TimerMechanism hash: ${timerMechanism.hashCode()}")

    val projects by projectViewModel.projects.collectAsState()
    val currentProject by timerMechanism.currentProject.collectAsState()
    // val currentProjectState by timerMechanism.currentProjectState.collectAsState() // Not directly used in this snippet, but keep if needed elsewhere

    Log.d("ProjectsScreen", "Active project: ${currentProject?.name}")



    Box(Modifier.fillMaxSize()) {
        //Title
        Text("Projects", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(10.dp, 25.dp, 0.dp, 16.dp))
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(0.dp, 70.dp, 0.dp, 0.dp)
        ) {
//            //title
//            item {
//                Text("Projects", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(10.dp, 25.dp, 0.dp, 16.dp))
//            }
            items(projects) { project ->
                val isTimerActiveForProject = currentProject?.id == project.id
                ProjectItem(
                    project = project,
                    isTimerActive = isTimerActiveForProject,
                    onClick = {
                        Log.d("projectsScreen", "click on ${project.name}")
                        if (currentProject?.id !=null) {
                            selectedProjectForDialog = currentProject
                            sessionNote = "" // Clear previous note when dialog opens
                            showTimerActionsDialog = true
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
                .align(Alignment.TopEnd)
                .padding(16.dp, 10.dp, 16.dp, 0.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add Project")
        }

        if (showTimerActionsDialog && selectedProjectForDialog != null) {
            AlertDialog(
                onDismissRequest = {
                    showTimerActionsDialog = false
                    selectedProjectForDialog = null
                    sessionNote = "" // Clear note on dismiss
                },
                title = { Text(text = "Timer Options: ${selectedProjectForDialog?.name ?: "Project"}") },
                text = {
                    // Use a Column to arrange the question and the TextField
                    Column {
                        Text(text = "Do you want to stop the timer?")
                        Spacer(modifier = Modifier.height(8.dp)) // Add some space
                        OutlinedTextField(
                            value = sessionNote,
                            onValueChange = { sessionNote = it },
                            label = { Text("Optional: Add a note") },
                            singleLine = false, // Allow multi-line input if desired
                            modifier = Modifier.padding(top = 8.dp) // Add some padding
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            // Pass the note to the stop function
                            // You'll need to modify timerMechanism.stop to accept a note
                            timerMechanism.stop(note = sessionNote, clearCurrentProject = true)
                            showTimerActionsDialog = false
                            selectedProjectForDialog = null
                            sessionNote = "" // Clear note after submission
                        }
                    ) {
                        Text("Stop Timer")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showTimerActionsDialog = false
                            selectedProjectForDialog = null
                            sessionNote = "" // Clear note on cancel
                        }
                    ) {
                        Text("No")
                    }
                }
            )
        }
    }
}
