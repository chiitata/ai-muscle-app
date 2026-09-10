package com.example.aimuscle.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.aimuscle.data.models.ExerciseTemplate
import com.example.aimuscle.di.AppModule
import com.example.aimuscle.presentation.screens.HistoryScreen
import com.example.aimuscle.presentation.screens.HomeScreen
import com.example.aimuscle.presentation.screens.InputScreen
import com.example.aimuscle.presentation.screens.NLPConfirmScreen
import com.example.aimuscle.presentation.screens.TemplateScreen
import com.example.aimuscle.presentation.viewmodels.HistoryViewModel
import com.example.aimuscle.presentation.viewmodels.HomeViewModel
import com.example.aimuscle.presentation.viewmodels.InputViewModel
import com.example.aimuscle.presentation.viewmodels.NLPConfirmViewModel
import com.example.aimuscle.presentation.viewmodels.TemplateViewModel
import com.example.aimuscle.presentation.viewmodels.ViewModelFactory
import com.example.aimuscle.ui.navigation.AppNavigation
import com.example.aimuscle.ui.theme.AIMuscleTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * MainActivity - Jetpack Compose entry point for AIMuscle app
 *
 * Responsibilities:
 * - Initialize Dependency Injection (AppModule)
 * - Set up Compose UI with AIMuscleTheme
 * - Manage navigation state between 5 screens
 * - Instantiate ViewModels via ViewModelFactory
 * - Route navigation callbacks to appropriate screens
 *
 * Navigation Structure:
 * - Home: Main dashboard (entry point)
 * - Input: Natural language input screen
 * - NLPConfirm: Confirmation screen after NLP parsing
 * - Template: Template management screen
 * - History: View past workout sessions
 *
 * DI Initialization:
 *   AppModule.initializeDatabase(context) - Called in onCreate()
 *   ViewModelFactory provides all ViewModel instances
 *
 * Usage:
 *   MainActivity is declared in AndroidManifest.xml as launcher activity
 *   Automatically invoked when app starts
 *
 * State Management:
 *   Navigation state held in composable state via MutableStateFlow
 *   Screen-specific state managed by individual ViewModels
 */
class MainActivity : ComponentActivity() {

    private lateinit var viewModelFactory: ViewModelFactory
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var inputViewModel: InputViewModel
    private lateinit var nlpConfirmViewModel: NLPConfirmViewModel
    private lateinit var templateViewModel: TemplateViewModel
    private lateinit var historyViewModel: HistoryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create ViewModelFactory
        // Note: AppModule.initializeDatabase() is called by AIMuscleApplication.onCreate()
        viewModelFactory = ViewModelFactory(this)

        // Initialize ViewModels
        homeViewModel = ViewModelProvider(this, viewModelFactory).get(HomeViewModel::class.java)
        inputViewModel = ViewModelProvider(this, viewModelFactory).get(InputViewModel::class.java)
        nlpConfirmViewModel = ViewModelProvider(this, viewModelFactory).get(NLPConfirmViewModel::class.java)
        templateViewModel = ViewModelProvider(this, viewModelFactory).get(TemplateViewModel::class.java)
        historyViewModel = ViewModelProvider(this, viewModelFactory).get(HistoryViewModel::class.java)

        // Set Compose UI
        setContent {
            AIMuscleTheme {
                AIMuscleApp(
                    homeViewModel = homeViewModel,
                    inputViewModel = inputViewModel,
                    nlpConfirmViewModel = nlpConfirmViewModel,
                    templateViewModel = templateViewModel,
                    historyViewModel = historyViewModel
                )
            }
        }
    }
}

/**
 * AIMuscleApp - Root composable managing app navigation and screens
 *
 * Acts as the main navigation coordinator, displaying the current screen
 * based on navigation state and handling navigation callbacks.
 *
 * Navigation Flow:
 * 1. User starts at Home screen
 * 2. FAB click navigates to Input screen
 * 3. After NLP parsing, navigates to NLPConfirm screen
 * 4. Confirmation saves workout, returns to Home
 * 5. Template/History screens accessible from various entry points
 *
 * @param homeViewModel Manages home screen state (templates)
 * @param inputViewModel Manages input screen state (user text)
 * @param nlpConfirmViewModel Manages NLP confirmation state (parsed exercises)
 * @param templateViewModel Manages template creation/management
 * @param historyViewModel Manages history screen state (past sessions)
 */
@Composable
fun AIMuscleApp(
    homeViewModel: HomeViewModel,
    inputViewModel: InputViewModel,
    nlpConfirmViewModel: NLPConfirmViewModel,
    templateViewModel: TemplateViewModel,
    historyViewModel: HistoryViewModel
) {
    // Navigation state management
    val navigationState = remember { MutableStateFlow<AppNavigation>(AppNavigation.Home) }
    val currentScreen = navigationState.collectAsState()

    // Helper function to navigate
    val navigateTo = { screen: AppNavigation ->
        navigationState.value = screen
    }

    // Helper function to navigate back to home
    val navigateHome = { ->
        navigationState.value = AppNavigation.Home
    }

    // Render current screen based on navigation state
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (currentScreen.value) {
                AppNavigation.Home -> {
                    HomeScreen(
                        homeViewModel = homeViewModel,
                        onStartSession = {
                            navigateTo(AppNavigation.Input)
                        },
                        onSelectTemplate = { template ->
                            // Start session with selected template
                            navigateTo(AppNavigation.NLPConfirm)
                        }
                    )
                }

                AppNavigation.Input -> {
                    InputScreen(
                        inputViewModel = inputViewModel,
                        onBack = { navigateHome() },
                        onParse = { inputText ->
                            // After input is submitted, parse and navigate to NLP confirmation
                            nlpConfirmViewModel.parseInput(inputText)
                            navigateTo(AppNavigation.NLPConfirm)
                        }
                    )
                }

                AppNavigation.NLPConfirm -> {
                    NLPConfirmScreen(
                        nlpConfirmViewModel = nlpConfirmViewModel,
                        onBack = { navigateHome() },
                        onSaveSuccess = {
                            // After confirmation and save, return to home
                            homeViewModel.loadTemplates()
                            navigateHome()
                        }
                    )
                }

                AppNavigation.Template -> {
                    TemplateScreen(
                        templateViewModel = templateViewModel,
                        onBack = { navigateHome() },
                        onSelectTemplate = { templateId ->
                            // After template is selected, refresh home and return
                            homeViewModel.loadTemplates()
                            navigateHome()
                        }
                    )
                }

                AppNavigation.History -> {
                    HistoryScreen(
                        historyViewModel = historyViewModel,
                        onBack = { navigateHome() },
                        onSelectSession = { sessionId ->
                            // Can implement session detail view here
                        }
                    )
                }
            }
        }
    }
}
