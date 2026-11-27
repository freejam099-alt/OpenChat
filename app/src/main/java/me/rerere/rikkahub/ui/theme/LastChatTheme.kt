package me.rerere.rikkahub.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.wexpa.liquidglass.LiquidGlassTheme

/**
 * Single root theme for LastChat.
 *
 * `RikkahubTheme` resolves Material You colours and exposes `LocalDarkMode`; `LiquidGlassTheme`
 * then records the screen once per frame so every glass component below it can refract what is
 * actually behind it. The `background` MUST be the colour the app paints where it draws nothing,
 * otherwise glass panels grow a halo of the wrong colour (per Liquid Glass getting-started guide).
 */
@Composable
fun LastChatTheme(
    content: @Composable () -> Unit,
) {
    RikkahubTheme {
        val dark = LocalDarkMode.current
        val ground = MaterialTheme.colorScheme.background
        LiquidGlassTheme(
            dark = dark,
            background = ground,
            content = content,
        )
    }
}
