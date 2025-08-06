// ui/WelcomeScreen.kt
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
import androidx.compose.ui.platform.LocalContext
@Composable
fun WelcomeScreen(onGetStarted: () -> Unit) { // Changed parameter
    // inside your composable:
    val context = LocalContext.current
    val pkgInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    val version = pkgInfo.versionName
    LaunchedEffect(Unit) {
        delay(500)
        onGetStarted() // Call the lambda instead of navigating directly
    }
    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("ChronoLog v$version")
    }
}