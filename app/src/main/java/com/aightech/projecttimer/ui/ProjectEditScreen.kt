package com.aightech.projecttimer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
//import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.aightech.projecttimer.model.ProjectViewModel
import com.aightech.projecttimer.model.Project
import java.time.LocalDate
import java.util.UUID

@Composable
fun ProjectEditScreen(
    projectId: String,
    navController: NavController,
    viewModel: ProjectViewModel = viewModel()
) {
    // collect projects list
    val projects by viewModel.projects.collectAsState()

    // find existing or create new
    val existing = remember(projectId, projects) {
        projects.find { it.id == projectId }
    }
    val initial = if (projectId == "new" || existing == null) {
        Project(
            id = UUID.randomUUID().toString(),
            name = "",
            color = 0xFF000000,
            expectedHours = 0f,
            hoursDone = 0f
        )
    } else existing

    var name by remember { mutableStateOf(initial.name) }
    var colorHex by remember { mutableStateOf(initial.color.toUInt().toString(16).uppercase()) }
    var expectedHours by remember { mutableStateOf(initial.expectedHours.toString()) }
    var hoursDone by remember { mutableStateOf(initial.hoursDone.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Project Name") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = colorHex,
            onValueChange = { colorHex = it },
            label = { Text("Color (HEX)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = expectedHours,
            onValueChange = { expectedHours = it },
            label = { Text("Expected Hours") },
            modifier = Modifier.fillMaxWidth(),
//            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        OutlinedTextField(
            value = hoursDone,
            onValueChange = { hoursDone = it },
            label = { Text("Hours Done") },
            modifier = Modifier.fillMaxWidth(),
//            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    if (projectId != "new") {
                        viewModel.removeProject(initial)
                    }
                    navController.popBackStack()
                },
                modifier = Modifier.weight(1f)
            ) { Text("Delete") }
            Button(
                onClick = {
                    val updated = Project(
                        id = initial.id,
                        name = name,
                        color = colorHex.toLong(16),
                        expectedHours = expectedHours.toFloatOrNull() ?: 0f,
                        hoursDone = hoursDone.toFloatOrNull() ?: 0f
                    )
                    if (projectId == "new" || existing == null) {
                        viewModel.addProject(updated)
                    } else {
                        viewModel.updateProject(updated)
                    }
                    navController.popBackStack()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Save")
            }
        }
    }
}
