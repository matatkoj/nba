package com.matatkoj.nbaplayers.ui.transition

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.slideIn
import androidx.compose.ui.unit.IntOffset

fun enterSlideFromRightTransition(): EnterTransition {
    return slideIn(initialOffset = { IntOffset(it.width, 0) })
}

fun enterSlideFromLeftTransition(): EnterTransition {
    return slideIn(initialOffset = { IntOffset(-it.width, 0) })
}