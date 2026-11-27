package me.rerere.rikkahub.data.ai

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import me.rerere.ai.provider.SiliconFlowFreeApiPolicy
import me.rerere.rikkahub.utils.JsonInstant
import me.rerere.rikkahub.utils.jsonPrimitiveOrNull
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class AIRequestInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val host = request.url.host

        if (host == "api.siliconflow.cn") {
            request = processSiliconCloudRequest(request)
        }

        return chain.proceed(request)
    }

    private fun processSiliconCloudRequest(request: Request): Request {
        val bodyJson = request.readBodyAsJson()
        val model = bodyJson?.jsonObject["model"]?.jsonPrimitiveOrNull?.content
        // Remote config was removed; no free-model list or key is bundled anymore.
        val freeModels = emptyList<String>()
        val freeApiKey = ""
        val shouldUseFreeApiKey = SiliconFlowFreeApiPolicy.shouldUseFreeApiKey(
            host = request.url.host,
            path = request.url.encodedPath,
            authorizationHeader = request.header("Authorization"),
            model = model,
            freeModels = freeModels,
        )

        if (shouldUseFreeApiKey) {
            return request.newBuilder()
                .header("Authorization", "Bearer $freeApiKey")
                .build()
        }

        return request
    }
}

private fun Request.readBodyAsJson(): JsonElement? {
    val contentType = body?.contentType()
    if (contentType?.type == "application" && contentType.subtype == "json") {
        val buffer = okio.Buffer()
        buffer.use {
            body?.writeTo(it)
            return JsonInstant.parseToJsonElement(buffer.readUtf8())
        }
    }
    return null
}
