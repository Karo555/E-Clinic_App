package com.example.e_clinic_app.ui.bottomNavBar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.e_clinic_app.ui.navigation.Routes
/**
 * A composable function that represents the Bottom Navigation Bar in the e-clinic application.
 *
 * This navigation bar provides quick access to the Home, Chat, and Settings screens. It highlights
 * the currently selected tab and allows users to navigate between these screens using the provided
 * `NavController`.
 *
 * @param navController The `NavController` used for navigation between screens.
 */
@Composable
fun BottomNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    BottomAppBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = currentRoute == Routes.HOME,
            onClick = {
                if (currentRoute != Routes.HOME) {
                    navController.navigate(Routes.HOME) {
                        launchSingleTop = true
                        popUpTo(navController.graph.startDestinationId) { inclusive = false }
                    }
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chat") },
            label = { Text("Chat") },
            selected = currentRoute == Routes.CHAT_TAB,
            onClick = {
                if (currentRoute != Routes.CHAT_TAB) {
                    navController.navigate(Routes.CHAT_TAB) {
                        launchSingleTop = true
                        popUpTo(navController.graph.startDestinationId) { inclusive = false }
                    }
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") },
            selected = currentRoute == Routes.SETTINGS_TAB,
            onClick = {
                if (currentRoute != Routes.SETTINGS_TAB) {
                    navController.navigate(Routes.SETTINGS_TAB) {
                        launchSingleTop = true
                        popUpTo(navController.graph.startDestinationId) { inclusive = false }
                    }
                }
            }
        )
    }

}