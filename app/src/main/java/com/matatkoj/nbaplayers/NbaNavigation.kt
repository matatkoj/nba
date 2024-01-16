package com.matatkoj.nbaplayers

sealed class NbaNavigation(val route: String) {

    companion object {
        const val PLAYERS_PATH = "players"
        const val TEAMS_PATH = "teams"

        const val PLAYER_ID_KEY = "playerId"
        const val TEAM_ID_KEY = "teamId"
    }

    data object PlayerList: NbaNavigation("$PLAYERS_PATH")
    data object PlayerDetail: NbaNavigation("$PLAYERS_PATH/{$PLAYER_ID_KEY}")
    data object TeamDetail: NbaNavigation("$TEAMS_PATH/{$TEAM_ID_KEY}")
}