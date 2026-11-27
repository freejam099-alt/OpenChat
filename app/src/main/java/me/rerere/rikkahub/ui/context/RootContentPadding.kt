package me.rerere.rikkahub.ui.context

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

/**
 * Content padding reserved by the root Scaffold for the global floating navbar and system bars.
 * Screens consume this instead of hardcoding navbar-height guesses.
 */
val LocalRootContentPadding = staticCompositionLocalOf<PaddingValues> { PaddingValues(0.dp) }
