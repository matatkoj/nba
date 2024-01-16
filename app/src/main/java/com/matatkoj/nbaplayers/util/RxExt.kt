package com.matatkoj.nbaplayers.util

import com.jakewharton.rxrelay3.BehaviorRelay

fun <T> BehaviorRelay<T>.valueOrNull(): T? {
    return this.value
}

fun <T> BehaviorRelay<T>.requireValue(): T {
    return valueOrNull()
        ?: throw IllegalStateException("Missing required value in BehaviorRelay!")
}