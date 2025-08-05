// ui/Navigation.kt
package com.aightech.projecttimer.ui

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination // Keep this
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings

@Composable
fun BottomNav(nav: NavController) {
    val items: List<Pair<String, ImageVector>> = listOf(
        "projects" to Icons.Filled.List,
        "sessions" to Icons.Filled.CalendarToday,
        "settings" to Icons.Filled.Settings
    )
    val navBackStackEntry by nav.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        items.forEach { (route, icon) ->
            NavigationBarItem(
                icon = { Icon(icon, contentDescription = null) },
                selected = currentDestination?.route == route,
                onClick = {
                    nav.navigate(route) {
                        // This should now correctly pop up to "projects" (or whatever the start
                        // destination of the main NavHost is)
                        popUpTo(nav.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}