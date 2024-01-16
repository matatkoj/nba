package com.matatkoj.nbaplayers.util

import com.jakewharton.rxrelay3.BehaviorRelay

fun <T> BehaviorRelay<T>.valueOrNull(): T? {
    return this.value
}

fun <T> BehaviorRelay<T>.requireValue(): T {
    return this.value
        ?: throw IllegalStateException("Missing required value in BehaviorRelay!")
}