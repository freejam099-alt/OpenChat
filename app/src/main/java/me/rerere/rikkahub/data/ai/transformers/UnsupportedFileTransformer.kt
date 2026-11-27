package me.rerere.rikkahub.data.ai.transformers

import me.rerere.ai.ui.UIMessage
import me.rerere.ai.ui.UIMessagePart
import me.rerere.ai.provider.Modality

/**
 * Transforms residual unsupported attachments into text references after the
 * dedicated document and OCR transformers have already had a chance to consume them.
 */
object UnsupportedFileTransformer : InputMessageTransformer {
    internal fun buildResidualImageFallbackText(
        fileName: String,
        sourceUrl: String,
        workspaceEnabled: Boolean,
    ): String {
        return if (workspaceEnabled) {
            "\n[Image attachment: $fileName - The selected model cannot inspect image pixels directly in this turn because no OCR text was available. If the user explicitly wants tool-based file processing, use the bound Linux workspace tools and import/read the original attachment URL as needed. URL: $sourceUrl]\n"
        } else {
            "\n[Image attachment: $fileName - The selected model cannot inspect image pixels directly in this turn, and no OCR text was available. Do not infer image contents from this attachment alone.]\n"
        }
    }

    override suspend fun transform(
        ctx: TransformerContext,
        messages: List<UIMessage>,
    ): List<UIMessage> {
        val isWorkspaceEnabled = ctx.assistant.workspaceId != null
        val modelSupportsImages = ctx.model.inputModalities.contains(Modality.IMAGE)

        return messages.map { msg ->
            msg.copy(parts = msg.parts.map { part ->
                when (part) {
                    is UIMessagePart.Document -> {
                        // Supported native types (Images/Video/Audio/Text/PDF)
                        val isNative = part.mime.startsWith("image/") ||
                            part.mime.startsWith("text/") ||
                            part.mime.startsWith("video/") ||
                            part.mime.startsWith("audio/") ||
                            part.mime == "application/pdf"

                        if (!isNative) {
                            if (isWorkspaceEnabled) {
                                UIMessagePart.Text("\n[Attachment: ${part.fileName} (${part.mime}) - The bound Linux workspace can process this file. Use workspace_shell or workspace_read_file with the original URL/content as needed. URL: ${part.url}]\n")
                            } else {
                                UIMessagePart.Text("\n[Attachment: ${part.fileName} (${part.mime}) - This file format cannot be processed directly by the model. A Linux workspace must be bound to this assistant to extract or process archive and binary files.]\n")
                            }
                        } else {
                            part
                        }
                    }
                    is UIMessagePart.Image -> {
                        if (!modelSupportsImages) {
                            val filename = part.url.substringAfterLast("/").substringBefore("?").ifEmpty { "image.jpg" }
                            UIMessagePart.Text(
                                buildResidualImageFallbackText(
                                    fileName = filename,
                                    sourceUrl = part.url,
                                    workspaceEnabled = isWorkspaceEnabled,
                                )
                            )
                        } else {
                            part
                        }
                    }
                    else -> part
                }
            })
        }
    }
}

private val ARCHIVE_AND_BINARY_EXTENSIONS = setOf(
    "zip", "tar", "gz", "tgz", "bz2", "xz", "7z", "rar", "z", "lz", "lzma", "lzh",
    "apk", "jar", "war", "ear", "aab", "dex", "class",
    "iso", "img", "dmg", "vmdk", "qcow2",
    "bin", "exe", "dll", "so", "dylib", "elf", "o", "obj", "pyc", "pyo", "pyd",
    "db", "sqlite", "sqlite3", "dat", "bak"
)

private val EXPLICIT_BINARY_MIME_TYPES = setOf(
    "application/zip",
    "application/x-zip-compressed",
    "application/x-tar",
    "application/gzip",
    "application/x-gzip",
    "application/x-bzip2",
    "application/x-7z-compressed",
    "application/x-rar-compressed",
    "application/octet-stream",
    "application/vnd.android.package-archive",
    "application/x-executable",
    "application/x-sharedlib",
    "application/java-archive"
)

fun isArchiveOrBinaryFile(fileName: String, mime: String): Boolean {
    val ext = fileName.substringAfterLast('.', "").lowercase()
    if (ext in ARCHIVE_AND_BINARY_EXTENSIONS) return true
    if (mime.lowercase() in EXPLICIT_BINARY_MIME_TYPES) return true
    return false
}

