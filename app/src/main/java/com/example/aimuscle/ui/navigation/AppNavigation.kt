package com.example.aimuscle.ui.navigation

/**
 * AppNavigation - Sealed class defining all navigation screens and routes
 *
 * Encapsulates navigation state for the entire application with 5 main screens:
 * - Home: Display exercise templates and start session
 * - Input: Capture natural language input from user
 * - NLPConfirm: Confirm parsed workout data from NLP engine
 * - Template: Create or manage exercise templates
 * - History: View past workout sessions
 *
 * Each screen is an object representing a unique navigation destination.
 * Screens can carry data through sealed class properties for passing state.
 *
 * Usage:
 *   sealed class AppNavigation {
 *       object Home : AppNavigation()
 *       object Input : AppNavigation()
 *       // ...
 *   }
 *
 *   private val _currentScreen = MutableStateFlow<AppNavigation>(AppNavigation.Home)
 *   val currentScreen = _currentScreen.asStateFlow()
 *
 *   fun navigateTo(screen: AppNavigation) {
 *       _currentScreen.value = screen
 *   }
 */
sealed class AppNavigation {
    /**
     * Home Screen - Main dashboard showing exercise templates
     * Entry point of the application with FAB to start new session
     */
    object Home : AppNavigation()

    /**
     * Input Screen - Natural language input for workout description
     * User provides text description of their workout
     */
    object Input : AppNavigation()

    /**
     * NLPConfirm Screen - Confirm parsed workout data
     * Shows parsed exercises and allows user to confirm/edit before saving
     */
    object NLPConfirm : AppNavigation()

    /**
     * Template Screen - Create or manage exercise templates
     * Allows users to save workout configurations as reusable templates
     */
    object Template : AppNavigation()

    /**
     * History Screen - View and manage past workout sessions
     * Displays all completed workout sessions with details and stats
     */
    object History : AppNavigation()
}
