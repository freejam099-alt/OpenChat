package me.rerere.rikkahub.ui.theme

import androidx.compose.runtime.Composable

/**
 * Root theme for LastChat.
 *
 * The Liquid Glass overlay engine was removed: it draws glass in a separate root overlay, which
 * produced double shapes, refraction of content that should not be behind the panel, and
 * cross-window failures inside dialogs/sheets. Glass is now expressed with translucent
 * Material3 surfaces (shape + border + colour with alpha) drawn in-place — no overlay layer.
 */
@Composable
fun LastChatTheme(
    content: @Composable () -> Unit,
) {
    RikkahubTheme(content = content)
}
