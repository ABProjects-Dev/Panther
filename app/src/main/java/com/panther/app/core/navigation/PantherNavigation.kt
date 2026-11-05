package com.panther.app.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.panther.app.R
import com.panther.app.feature.*

private data class MainDestination(val route: String, val label: Int, val icon: ImageVector)
private val destinations = listOf(
    MainDestination("home", R.string.home, Icons.Default.Home),
    MainDestination("programs", R.string.programs, Icons.Default.List),
    MainDestination("history", R.string.history, Icons.Default.Favorite),
    MainDestination("profile", R.string.profile, Icons.Default.Person),
)

@Composable
fun PantherNavigation(programs: ProgramsViewModel, profile: ProfileViewModel, settings: SettingsViewModel) {
    val navController = rememberNavController()
    val entry by navController.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                destinations.forEach { destination ->
                    NavigationBarItem(
                        selected = route == destination.route || (destination.route == "programs" && (route?.startsWith("programs?") == true || route == "details/{id}")),
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(destination.icon, contentDescription = null) },
                        label = { Text(stringResource(destination.label)) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.primary, selectedTextColor = MaterialTheme.colorScheme.primary, indicatorColor = MaterialTheme.colorScheme.surface),
                    )
                }
            }
        },
    ) { padding ->
        NavHost(navController = navController, startDestination = "home", modifier = Modifier.padding(padding)) {
            composable("home") { HomeScreen(programs, profile, onCreate = { navController.navigate("programs?create=true") }, onDetails = { navController.navigate("details/$it") }) }
            composable("programs?create={create}", arguments = listOf(navArgument("create") { type = NavType.BoolType; defaultValue = false })) { backStack ->
                ProgramsScreen(programs, initiallyCreate = backStack.arguments?.getBoolean("create") == true, onDetails = { navController.navigate("details/$it") })
            }
            composable("history") { HistoryScreen(programs) { navController.navigate("details/$it") } }
            composable("profile") { ProfileScreen(profile) { navController.navigate("settings") } }
            composable("settings") { SettingsScreen(settings) { navController.popBackStack() } }
            composable("details/{id}") { backStack ->
                val id = backStack.arguments?.getString("id")?.toLongOrNull()
                if (id != null) ProgramDetailsScreen(programs, id) { navController.popBackStack() }
            }
        }
    }
}
