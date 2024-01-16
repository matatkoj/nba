package com.matatkoj.nbaplayers.common

import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable

open class NbaViewModel: ViewModel() {

    protected val clear = CompositeDisposable()

    override fun onCleared() {
        clear.dispose()
        super.onCleared()
    }
}