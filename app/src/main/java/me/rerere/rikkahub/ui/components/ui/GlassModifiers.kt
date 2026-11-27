package me.rerere.rikkahub.ui.components.ui

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import com.wexpa.liquidglass.Glass
import com.wexpa.liquidglass.GlassStyle
import com.wexpa.liquidglass.glassEffect
import me.rerere.rikkahub.ui.theme.AppShapes
import me.rerere.rikkahub.ui.theme.LocalDarkMode

/**
 * LastChat's glass surface modifier for floating cards.
 *
 * `fallbackSurface` keeps labels readable on API < 33, where Liquid Glass degrades from the
 * refraction shader to a tinted surface with rim lighting (see Liquid Glass getting-started).
 */
@Composable
fun Modifier.lastChatGlass(
    shape: Shape = AppShapes.CardMedium,
): Modifier {
    val dark = LocalDarkMode.current
    val fallbackSurface = MaterialTheme.colorScheme.surfaceContainer
    val glass = remember(dark, fallbackSurface) {
        Glass(
            GlassStyle.inApp(dark = dark).copy(fallbackSurface = fallbackSurface),
        )
    }
    return this.glassEffect(glass, shape)
}

/** Interactive glass for controls that act on a tap (FAB, send, add buttons). */
@Composable
fun Modifier.lastChatGlassInteractive(
    shape: Shape = CircleShape,
): Modifier {
    val dark = LocalDarkMode.current
    val fallbackSurface = MaterialTheme.colorScheme.surfaceContainer
    val glass = remember(dark, fallbackSurface) {
        Glass(
            GlassStyle.inApp(dark = dark).copy(fallbackSurface = fallbackSurface),
        ).interactive()
    }
    return this.glassEffect(glass, shape)
}
