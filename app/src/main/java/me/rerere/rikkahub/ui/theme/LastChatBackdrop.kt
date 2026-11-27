package me.rerere.rikkahub.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import com.kyant.backdrop.backdrops.LayerBackdrop

/**
 * Backdrop recorded once at the app root and shared with Kyant0 glass controls
 * (cylindrical composer capsule, buttons). See Kyant0/AndroidLiquidGlass.
 */
val LocalLastChatBackdrop = compositionLocalOf<LayerBackdrop?> { null }

@Composable
fun rememberLastChatBackdrop(): LayerBackdrop =
    com.kyant.backdrop.backdrops.rememberLayerBackdrop()
