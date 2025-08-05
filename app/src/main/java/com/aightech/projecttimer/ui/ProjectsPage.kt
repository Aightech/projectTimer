// ui/ProjectsPage.kt
package com.aightech.projecttimer.ui

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aightech.projecttimer.model.ProjectModel
import com.aightech.projecttimer.model.TimerMechanism
import com.aightech.projecttimer.ui.components.ProjectItem



@Composable
fun ProjectsPage(
    viewModel: ProjectModel = viewModel(),
    timer: TimerMechanism = viewModel()
) {
    val projects by viewModel.projects.collectAsState()
    Box(Modifier.fillMaxSize()) {
        LazyColumn {
            itemsIndexed(projects, key = { _, p -> p.id }) { idx, project ->
                ProjectItem(
                    project = project,
                    onClick = { timer.toggleSession(project) },
                    onEdit = { viewModel.edit(project) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(project.id) {
                            detectDragGesturesAfterLongPress { _, _ ->
                                // TODO: implement reorder logic
                                viewModel.reorder(idx, idx)
                            }
                        }
                )
            }
        }
        FloatingActionButton(
            onClick = { viewModel.addNew() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
        }
    }
}