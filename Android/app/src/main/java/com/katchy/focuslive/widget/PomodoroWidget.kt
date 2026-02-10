package com.katchy.focuslive.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.katchy.focuslive.R

import androidx.glance.currentState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import android.content.Intent
import com.katchy.focuslive.service.TimerService

class PomodoroWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val timeLeft = prefs[stringPreferencesKey("time_left")] ?: "25:00"
            val isRunning = prefs[booleanPreferencesKey("is_running")] ?: false
            val status = prefs[stringPreferencesKey("status")] ?: "Modo Enfoque"

            GlanceTheme {
                PomodoroContent(timeLeft, isRunning, status)
            }
        }
    }

    @Composable
    private fun PomodoroContent(timeLeft: String, isRunning: Boolean, status: String) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color(0xFF111827)) // Rich Dark Background
                .clickable(
                    actionStartActivity<com.katchy.focuslive.MainActivity>(
                        actionParametersOf(ActionParameters.Key<String>("destination") to "focus")
                    )
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = status.uppercase(),
                    style = TextStyle(
                        color = androidx.glance.unit.ColorProvider(Color.White.copy(alpha = 0.6f)),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
                
                Spacer(modifier = GlanceModifier.height(4.dp))
                
                Text(
                    text = timeLeft,
                    style = TextStyle(
                        color = androidx.glance.unit.ColorProvider(Color.White),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                
                Spacer(modifier = GlanceModifier.height(16.dp))
                
                // Minimal Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically 
                ) {
                    // Reuse action icons if possible, or text buttons as fallback
                     Image(
                        provider = ImageProvider(if(isRunning) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play),
                        contentDescription = "Toggle",
                        modifier = GlanceModifier
                            .size(48.dp)
                            .background(androidx.glance.unit.ColorProvider(Color.White))
                            .clickable(actionRunCallback<ToggleTimerAction>())
                            .padding(12.dp)
                    )
                }
            }
        }
    }
}


class ToggleTimerAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val intent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_TOGGLE_TIMER
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }
}

class ResetTimerAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val intent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_RESET_TIMER
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }
}
