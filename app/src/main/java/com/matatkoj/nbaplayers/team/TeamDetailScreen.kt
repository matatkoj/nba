package com.matatkoj.nbaplayers.team

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rxjava3.subscribeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.matatkoj.nbaplayers.R
import com.matatkoj.nbaplayers.data.response.Team
import com.matatkoj.nbaplayers.ui.compose.DetailsLoadingLayout
import com.matatkoj.nbaplayers.ui.compose.ErrorLayout
import com.matatkoj.nbaplayers.ui.compose.InfoBox
import com.matatkoj.nbaplayers.ui.compose.InfoRow
import com.matatkoj.nbaplayers.ui.compose.NbaTopBar
import com.matatkoj.nbaplayers.util.loadTeamPicture

@Composable
fun TeamDetailScreen(
    viewModel: TeamDetailViewModel = hiltViewModel()
) {
    Column(modifier = Modifier.fillMaxSize()) {
        val teamDetails by viewModel.teamDetailsStream
            .subscribeAsState(initial = viewModel.getTeamDetailsBlocking())

        TopBar(teamDetails = teamDetails)

        TeamDetailsLayout(
            teamDetails = teamDetails,
            onRetryClicked = remember { { viewModel.onRetryClicked() } }
        )
    }
}

@Composable
private fun TopBar(teamDetails: TeamDetailViewModel.TeamDetails) {
    val title = when (teamDetails) {
        is TeamDetailViewModel.TeamDetails.Error,
        TeamDetailViewModel.TeamDetails.Loading -> ""
        is TeamDetailViewModel.TeamDetails.Success -> {
            with (teamDetails.details) {
                stringResource(R.string.team_detail_title).format(id, name)
            }
        }
    }
    NbaTopBar(title = title)
}

@Composable
private fun TeamDetailsLayout(
    teamDetails: TeamDetailViewModel.TeamDetails,
    onRetryClicked: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        DetailsLoadingLayout(teamDetails == TeamDetailViewModel.TeamDetails.Loading)

        TeamDetailsDataLayout(teamDetails = teamDetails)

        TeamErrorLayout(
            teamDetails = teamDetails,
            onRetryClicked = onRetryClicked
        )
    }
}

@Composable
private fun TeamDetailsDataLayout(
    teamDetails: TeamDetailViewModel.TeamDetails
) {
    AnimatedVisibility(
        modifier = Modifier.fillMaxSize(),
        visible = teamDetails is TeamDetailViewModel.TeamDetails.Success,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        if (teamDetails is TeamDetailViewModel.TeamDetails.Success) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                BoxWithConstraints(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    val logoBitmap = loadTeamPicture(teamDetails.details.abbreviation)

                    Image(
                        modifier = Modifier.size(maxWidth, maxWidth),
                        bitmap = logoBitmap.value.asImageBitmap(),
                        contentDescription = teamDetails.details.name
                    )
                }

                TeamInfo(team = teamDetails.details)

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun TeamInfo(
    team: Team
) {
    InfoBox(title = stringResource(R.string.team_detail_info)) {
        InfoRow(
            label = stringResource(R.string.team_detail_full_name),
            value = team.fullName
        )

        InfoRow(
            label = stringResource(R.string.team_detail_abbreviation),
            value = team.abbreviation
        )

        InfoRow(
            label = stringResource(R.string.team_detail_city),
            value = team.city
        )

        InfoRow(
            label = stringResource(R.string.team_detail_conference),
            value = team.conference
        )

        InfoRow(
            label = stringResource(R.string.team_detail_division),
            value = team.division
        )
    }
}

@Composable
private fun TeamErrorLayout(
    teamDetails: TeamDetailViewModel.TeamDetails,
    onRetryClicked: () -> Unit
) {
    AnimatedVisibility(
        modifier = Modifier.fillMaxSize(),
        visible = teamDetails is TeamDetailViewModel.TeamDetails.Error,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        ErrorLayout(
            title = stringResource(R.string.team_detail_error),
            onRetryClicked = onRetryClicked
        )
    }
}