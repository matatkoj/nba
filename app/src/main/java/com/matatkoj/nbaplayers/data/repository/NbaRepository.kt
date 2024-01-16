package com.matatkoj.nbaplayers.data.repository

import com.matatkoj.nbaplayers.data.api.NbaApi
import com.matatkoj.nbaplayers.data.response.Player
import com.matatkoj.nbaplayers.data.response.PlayerList
import com.matatkoj.nbaplayers.data.response.Team
import dagger.hilt.android.scopes.ActivityScoped
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

@ActivityScoped
class NbaRepository @Inject constructor(
    private val api: NbaApi
) {

    fun getPlayerList(page: Int, pageSize: Int): Single<PlayerList> {
        return api.getPlayerList(page, pageSize)
    }

    fun getPlayer(id: Int): Single<Player> {
        return api.getPlayer(id)
    }

    fun getTeam(id: Int): Single<Team> {
        return api.getTeam(id)
    }
}