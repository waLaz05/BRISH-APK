package com.katchy.focuslive.ui.main

import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.scaleOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.katchy.focuslive.ui.home.HomeScreen
import com.katchy.focuslive.ui.navigation.Screen
import com.katchy.focuslive.ui.navigation.bottomNavItems
import com.katchy.focuslive.ui.theme.AntiFlashWhite
import com.katchy.focuslive.ui.theme.AntiPrimary

import androidx.compose.animation.scaleIn
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.EaseOutBack

@Composable
fun MainScreen(
    initialDestination: String? = null,
    isGuest: Boolean = false,
    onNavigateToLogin: () -> Unit = {},
    viewModel: MainViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Listen for deep link events
    val deepLinkDestination by com.katchy.focuslive.NavigationBus.destination.collectAsState(initial = null)
    
    val isPlannerEnabled by viewModel.isPlannerEnabled.collectAsState()
    val isNotesEnabled by viewModel.isNotesEnabled.collectAsState()
    val isFinanceEnabled by viewModel.isFinanceEnabled.collectAsState()
    val isHabitsEnabled by viewModel.isHabitsEnabled.collectAsState()

    val activeBottomNavItems = remember(isPlannerEnabled, isNotesEnabled, isFinanceEnabled, isHabitsEnabled) {
        mutableListOf<Screen>(Screen.Home).apply {
            if (isPlannerEnabled) add(Screen.Planner)
            if (isNotesEnabled) add(Screen.Notes)
            if (isHabitsEnabled) add(Screen.Habits)
            if (isFinanceEnabled) add(Screen.Finance)
        }
    }
    
    LaunchedEffect(deepLinkDestination) {
        deepLinkDestination?.let { dest ->
            try {
                // Determine if destination is a top-level tab
                val isTab = when(dest) {
                    Screen.Home.route, Screen.Planner.route, Screen.Notes.route, Screen.Habits.route, Screen.Finance.route -> true
                    else -> false
                }
                
                navController.navigate(dest) {
                    // If it's a tab, pop up to start to mimic tab switching
                    if (isTab) {
                         popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            } catch (e: Exception) {
                // Route might not exist or other nav error
                e.printStackTrace()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        NavHost(
            navController = navController,
            startDestination = initialDestination ?: Screen.Home.route,
            modifier = Modifier.fillMaxSize()
        ) {
            // iOS-Style Transitions
            val tabEnter = fadeIn(tween(300)) + scaleIn(initialScale = 0.98f, animationSpec = tween(300, easing = androidx.compose.animation.core.EaseOutQuart))
            val tabExit = fadeOut(tween(200))

            // Nested Screen Transitions (iOS Slide)
            val nestedEnter = slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(400, easing = androidx.compose.animation.core.EaseOutQuart))
            val nestedExit = slideOutHorizontally(targetOffsetX = { -it / 3 }, animationSpec = tween(400, easing = androidx.compose.animation.core.EaseOutQuart))
            val nestedPopEnter = slideInHorizontally(initialOffsetX = { -it / 3 }, animationSpec = tween(400, easing = androidx.compose.animation.core.EaseOutQuart))
            val nestedPopExit = slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(400, easing = androidx.compose.animation.core.EaseOutQuart))

            composable(
                route = Screen.Home.route,
                enterTransition = { tabEnter },
                exitTransition = { tabExit },
                popEnterTransition = { tabEnter },
                popExitTransition = { tabExit }
            ) {
                HomeScreen(
                    isGuest = isGuest,
                    onNavigateToLogin = onNavigateToLogin,
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToFocus = { navController.navigate(Screen.FocusMode.route) },
                    onNavigateToMascot = { navController.navigate(Screen.MascotSelection.route) }
                )
            }
            composable(
                route = Screen.Planner.route,
                enterTransition = { tabEnter },
                exitTransition = { tabExit },
                popEnterTransition = { tabEnter },
                popExitTransition = { tabExit }
            ) {
                com.katchy.focuslive.ui.planner.PlannerScreen()
            }
            composable(
                route = Screen.Notes.route,
                enterTransition = { tabEnter },
                exitTransition = { tabExit },
                popEnterTransition = { tabEnter },
                popExitTransition = { tabExit }
            ) {
                com.katchy.focuslive.ui.notes.NotesScreen()
            }
            composable(
                route = Screen.Habits.route,
                enterTransition = { tabEnter },
                exitTransition = { tabExit },
                popEnterTransition = { tabEnter },
                popExitTransition = { tabExit }
            ) { 
                com.katchy.focuslive.ui.habits.HabitsScreen() 
            }

            composable(
                route = Screen.Finance.route,
                enterTransition = { tabEnter },
                exitTransition = { tabExit },
                popEnterTransition = { tabEnter },
                popExitTransition = { tabExit }
            ) { 
                com.katchy.focuslive.ui.finance.FinanceScreen() 
            }
            
            // Setting: Slide in from Right
            composable(
                route = Screen.Settings.route,
                enterTransition = { nestedEnter },
                exitTransition = { nestedExit },
                popEnterTransition = { nestedPopEnter },
                popExitTransition = { nestedPopExit }            ) { 
                com.katchy.focuslive.ui.settings.SettingsScreen(
                    onNavigateToLogin = onNavigateToLogin,
                    onBack = { navController.popBackStack() }
                ) 
            }
            
            // Focus Mode: iPhone-style Scale/Expand (Window Open Effect)
            composable(
                route = Screen.FocusMode.route,
                enterTransition = { 
                    scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(400, easing = androidx.compose.animation.core.EaseOutExpo)
                    ) + fadeIn(animationSpec = tween(300))
                },
                exitTransition = { 
                    scaleOut(
                        targetScale = 0.92f,
                        animationSpec = tween(400, easing = androidx.compose.animation.core.EaseOutExpo)
                    ) + fadeOut(animationSpec = tween(300))
                },
                popEnterTransition = { 
                    scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(400, easing = androidx.compose.animation.core.EaseOutExpo)
                    ) + fadeIn(animationSpec = tween(300))
                },
                popExitTransition = { 
                    scaleOut(
                        targetScale = 0.92f,
                        animationSpec = tween(400, easing = androidx.compose.animation.core.EaseOutExpo)
                    ) + fadeOut(animationSpec = tween(300))
                }
            ) {
                com.katchy.focuslive.ui.focus.FocusSessionScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Mascot Selection: Modern Sheet-like slide up
            composable(
                route = Screen.MascotSelection.route,
                enterTransition = { nestedEnter },
                exitTransition = { nestedExit },
                popEnterTransition = { nestedPopEnter },
                popExitTransition = { nestedPopExit }            ) {
                com.katchy.focuslive.ui.components.MascotSelectionScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }

        // Floating Bottom Nav
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp)
                .padding(bottom = 16.dp)
                .fillMaxWidth()
                .height(80.dp)
                .shadow(
                    elevation = 24.dp, 
                    shape = RoundedCornerShape(28.dp),
                    spotColor = Color(0x40000000),
                    ambientColor = Color(0x20000000)
                ),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(28.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                activeBottomNavItems.forEach { screen ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    
                    val animatedBackgroundColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent,
                        label = "navItemBg"
                    )
                    
                    val animatedIconScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.2f else 1f,
                        label = "navItemScale",
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )

                    val animatedIconColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        label = "navItemColor"
                    )

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(animatedBackgroundColor)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null // Remove default ripple
                            ) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = screen.icon,
                            contentDescription = screen.title,
                            tint = animatedIconColor,
                            modifier = Modifier
                                .size(24.dp)
                                .graphicsLayer {
                                    scaleX = animatedIconScale
                                    scaleY = animatedIconScale
                                }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MaterialTheme.typography.headlineMedium, color = Color.Black)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Próximamente en el Ecosistema Wlaz", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
        }
    }
}
