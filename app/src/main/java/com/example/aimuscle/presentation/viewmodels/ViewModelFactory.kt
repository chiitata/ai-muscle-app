package com.example.aimuscle.presentation.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.aimuscle.data.repository.WorkoutRepository
import com.example.aimuscle.di.AppModule
import com.example.aimuscle.nlp.NLPEngine

/**
 * ViewModelFactory - Create ViewModel instances with proper dependency injection
 *
 * Provides factory methods for creating all ViewModels with their dependencies:
 * - HomeViewModel
 * - InputViewModel
 * - NLPConfirmViewModel
 * - TemplateViewModel
 * - HistoryViewModel
 *
 * Usage:
 *   val factory = ViewModelFactory(context)
 *   val homeViewModel = ViewModelProvider(this, factory).get(HomeViewModel::class.java)
 */
@Suppress("UNCHECKED_CAST")
class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    private val repository: WorkoutRepository = AppModule.getRepository()
    private val nlpEngine: NLPEngine = AppModule.getNLPEngine(context)

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            HomeViewModel::class.java -> {
                HomeViewModel(repository) as T
            }
            InputViewModel::class.java -> {
                InputViewModel() as T
            }
            NLPConfirmViewModel::class.java -> {
                NLPConfirmViewModel(nlpEngine, repository) as T
            }
            TemplateViewModel::class.java -> {
                TemplateViewModel(repository) as T
            }
            HistoryViewModel::class.java -> {
                HistoryViewModel(repository) as T
            }
            else -> {
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
}
