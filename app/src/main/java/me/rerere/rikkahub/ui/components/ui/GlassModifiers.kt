package me.rerere.rikkahub.ui.components.ui

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape

/**
 * Compatibility helpers kept so existing call sites compile.
 *
 * They intentionally apply no overlay: the Liquid Glass overlay renderer produced duplicate
 * shapes and refracted content that was not behind the panel. Glass is now expressed directly
 * by translucent Material3 surfaces with shape/border, so these modifiers are no-ops.
 */
@Composable
fun Modifier.lastChatGlass(
    shape: Shape = CircleShape,
): Modifier = this

@Composable
fun Modifier.lastChatGlassInteractive(
    shape: Shape = CircleShape,
): Modifier = this
