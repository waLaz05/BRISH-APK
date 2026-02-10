package com.katchy.focuslive.data.model

import androidx.compose.ui.graphics.Color

enum class MascotType(
    val id: String,
    val displayName: String,
    val role: String,
    val quote: String,
    val description: String,
    val accentColor: Color
) {
    POPPIN(
        id = "poppin",
        displayName = "Poppin",
        role = "EL COACH TÓXICO",
        quote = "\"¡Deja de ser gil y chapa tu ritmo!\"",
        description = "Te grita (con cariño) para que dejes de procastinar. Full pilas y directo a la yugular.",
        accentColor = Color(0xFFEF4444) // Red
    ),
    KITTY(
        id = "kitty",
        displayName = "Kitty",
        role = "LA PATRONA",
        quote = "\"Resultados quiero, no floro.\"",
        description = "Estricta, directa y sin pelos en la lengua. Si te relajas, te cae. La única mujer del grupo.",
        accentColor = Color(0xFF9CA3AF) // Gray
    ),
    PACO(
        id = "paco",
        displayName = "Paco",
        role = "EL PATA LEAL",
        quote = "\"Vamos con fe, causa. Tú eres.\"",
        description = "Tu brother incondicional. Siempre te apoya y te sube la moral cuando estás bajoneado.",
        accentColor = Color(0xFFF97316) // Orange
    ),
    PANDA(
        id = "panda",
        displayName = "Panda",
        role = "EL MAESTRO RELAX",
        quote = "\"Llévala suave, sin palta.\"",
        description = "Te enseña a chambear sin estresarte. Full vibras positivas y cero dramas.",
        accentColor = Color(0xFF1F2937) // Dark/Black
    ),
    CEBRIC(
        id = "cebric",
        displayName = "Cebric",
        role = "EL VISIONARIO",
        quote = "\"¡Alucina esa idea, bravazo!\"",
        description = "El creativo del grupo. Siempre pensando fuera de la caja y soñando en grande.",
        accentColor = Color(0xFF4ADE80) // Green
    ),
    TURTLE(
        id = "turtle",
        displayName = "Torty",
        role = "EL TÍO SABIO",
        quote = "\"Despacio se llega lejos, sobrino.\"",
        description = "Lento pero seguro. Te da consejos de viejo zorro para que no metas la pata.",
        accentColor = Color(0xFF0D9488) // Teal
    ),
    LLAMA(
        id = "llama",
        displayName = "Kuzco",
        role = "EL CAUSA",
        quote = "\"¡Habla barrio! ¡Al toque roque!\"",
        description = "El alma de la fiesta. Full jerga, full pilas y 100% peruano. Te pone las pilas en una.",
        accentColor = Color(0xFFD97706) // Amber/Brown
    )
}
