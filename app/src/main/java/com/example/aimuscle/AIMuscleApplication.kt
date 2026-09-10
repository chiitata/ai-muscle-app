package com.example.aimuscle

import android.app.Application
import com.example.aimuscle.di.AppModule

/**
 * AIMuscleApplication - Custom Application class for app-level initialization
 *
 * Responsibilities:
 * - Initialize AppModule on app startup
 * - Ensure database is ready before any activity starts
 * - Handle application-wide configuration
 *
 * Declaration in AndroidManifest.xml:
 *   <application
 *       android:name=".AIMuscleApplication"
 *       ... other attributes ...
 *   >
 *
 * Initialization Flow:
 * 1. System creates AIMuscleApplication instance
 * 2. onCreate() is called
 * 3. AppModule.initializeDatabase(this) initializes Room DB
 * 4. MainActivity and other activities can safely access AppModule
 *
 * Usage:
 *   This class is automatically instantiated by Android system
 *   No manual instantiation needed
 */
class AIMuscleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Dependency Injection container
        // Must be called before any activities are created
        AppModule.initializeDatabase(this)
    }
}
