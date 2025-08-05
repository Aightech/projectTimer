// MainActivity.kt
package com.aightech.projecttimer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aightech.projecttimer.ui.BottomNav
import com.aightech.projecttimer.ui.ProjectsPage
import com.aightech.projecttimer.ui.SessionsPage
import com.aightech.projecttimer.ui.SettingsPage
import com.aightech.projecttimer.ui.WelcomePage
import com.aightech.projecttimer.util.NotificationHelper
import com.aightech.projecttimer.util.PersistenceManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PersistenceManager.init(this)
        NotificationHelper.createChannel(this)
        setContent {
            val navController = rememberNavController()
            Scaffold(
                bottomBar = { BottomNav(navController) }
            ) { inner ->
                NavHost(
                    navController = navController,
                    startDestination = "welcome",
                    modifier = Modifier.padding(inner)
                ) {
                    composable("welcome") { WelcomePage(navController) }
                    composable("projects") { ProjectsPage() }
                    composable("sessions") { SessionsPage() }
                    composable("settings") { SettingsPage() }
                }
            }
        }
    }
}