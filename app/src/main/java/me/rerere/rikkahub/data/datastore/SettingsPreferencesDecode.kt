package me.rerere.rikkahub.data.datastore

import android.util.Log
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import me.rerere.ai.provider.ProviderSetting
import me.rerere.common.http.jsonArrayOrNull
import me.rerere.rikkahub.data.ai.mcp.McpServerConfig
import me.rerere.rikkahub.data.model.Assistant
import me.rerere.rikkahub.utils.JsonInstant
import me.rerere.search.SearchServiceOptions
import me.rerere.tts.provider.TTSProviderSetting
import kotlin.uuid.Uuid

private const val TAG = "SettingsDecode"

/**
 * Decode a JSON array one element at a time so a single unknown polymorphic
 * subtype (removed provider, old search backend, etc.) cannot wipe the rest
 * of settings — or the user's chats, which are keyed by assistant IDs stored here.
 */
internal inline fun <reified T> decodePreferenceList(json: String?): List<T>? {
    if (json.isNullOrBlank()) return null
    val element = runCatching { JsonInstant.parseToJsonElement(json) }.getOrElse {
        logDecodeError("Failed to parse preference JSON array for ${T::class.simpleName}", it)
        return null
    }
    val array = element.jsonArrayOrNull ?: return runCatching {
        JsonInstant.decodeFromString<List<T>>(json)
    }.getOrElse {
        logDecodeError("Failed to decode preference list ${T::class.simpleName}", it)
        null
    }
    return array.mapNotNull { item ->
        runCatching { JsonInstant.decodeFromJsonElement<T>(item) }.getOrElse {
            logDecodeError("Skipping invalid ${T::class.simpleName} entry", it)
            null
        }
    }
}

internal inline fun <reified T> decodePreferenceValue(json: String?, fallback: T): T {
    if (json.isNullOrBlank()) return fallback
    return runCatching { JsonInstant.decodeFromString<T>(json) }.getOrElse {
        logDecodeError("Failed to decode preference value ${T::class.simpleName}", it)
        fallback
    }
}

private fun logDecodeError(message: String, error: Throwable) {
    runCatching { Log.w(TAG, message, error) }
}

/**
 * Decode settings from a backup blob one polymorphic list entry at a time. Old backups may
 * contain provider subtypes that no longer exist (e.g. an on-device provider); dropping just
 * those entries keeps the rest of the settings — and the chats keyed by their assistant IDs —
 * restorable.
 */
internal fun decodeSettingsFromBackup(raw: String): Settings? {
    val element = runCatching { JsonInstant.parseToJsonElement(raw) }.getOrElse {
        logDecodeError("Failed to parse backup settings JSON", it)
        return null
    }
    val obj = element as? JsonObject ?: run {
        logDecodeError("Backup settings JSON was not an object", IllegalArgumentException("Not a JSON object"))
        return null
    }
    val sanitized = obj.toMap().toMutableMap().apply {
        sanitizeEntryList<ProviderSetting>("providers")
        sanitizeEntryList<Assistant>("assistants")
        sanitizeEntryList<SearchServiceOptions>("searchServices")
        sanitizeEntryList<McpServerConfig>("mcpServers")
        sanitizeEntryList<TTSProviderSetting>("ttsProviders")
    }
    return runCatching { JsonInstant.decodeFromJsonElement<Settings>(JsonObject(sanitized)) }
        .getOrElse {
            logDecodeError("Failed to decode backup settings", it)
            null
        }
}

private inline fun <reified T> MutableMap<String, JsonElement>.sanitizeEntryList(key: String) {
    val array = this[key] as? JsonArray ?: return
    this[key] = JsonArray(array.filter { item ->
        runCatching { JsonInstant.decodeFromJsonElement<T>(item) }
            .onFailure { logDecodeError("Skipping invalid $key entry in backup settings", it) }
            .isSuccess
    })
}

internal fun decodeUuidOrNull(raw: String?): Uuid? {
    if (raw.isNullOrBlank()) return null
    return runCatching { Uuid.parse(raw) }.getOrNull()
}

internal fun decodeUuid(raw: String?, fallback: Uuid): Uuid = decodeUuidOrNull(raw) ?: fallback

/**
 * Re-attach stub assistants for conversation rows whose character was dropped from
 * DataStore (typical after a settings parse failure). Does not invent assistants
 * when there are no matching chats.
 */
internal fun Settings.withRecoveredAssistantsFromConversations(
    conversationAssistantIds: Collection<String>,
): Settings {
    val known = assistants.map { it.id }.toSet()
    val recovered = conversationAssistantIds
        .mapNotNull(::decodeUuidOrNull)
        .distinct()
        .filter { it !in known }
        .map { id ->
            Assistant(
                id = id,
                name = "Recovered",
            )
        }
    if (recovered.isEmpty()) return this
    val mergedAssistants = assistants + recovered
    val selectedExists = mergedAssistants.any { it.id == assistantId }
    return copy(
        assistants = mergedAssistants,
        assistantId = if (selectedExists) assistantId else recovered.first().id,
    )
}
