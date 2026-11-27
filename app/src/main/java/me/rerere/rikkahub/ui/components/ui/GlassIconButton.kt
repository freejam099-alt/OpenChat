package me.rerere.rikkahub.ui.components.ui

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.wexpa.liquidglass.Glass
import com.wexpa.liquidglass.material3.IconButton

/** Round glass icon button — one of the few components that opts into Liquid Glass. */
@Composable
fun GlassIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        glass = Glass.Regular.interactive(),
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = CircleShape,
        content = content,
    )
}
