package com.example.e_clinic_app.ui.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons.Default
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material3.Icon
import androidx.compose.material3.Text


@Preview(showBackground = true)
@Composable
fun BottomNavItemPreview() {
    val items = listOf(
        BottomNavItem("Home", Default.Home, "home"),
        BottomNavItem("Chat", Icons.AutoMirrored.Filled.Chat, "chat"),
        BottomNavItem("Settings", Default.Settings, "settings")
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    NavigationBarItem(
                        selected = false,
                        onClick = { /* No-op for preview */ },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        // Content above nav bar (empty for now)
        Box(modifier = Modifier.padding(innerPadding))
    }
}
