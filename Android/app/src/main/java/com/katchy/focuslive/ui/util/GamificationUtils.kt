package com.katchy.focuslive.ui.util

import androidx.compose.ui.graphics.Color

object GamificationUtils {

    fun getLevelTitle(level: Int): String {
        return when (level) {
            in 1..4 -> "Iniciado"
            in 5..9 -> "Aprendiz"
            in 10..19 -> "Especialista"
            in 20..49 -> "Experto"
            in 50..99 -> "Maestro"
            else -> "Leyenda"
        }
    }

    fun getLevelColor(level: Int): Color {
        return when (level) {
            in 1..4 -> Color(0xFF4CAF50) // Green
            in 5..9 -> Color(0xFF2196F3) // Blue
            in 10..19 -> Color(0xFF9C27B0) // Purple
            in 20..49 -> Color(0xFFFF9800) // Orange
            else -> Color(0xFFFFD700) // Gold
        }
    }
}
