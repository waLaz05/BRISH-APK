package com.katchy.focuslive.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.currentState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.katchy.focuslive.MainActivity
import com.katchy.focuslive.R

class StreakWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val streak = prefs[intPreferencesKey("current_streak")] ?: 0
            
            // Duolingo Style Theme
            // Orange/Red Gradient simulation using Box background color (Glance doesn't support Gradients easily yet without xml)
            // We'll use a solid vibrant Orange for now.
            
            GlanceTheme {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(ColorProvider(Color(0xFFF97316))) // Orange-500
                        .clickable(actionStartActivity<MainActivity>())
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Left Side: Fire + Streak
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                             Image(
                                provider = ImageProvider(R.drawable.ic_fire_streak),
                                contentDescription = "Fire",
                                modifier = GlanceModifier.size(40.dp)
                            )
                            Text(
                                text = "$streak",
                                style = TextStyle(
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorProvider(Color.White)
                                )
                            )
                            Text(
                                text = "días",
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ColorProvider(Color.White.copy(alpha = 0.9f))
                                )
                            )
                        }
                        
                        Spacer(modifier = GlanceModifier.width(24.dp))
                        
                        // Right Side: Mascot (Placeholder)
                        // Using app icon as mascot placeholder
                        Image(
                            provider = ImageProvider(R.mipmap.ic_launcher),
                            contentDescription = "Mascot",
                            modifier = GlanceModifier.size(80.dp)
                        )
                    }
                    
                    // Bottom Text
                    Box(
                        modifier = GlanceModifier.fillMaxSize(),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                         Text(
                            text = "¡Estás de vuelta!",
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorProvider(Color.White)
                            )
                        )
                    }
                }
            }
        }
    }
}
