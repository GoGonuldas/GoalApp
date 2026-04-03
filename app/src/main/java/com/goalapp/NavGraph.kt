package com.goalapp

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.goalapp.ui.add.AddGoalScreen
import com.goalapp.ui.detail.GoalDetailScreen
import com.goalapp.ui.home.HomeScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AddGoal : Screen("add_goal")
    object GoalDetail : Screen("goal_detail/{goalId}") {
        fun createRoute(goalId: Long) = "goal_detail/$goalId"
    }
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {

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

        composable(Screen.AddGoal.route) {
            AddGoalScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.GoalDetail.route,
            arguments = listOf(navArgument("goalId") { type = NavType.LongType })
        ) {
            GoalDetailScreen(onBack = { navController.popBackStack() })
        }
    }
}
