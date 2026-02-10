package com.katchy.focuslive.data.model

enum class AppTheme(val displayName: String) {
    LIGHT("Claro"),
    DARK("Oscuro"),
    SYSTEM("Sistema");

    companion object {
        fun fromName(name: String): AppTheme {
            return entries.find { it.name == name } ?: LIGHT
        }
    }
}
