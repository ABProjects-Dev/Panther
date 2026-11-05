package com.panther.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
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
        super.onCreate(savedInstanceState)
        setContent {
            val choice by settings.theme.collectAsStateWithLifecycle()
            val dark = when (choice) { ThemeChoice.DARK -> true; ThemeChoice.LIGHT -> false; ThemeChoice.SYSTEM -> isSystemInDarkTheme() }
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                PantherTheme(darkTheme = dark) { PantherNavigation(programs, profile, settings) }
            }
        }
    }
}
