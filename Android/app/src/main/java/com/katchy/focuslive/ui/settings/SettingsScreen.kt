package com.katchy.focuslive.ui.settings

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.core.net.toUri
import kotlinx.coroutines.delay
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.katchy.focuslive.data.model.AppTheme
import com.katchy.focuslive.ui.theme.*

@Composable
fun SettingsScreen(
    onNavigateToLogin: () -> Unit,
    onBack: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val currentTheme by viewModel.currentTheme.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    
    val isPlannerEnabled by viewModel.isPlannerEnabled.collectAsState()
    val isNotesEnabled by viewModel.isNotesEnabled.collectAsState()
    val isFinanceEnabled by viewModel.isFinanceEnabled.collectAsState()
    val isHabitsEnabled by viewModel.isHabitsEnabled.collectAsState()
    val isGamificationEnabled by viewModel.isGamificationEnabled.collectAsState()

    val isPomodoroSoundEnabled by viewModel.isPomodoroSoundEnabled.collectAsState()
    val currentAccentColor by viewModel.accentColor.collectAsState()
    
    var showColorPicker by remember { mutableStateOf(false) }
    
    var visibleSections by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        visibleSections = 0
        repeat(6) {
            delay(80)
            visibleSections++
        }
    }
    
    val scrollState = rememberScrollState()
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .systemBarsPadding()
            .padding(24.dp)
    ) {
        // --- Header ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "AJUSTES",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Configuración",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Black
                )
            }
            
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack, 
                    contentDescription = "Volver",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // --- Profile Section ---
        // --- Profile Section ---
        AnimatableSection(
            visible = visibleSections >= 1
        ) {
            Column {
                Text(
                    text = "PERFIL",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )
                
                SettingsGroup {
                     Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { /* Edit Profile */ }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(AntiPrimary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user?.email?.take(1)?.uppercase() ?: "U",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = AntiPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = user?.displayName ?: "Usuario",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                             Text(
                                text = user?.email ?: "correo@ejemplo.com",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowForwardIos,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.5f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Appearance Section ---
        AnimatableSection(
            visible = visibleSections >= 2
        ) {
            Column {
                Text(
                    text = "APARIENCIA",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

                SettingsGroup {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ThemeOptionCard(
                                title = "Claro",
                                icon = Icons.Rounded.LightMode,
                                selected = currentTheme == AppTheme.LIGHT,
                                onClick = { 
                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                    viewModel.setTheme(AppTheme.LIGHT) 
                                },
                                modifier = Modifier.weight(1f)
                            )
                            ThemeOptionCard(
                                title = "Oscuro",
                                icon = Icons.Rounded.DarkMode,
                                selected = currentTheme == AppTheme.DARK,
                                onClick = { 
                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                    viewModel.setTheme(AppTheme.DARK) 
                                },
                                modifier = Modifier.weight(1f)
                            )
                            ThemeOptionCard(
                                title = "Sistema",
                                icon = Icons.Rounded.SettingsBrightness,
                                selected = currentTheme == AppTheme.SYSTEM,
                                onClick = { 
                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                    viewModel.setTheme(AppTheme.SYSTEM) 
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.2f))
                        
                        SettingsItem(
                            icon = Icons.Rounded.Palette,
                            title = "Color de acento",
                            iconTint = Color(currentAccentColor),
                            onClick = { showColorPicker = true }
                        ) {
                             Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(currentAccentColor))
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                            )
                        }   
                        
                        if (showColorPicker) {
                            ColorPickerDialog(
                                currentColor = currentAccentColor,
                                onDismiss = { showColorPicker = false },
                                onColorSelected = { 
                                    viewModel.setAccentColor(it)
                                    showColorPicker = false
                                }
                            )
                        }   
                        

                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Preferences Section ---
        AnimatableSection(
            visible = visibleSections >= 3
        ) {
            Column {
                Text(
                    text = "PREFERENCIAS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

                SettingsGroup {
                     Column {
                        SettingsItem(
                            icon = Icons.Rounded.Notifications,
                            title = "Notificaciones push",
                            iconTint = Color(0xFF3B82F6),
                            showDivider = true
                        ) {
                            Switch(
                                checked = true,
                                onCheckedChange = { },
                                thumbContent = { Icon(Icons.Rounded.Check, null) }
                            )
                        }
                        
                        SettingsItem(
                            icon = Icons.Rounded.Timer,
                            title = "Sonido de Pomodoro",
                            iconTint = Color(0xFFF97316),
                            showDivider = false
                        ) {
                            Switch(
                                checked = isPomodoroSoundEnabled,
                                onCheckedChange = { viewModel.setPomodoroSoundEnabled(it) }
                            )
                        }
                     }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Modules Section ---
        AnimatableSection(
            visible = visibleSections >= 4
        ) {
            Column {
                Text(
                    text = "MÓDULOS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )
                
                SettingsGroup {
                    Column {
                        SettingsItem(
                            icon = Icons.Rounded.Event,
                            title = "Agenda (Planner)",
                            iconTint = Color(0xFF3B82F6),
                            showDivider = true
                        ) {
                            Switch(
                                checked = isPlannerEnabled,
                                onCheckedChange = { viewModel.setPlannerEnabled(it) }
                            )
                        }
                        SettingsItem(
                            icon = Icons.Rounded.NoteAlt,
                            title = "Notas",
                            iconTint = Color(0xFF8B5CF6),
                            showDivider = true
                        ) {
                            Switch(
                                checked = isNotesEnabled,
                                onCheckedChange = { viewModel.setNotesEnabled(it) }
                            )
                        }
                        SettingsItem(
                            icon = Icons.Rounded.MonitorHeart,
                            title = "Hábitos",
                            iconTint = Color(0xFFF59E0B),
                            showDivider = true
                        ) {
                            Switch(
                                checked = isHabitsEnabled,
                                onCheckedChange = { viewModel.setHabitsEnabled(it) }
                            )
                        }
                        SettingsItem(
                            icon = Icons.Rounded.Payments,
                            title = "Finanzas",
                            iconTint = Color(0xFF10B981),
                            showDivider = true
                        ) {
                            Switch(
                                checked = isFinanceEnabled,
                                onCheckedChange = { viewModel.setFinanceEnabled(it) }
                            )
                        }
                        SettingsItem(
                            icon = Icons.Rounded.Star,
                            title = "Gamificación (XP/Nivel)",
                            iconTint = Color(0xFFFFD700),
                            showDivider = false
                        ) {
                            Switch(
                                checked = isGamificationEnabled,
                                onCheckedChange = { viewModel.setGamificationEnabled(it) }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Application Section ---
        AnimatableSection(
            visible = visibleSections >= 5
        ) {
            Column {
                Text(
                    text = "APLICACIÓN",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

                SettingsGroup {
                    Column {
                        SettingsItem(
                            icon = null,
                            title = "Versión",
                            subtitle = "2.4.0 (Build 102)",
                            showDivider = true
                        )
                        
                        SettingsItem(
                            icon = null,
                            title = "Soporte y Ayuda",
                            onClick = {
                                try {
                                    val url = "https://wa.me/966552520"
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
                                    intent.data = url.toUri()
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                }
                            },
                            showDivider = false
                        ) {
                            Icon(
                                Icons.AutoMirrored.Rounded.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.5f)
                            )
                        }
                    }
                }

                 Spacer(modifier = Modifier.height(32.dp))

                // Logout Button
                Button(
                    onClick = {
                        viewModel.logout()
                        onNavigateToLogin()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp)
                ) {
                    Icon(Icons.AutoMirrored.Rounded.Logout, null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cerrar sesión", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(120.dp))
    }
}

@Composable
fun SettingsGroup(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 0.dp
    ) {
        content()
    }
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector?,
    title: String,
    subtitle: String? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    showDivider: Boolean = false,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (trailing != null) {
                trailing()
            }
        }
        if (showDivider) {
             HorizontalDivider(
                modifier = Modifier.padding(start = if(icon!=null) 68.dp else 16.dp, end = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(0.2f)
            )
        }
    }
}

@Composable
fun ThemeOptionCard(
    title: String, 
    icon: androidx.compose.ui.graphics.vector.ImageVector, 
    selected: Boolean, 
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.05f else 1f,
        label = "themeScale",
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    val containerColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.background,
        label = "themeContainer",
        animationSpec = spring()
    )
    
    val contentColor by animateColorAsState(
        targetValue = if (selected) AntiPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "themeContent",
        animationSpec = spring()
    )

    Surface(
        modifier = modifier
            .height(80.dp)
            .scale(scale)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = containerColor, 
        border = if(selected) androidx.compose.foundation.BorderStroke(2.dp, AntiPrimary) else null,
        shadowElevation = if(selected) 4.dp else 0.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon, 
                null, 
                tint = contentColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if(selected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )
        }
    }
}

@Composable
fun AnimatableSection(
    visible: Boolean,
    content: @Composable () -> Unit
) {
        AnimatedVisibility(
            visible = visible, 
            enter = fadeIn() + slideInVertically { 20 }
        ) {
            content()
        }
}

@Composable
fun ColorPickerDialog(
    currentColor: Int,
    onDismiss: () -> Unit,
    onColorSelected: (Int) -> Unit
) {
    val colors = listOf(
        0xFF4F46E5, // Default Indigo
        0xFF8B5CF6, // Violet
        0xFFAE2C6C, // Magentaish
        0xFFEC4899, // Pink
        0xFFF43F5E, // Rose
        0xFFEF4444, // Red
        0xFFF97316, // Orange
        0xFFF59E0B, // Amber
        0xFF84CC16, // Lime
        0xFF10B981, // Emerald
        0xFF06B6D4, // Cyan
        0xFF0EA5E9, // Sky
        0xFF3B82F6  // Blue
    )

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.width(320.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Elige tu color",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 48.dp),
                    modifier = Modifier.fillMaxWidth().height(240.dp), // Height limit
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(colors) { colorLong ->
                        val colorInt = colorLong.toInt()
                        val isSelected = currentColor == colorInt
                        
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(colorInt))
                                .border(if (isSelected) 3.dp else 0.dp, if (isSelected) Color.White.copy(alpha=0.6f) else Color.Transparent, CircleShape)
                                .clickable { onColorSelected(colorInt) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    Icons.Rounded.Check,
                                    null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                TextButton(onClick = onDismiss) {
                    Text("CANCELAR")
                }
            }
        }
    }
}
