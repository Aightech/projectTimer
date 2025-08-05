// ui/SessionsPage.kt
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aightech.projecttimer.model.SessionModel
import com.aightech.projecttimer.ui.components.SessionItem
import java.time.format.DateTimeFormatter


@Composable
fun SessionsPage(viewModel: SessionModel = viewModel()) {
    val sessions by viewModel.sessions.collectAsState()
    val grouped = sessions.groupBy { it.date }
    LazyColumn(Modifier.fillMaxSize()) {
        grouped.forEach { (date, list) ->
            item { DateSeparator(date.format(DateTimeFormatter.ISO_DATE)) }
            items(list) { session ->
                SessionItem(session = session, onClick = { viewModel.edit(session) })
            }
        }
    }
    FloatingActionButton(
        onClick = { viewModel.addNew() },
//        modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
    ) {
        Icon(Icons.Filled.Add, contentDescription = null)
    }
}

@Composable
fun DateSeparator(date: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Divider(Modifier.weight(1f).align(Alignment.CenterVertically))
        Text(date, Modifier.padding(horizontal = 8.dp))
        Divider(Modifier.weight(1f).align(Alignment.CenterVertically))
    }
}