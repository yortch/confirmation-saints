package com.yortch.confirmationsaints.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.yortch.confirmationsaints.localization.AppStrings
import com.yortch.confirmationsaints.localization.LocalAppLanguage
import com.yortch.confirmationsaints.viewmodel.SaintListViewModel
import com.yortch.confirmationsaints.ui.screens.about.AboutConfirmationScreen
import com.yortch.confirmationsaints.ui.screens.categories.CategoryBrowseScreen
import com.yortch.confirmationsaints.ui.screens.categories.CategorySaintsScreen
import com.yortch.confirmationsaints.ui.screens.saints.SaintDetailScreen
import com.yortch.confirmationsaints.ui.screens.saints.SaintListScreen
import com.yortch.confirmationsaints.ui.screens.settings.SettingsScreen

/**
 * Top-level navigation scaffold — 4-tab bottom nav + per-tab back stack +
 * detail pushes. The nested-per-tab approach matches iOS `TabView` +
 * `NavigationStack` semantics.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    onReplayWelcome: () -> Unit,
) {
    val navController = rememberNavController()
    val language = LocalAppLanguage.current
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route.orEmpty()

    val tabs = listOf(
        TabItem(Screen.About, Icons.Default.Book, AppStrings.localized("About", language)),
        TabItem(Screen.Explore, Icons.Default.Explore, AppStrings.localized("Explore", language)),
        TabItem(Screen.Saints, Icons.Default.Groups, AppStrings.localized("Saints", language)),
        TabItem(Screen.Settings, Icons.Default.Settings, AppStrings.localized("Settings", language)),
    )

    Scaffold(
        topBar = {
            val title = resolveTitle(currentRoute, backStackEntry, language)
            val isDetailRoute = currentRoute.contains("SaintDetail") ||
                currentRoute.contains("CategorySaints")
            val canPop = navController.previousBackStackEntry != null
            if (title != null) {
                TopAppBar(
                    title = { Text(title) },
                    navigationIcon = {
                        if (isDetailRoute && canPop) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = AppStrings.localized("Back", language),
                                )
                            }
                        }
                    },
                )
            }
        },
        bottomBar = {
            NavigationBar {
                tabs.forEach { tab ->
                    val routeClass = tab.screen::class.qualifiedName ?: ""
                    val selected = backStackEntry?.destination?.hierarchy?.any {
                        it.route == routeClass
                    } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = { navController.navigateTopLevel(tab.screen) },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Saints,
            modifier = Modifier.padding(padding),
        ) {
            // About tab — just the info screen.
            navigation<Screen.About>(startDestination = Screen.AboutHome) {
                composable<Screen.AboutHome> { AboutConfirmationScreen() }
            }

            // Explore tab — category browse → category saints → saint detail.
            navigation<Screen.Explore>(startDestination = Screen.ExploreHome) {
                composable<Screen.ExploreHome> {
                    CategoryBrowseScreen(
                        onValueClick = { groupId, valueId, title ->
                            navController.navigate(Screen.CategorySaints(groupId, valueId, title))
                        },
                    )
                }
                composable<Screen.CategorySaints> { entry ->
                    val route = entry.toRoute<Screen.CategorySaints>()
                    CategorySaintsScreen(
                        groupId = route.groupId,
                        valueId = route.valueId,
                        onSaintClick = { id -> navController.navigate(Screen.SaintDetail(id)) },
                    )
                }
                composable<Screen.SaintDetail> { entry ->
                    val route = entry.toRoute<Screen.SaintDetail>()
                    SaintDetailScreen(saintId = route.saintId)
                }
            }

            // Saints tab — list → saint detail.
            navigation<Screen.Saints>(startDestination = Screen.SaintsHome) {
                composable<Screen.SaintsHome> {
                    SaintListScreen(
                        onSaintClick = { id -> navController.navigate(Screen.SaintDetail(id)) },
                    )
                }
                composable<Screen.SaintDetail> { entry ->
                    val route = entry.toRoute<Screen.SaintDetail>()
                    SaintDetailScreen(saintId = route.saintId)
                }
            }

            // Settings tab.
            navigation<Screen.Settings>(startDestination = Screen.SettingsHome) {
                composable<Screen.SettingsHome> {
                    SettingsScreen(onReplayWelcome = onReplayWelcome)
                }
            }
        }
    }
}

private data class TabItem(val screen: Screen, val icon: ImageVector, val label: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun resolveTitle(
    currentRoute: String,
    backStackEntry: androidx.navigation.NavBackStackEntry?,
    language: com.yortch.confirmationsaints.localization.AppLanguage,
): String? {
    return when {
        currentRoute.endsWith("AboutHome") -> AppStrings.localized("About Confirmation", language)
        currentRoute.endsWith("ExploreHome") -> AppStrings.localized("Explore", language)
        currentRoute.endsWith("SaintsHome") -> AppStrings.localized("Saints", language)
        currentRoute.endsWith("SettingsHome") -> AppStrings.localized("Settings", language)
        currentRoute.contains("SaintDetail") -> {
            val saintId = runCatching {
                backStackEntry?.toRoute<Screen.SaintDetail>()?.saintId
            }.getOrNull()
            if (saintId != null && backStackEntry != null) {
                val viewModel: SaintListViewModel = hiltViewModel(backStackEntry)
                val state by viewModel.state.collectAsStateWithLifecycle()
                state.saints.firstOrNull { it.id == saintId }?.name ?: ""
            } else {
                ""
            }
        }
        currentRoute.contains("CategorySaints") -> {
            runCatching { backStackEntry?.toRoute<Screen.CategorySaints>()?.title }.getOrNull()
        }
        else -> null
    }
}

private fun NavHostController.navigateTopLevel(screen: Screen) {
    navigate(screen) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
