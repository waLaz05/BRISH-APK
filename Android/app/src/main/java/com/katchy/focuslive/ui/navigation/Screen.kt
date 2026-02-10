package com.katchy.focuslive.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home_panel", "Hub", Icons.Rounded.GridView)
    object Planner : Screen("planner", "Planner", Icons.Rounded.Event) // Daily Tasks
    object Notes : Screen("notes", "Notas", Icons.Rounded.NoteAlt)
    object Habits : Screen("habits", "Hábitos", Icons.Rounded.MonitorHeart)
    object Finance : Screen("finance", "Finanzas", Icons.Rounded.Payments)

    object Settings : Screen("settings", "Ajustes", Icons.Rounded.Settings)
    object FocusMode : Screen("focus_mode", "Enfoque", Icons.Rounded.Fullscreen)
    object MascotSelection : Screen("mascot_selection", "Colección", Icons.Rounded.Face)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Planner,
    Screen.Notes,
    Screen.Habits,
    Screen.Finance
)
