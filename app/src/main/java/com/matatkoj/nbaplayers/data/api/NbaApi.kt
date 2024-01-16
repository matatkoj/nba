package com.matatkoj.nbaplayers.data.api

import com.matatkoj.nbaplayers.data.response.Player
import com.matatkoj.nbaplayers.data.response.PlayerList
import com.matatkoj.nbaplayers.data.response.Team
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface NbaApi {

    @GET("players")
    fun getPlayerList(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int
    ): Single<PlayerList>

    @GET("players/{id}")
    fun getPlayer(
        @Path("id") id: Int
    ): Single<Player>

    @GET("teams/{id}")
    fun getTeam(
        @Path("id") id: Int
    ): Single<Team>
}