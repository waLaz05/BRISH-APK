package com.katchy.focuslive.util

object GamificationLogic {
    
    // Badge IDs
    const val BADGE_EARLY_BIRD = "EARLY_BIRD" // Task before 7 AM
    const val BADGE_FINANCIER = "MASTER_FINANCIER" // 30 transactions
    const val BADGE_STREAK_3 = "STREAK_3"
    const val BADGE_STREAK_7 = "STREAK_7"
    const val BADGE_STREAK_30 = "STREAK_30"

    fun getBadgeIcon(badgeId: String): String {
        return when(badgeId) {
            BADGE_EARLY_BIRD -> "🌅"
            BADGE_FINANCIER -> "💰"
            BADGE_STREAK_3 -> "🔥"
            BADGE_STREAK_7 -> "🔥"
            BADGE_STREAK_30 -> "👺"
            else -> "🎖️"
        }
    }
    
    fun getBadgeName(badgeId: String): String {
        return when(badgeId) {
            BADGE_EARLY_BIRD -> "El Madrugador"
            BADGE_FINANCIER -> "Financiero Maestro"
            BADGE_STREAK_3 -> "En Racha (3)"
            BADGE_STREAK_7 -> "Imparable (7)"
            BADGE_STREAK_30 -> "Leyenda (30)"
            else -> "Insignia"
        }
    }
}
