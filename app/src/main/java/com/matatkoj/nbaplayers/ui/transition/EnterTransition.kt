package com.matatkoj.nbaplayers.ui.transition

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.tween
import androidx.navigation.NavBackStackEntry
import com.matatkoj.nbaplayers.Constants.NAVIGATION_ANIMATION_DURATION

fun AnimatedContentTransitionScope<NavBackStackEntry>.enterTransition(): EnterTransition {
    return slideIntoContainer(
        AnimatedContentTransitionScope.SlideDirection.Left,
        animationSpec = tween(NAVIGATION_ANIMATION_DURATION)
    )
}

fun AnimatedContentTransitionScope<NavBackStackEntry>.popEnterTransition(): EnterTransition {
    return slideIntoContainer(
        AnimatedContentTransitionScope.SlideDirection.Right,
        animationSpec = tween(NAVIGATION_ANIMATION_DURATION)
    )
}