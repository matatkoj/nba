package com.matatkoj.nbaplayers.ui.transition

import androidx.compose.animation.ExitTransition
import androidx.compose.animation.slideOut
import androidx.compose.ui.unit.IntOffset

fun exitSlideToRightTransition(): ExitTransition {
    return slideOut(targetOffset = { IntOffset(it.width, 0) })
}

fun exitSlideToLeftTransition(): ExitTransition {
    return slideOut(targetOffset = { IntOffset(-it.width, 0) })
}