package me.rerere.rikkahub.ui.components.ui

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.wexpa.liquidglass.Glass
import com.wexpa.liquidglass.GlassStyle
import com.wexpa.liquidglass.glassEffect
import me.rerere.rikkahub.ui.theme.AppShapes
import me.rerere.rikkahub.ui.theme.LocalDarkMode
import me.rerere.rikkahub.ui.theme.LocalLastChatBackdrop

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

/**
 * Cylindrical/lens glass from Kyant0 AndroidLiquidGlass. Used on the chat composer capsule and
 * other controls that need a true refraction cylinder rather than a flat frosted surface.
 * Falls back to the plain modifier when the root backdrop is unavailable (e.g. previews).
 */
@Composable
fun Modifier.glassCylinder(
    shape: Shape = RoundedCornerShape(24.dp),
): Modifier {
    val backdrop = LocalLastChatBackdrop.current ?: return this
    return this.drawBackdrop(
        backdrop = backdrop,
        shape = { shape },
        effects = {
            vibrancy()
            blur(8f.dp.toPx())
            lens(10f.dp.toPx(), 20f.dp.toPx())
        },
    )
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
