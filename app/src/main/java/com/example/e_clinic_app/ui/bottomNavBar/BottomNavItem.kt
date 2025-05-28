package com.example.e_clinic_app.ui.bottomNavBar

import androidx.compose.ui.graphics.vector.ImageVector
/**
 * A data class representing an item in the Bottom Navigation Bar.
 *
 * Each item includes a label, an icon, and a route for navigation.
 *
 * @property label The text label displayed for the navigation item.
 * @property icon The icon associated with the navigation item.
 * @property route The navigation route corresponding to the item.
 */
data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)
