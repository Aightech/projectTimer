// ui/WelcomePage.kt
package com.aightech.projecttimer.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@Composable
fun WelcomePage(nav: NavController) {
    LaunchedEffect(Unit) {
        delay(2000)
        nav.navigate("projects") { popUpTo("welcome") { inclusive = true } }
    }
    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Project Timer v1.0")
    }
}