package com.heaton.funnyvote.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.heaton.funnyvote.ui.create.CreateVoteScreen
import com.heaton.funnyvote.ui.detail.VoteDetailScreen
import com.heaton.funnyvote.ui.main.MainScreen
import com.heaton.funnyvote.ui.profile.ProfileScreen

@Composable
fun FunnyVoteApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen(
                onNavigateToDetail = { voteCode ->
                    navController.navigate("detail/$voteCode")
                },
                onNavigateToCreate = {
                    navController.navigate("create")
                },
                onNavigateToProfile = {
                    navController.navigate("profile")
                }
            )
        }
        composable("detail/{voteCode}") { backStackEntry ->
            val voteCode = backStackEntry.arguments?.getString("voteCode") ?: ""
            VoteDetailScreen(
                voteCode = voteCode,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("create") {
            CreateVoteScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("profile") {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
