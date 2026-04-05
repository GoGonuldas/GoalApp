package com.goalapp

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import androidx.compose.foundation.layout.padding
import com.goalapp.ui.add.AddGoalScreen
import com.goalapp.ui.archive.ArchiveScreen
import com.goalapp.ui.detail.GoalDetailScreen
import com.goalapp.ui.home.HomeScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Archive : Screen("archive")
    object AddGoal : Screen("add_goal")
    object GoalDetail : Screen("goal_detail/{goalId}") {
        fun createRoute(goalId: Long) = "goal_detail/$goalId"
    }
}

private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem(route = Screen.Home.route, label = "Anasayfa", icon = Icons.Filled.Home),
    BottomNavItem(route = Screen.Archive.route, label = "Arsiv", icon = Icons.Filled.Archive)
)

@Composable
fun NavGraph(navController: NavHostController) {
    val backStackEntry = navController.currentBackStackEntryAsState().value
    val currentDestination = backStackEntry?.destination
    val showBottomBar = currentDestination?.route in setOf(Screen.Home.route, Screen.Archive.route)

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination
                            ?.hierarchy
                            ?.any { it.route == item.route } == true

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(Screen.Home.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {

            composable(Screen.Home.route) {
                HomeScreen(
                    onGoalClick = { id ->
                        navController.navigate(Screen.GoalDetail.createRoute(id))
                    },
                    onAddGoal = {
                        navController.navigate(Screen.AddGoal.route)
                    }
                )
            }

            composable(Screen.Archive.route) {
                ArchiveScreen(
                    onGoalClick = { id ->
                        navController.navigate(Screen.GoalDetail.createRoute(id))
                    }
                )
            }

            composable(Screen.AddGoal.route) {
                AddGoalScreen(
                    onBack = { navController.popBackStack() },
                    onSaveSuccess = {
                        val popped = navController.popBackStack(Screen.Home.route, inclusive = false)
                        if (!popped) {
                            navController.navigate(Screen.Home.route) {
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }

            composable(
                route = Screen.GoalDetail.route,
                arguments = listOf(navArgument("goalId") { type = NavType.LongType })
            ) {
                GoalDetailScreen(
                    onBack = { navController.popBackStack() },
                    onSaveProgressSuccess = {
                        val popped = navController.popBackStack(Screen.Home.route, inclusive = false)
                        if (!popped) {
                            navController.navigate(Screen.Home.route) {
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }
        }
    }
}
