package com.heaton.funnyvote.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.heaton.funnyvote.ui.create.CreateVoteScreen
import com.heaton.funnyvote.ui.detail.VoteDetailScreen
import com.heaton.funnyvote.ui.home.HomeScreen
import com.heaton.funnyvote.ui.profile.ProfileScreen
import kotlinx.serialization.Serializable

@Serializable
object HomeRoute

@Serializable
data class VoteDetailRoute(val voteCode: String)

@Serializable
object CreateVoteRoute

@Serializable
object ProfileRoute

@Composable
fun FunnyVoteNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier
    ) {
        composable<HomeRoute> {
            HomeScreen(
                onNavigateToDetail = { code ->
                    navController.navigate(VoteDetailRoute(voteCode = code))
                },
                onNavigateToCreate = {
                    navController.navigate(CreateVoteRoute)
                },
                onNavigateToProfile = {
                    navController.navigate(ProfileRoute)
                }
            )
        }

        composable<VoteDetailRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<VoteDetailRoute>()
            VoteDetailScreen(
                voteCode = route.voteCode,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<CreateVoteRoute> {
            CreateVoteScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<ProfileRoute> {
            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
