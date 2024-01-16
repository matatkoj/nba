package com.matatkoj.nbaplayers.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rxjava3.subscribeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.matatkoj.nbaplayers.NbaNavigation.Companion.TEAMS_PATH
import com.matatkoj.nbaplayers.R
import com.matatkoj.nbaplayers.data.response.Player
import com.matatkoj.nbaplayers.ui.compose.DetailsLoadingLayout
import com.matatkoj.nbaplayers.ui.compose.ErrorLayout
import com.matatkoj.nbaplayers.ui.compose.InfoBox
import com.matatkoj.nbaplayers.ui.compose.InfoRow
import com.matatkoj.nbaplayers.ui.compose.NbaTopBar

@Composable
fun PlayerDetailScreen(
    navController: NavController,
    viewModel: PlayerDetailViewModel = hiltViewModel()
) {
    Column(modifier = Modifier.fillMaxSize()) {
        val playerDetails by viewModel.playerDetailsStream
            .subscribeAsState(initial = viewModel.getPlayerDetailsBlocking())

        TopBar(playerDetails)

        PlayerDetailsLayout(
            playerDetails = playerDetails,
            navController = navController,
            onRetryClicked = remember { { viewModel.onRetryClicked() } }
        )
    }
}

@Composable
private fun TopBar(playerDetails: PlayerDetailViewModel.PlayerDetails) {
    val title = when (playerDetails) {
        is PlayerDetailViewModel.PlayerDetails.Error,
        PlayerDetailViewModel.PlayerDetails.Loading -> ""
        is PlayerDetailViewModel.PlayerDetails.Success -> {
            with (playerDetails.details) {
                stringResource(R.string.player_detail_title).format(
                    id, firstName, lastName
                )
            }
        }
    }

    NbaTopBar(title = title)
}

@Composable
private fun PlayerDetailsLayout(
    playerDetails: PlayerDetailViewModel.PlayerDetails,
    navController: NavController,
    onRetryClicked: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        DetailsLoadingLayout(visible = playerDetails == PlayerDetailViewModel.PlayerDetails.Loading)

        PlayerDetailsDataLayout(
            playerDetails = playerDetails,
            onTeamClicked = remember { {
                navController.navigate("$TEAMS_PATH/$it")
            } }
        )

        PlayerErrorLayout(
            playerDetails = playerDetails,
            onRetryClicked = onRetryClicked
        )
    }
}

@Composable
private fun PlayerDetailsDataLayout(
    playerDetails: PlayerDetailViewModel.PlayerDetails,
    onTeamClicked: (Int) -> Unit
) {
    AnimatedVisibility(
        modifier = Modifier.fillMaxSize(),
        visible = playerDetails is PlayerDetailViewModel.PlayerDetails.Success,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        if (playerDetails is PlayerDetailViewModel.PlayerDetails.Success) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                PhysicalInfoLayout(player = playerDetails.details)

                LeagueInfo(
                    player = playerDetails.details,
                    onTeamClicked = onTeamClicked
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun PhysicalInfoLayout(
    player: Player
) {
    val heightValue = listOfNotNull(
        player.heightFeet?.let { stringResource(R.string.player_detail_height_ft).format(it) },
        player.heightInches?.let { stringResource(R.string.player_detail_height_in).format(it) }
    )
        .takeIf { it.isNotEmpty() }
        ?.joinToString(" ")

    if (heightValue != null || player.weightPounds != null) {
        InfoBox(
            title = stringResource(R.string.player_detail_physical_info),
        ) {
            if (heightValue != null) {
                InfoRow(
                    label = stringResource(R.string.player_detail_height),
                    value = heightValue
                )
            }

            if (player.weightPounds != null) {
                InfoRow(
                    label = stringResource(R.string.player_detail_weight),
                    value = stringResource(R.string.player_detail_weigh_pounds)
                        .format(player.weightPounds)
                )
            }
        }
    }
}

@Composable
private fun LeagueInfo(
    player: Player,
    onTeamClicked: (Int) -> Unit
) {
    InfoBox(
        title = stringResource(R.string.player_detail_league_info),
    ) {
        InfoRow(
            label = stringResource(R.string.player_detail_team),
            value = player.team.name,
            onClick = remember { { onTeamClicked(player.team.id) } }
        )

        val positionValue = remember(player.position) {
            player.position.takeIf { it.isNotBlank() }
        }

        if (positionValue != null) {
            InfoRow(
                label = stringResource(R.string.player_detail_position),
                value = player.position
            )
        }
    }
}

@Composable
private fun PlayerErrorLayout(
    playerDetails: PlayerDetailViewModel.PlayerDetails,
    onRetryClicked: () -> Unit
) {
    AnimatedVisibility(
        modifier = Modifier.fillMaxSize(),
        visible = playerDetails is PlayerDetailViewModel.PlayerDetails.Error,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        ErrorLayout(
            title = stringResource(R.string.player_detail_error),
            onRetryClicked = onRetryClicked
        )
    }
}