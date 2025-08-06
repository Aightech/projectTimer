// ui/SettingsScreen.kt
package com.aightech.projecttimer.ui

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aightech.projecttimer.model.SettingsViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import androidx.compose.ui.platform.LocalContext
import com.aightech.projecttimer.util.AnalyticsUtil


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: SettingsViewModel = viewModel()) {
    val theme by vm.isDarkTheme.collectAsState()
    val selectedStartDayOfWeek by vm.startDayOfWeek.collectAsState()

    val weekly = AnalyticsUtil.calculateWeeklyHours()
    val monthly = AnalyticsUtil.calculateMonthlyHours()

    val context = LocalContext.current
    val pkgInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    val version = pkgInfo.versionName
    val scope = rememberCoroutineScope()

    // --- State for ICS Export Date Range Dialog ---
    var showIcsDateRangeDialog by remember { mutableStateOf(false) }
    var icsStartDate by remember { mutableStateOf<LocalDate?>(null) }
    var icsEndDate by remember { mutableStateOf<LocalDate?>(null) }

    // --- ActivityResultLaunchers ---
    val exportJsonLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { outputUri ->
            scope.launch {
                val jsonString = vm.exportJson()
                if (jsonString != null) {
                    try {
                        context.contentResolver.openOutputStream(outputUri)?.use { outputStream ->
                            outputStream.writer().use { it.write(jsonString) }
                        }
                        Toast.makeText(context, "Data exported successfully", Toast.LENGTH_SHORT).show()
                    } catch (e: IOException) {
                        Log.e("SettingsScreen", "Error writing JSON to file", e)
                        Toast.makeText(context, "Error exporting data: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(context, "Failed to generate export data", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val importJsonLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { inputUri ->
            scope.launch {
                try {
                    context.contentResolver.openInputStream(inputUri)?.use { inputStream ->
                        val jsonString = inputStream.reader().use { it.readText() }
                        val success = vm.importJson(jsonString)
                        if (success) {
                            Toast.makeText(context, "Data imported successfully", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Error importing data", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: IOException) {
                    Log.e("SettingsScreen", "Error reading JSON from file", e)
                    Toast.makeText(context, "Error reading import file: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val exportIcsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/calendar")
    ) { uri ->
        uri?.let { outputUri ->
            // The actual export logic is now called from the dialog's confirm button
            // We need icsStartDate and icsEndDate to be set
            if (icsStartDate == null || icsEndDate == null) {
                Toast.makeText(context, "Start or end date not set for ICS export.", Toast.LENGTH_LONG).show()
                return@rememberLauncherForActivityResult
            }
            scope.launch {
                val icsString = vm.exportICS(icsStartDate!!, icsEndDate!!)
                if (icsString != null) {
                    try {
                        context.contentResolver.openOutputStream(outputUri)?.use { outputStream ->
                            outputStream.writer().use { it.write(icsString) }
                        }
                        Toast.makeText(context, "ICS exported successfully", Toast.LENGTH_SHORT).show()
                    } catch (e: IOException) {
                        Log.e("SettingsScreen", "Error writing ICS to file", e)
                        Toast.makeText(context, "Error exporting ICS: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(context, "No sessions to export or error generating ICS data.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    // --- UI ---
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.Start
    ) {
//        tittle: Settings
        Text("Settings", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text("App Version: ${version}", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(8.dp))

//        // --- Appearance Section ---
//        Text("Appearance", style = MaterialTheme.typography.titleMedium)
//        Row(
//            verticalAlignment = Alignment.CenterVertically,
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            Text("Dark Theme")
//            Switch(checked = theme, onCheckedChange = vm::toggleTheme)
//        }
//        Divider(modifier = Modifier.padding(vertical = 8.dp))

                // --- Data analyse Section ---
        Text("Stats", style = MaterialTheme.typography.titleMedium)
        Text("Hours this week: ${weekly}h")
        Text("Hours this month: ${monthly}h")
        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // --- Parameters Management Section ---
        Text("Parameters Management", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        var startingDayDropdownExpanded by remember { mutableStateOf(false) }
        val daysOfWeek = remember { DayOfWeek.values().toList() }

        ExposedDropdownMenuBox(
            expanded = startingDayDropdownExpanded,
            onExpandedChange = { startingDayDropdownExpanded = !startingDayDropdownExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedStartDayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                onValueChange = {},
                label = { Text("Start Day of Week") },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = startingDayDropdownExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = startingDayDropdownExpanded,
                onDismissRequest = { startingDayDropdownExpanded = false }
            ) {
                daysOfWeek.forEach { day ->
                    DropdownMenuItem(
                        text = { Text(day.getDisplayName(TextStyle.FULL, Locale.getDefault())) },
                        onClick = {
                            vm.setStartDayOfWeek(day)
                            startingDayDropdownExpanded = false
                        }
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))

        // --- Data Management Section ---
        Text("Data Management", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val defaultFileName = "project_timer_backup_${LocalDate.now()}.json"
                    exportJsonLauncher.launch(defaultFileName)
                },
                modifier = Modifier.weight(1f)
            ) { Text("Export JSON") }
            Button(
                onClick = { importJsonLauncher.launch("application/json") },
                modifier = Modifier.weight(1f)
            ) { Text("Import JSON") }
        }
        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = {
                // Reset dates and show dialog
                icsStartDate = LocalDate.now().minusMonths(1) // Default start: 1 month ago
                icsEndDate = LocalDate.now()                   // Default end: today
                showIcsDateRangeDialog = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Export to ICS")
        }

        Spacer(Modifier.weight(1f)) // Pushes content to top
    }

    // --- ICS Date Range Selection Dialog ---
    if (showIcsDateRangeDialog) {
        IcsDateRangePickerDialog(
            initialStartDate = icsStartDate ?: LocalDate.now().minusMonths(1),
            initialEndDate = icsEndDate ?: LocalDate.now(),
            onDismiss = { showIcsDateRangeDialog = false },
            onConfirm = { startDate, endDate ->
                showIcsDateRangeDialog = false
                icsStartDate = startDate
                icsEndDate = endDate
                // Now launch the file picker
                val defaultFileName = "project_timer_calendar_${LocalDate.now()}.ics"
                exportIcsLauncher.launch(defaultFileName)
            }
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IcsDateRangePickerDialog(
    initialStartDate: LocalDate,
    initialEndDate: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (startDate: LocalDate, endDate: LocalDate) -> Unit
) {
    val context = LocalContext.current
    // These 'temp' states hold the dates selected within this dialog session
    var tempStartDate by remember { mutableStateOf(initialStartDate) }
    var tempEndDate by remember { mutableStateOf(initialEndDate) }

    // These control the visibility of the DatePickerDialogs themselves
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember { DateTimeFormatter.ofLocalizedDate(java.time.format.FormatStyle.MEDIUM) }

    AlertDialog( // This is the main custom dialog wrapper
        onDismissRequest = onDismiss,
        title = { Text("Select Date Range for ICS Export") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Start Date Display and Click Target
                OutlinedTextField(
                    value = tempStartDate.format(dateFormatter),
                    onValueChange = {}, // No change handler needed
                    readOnly = true,   // Remains read-only
                    label = { Text("Start Date") },
                    trailingIcon = { Icon(Icons.Filled.DateRange, "Select start date") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() }, // To ensure no ripple if not desired on a non-interactive component
                            indication = null, // No visual indication for the click itself, DatePickerDialog provides feedback
                            onClick = {
                                Log.d("DatePicker", "Start Date OutlinedTextField Clicked")
                                showStartDatePicker = true
                            }
                        ),
                    enabled = false, // Disabling the TextField makes it non-focusable and ensures the whole area is clickable
                    colors = OutlinedTextFieldDefaults.colors( // Adjust colors if disabled state looks too faded
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                // End Date Display and Click Target
                OutlinedTextField(
                    value = tempEndDate.format(dateFormatter),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("End Date") },
                    trailingIcon = { Icon(Icons.Filled.DateRange, "Select end date") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                Log.d("DatePicker", "End Date OutlinedTextField Clicked")
                                showEndDatePicker = true
                            }
                        ),
                    enabled = false, // Disable to make it non-focusable
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        },
        confirmButton = { // Confirm button for the AlertDialog wrapper
            TextButton(
                onClick = {
                    if (tempEndDate.isBefore(tempStartDate)) {
                        Toast.makeText(context, "End date cannot be before start date.", Toast.LENGTH_LONG).show()
                    } else {
                        onConfirm(tempStartDate, tempEndDate) // This sends the confirmed dates back
                    }
                }
            ) { Text("Export") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        properties = DialogProperties(dismissOnClickOutside = false)
    )

    // Material DatePickerDialog for Start Date
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = tempStartDate // Use the current tempStartDate
                .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            yearRange = (LocalDate.now().year - 100)..(LocalDate.now().year + 100)
        )
        val confirmEnabled by remember { derivedStateOf { datePickerState.selectedDateMillis != null } }

        DatePickerDialog( // This is the actual Material component
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        Log.d("DatePicker", "Start Date Picker Dialog OK clicked") // ADD THIS
                        showStartDatePicker = false
                        datePickerState.selectedDateMillis?.let { millis ->
                            Log.d("DatePicker", "Start Date selected millis: $millis") // ADD THIS
                            tempStartDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                            Log.d("DatePicker", "tempStartDate updated to: $tempStartDate") // ADD THIS
                        }
                    },
                    enabled = confirmEnabled
                ) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = datePickerState) // The calendar UI
        }
    }

    // Material DatePickerDialog for End Date
    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = tempEndDate // Use the current tempEndDate
                .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            yearRange = (LocalDate.now().year - 100)..(LocalDate.now().year + 100),
            selectableDates = object : androidx.compose.material3.SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val selectedDate = Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneId.systemDefault()).toLocalDate()
                    return !selectedDate.isBefore(tempStartDate)
                }
                override fun isSelectableYear(year: Int): Boolean {
                    return year in (LocalDate.now().year - 100)..(LocalDate.now().year + 100)
                }
            }
        )
        val confirmEnabled by remember { derivedStateOf { datePickerState.selectedDateMillis != null } }

        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showEndDatePicker = false // Hide the DatePickerDialog
                        datePickerState.selectedDateMillis?.let { millis ->
                            // Update tempEndDate when "OK" is clicked
                            tempEndDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        }
                    },
                    enabled = confirmEnabled
                ) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = datePickerState) // The calendar UI
        }
    }
}

