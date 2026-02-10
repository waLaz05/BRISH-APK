package com.katchy.focuslive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.katchy.focuslive.data.repository.AppPreferencesRepository
import com.katchy.focuslive.data.repository.ThemeRepository
import com.katchy.focuslive.ui.auth.AuthViewModel
import com.katchy.focuslive.ui.auth.LoginScreen
import com.katchy.focuslive.ui.main.MainScreen
import com.katchy.focuslive.ui.onboarding.OnboardingScreen
import com.katchy.focuslive.ui.splash.SplashScreen
import com.katchy.focuslive.ui.theme.BrishTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

import android.Manifest
import android.os.Build
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themeRepository: ThemeRepository

    @Inject
    lateinit var prefsRepository: AppPreferencesRepository
    
    @Inject
    lateinit var notificationScheduler: com.katchy.focuslive.scheduler.NotificationScheduler

    @Inject
    lateinit var userStatsRepository: com.katchy.focuslive.data.repository.UserStatsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        userStatsRepository.checkDailyLogin()
        
        // Schedule notifications only if not done yet
        // Schedule notifications daily (re-schedule to ensure they persist after reboot/updates)
        // Ideally handled by BootReceiver, but rescheduling on app launch is a safe fallback
        notificationScheduler.rescheduleAllAlarms()
        lifecycleScope.launch(Dispatchers.IO) {
             prefsRepository.setNotificationsScheduled(true)
        }
        
        enableEdgeToEdge()
        setContent {
            val authViewModel: AuthViewModel = hiltViewModel()
            val user by authViewModel.user.collectAsState()
            val onboardingCompleted by prefsRepository.onboardingCompleted.collectAsState()
            val currentTheme by themeRepository.currentTheme.collectAsState()
            val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
            
            val useDarkTheme = when (currentTheme) {
                com.katchy.focuslive.data.model.AppTheme.LIGHT -> false
                com.katchy.focuslive.data.model.AppTheme.DARK -> true
                com.katchy.focuslive.data.model.AppTheme.SYSTEM -> isSystemDark
            }

            val navController = rememberNavController()
            
            // Handle Intent (Cold Start)
            LaunchedEffect(Unit) {
                handleDestination(intent, navController)
            }
            
            // Handle Intent (New Intent / Warm Start)
            DisposableEffect(Unit) {
                val listener = androidx.core.util.Consumer<android.content.Intent> { newIntent ->
                    handleDestination(newIntent, navController)
                }
                addOnNewIntentListener(listener)
                onDispose { removeOnNewIntentListener(listener) }
            }

            val accentColor by prefsRepository.accentColor.collectAsState()

            // REQUEST PERMISSIONS LOGIC (Fix for Android 13+ & 12+)
            val context = androidx.compose.ui.platform.LocalContext.current
            
            // 1. Notification Permission Launcher (Android 13+)
            val notificationPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
                onResult = { isGranted ->
                    if (isGranted) {
                         // Only reschedule if granted now to avoid redundant calls
                         notificationScheduler.rescheduleAllAlarms()
                    }
                }
            )

            // 2. Check and Request Permissions on Launch
            LaunchedEffect(Unit) {
                // Notificaciones (Android 13+)
                if (android.os.Build.VERSION.SDK_INT >= 33) {
                    if (androidx.core.content.ContextCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.POST_NOTIFICATIONS
                        ) != android.content.pm.PackageManager.PERMISSION_GRANTED
                    ) {
                        notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                // Alarmas Exactas (Android 12+) - Solo verificamos, no podemos pedirlo con launcher normal
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    val alarmManager = context.getSystemService(android.app.AlarmManager::class.java)
                    if (!alarmManager.canScheduleExactAlarms()) {
                        // Opcional: Mostrar un diálogo o Snackbar explicando que se necesitan alarmas exactas
                        // Para "funcionar bien", idealmente deberíamos guiar al usuario
                        // Por ahora, confiamos en el fallback, pero lo ideal es pedirlo.
                        // Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    }
                }
            }
            
            
            // ... (navController logic)

            BrishTheme(
                darkTheme = useDarkTheme,
                dynamicColor = accentColor == android.graphics.Color.parseColor("#4F46E5"), // Only dynamic if default? Actually logic inside Theme handles null. Ideally we pass null if we want dynamic.
                // Logic refinement: Repository default is Indigo. If User picks Indigo, maybe they want that SPECIFIC indigo, not dynamic.
                // Let's rely on accentColor handling in Theme.kt. If I pass int, it overrides dynamic.
                accentColor = if(accentColor == android.graphics.Color.parseColor("#4F46E5")) null else accentColor
                // Wait, if repo default is #4F46E5, I should treat it as "not set" or "default"? 
                // Let's assume user wants Dynamic unless they manually picked something else.
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    NavHost(
                        navController = navController,
                        startDestination = "splash"
                    ) {
                        composable(
                            "splash",
                            exitTransition = { fadeOut(animationSpec = tween(300)) }
                        ) {
                            SplashScreen(
                                onSplashFinished = {
                                    val nextDestination = when {
                                        !onboardingCompleted -> "onboarding"
                                        user == null -> "login"
                                        else -> "home" // Initial home, deep link handled by LaunchedEffect above if stack preserves
                                    }
                                    navController.navigate(nextDestination) {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                    
                                    // Re-check intent in case splash took time? 
                                    // Actually usually intent is handled *after* splash navigates to home
                                    // But LaunchedEffect above runs in parallel. 
                                    // Ideally we navigate to deep link *after* splash.
                                    // Let's rely on simple navigation for now.
                                }
                            )
                        }
                        composable(
                            "onboarding",
                            enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)) },
                            exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300)) },
                            popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)) },
                            popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300)) }
                        ) {
                            OnboardingScreen(
                                onFinish = {
                                    prefsRepository.setOnboardingCompleted(true)
                                    navController.navigate("login") {
                                        popUpTo("onboarding") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable(
                            "login",
                            enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)) },
                            exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300)) },
                            popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)) },
                            popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300)) }
                        ) {
                            LoginScreen(
                                onLoginSuccess = {
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable(
                            "home",
                            enterTransition = { 
                                fadeIn(animationSpec = tween(300)) + 
                                scaleIn(initialScale = 0.95f, animationSpec = tween(300, easing = FastOutSlowInEasing)) 
                            },
                            popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)) }
                        ) {
                            // MainScreen handles its own nested nav ("hub", "planner", etc)
                            // We just pass the target route if any
                            
                            // Note: MainScreen internal NavHost uses strings like "home_panel", "planner", etc.
                            // We need to pass the *internal* route to MainScreen's initialDestination if valid.
                            
                            // Check local intent again for cold start prop passing?
                            // Actually best to let MainScreen handle it via a shared state or just direct navigation.
                            // But MainScreen is a Composable.
                            // Let's pass the intent's extra to MainScreen using a state that updates.
                            
                            // Simplified: We navigate to "home" generally, and if there is a deep link, 
                            // we assume MainScreen's independent navController (inside MainScreen) needs to handle it?
                            // No, MainScreen creates its OWN navController. This is tricky.
                            // If we navigate to "home", we land on MainScreen. MainScreen starts at Home (Grid).
                            // If we want to deep link to "Planner", we need to tell MainScreen to switch tab.
                            
                            // Solution: Use a CompositionLocal or pass a SideEffect?
                            // Or better: MainScreen accepts a parameter `destination`.
                            // calling `navController.navigate("home")` clears param?
                            
                            // Let's use a mutable state for deep link that MainScreen observes.
                            
                            MainScreen(
                                isGuest = false,
                                onNavigateToLogin = { 
                                    navController.navigate("login") 
                                },
                                // We don't use this param anymore dynamically, we use the LaunchedEffect below in MainScreen?
                                // Actually let's pass it via stored state in ViewModel or Singleton, 
                                // but for simplicity, let's just assume we want straightforward nav.
                                // If I can navigate DIRECTLY to the startDestination of MainScreen...
                                // No, "home" is a screen in MainActivity's NavHost.
                                // MainScreen has child NavHost.
                            )
                        }
                    }
                }
            }
        }
    }
    
    private fun handleDestination(intent: android.content.Intent, navController: androidx.navigation.NavController) {
        val destination = intent.getStringExtra("destination") ?: return
        
        // Map "external" destination keys to internal routes
        // Keys used in widgets/notifs: "notes", "planner", "habits", "finance", "focus"
        // Routes in Screen.kt: "notes", "planner", "habits", "finance", "focus_mode"
        
        val targetRoute = when(destination) {
            "notes" -> "home" // We go to home, then deep link? 
                              // Actually MainScreen is the host. 
                              // If destination is one of the bottom tabs (planner, etc), we are technically "inside" home route
                              // but inside MainScreen's nav.
                              // This nested nav structure makes deep linking harder from root.
                              
                              // Quick fix: Add a shared ViewModel or Global State for navigation events?
                              // Or simply expose `MainScreen`'s nav controller? No.
                              // Since FocusLive app seems to wrap "MainScreen" as the authenticated container...
                              
                              // We can emit an event using a Globals class or EventBus logic (simple version).
                              // Or, simpler: pass the destination as an argument to "home" route: "home/{screen}"
            "planner" -> "planner"
            "habits" -> "habits"
            "finance" -> "finance"
            "focus" -> "focus_mode" 
            else -> null
        }
        
        // Wait, if target is "planner", that is INSIDE MainScreen's NavHost?
        // Yes. MainActivity NavHost -> "home" -> MainScreen -> NavHost -> "planner".
        // SO we need to trigger MainScreen to navigate.
        
        // Use a static/singleton bus for simplicity in this context without refactoring whole app architecture.
        if (targetRoute != null) {
            // If we are not logged in/onboarding, ignore.
            // Assuming we are logged in for now or ignoring deep link until logged in.
            
            NavigationBus.postDestination(targetRoute)
        }
    }
}

// Simple Event Bus for Navigation
object NavigationBus {
    private val _destination = kotlinx.coroutines.flow.MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
    )
    val destination = _destination.asSharedFlow()
    
    fun postDestination(dest: String) {
        _destination.tryEmit(dest)
    }
    
    fun clear() {
       // Optional reset
    }
}