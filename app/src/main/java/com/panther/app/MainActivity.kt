package com.panther.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.panther.app.core.designsystem.theme.PantherTheme
import com.panther.app.core.navigation.PantherNavigation
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // The foundation currently ships Persian only. Revisit when adding language selection.
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                PantherTheme { PantherNavigation() }
            }
        }
    }
}
