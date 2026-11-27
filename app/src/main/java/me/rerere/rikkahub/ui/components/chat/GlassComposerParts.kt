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
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.rerere.rikkahub.ui.theme.AppSize

/** Translucent in-place glass (no overlay renderer). */
@Composable
private fun glassColor(alpha: Float = 0.55f): Color =
    MaterialTheme.colorScheme.surfaceContainer.copy(alpha = alpha)

private val glassBorder: BorderStroke
    @Composable get() = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))

/** Input capsule with a real rounded shape, not a floating overlay. */
@Composable
fun RowScope.GlassComposerCapsule(
    modifier: Modifier = Modifier.weight(1f).heightIn(min = AppSize.ChromePill),
    content: @Composable () -> Unit,
) {
    Surface(
        shape = LastChatComposerInputShape,
        color = glassColor(),
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = glassBorder,
        modifier = modifier.defaultMinSize(minHeight = AppSize.ChromePill),
        content = content,
    )
}

/** Add button: long click handled on the inner box. */
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
        color = glassColor(),
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        border = glassBorder,
        modifier = modifier,
    ) {
        Box(
            Modifier.fillMaxSize().combinedClickable(onLongClick = onLongClick, onClick = onClick),
            contentAlignment = Alignment.Center,
            content = { icon() },
        )
    }
}

/** Send/action button. */
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
        color = glassColor(0.75f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = glassBorder,
        modifier = modifier,
    ) {
        content(action)
    }
}
