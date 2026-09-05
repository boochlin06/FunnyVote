package com.heaton.funnyvote.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.heaton.funnyvote.ui.about.AboutScreen
import com.heaton.funnyvote.ui.about.sub.AboutAppScreen
import com.heaton.funnyvote.ui.about.sub.AuthorInfoScreen
import com.heaton.funnyvote.ui.about.sub.LicenceScreen
import com.heaton.funnyvote.ui.about.sub.ProblemScreen
import com.heaton.funnyvote.ui.create.CreateVoteScreen
import com.heaton.funnyvote.ui.detail.VoteDetailScreen
import com.heaton.funnyvote.ui.home.HomeScreen
import com.heaton.funnyvote.ui.personal.PersonalScreen
import com.heaton.funnyvote.ui.profile.ProfileScreen
import com.heaton.funnyvote.ui.tutorial.TutorialScreen
import com.heaton.funnyvote.ui.welcome.WelcomeScreen
import kotlinx.serialization.Serializable

@Serializable
object WelcomeRoute

@Serializable
object HomeRoute

@Serializable
data class VoteDetailRoute(val voteCode: String)

@Serializable
object CreateVoteRoute

@Serializable
object ProfileRoute

@Serializable
data class PersonalRoute(
    val authorId: String,
    val authorName: String,
    val authorIcon: String? = null
)

@Serializable
object AboutRoute

@Serializable
object TutorialRoute

@Serializable
object AboutAppRoute

@Serializable
object AuthorInfoRoute

@Serializable
object LicenceRoute

@Serializable
object ProblemRoute

@Composable
fun FunnyVoteNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    initialVoteCode: String? = null
) {
    LaunchedEffect(initialVoteCode) {
        if (!initialVoteCode.isNullOrBlank()) {
            navController.navigate(VoteDetailRoute(voteCode = initialVoteCode))
        }
    }

    NavHost(
        navController = navController,
        startDestination = WelcomeRoute,
        modifier = modifier
    ) {
        composable<WelcomeRoute> {
            WelcomeScreen(
                onNavigateToHome = {
                    navController.navigate(HomeRoute) {
                        popUpTo<WelcomeRoute> { inclusive = true }
                    }
                }
            )
        }

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
                },
                onNavigateToAbout = {
                    navController.navigate(AboutRoute)
                },
                onNavigateToTutorial = {
                    navController.navigate(TutorialRoute)
                },
                onNavigateToAuthor = { id, name, icon ->
                    navController.navigate(PersonalRoute(authorId = id, authorName = name, authorIcon = icon))
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
                },
                onVoteClick = { code ->
                    navController.navigate(VoteDetailRoute(voteCode = code))
                }
            )
        }

        composable<PersonalRoute> {
            PersonalScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onVoteClick = { code ->
                    navController.navigate(VoteDetailRoute(voteCode = code))
                }
            )
        }

        composable<AboutRoute> {
            AboutScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToAboutApp = {
                    navController.navigate(AboutAppRoute)
                },
                onNavigateToTutorial = {
                    navController.navigate(TutorialRoute)
                },
                onNavigateToAuthorInfo = {
                    navController.navigate(AuthorInfoRoute)
                },
                onNavigateToLicence = {
                    navController.navigate(LicenceRoute)
                },
                onNavigateToProblem = {
                    navController.navigate(ProblemRoute)
                }
            )
        }

        composable<AboutAppRoute> {
            AboutAppScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<AuthorInfoRoute> {
            AuthorInfoScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<LicenceRoute> {
            LicenceScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<ProblemRoute> {
            ProblemScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<TutorialRoute> {
            TutorialScreen(
                onFinish = {
                    navController.popBackStack()
                }
            )
        }
    }
}
