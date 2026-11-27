package me.rerere.rikkahub.ui.components.chat

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.unit.dp
import com.wexpa.liquidglass.Glass
import com.wexpa.liquidglass.GlassStyle
import com.wexpa.liquidglass.material3.Surface
import me.rerere.rikkahub.ui.theme.AppSize
import me.rerere.rikkahub.ui.theme.LocalDarkMode

/** The only glass material used by the app, scoped to the chat composer + controls. */
@Composable
private fun composerGlass(interactive: Boolean = false): Glass {
    val dark = LocalDarkMode.current
    val fallback = MaterialTheme.colorScheme.surfaceContainer
    return remember(dark, fallback, interactive) {
        val glass = Glass(GlassStyle.inApp(dark = dark).copy(fallbackSurface = fallback))
        if (interactive) glass.interactive() else glass
    }
}

private val glassBorder: BorderStroke
    @Composable get() = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

/** Cylindrical glass input capsule. */
@Composable
fun RowScope.GlassComposerCapsule(
    modifier: Modifier = Modifier.weight(1f).heightIn(min = AppSize.ChromePill),
    content: @Composable () -> Unit,
) {
    Surface(
        shape = LastChatComposerInputShape,
        glass = composerGlass(),
        border = glassBorder,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = modifier.defaultMinSize(minHeight = AppSize.ChromePill),
        content = content,
    )
}

/** Round glass add button; long click is handled by the inner box. */
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
        border = glassBorder,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    ) {
        Box(
            Modifier.fillMaxSize().combinedClickable(onLongClick = onLongClick, onClick = onClick),
            contentAlignment = Alignment.Center,
            content = { icon() },
        )
    }
}

/** Round glass send/action button. */
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
        border = glassBorder,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = modifier,
    ) {
        content(action)
    }
}
