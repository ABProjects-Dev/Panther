package com.panther.app.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.panther.app.R
import com.panther.app.feature.HomeScreen
import com.panther.app.feature.ProfileScreen
import com.panther.app.feature.ProfileViewModel
import com.panther.app.feature.ProgramEditorScreen
import com.panther.app.feature.ProgramsScreen
import com.panther.app.feature.ProgramsViewModel
import com.panther.app.feature.SettingsScreen
import com.panther.app.feature.TrainingDayScreen
import com.panther.app.feature.SettingsViewModel

private const val EDITOR_ROUTE = "editor/{id}"
private const val DAY_ROUTE = "day/{programId}/{dayId}"

private data class MainDestination(val route: String, val label: Int, val icon: ImageVector)

private val destinations = listOf(
    MainDestination("home", R.string.home, Icons.Default.Home),
    MainDestination("programs", R.string.programs, Icons.AutoMirrored.Filled.List),
    MainDestination("profile", R.string.profile, Icons.Default.Person),
)

@Composable
fun PantherNavigation(
    programs: ProgramsViewModel,
    profile: ProfileViewModel,
    settings: SettingsViewModel,
) {
    val navController = rememberNavController()
    val entry by navController.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                destinations.forEach { destination ->
                    val isRootRoute = route == destination.route
                    val isSelected = isRootRoute ||
                        (destination.route == "programs" && (route == EDITOR_ROUTE || route == DAY_ROUTE)) ||
                        (destination.route == "profile" && route == "settings")
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            val currentRoute = navController.currentBackStackEntry?.destination?.route
                            val currentlyRoot = currentRoute == destination.route
                            val currentlySelected = currentlyRoot ||
                                (destination.route == "programs" && (currentRoute == EDITOR_ROUTE || currentRoute == DAY_ROUTE)) ||
                                (destination.route == "profile" && currentRoute == "settings")
                            if (currentlySelected && !currentlyRoot) {
                                navController.popBackStack(destination.route, inclusive = false)
                            } else if (!currentlyRoot) {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(destination.icon, contentDescription = null) },
                        label = { Text(stringResource(destination.label)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(padding),
        ) {
            composable("home") {
                HomeScreen(
                    programs = programs,
                    profile = profile,
                    onOpenDay = { dayId, programId -> navController.navigate("day/$programId/$dayId") },
                )
            }
            composable("programs") {
                ProgramsScreen(
                    viewModel = programs,
                    onDetails = { navController.navigate("editor/$it") },
                )
            }
            composable("profile") {
                ProfileScreen(
                    viewModel = profile,
                    onSettings = { navController.navigate("settings") },
                )
            }
            composable("settings") {
                SettingsScreen(
                    viewModel = settings,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(
                route = EDITOR_ROUTE,
                arguments = listOf(navArgument("id") { type = NavType.LongType }),
            ) { backStack ->
                ProgramEditorScreen(
                    viewModel = programs,
                    settings = settings,
                    // A missing argument falls back to 0, which the screen reports as "not found"
                    // instead of leaving an empty destination on the back stack.
                    programId = backStack.arguments?.getLong("id") ?: 0L,
                    onBack = { navController.popBackStack() },
                    onOpenDay = { dayId, programId ->
                        navController.navigate("day/$programId/$dayId")
                    },
                )
            }
            // Every training day has its own screen, reached from its card on the program page.
            composable(
                route = DAY_ROUTE,
                arguments = listOf(
                    navArgument("programId") { type = NavType.LongType },
                    navArgument("dayId") { type = NavType.LongType },
                ),
            ) { backStack ->
                TrainingDayScreen(
                    viewModel = programs,
                    programId = backStack.arguments?.getLong("programId") ?: 0L,
                    dayId = backStack.arguments?.getLong("dayId") ?: 0L,
                    onBack = { navController.popBackStack() },
                    onDayDeleted = {
                        // The day screen's only remaining destination is the program page.
                        navController.popBackStack()
                    },
                )
            }
        }
    }
}
