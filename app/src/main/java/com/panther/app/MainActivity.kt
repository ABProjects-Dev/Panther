package com.panther.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.panther.app.core.designsystem.theme.PantherTheme
import com.panther.app.core.navigation.PantherNavigation
import com.panther.app.core.settings.ThemeChoice
import com.panther.app.feature.ProfileViewModel
import com.panther.app.feature.ProgramsViewModel
import com.panther.app.feature.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val programs: ProgramsViewModel by viewModels()
    private val profile: ProfileViewModel by viewModels()
    private val settings: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Required from targetSdk 35 onward: opt in to edge-to-edge and keep system bar icon
        // contrast synchronized with the user's selected in-app theme.
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val choice by settings.theme.collectAsStateWithLifecycle()
            val dark = when (choice) {
                ThemeChoice.DARK -> true
                ThemeChoice.LIGHT -> false
                ThemeChoice.SYSTEM -> isSystemInDarkTheme()
            }
            DisposableEffect(dark) {
                val barStyle = if (dark) {
                    SystemBarStyle.dark(Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                }
                enableEdgeToEdge(statusBarStyle = barStyle, navigationBarStyle = barStyle)
                onDispose {}
            }
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                PantherTheme(darkTheme = dark) { PantherNavigation(programs, profile, settings) }
            }
        }
    }
}
