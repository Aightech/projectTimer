package com.aightech.projecttimer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.aightech.projecttimer.model.Project
import com.aightech.projecttimer.model.Session // Ensure Session is imported
import com.aightech.projecttimer.R
import java.time.format.DateTimeFormatter

@Composable
fun ProjectItem(
    project: Project,
    isTimerActive: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardContainerColor = if (isTimerActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    Card(
        modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardContainerColor
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color(project.color)) // Assuming project.color is a Long representing Color
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = project.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "%.2fh / %.2fh".format(project.hoursDone, project.expectedHours),
                    style = MaterialTheme.typography.bodySmall
                )
                if (isTimerActive) {
                    Text(
                        text = "Timer Active",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            IconButton(onClick = { onEdit() }) {
                Icon(
                    painter = painterResource(id = R.drawable.outline_atr_24),
                    contentDescription = "Edit project"
                )
            }
        }
    }
}
@Composable
fun SessionItem(
    session: Session,
    projectColor: Long, // Add projectColor parameter
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier // Apply the passed-in modifier here
            .padding(8.dp) // Apply padding around the Card
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp) // Padding for the content inside the Card
        ) {
            // Use project color from parameter
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color(projectColor)) // Use the passed projectColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.title,
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = session.startTime.format(DateTimeFormatter.ofPattern("HH:mm")) +
                            "  •  %.2fh min".format(session.durationMinutes),
                    style = MaterialTheme.typography.bodySmall
                )
                session.note
                    ?.takeIf { it.isNotBlank() }
                    ?.let { note ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = note,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2
                        )
                    }
            }
        }
    }
}

