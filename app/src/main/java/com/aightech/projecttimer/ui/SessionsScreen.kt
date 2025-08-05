// ui/SessionsScreen.kt
package com.aightech.projecttimer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel // Still needed if other ViewModels are scoped here
import androidx.navigation.NavController
import com.aightech.projecttimer.model.SessionViewModel // Import SessionViewModel
import com.aightech.projecttimer.model.ProjectViewModel
import com.aightech.projecttimer.ui.components.SessionItem
import java.time.format.DateTimeFormatter

@Composable
fun SessionsScreen(
    navController: NavController,
    sessionViewModel: SessionViewModel, // Accepts SessionViewModel instance
    projectViewModel: ProjectViewModel  // Accepts ProjectViewModel instance
) {
    val sessions by sessionViewModel.sessions.collectAsState()
    val projects by projectViewModel.projects.collectAsState()

    val grouped = sessions.groupBy { it.date }
    Box(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize()) {
            grouped.forEach { (date, list) ->
                item { DateSeparator(date.format(DateTimeFormatter.ISO_DATE)) }
                items(list) { session ->
                    val project = projects.find { p -> p.id == session.projectId }
                    val projectColor = project?.color ?: Color.LightGray.value.toLong()

                    SessionItem(
                        session = session,
                        projectColor = projectColor,
                        onClick = {
                            navController.navigate("session_edit/${session.id}")
                        }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = {
                navController.navigate("session_edit/new")
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add Session")
        }
    }
}

@Composable
fun DateSeparator(dateText: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Divider(Modifier.weight(1f))
        Text(dateText, Modifier.padding(horizontal = 8.dp))
        Divider(Modifier.weight(1f))
    }
}
