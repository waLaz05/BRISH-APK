package com.katchy.focuslive.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.glance.LocalContext
import dagger.hilt.android.EntryPointAccessors
import com.katchy.focuslive.di.WidgetEntryPoint


import com.katchy.focuslive.data.model.Task as AppTask


import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.width
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.layout.size
import androidx.glance.Image
import androidx.glance.ImageProvider
import com.katchy.focuslive.R

class TasksWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val appContext = LocalContext.current.applicationContext
            val entryPoint = EntryPointAccessors.fromApplication(appContext, WidgetEntryPoint::class.java)
            val repository = entryPoint.taskRepository()
            
            val tasks by repository.getTasksForDate(System.currentTimeMillis()).collectAsState(initial = emptyList())

            GlanceTheme {
                TasksContent(tasks)
            }
        }
    }

    @Composable
    private fun TasksContent(tasks: List<AppTask>) {
        // Aesthetic Palette (Premium Tasks)
        val bgColor = Color(0xFFF8FAFC) // Slate-50 (Clean, professional background)
        val primaryColor = Color(0xFF4F46E5) // Indigo-600 (Vibrant accent)
        val textColor = Color(0xFF1E293B) // Slate-800 (Deep contrast)
        val secondaryTextColor = Color(0xFF64748B) // Slate-500

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(bgColor)
                .appWidgetBackground()
                .cornerRadius(24.dp)
                .padding(20.dp)
        ) {
            Column(modifier = GlanceModifier.fillMaxSize()) {
                // Header Region
                Row(
                    modifier = GlanceModifier.fillMaxWidth().clickable(
                         actionStartActivity<com.katchy.focuslive.MainActivity>(
                            actionParametersOf(ActionParameters.Key<String>("destination") to "planner")
                        )
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mi Día",
                        style = TextStyle(
                            color = androidx.glance.unit.ColorProvider(textColor),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    )
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    // Task Count Bubble
                    if (tasks.isNotEmpty()) {
                        Box(
                            modifier = GlanceModifier
                                .background(primaryColor.copy(alpha = 0.1f))
                                .cornerRadius(12.dp)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${tasks.count { !it.isCompleted }}",
                                style = TextStyle(
                                    color = androidx.glance.unit.ColorProvider(primaryColor),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }
                
                Spacer(modifier = GlanceModifier.height(16.dp))
                
                // Content Region
                if (tasks.isEmpty()) {
                    Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                         Text(
                            text = "¡Día despejado! ☀️", 
                            style = TextStyle(
                                color = androidx.glance.unit.ColorProvider(secondaryTextColor),
                                fontSize = 14.sp
                            )
                        )
                    }
                } else {
                    Column(modifier = GlanceModifier.fillMaxWidth()) {
                        tasks.take(5).forEach { task ->
                            TaskItem(task, primaryColor, textColor, secondaryTextColor)
                            Spacer(modifier = GlanceModifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
    
    @Composable
    private fun TaskItem(
        task: AppTask, 
        accentColor: Color, 
        titleColor: Color, 
        subtitleColor: Color
    ) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(if (task.isCompleted) Color.Transparent else Color.White)
                .cornerRadius(12.dp)
                .padding(12.dp)
                .clickable(actionRunCallback<ToggleTaskAction>(actionParametersOf(ToggleTaskAction.taskIdKey to task.id))),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox Icon logic
            val checkColor = if (task.isCompleted) accentColor.copy(alpha = 0.5f) else accentColor
            
            Box(
                modifier = GlanceModifier
                    .size(20.dp)
                    .cornerRadius(6.dp)
                    .background(if (task.isCompleted) checkColor else Color.Transparent)
                    .padding(2.dp)
            ) {
                 if (task.isCompleted) {
                     // Simple checkmark visual using text as fallback for simplicity in Widget
                     Text("✓", style = TextStyle(color = androidx.glance.unit.ColorProvider(Color.White), fontWeight = FontWeight.Bold, fontSize = 14.sp))
                 } else {
                     // Empty box border simulation
                     Box(modifier = GlanceModifier.fillMaxSize().background(accentColor.copy(alpha=0.1f)).cornerRadius(4.dp)) {}
                 }
            }
            
            Spacer(modifier = GlanceModifier.width(12.dp))
            
            Text(
                text = task.title,
                style = TextStyle(
                    color = androidx.glance.unit.ColorProvider(if (task.isCompleted) subtitleColor else titleColor),
                    fontSize = 14.sp,
                    fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.Medium,
                    textDecoration = if (task.isCompleted) androidx.glance.text.TextDecoration.LineThrough else androidx.glance.text.TextDecoration.None
                ),
                maxLines = 1
            )
        }
    }
}

class ToggleTaskAction : ActionCallback {
    companion object {
        val taskIdKey = ActionParameters.Key<String>("taskId")
    }

    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val taskId = parameters[taskIdKey] ?: return
        
        val appContext = context.applicationContext
        val entryPoint = EntryPointAccessors.fromApplication(appContext, WidgetEntryPoint::class.java)
        val repository = entryPoint.taskRepository()
        
        repository.toggleTaskCompletion(taskId)
        
        // Force update of widget
        TasksWidget().update(context, glanceId)
    }
}

