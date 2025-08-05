
// ui/SettingsPage.kt
package com.aightech.projecttimer.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aightech.projecttimer.model.SettingsModel
import com.aightech.projecttimer.util.AnalyticsUtil
import com.aightech.projecttimer.model.ExportMechanism
import java.time.LocalDate
import androidx.compose.foundation.layout.*

@Composable
fun SettingsPage(vm: SettingsModel = viewModel()) {
    val theme by vm.isDarkTheme.collectAsState()
    val weekly = AnalyticsUtil.calculateWeeklyHours()
    val monthly = AnalyticsUtil.calculateMonthlyHours()

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text("App version: 1.0")
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Dark theme")
            Spacer(Modifier.width(8.dp))
            Switch(checked = theme, onCheckedChange = vm::toggleTheme)
        }
        Spacer(Modifier.height(16.dp))
        Text("Analytics")
        Text("Hours this week: ${weekly}h")
        Text("Hours this month: ${monthly}h")
        Spacer(Modifier.height(16.dp))
        Button(onClick = {
            ExportMechanism.exportICS(
                start = LocalDate.now().minusMonths(1),
                end = LocalDate.now()
            )
        }) {
            Text("Export ICS")
        }
    }
}