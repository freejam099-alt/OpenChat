package me.rerere.highlight.android

import android.content.Context
import me.rerere.highlight.Highlighter
import me.rerere.highlight.HighlightToken

/**
 * Plain-text highlighter.
 *
 * The embedded JS runtime was removed, so syntax highlighting is intentionally
 * a no-op: code is returned as a single plain token and rendered as-is.
 * The [Highlighter] contract and [HighlightToken] types remain unchanged for callers.
 */
class AndroidHighlighter(@Suppress("UNUSED_PARAMETER") ctx: Context) : Highlighter {
    override suspend fun highlight(code: String, language: String): List<HighlightToken> {
        return listOf(HighlightToken.Plain(content = code))
    }

    override fun destroy() = Unit
}
