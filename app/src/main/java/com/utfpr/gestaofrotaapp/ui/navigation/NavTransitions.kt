package com.utfpr.gestaofrotaapp.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavBackStackEntry

private const val DURATION = 300

/** Shared Axis (Z) — lista → detalhe (hierárquica). Usa scale para efeito de Container Transform. */
fun AnimatedContentTransitionScope<NavBackStackEntry>.sharedAxisZEnter() =
    slideInHorizontally(animationSpec = tween(DURATION)) { it } +
            fadeIn(animationSpec = tween(DURATION)) +
            scaleIn(
                initialScale = 0.9f,
                animationSpec = tween(DURATION)
            )

fun AnimatedContentTransitionScope<NavBackStackEntry>.sharedAxisZExit() =
    slideOutHorizontally(animationSpec = tween(DURATION)) { -it / 4 } +
            fadeOut(animationSpec = tween(DURATION)) +
            scaleOut(
                targetScale = 0.9f,
                animationSpec = tween(DURATION)
            )

fun AnimatedContentTransitionScope<NavBackStackEntry>.sharedAxisZPopEnter() =
    slideInHorizontally(animationSpec = tween(DURATION)) { -it / 4 } +
            fadeIn(animationSpec = tween(DURATION)) +
            scaleIn(
                initialScale = 0.95f,
                animationSpec = tween(DURATION)
            )

fun AnimatedContentTransitionScope<NavBackStackEntry>.sharedAxisZPopExit() =
    slideOutHorizontally(animationSpec = tween(DURATION)) { it } +
            fadeOut(animationSpec = tween(DURATION)) +
            scaleOut(
                targetScale = 0.95f,
                animationSpec = tween(DURATION)
            )

/** Fade Through — telas sem relação hierárquica (ex: lista ↔ formulário) */
fun AnimatedContentTransitionScope<NavBackStackEntry>.fadeThroughEnter() =
    fadeIn(animationSpec = tween(DURATION)) +
            scaleIn(
                initialScale = 0.95f,
                animationSpec = tween(DURATION)
            )

fun AnimatedContentTransitionScope<NavBackStackEntry>.fadeThroughExit() =
    fadeOut(animationSpec = tween(DURATION)) +
            scaleOut(
                targetScale = 0.95f,
                animationSpec = tween(DURATION)
            )

fun AnimatedContentTransitionScope<NavBackStackEntry>.fadeThroughPopEnter() =
    fadeIn(animationSpec = tween(DURATION)) +
            scaleIn(
                initialScale = 0.95f,
                animationSpec = tween(DURATION)
            )

fun AnimatedContentTransitionScope<NavBackStackEntry>.fadeThroughPopExit() =
    fadeOut(animationSpec = tween(DURATION)) +
            scaleOut(
                targetScale = 0.95f,
                animationSpec = tween(DURATION)
            )
