package me.rerere.rikkahub.ui.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.kyant.backdrop.backdrops.layerBackdrop
import com.wexpa.liquidglass.LiquidGlassTheme

/**
 * Single root theme for LastChat.
 *
 * `RikkahubTheme` resolves Material You colours and exposes `LocalDarkMode`; `LiquidGlassTheme`
 * records the screen for the shayxo glass components, while the Kyant0 `LayerBackdrop` records
 * the same screen for cylindrical/refraction controls. `background` must be the colour the app
 * paints where it draws nothing, otherwise glass panels grow a halo of the wrong colour.
 */
@Composable
fun LastChatTheme(
    content: @Composable () -> Unit,
) {
    RikkahubTheme {
        val dark = LocalDarkMode.current
        val ground = MaterialTheme.colorScheme.background
        val backdrop = rememberLastChatBackdrop()
        Box(
            Modifier
                .fillMaxSize()
                .layerBackdrop(backdrop),
        ) {
            CompositionLocalProvider(LocalLastChatBackdrop provides backdrop) {
                LiquidGlassTheme(
                    dark = dark,
                    background = ground,
                    content = content,
                )
            }
        }
    }
}
