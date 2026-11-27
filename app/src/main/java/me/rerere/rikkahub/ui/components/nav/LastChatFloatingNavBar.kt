package me.rerere.rikkahub.ui.components.nav

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wexpa.liquidglass.GlassTabBar
import me.rerere.rikkahub.ui.hooks.HapticPattern
import me.rerere.rikkahub.ui.hooks.PremiumHaptics
import me.rerere.rikkahub.ui.hooks.rememberPremiumHaptics

private data class FloatingTab(val label: String, val icon: ImageVector)

private val floatingTabs = listOf(
    FloatingTab("Chat", Icons.Rounded.Chat),
    FloatingTab("Provider", Icons.Rounded.Tune),
    FloatingTab("UI", Icons.Rounded.Palette),
    FloatingTab("More", Icons.Rounded.GridView),
)

/** Global iOS-style floating glass tab bar (the only navbar glass in the app). */
@Composable
fun LastChatFloatingNavBar(
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    // AGENTS.md forbids direct LocalHapticFeedback usage: bridge the tab ticks to PremiumHaptics.
    val premiumHaptics = rememberPremiumHaptics()
    val hapticAdapter = remember(premiumHaptics) {
        object : HapticFeedback {
            override fun performHapticFeedback(type: HapticFeedbackType) {
                when (type) {
                    HapticFeedbackType.LongPress -> premiumHaptics.perform(HapticPattern.Pop)
                    HapticFeedbackType.TextHandleMove -> premiumHaptics.perform(HapticPattern.Tick)
                    else -> premiumHaptics.perform(HapticPattern.Selection)
                }
            }
        }
    }
    CompositionLocalProvider(LocalHapticFeedback provides hapticAdapter) {
        GlassTabBar(
            itemCount = floatingTabs.size,
            selectedIndex = selectedIndex,
            onSelected = onSelected,
            modifier = modifier
                .navigationBarsPadding()
                .padding(horizontal = 14.dp)
                .shadow(15.dp, RoundedCornerShape(percent = 50), clip = false),
        ) { index, isSelected ->
            val tab = floatingTabs[index]
            val tint: Color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                LocalContentColor.current
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(tab.icon, contentDescription = tab.label, tint = tint)
                Spacer(Modifier.height(3.dp))
                Text(
                    text = tab.label,
                    color = tint,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
        }
    }
}
