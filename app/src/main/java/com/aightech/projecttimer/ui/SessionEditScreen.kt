package com.aightech.projecttimer.ui

import androidx.compose.foundation.clickable // Keep if you use it for date/time pickers
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.aightech.projecttimer.model.ProjectViewModel
import com.aightech.projecttimer.model.SessionViewModel
import com.aightech.projecttimer.model.Session
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionEditScreen(
    sessionId: String,
    navController: NavController,
    projectViewModel: ProjectViewModel = viewModel(), // Assuming you might need it for project list
    sessionViewModel: SessionViewModel = viewModel()
) {
    val projects by projectViewModel.projects.collectAsState()
    val sessions by sessionViewModel.sessions.collectAsState()

    val existingSession = remember(sessionId, sessions) {
        sessions.find { it.id == sessionId }
    }

    // State for editable fields
    var selectedProjectId by remember { mutableStateOf("") }
    var sessionTitle by remember { mutableStateOf("") }
    var sessionDateString by remember { mutableStateOf("") }
    var startTimeString by remember { mutableStateOf("") }
    var endTimeString by remember { mutableStateOf("") } // Can be empty if not set
    var note by remember { mutableStateOf("") }

    // State for project dropdown
    var projectsDropdownExpanded by remember { mutableStateOf(false) }

    // Date and Time formatters
    val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE // YYYY-MM-DD
    val timeFormatter = DateTimeFormatter.ISO_LOCAL_TIME // HH:MM:SS or HH:MM

    LaunchedEffect(existingSession, projects) {
        if (existingSession != null) {
            selectedProjectId = existingSession.projectId
            sessionTitle = existingSession.title
            sessionDateString = existingSession.date.format(dateFormatter)
            startTimeString = existingSession.startTime.format(timeFormatter)
            endTimeString = existingSession.endTime?.format(timeFormatter) ?: ""
            note = existingSession.note ?: ""
        } else { // Defaults for a new session
            if (projects.isNotEmpty()) {
                selectedProjectId = projects.first().id
                sessionTitle = projects.first().name // Default title from project name
            } else {
                sessionTitle = "New Session"
            }
            sessionDateString = LocalDate.now().format(dateFormatter)
            startTimeString = LocalTime.now().format(timeFormatter)
            endTimeString = "" // No default end time for new manual session unless specified
            note = ""
        }
    }

    val isNewSession = sessionId == "new" || existingSession == null
    val selectedProjectName = projects.find { it.id == selectedProjectId }?.name ?: "Select Project"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = if (isNewSession) "New Session" else "Edit Session: ${existingSession?.title ?: ""}",
            style = MaterialTheme.typography.headlineSmall
        )

        // Project Selector Dropdown
        ExposedDropdownMenuBox(
            expanded = projectsDropdownExpanded,
            onExpandedChange = { projectsDropdownExpanded = !projectsDropdownExpanded }
        ) {
            OutlinedTextField(
                value = selectedProjectName,
                onValueChange = {},
                label = { Text("Project") },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = projectsDropdownExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = projectsDropdownExpanded,
                onDismissRequest = { projectsDropdownExpanded = false }
            ) {
                projects.forEach { project ->
                    DropdownMenuItem(
                        text = { Text(project.name) },
                        onClick = {
                            selectedProjectId = project.id
                            if (isNewSession || sessionTitle.isBlank() || sessionTitle == "New Session" || projects.find { it.name == sessionTitle } != null) {
                                // Auto-update title if it's new, blank, or was a previous default project name
                                sessionTitle = project.name
                            }
                            projectsDropdownExpanded = false
                        }
                    )
                }
                if (projects.isEmpty()) { /* ... */ }
            }
        }

        OutlinedTextField(
            value = sessionTitle,
            onValueChange = { sessionTitle = it },
            label = { Text("Session Title") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // TODO: Replace with DatePickerDialog for better UX
        OutlinedTextField(
            value = sessionDateString,
            onValueChange = { sessionDateString = it },
            label = { Text("Date (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(LocalDate.now().format(dateFormatter)) }
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // TODO: Replace with TimePickerDialog for better UX
            OutlinedTextField(
                value = startTimeString,
                onValueChange = { startTimeString = it },
                label = { Text("Start Time (HH:MM)") },
                modifier = Modifier.weight(1f),
                placeholder = { Text(LocalTime.now().format(timeFormatter)) }
            )
            // TODO: Replace with TimePickerDialog for better UX
            OutlinedTextField(
                value = endTimeString,
                onValueChange = { endTimeString = it },
                label = { Text("End Time (HH:MM, Optional)") },
                modifier = Modifier.weight(1f),
                placeholder = { Text(LocalTime.now().plusHours(1).format(timeFormatter)) }
            )
        }

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Note (Optional)") },
            modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
            singleLine = false
        )

        Spacer(modifier = Modifier.weight(1f))



        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Button(
                onClick = {
                    if (existingSession != null) {
                        sessionViewModel.deleteSession(existingSession)
                    }
                    navController.popBackStack()

                },
                modifier = Modifier.weight(1f)
            ) { Text("Delete") }
            Button(
                onClick = {
                    if (selectedProjectId.isBlank()) {
                        println("Error: Project must be selected")
                        return@Button
                    }
                    if (sessionTitle.isBlank()) {
                        println("Error: Session title cannot be blank")
                        return@Button
                    }

                    val parsedDate = try {
                        LocalDate.parse(sessionDateString, dateFormatter)
                    } catch (e: DateTimeParseException) {
                        println("Error parsing date: $e"); null
                    }
                    val parsedStartTime = try {
                        LocalTime.parse(startTimeString, timeFormatter)
                    } catch (e: DateTimeParseException) {
                        println("Error parsing start time: $e"); null
                    }
                    val parsedEndTime = if (endTimeString.isNotBlank()) {
                        try {
                            LocalTime.parse(endTimeString, timeFormatter)
                        } catch (e: DateTimeParseException) {
                            println("Error parsing end time: $e"); null
                        }
                    } else null

                    if (parsedDate == null || parsedStartTime == null || (endTimeString.isNotBlank() && parsedEndTime == null)) {
                        println("Error: Invalid date or time format.")
                        return@Button
                    }

                    if (parsedEndTime != null && parsedEndTime.isBefore(parsedStartTime)) {
                        println("Error: End time cannot be before start time on the same day.")
                        return@Button
                    }

                    // Calculate duration if endTime is present
                    val durationMins = if (parsedEndTime != null) {
                        Duration.between(parsedStartTime, parsedEndTime).toMinutes().toFloat()
                    } else {
                        0.0f // Or existingSession?.durationMinutes if you want to preserve it when endTime is cleared
                    }


                    val sessionToSave = Session(
                        id = existingSession?.id ?: UUID.randomUUID().toString(),
                        projectId = selectedProjectId,
                        title = sessionTitle,
                        date = parsedDate,
                        startTime = parsedStartTime,
                        endTime = parsedEndTime, // Can be null
                        note = note.takeIf { it.isNotBlank() },
                        durationMinutes = durationMins
                    )

                    if (isNewSession) {
                        sessionViewModel.addSession(sessionToSave)
                    } else {
                        sessionViewModel.updateSession(sessionToSave) // Now this should resolve
                    }
                    navController.popBackStack()
                },
                modifier = Modifier.weight(1f),
                enabled = projects.isNotEmpty()
            ) {
                Text("Save Session")
            }
        }
    }
}
