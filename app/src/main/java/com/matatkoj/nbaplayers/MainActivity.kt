package com.matatkoj.nbaplayers

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.matatkoj.nbaplayers.NbaNavigation.Companion.PLAYERS_PATH
import com.matatkoj.nbaplayers.player.PlayerDetailScreen
import com.matatkoj.nbaplayers.players.PlayerListScreen
import com.matatkoj.nbaplayers.team.TeamDetailScreen
import com.matatkoj.nbaplayers.ui.theme.NBAPlayersTheme
import com.matatkoj.nbaplayers.ui.transition.enterTransition
import com.matatkoj.nbaplayers.ui.transition.exitTransition
import com.matatkoj.nbaplayers.ui.transition.popEnterTransition
import com.matatkoj.nbaplayers.ui.transition.popExitTransition
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NBAPlayersTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentColor = MaterialTheme.colorScheme.background
                ) { paddingValues ->
                    val navController = rememberNavController()
                    NavHost(
                        modifier = Modifier.padding(paddingValues),
                        navController = navController,
                        startDestination = NbaNavigation.PlayerList.route,
                        enterTransition = { enterTransition() },
                        exitTransition = { exitTransition() },
                        popEnterTransition = { popEnterTransition() },
                        popExitTransition = { popExitTransition() }
                    ) {
                        composable(
                            route = NbaNavigation.PlayerList.route
                        ) {
                            PlayerListScreen(
                                onPlayerClicked = remember { {
                                    navController.navigate("$PLAYERS_PATH/$it")
                                } }
                            )
                        }

                        composable(
                            route = NbaNavigation.PlayerDetail.route,
                            arguments = listOf(
                                navArgument(NbaNavigation.PLAYER_ID_KEY) {
                                    this.type = NavType.IntType
                                }
                            )
                        ) {
                            PlayerDetailScreen(navController)
                        }

                        composable(
                            route = NbaNavigation.TeamDetail.route,
                            arguments = listOf(
                                navArgument(NbaNavigation.TEAM_ID_KEY) {
                                    this.type = NavType.IntType
                                }
                            )
                        ) {
                            TeamDetailScreen()
                        }
                    }
                }
            }
        }
    }
}