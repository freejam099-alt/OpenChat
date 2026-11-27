package me.rerere.rikkahub.ui.components.chat

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.wexpa.liquidglass.Glass
import com.wexpa.liquidglass.GlassStyle
import com.wexpa.liquidglass.material3.Surface
import me.rerere.rikkahub.ui.theme.AppSize
import me.rerere.rikkahub.ui.theme.LocalDarkMode

/** Glass used by the composer. The container itself is transparent: colour lives in the glass. */
@Composable
private fun composerGlass(interactive: Boolean = false): Glass {
    val dark = LocalDarkMode.current
    val fallback = MaterialTheme.colorScheme.surfaceContainer
    return remember(dark, fallback, interactive) {
        val glass = Glass(GlassStyle.inApp(dark = dark).copy(fallbackSurface = fallback))
        if (interactive) glass.interactive() else glass
    }
}

/** Full-glass input capsule (ui-core's Material Surface cannot depend on Liquid Glass). */
@Composable
fun RowScope.GlassComposerCapsule(
    modifier: Modifier = Modifier.weight(1f).heightIn(min = AppSize.ChromePill),
    content: @Composable () -> Unit,
) {
    Surface(
        shape = LastChatComposerInputShape,
        glass = composerGlass(),
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = modifier.defaultMinSize(minHeight = AppSize.ChromePill),
        content = content,
    )
}

/** Full-glass add button; long click is handled on the inner box, so glass stays non-interactive. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GlassComposerAddButton(
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
) {
    Surface(
        shape = CircleShape,
        glass = composerGlass(),
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    ) {
        Box(
            Modifier.fillMaxSize().combinedClickable(onLongClick = onLongClick, onClick = onClick),
            contentAlignment = Alignment.Center,
            content = icon,
        )
    }
}

/** Full-glass action/send button. */
@Composable
fun GlassComposerActionButton(
    action: LastChatComposerAction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (LastChatComposerAction) -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        glass = composerGlass(interactive = true),
        contentColor = Color.Unspecified,
        modifier = modifier,
    ) {
        content(action)
    }
}
