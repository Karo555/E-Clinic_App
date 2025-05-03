package com.example.e_clinic_app.ui.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.e_clinic_app.backend.home.DoctorHomeViewModel
import com.example.e_clinic_app.ui.chat.ChatTabScreen
import com.example.e_clinic_app.ui.home.doctor.DoctorHomeTabScreen
import com.example.e_clinic_app.ui.settings.SettingsTabScreen

@Composable
fun MainScreen(navController: NavController, viewModel: DoctorHomeViewModel) {
    val internalNavController = rememberNavController()

    val bottomNavItems = listOf(
        BottomNavItem("Home", Icons.Default.Home, "home"),
        BottomNavItem("Chat", Icons.AutoMirrored.Filled.Chat, "chat"),
        BottomNavItem("Settings", Icons.Default.Settings, "settings")
    )

    val currentDestination by internalNavController.currentBackStackEntryAsState()
    val currentRoute = currentDestination?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentRoute == item.route,
                        onClick = {
                            internalNavController.navigate(item.route) {
                                popUpTo(internalNavController.graph.findStartDestination().route!!) {
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
    ) { innerPadding ->
        NavHost(
            navController = internalNavController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") { DoctorHomeTabScreen(navController,viewModel) }
            composable("chat") { ChatTabScreen() }
            composable("settings") { SettingsTabScreen(navController) }
        }
    }
}