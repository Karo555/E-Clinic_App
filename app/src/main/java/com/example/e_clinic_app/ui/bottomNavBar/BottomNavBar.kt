package com.example.e_clinic_app.ui.bottomNavBar

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.e_clinic_app.ui.chat.ChatTabScreen
import com.example.e_clinic_app.ui.home.HomeTabScreen
import com.example.e_clinic_app.ui.navigation.Routes
import com.example.e_clinic_app.ui.settings.SettingsTabScreen
//TODO: Change the logic of the bottom navigation bar to use the same navController as the main activity


@Composable
fun BottomNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    BottomAppBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = currentRoute == "home",
            onClick = { navController.navigate(Routes.HOME) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chat") },
            label = { Text("Chat") },
            selected = currentRoute == "chat",
            onClick = { navController.navigate(Routes.CHAT_TAB) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") },
            selected = currentRoute == "settings",
            onClick = { navController.navigate(Routes.SETTINGS_TAB) }
        )
    }

}