package me.rerere.rikkahub.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.wexpa.liquidglass.LiquidGlassTheme

/**
 * Root theme. Liquid Glass is enabled here because a few opted-in components (floating navbar,
 * chat input bar, icon buttons and FABs) need the glass host. Nothing else in the tree opts in.
 */
@Composable
fun LastChatTheme(
    content: @Composable () -> Unit,
) {
    RikkahubTheme {
        LiquidGlassTheme(
            dark = LocalDarkMode.current,
            background = MaterialTheme.colorScheme.background,
            content = content,
        )
    }
}
