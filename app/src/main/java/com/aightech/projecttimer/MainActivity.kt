// MainActivity.kt
package com.aightech.projecttimer

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.* // Added for mutableStateOf, remember
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aightech.projecttimer.ui.BottomNav
import com.aightech.projecttimer.ui.ProjectsScreen
import com.aightech.projecttimer.ui.ProjectEditScreen
import com.aightech.projecttimer.ui.SessionsScreen
import com.aightech.projecttimer.ui.SettingsPage
import com.aightech.projecttimer.ui.WelcomePage
import com.aightech.projecttimer.util.NotificationHelper
import com.aightech.projecttimer.util.PersistenceManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aightech.projecttimer.model.ProjectViewModel
import android.util.Log
import com.aightech.projecttimer.model.SessionViewModel
import com.aightech.projecttimer.model.TimerMechanism
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

// import androidx.lifecycle.viewmodel.compose.viewModel // Duplicate import noted
class TimerMechanismFactory(
     private val application: Application,
     private val sessionViewModel: SessionViewModel,
     private val context: Context
 ) : ViewModelProvider.Factory {
     override fun <T : ViewModel> create(modelClass: Class<T>): T {
         Log.d("TimerMechanismFactory", "Creating TimerMechanism with SessionViewModel hash: ${sessionViewModel.hashCode()}")
         if (modelClass.isAssignableFrom(TimerMechanism::class.java)) {
             @Suppress("UNCHECKED_CAST")
             return TimerMechanism(application, sessionViewModel, context) as T
         }
         throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
     }
 }
class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("MainActivity", "POST_NOTIFICATIONS permission granted.")
            } else {
                Log.d("MainActivity", "POST_NOTIFICATIONS permission denied.")
            }
        }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PersistenceManager.init(this)
        NotificationHelper.createChannel(this)
        askNotificationPermission()
        val context = this


        setContent {
            val projectVM: ProjectViewModel = viewModel()
            // Hoist SessionViewModel and TimerMechanism
            val sessionVM: SessionViewModel = viewModel()
            val timerMechanism: TimerMechanism = viewModel(
                factory = TimerMechanismFactory(application, sessionVM,context),
            )

            val navController = rememberNavController()

            var showWelcome by remember { mutableStateOf(true) }

            if (showWelcome) {
                WelcomePage(onGetStarted = {
                    showWelcome = false
                })
            } else {
                Scaffold(
                    bottomBar = { BottomNav(navController) }
                ) { inner ->
                    NavHost(
                        navController = navController,
                        startDestination = "projects",
                        modifier = Modifier.padding(inner)
                    ) {
                        composable("projects") {
                            ProjectsScreen(
                                navController = navController,
                                projectViewModel = projectVM,
                                sessionViewModel = sessionVM,     // Pass hoisted SessionViewModel
                                timerMechanism = timerMechanism,   // Pass hoisted TimerMechanism
                            )
                        }
                        composable("project_edit/{projectId}") { backStack ->
                            val id = backStack.arguments?.getString("projectId") ?: "new"
                            ProjectEditScreen(projectId = id, navController, projectVM)
                        }
                        composable("sessions") {
                            SessionsScreen(
                                navController = navController,
                                sessionViewModel = sessionVM,    // Pass hoisted SessionViewModel
                                projectViewModel = projectVM
                            )
                        }
                        composable("session_edit/{sessionId}") { backStack ->
                            val id = backStack.arguments?.getString("sessionId") ?: "new"
                            // SessionEditScreen(navController, sessionId = id)
                        }
                        composable("settings") { SettingsPage() }
                    }
                }
            }
        }
    }
}
