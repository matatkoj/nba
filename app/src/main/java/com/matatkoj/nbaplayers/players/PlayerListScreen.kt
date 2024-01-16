package com.matatkoj.nbaplayers.players

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rxjava3.subscribeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.matatkoj.nbaplayers.R
import com.matatkoj.nbaplayers.data.response.Player
import com.matatkoj.nbaplayers.ui.compose.ErrorLayout
import com.matatkoj.nbaplayers.ui.compose.NbaTopBar
import com.matatkoj.nbaplayers.ui.compose.nbaShimmerBrush
import com.matatkoj.nbaplayers.util.loadTeamPicture
import kotlinx.collections.immutable.ImmutableList

@Composable
fun PlayerListScreen(
    onPlayerClicked: (Int) -> Unit,
    viewModel: PlayerListViewModel = hiltViewModel()
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        NbaTopBar(title = stringResource(R.string.players_list_title))

        val players by viewModel.playersStream
            .subscribeAsState(initial = viewModel.getPlayersBlocking())
        val pagerItem by viewModel.pagerItemStream
            .subscribeAsState(initial = viewModel.getPagerItemBlocking())

        val lazyColumnState = rememberLazyListState()

        PlayersListLayout(
            players = players,
            lazyColumnState = lazyColumnState,
            pagerItem = pagerItem,
            onRequestNextPage = remember { { viewModel.onNextPageRequested() } },
            onItemClicked = onPlayerClicked,
            onRetryClicked = remember { { viewModel.onRetryClicked() } }
        )
    }
}

@Composable
private fun PlayersListLayout(
    players: PlayerListViewModel.Players,
    pagerItem: PlayerListViewModel.PagerItem,
    lazyColumnState: LazyListState,
    onRequestNextPage: () -> Unit,
    onItemClicked: (Int) -> Unit,
    onRetryClicked: () -> Unit
) {
    when (players) {
        PlayerListViewModel.Players.Loading -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                for (i in 0..10) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .fillMaxWidth()
                            .height(96.dp)
                            .background(nbaShimmerBrush(), RoundedCornerShape(10.dp))
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        is PlayerListViewModel.Players.Success -> {
            val lastVisibleItemIndex = lazyColumnState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            LaunchedEffect(lastVisibleItemIndex) {
                if (
                    lastVisibleItemIndex > players.playerList.size - 3
                    && pagerItem == PlayerListViewModel.PagerItem.None
                ) {
                    onRequestNextPage()
                }
            }

            PlayersDataLayout(
                players = players.playerList,
                lazyColumnState = lazyColumnState,
                pagerItem = pagerItem,
                onItemClicked = onItemClicked,
                onRetryClicked = onRetryClicked
            )
        }
        PlayerListViewModel.Players.Error -> {
            ErrorLayout(
                title = stringResource(R.string.players_list_error),
                onRetryClicked = onRetryClicked
            )
        }
    }
}

@Composable
private fun PlayersDataLayout(
    players: ImmutableList<Player>,
    lazyColumnState: LazyListState,
    pagerItem: PlayerListViewModel.PagerItem,
    onItemClicked: (Int) -> Unit,
    onRetryClicked: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
        state = lazyColumnState,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(items = players, key = { item -> item.id }) { player ->
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onItemClicked(player.id) }
                    .background(MaterialTheme.colorScheme.secondary)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                val picture = loadTeamPicture(teamAbbreviation = player.team.abbreviation)

                TeamLogo(
                    picture = picture.value,
                    contentDescription = player.team.fullName
                )

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "${player.firstName} ${player.lastName}",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSecondary,
                        textAlign = TextAlign.End
                    )

                    Text(
                        text = player.team.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondary,
                        textAlign = TextAlign.End
                    )
                }
            }
        }

        when (pagerItem) {
            PlayerListViewModel.PagerItem.Loading -> {
                item(key = "progress") {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(60.dp)
                    )
                }
            }
            PlayerListViewModel.PagerItem.Retry -> {
                item(key = "retry") {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onRetryClicked() }
                            .background(MaterialTheme.colorScheme.error)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.retry_button),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onError
                        )
                    }
                }
            }
            PlayerListViewModel.PagerItem.None -> {}
        }
    }
}

@Composable
private fun TeamLogo(
    picture: Bitmap?,
    contentDescription: String
) {
    if (picture != null) {
        Image(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .size(80.dp),
            bitmap = picture.asImageBitmap(),
            contentDescription = contentDescription
        )
    }
}