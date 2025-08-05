// ui/WelcomePage.kt
package com.aightech.projecttimer.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
// NavController is no longer needed here if onGetStarted handles all navigation out.
// import androidx.navigation.NavController
import kotlinx.coroutines.delay

@Composable
fun WelcomePage(onGetStarted: () -> Unit) { // Changed parameter
    LaunchedEffect(Unit) {
        delay(500)
        onGetStarted() // Call the lambda instead of navigating directly
    }
    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Project Timer v1.0")
    }
}