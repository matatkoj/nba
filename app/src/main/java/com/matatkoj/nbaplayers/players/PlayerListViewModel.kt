package com.matatkoj.nbaplayers.players

import android.util.Log
import androidx.compose.runtime.Immutable
import com.jakewharton.rxrelay3.BehaviorRelay
import com.jakewharton.rxrelay3.PublishRelay
import com.matatkoj.nbaplayers.Constants.PAGE_SIZE
import com.matatkoj.nbaplayers.common.NbaViewModel
import com.matatkoj.nbaplayers.data.response.Player
import com.matatkoj.nbaplayers.data.response.PlayerList
import com.matatkoj.nbaplayers.data.repository.NbaRepository
import com.matatkoj.nbaplayers.util.requireValue
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class PlayerListViewModel @Inject constructor(
    nbaRepository: NbaRepository
): NbaViewModel() {

    companion object {
        private const val FIRST_PAGE_NUMBER = 1
    }

    sealed interface Players {
        data object Loading: Players
        data class Success(val playerList: PersistentList<Player>): Players
        data object Error: Players
    }

///// PAGINATION

    private var hasNextPage: Boolean = true

    private val nextPageRelay: BehaviorRelay<Int> = BehaviorRelay.createDefault(FIRST_PAGE_NUMBER)

    private val nextPageStream: Observable<Int> = nextPageRelay
        .observeOn(Schedulers.io())
        .subscribeOn(Schedulers.io())

    fun onNextPageRequested() {
        if (hasNextPage) nextPageRelay.accept((nextPageRelay.requireValue()) + 1)
    }

///// RETRY

    private val retryRelay: PublishRelay<Unit> = PublishRelay.create()

    private val retryStream: Flowable<Unit> = retryRelay
        .observeOn(Schedulers.io())
        .subscribeOn(Schedulers.io())
        .toFlowable(BackpressureStrategy.DROP)

    fun onRetryClicked() = retryRelay.accept(Unit)

///// DATA

    @Immutable
    sealed interface PagerItem {
        data object None: PagerItem
        data object Retry: PagerItem
        data object Loading: PagerItem
    }

    private val pagerItemRelay: BehaviorRelay<PagerItem> = BehaviorRelay.createDefault(PagerItem.None)

    val pagerItemStream: Observable<PagerItem> = pagerItemRelay
        .observeOn(Schedulers.io())
        .subscribeOn(Schedulers.io())

    fun getPagerItemBlocking(): PagerItem = pagerItemRelay.requireValue()

    private val playersRelay: BehaviorRelay<Players> = BehaviorRelay.createDefault(Players.Loading)

    val playersStream: Observable<Players> = playersRelay
        .observeOn(Schedulers.io())
        .subscribeOn(Schedulers.io())

    fun getPlayersBlocking(): Players = playersRelay.requireValue()

    private val fetchPlayersStream = nextPageStream
        .switchMapCompletable { nextPage ->
            nbaRepository.getPlayerList(nextPage, PAGE_SIZE)
                .timeout(10, TimeUnit.SECONDS)
                .doOnSubscribe { pagerItemRelay.accept(PagerItem.Loading) }
                .doOnSuccess(::onSuccess)
                .doOnError(::onError)
                .retryWhen {
                    it.switchMap {
                        retryStream
                            .doOnNext { playersRelay.accept(Players.Loading) }
                    }
                }
                .ignoreElement()
                .doOnComplete { pagerItemRelay.accept(PagerItem.None) }
        }

    private fun onSuccess(players: PlayerList) {
        hasNextPage = players.meta.nextPage != null

        val currentPlayerList = (playersRelay.requireValue() as? Players.Success)?.playerList
            ?: emptyList()

        playersRelay.accept(
            Players.Success(
                currentPlayerList.plus(players.data).toPersistentList()
            )
        )
    }

    private fun onError(error: Throwable) {
        if (playersRelay.requireValue() is Players.Success) {
            // if there is already some data, show only error notification
            pagerItemRelay.accept(PagerItem.Retry)
        } else {
            // otherwise show error instead of data
            playersRelay.accept(Players.Error)
        }
    }

    init {
        clear.add(
            fetchPlayersStream.subscribe()
        )
    }
}