package com.panther.app.core.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.panther.app.R
import com.panther.app.core.designsystem.theme.PantherSpacing

private const val FOUNDATION_ROUTE = "foundation"

@Composable
fun PantherNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = FOUNDATION_ROUTE) {
        composable(FOUNDATION_ROUTE) {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(PantherSpacing.large),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium)
                    Text(stringResource(R.string.foundation_message), style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
