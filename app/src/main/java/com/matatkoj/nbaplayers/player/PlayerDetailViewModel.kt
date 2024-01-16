package com.matatkoj.nbaplayers.player

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import com.jakewharton.rxrelay3.BehaviorRelay
import com.jakewharton.rxrelay3.PublishRelay
import com.matatkoj.nbaplayers.NbaNavigation.Companion.PLAYER_ID_KEY
import com.matatkoj.nbaplayers.common.NbaViewModel
import com.matatkoj.nbaplayers.data.response.Player
import com.matatkoj.nbaplayers.data.repository.NbaRepository
import com.matatkoj.nbaplayers.util.requireValue
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class PlayerDetailViewModel @Inject constructor(
    nbaRepository: NbaRepository,
    private val state: SavedStateHandle
): NbaViewModel() {

    private val playerId: Int get() = state[PLAYER_ID_KEY]
        ?: throw IllegalStateException("Missing player id argument!")

    @Immutable
    sealed interface PlayerDetails {
        data object Loading: PlayerDetails
        data class Success(val details: Player): PlayerDetails
        data class Error(val error: Throwable): PlayerDetails
    }

///// RETRY

    private val retryRelay: PublishRelay<Unit> = PublishRelay.create()

    private val retryStream: Flowable<Unit> = retryRelay
        .observeOn(Schedulers.io())
        .subscribeOn(Schedulers.io())
        .toFlowable(BackpressureStrategy.DROP)

    fun onRetryClicked() = retryRelay.accept(Unit)

///// PLAYER DETAIL

    private val playerDetailsRelay: BehaviorRelay<PlayerDetails> = BehaviorRelay.createDefault(
        PlayerDetails.Loading
    )
    val playerDetailsStream: Observable<PlayerDetails> = playerDetailsRelay
        .observeOn(Schedulers.io()).subscribeOn(Schedulers.io())

    fun getPlayerDetailsBlocking(): PlayerDetails = playerDetailsRelay.requireValue()

    private val fetchPlayerDetailsStream = nbaRepository.getPlayer(playerId)
        .timeout(10, TimeUnit.SECONDS)
        .map<PlayerDetails>(PlayerDetails::Success)
        .doOnSuccess(playerDetailsRelay::accept)
        .doOnError { playerDetailsRelay.accept(PlayerDetails.Error(it)) }
        .retryWhen {
            it.switchMap {
                retryStream
                    .doOnNext { playerDetailsRelay.accept(PlayerDetails.Loading) }
            }
        }

    init {
        clear.add(
            fetchPlayerDetailsStream.subscribe()
        )
    }
}