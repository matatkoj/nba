package com.matatkoj.nbaplayers.team

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import com.jakewharton.rxrelay3.BehaviorRelay
import com.jakewharton.rxrelay3.PublishRelay
import com.matatkoj.nbaplayers.NbaNavigation.Companion.TEAM_ID_KEY
import com.matatkoj.nbaplayers.common.NbaViewModel
import com.matatkoj.nbaplayers.data.repository.NbaRepository
import com.matatkoj.nbaplayers.data.response.Team
import com.matatkoj.nbaplayers.util.requireValue
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class TeamDetailViewModel @Inject constructor(
    nbaRepository: NbaRepository,
    private val savedStateHandle: SavedStateHandle
): NbaViewModel() {

    private val teamId: Int get() = savedStateHandle[TEAM_ID_KEY]
        ?: throw IllegalStateException("Missing team id argument!")

    @Immutable
    sealed interface TeamDetails {
        data object Loading: TeamDetails
        data class Success(val details: Team): TeamDetails
        data class Error(val error: Throwable): TeamDetails
    }

///// RETRY

    private val retryRelay: PublishRelay<Unit> = PublishRelay.create()

    private val retryStream: Flowable<Unit> = retryRelay
        .observeOn(Schedulers.io())
        .subscribeOn(Schedulers.io())
        .toFlowable(BackpressureStrategy.DROP)

    fun onRetryClicked() = retryRelay.accept(Unit)

///// TEAM DETAILS

    private val teamDetailsRelay: BehaviorRelay<TeamDetails> = BehaviorRelay.createDefault(
        TeamDetails.Loading
    )

    val teamDetailsStream: Observable<TeamDetails> = teamDetailsRelay
        .observeOn(Schedulers.io()).subscribeOn(Schedulers.io())

    fun getTeamDetailsBlocking(): TeamDetails = teamDetailsRelay.requireValue()

    private val fetchTeamDetailsStream = nbaRepository.getTeam(teamId)
        .timeout(10, TimeUnit.SECONDS)
        .map<TeamDetails>(TeamDetails::Success)
        .doOnSuccess(teamDetailsRelay::accept)
        .doOnError { teamDetailsRelay.accept(TeamDetails.Error(it)) }
        .retryWhen {
            it.switchMap {
                retryStream
                    .doOnNext { teamDetailsRelay.accept(TeamDetails.Loading) }
            }
        }

    init {
        clear.add(
            fetchTeamDetailsStream.subscribe()
        )
    }
}