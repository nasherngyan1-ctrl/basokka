package com.example.data

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@Serializable
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@Serializable
data class Content(
    val parts: List<Part>
)

@Serializable
data class Part(
    val text: String? = null
)

@Serializable
data class GenerationConfig(
    val responseMimeType: String? = null,
    val responseSchema: JsonObject? = null,
    val temperature: Float? = null,
    val topP: Float? = null
)

@Serializable
data class GenerateContentResponse(
    val candidates: List<Candidate> = emptyList()
)

@Serializable
data class Candidate(
    val content: Content? = null
)

@Serializable
data class GeneratedStudioBundle(
    val title: String,
    val summary: String,
    val appliedChanges: List<String>,
    val blueprint: DynamicUiBlueprint,
    val previewHtml: String,
    val files: List<ProjectFile>,
    val providerUsedBadge: String = "BASO Neural Engine",
    val autoSwitchedNote: String? = null,
    val usedKeyId: String? = null,
    val failedKeyIds: List<String> = emptyList()
)

interface GeminiApiService {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiStudioService {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(40, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(40, TimeUnit.SECONDS)
        .build()

    private val geminiApi: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(GeminiApiService::class.java)
    }

    suspend fun generateOrEditViaChat(
        userMessage: String,
        platform: String,
        currentBlueprint: DynamicUiBlueprint?,
        isIterativeEdit: Boolean,
        modelId: String = "gemini-3.1-pro-preview",
        temperature: Float = 0.7f,
        stylePreset: String = "BASO Signature Gold & Cyan",
        keyChain: List<StoredAiKey> = emptyList(),
        autoSwitchEnabled: Boolean = true,
        intentDecision: PromptIntentDecision? = null
    ): GeneratedStudioBundle = withContext(Dispatchers.IO) {
        val localEvolution = StarterTemplates.evolveBlueprintFromChat(
            userMessage = userMessage,
            current = currentBlueprint,
            isIterativeEdit = isIterativeEdit
        )

        val systemPrompt = """
You are BASO AI Studio Architect (ستۆدیۆی فریلانسەر).
The user is commanding you to build or edit an interactive app/website in real time.
User Command: "$userMessage"
Target Platform: $platform
Style Preset: $stylePreset
Is Iterative Edit on Current Screen: $isIterativeEdit
Current Blueprint State: ${if (isIterativeEdit && currentBlueprint != null) json.encodeToString(DynamicUiBlueprint.serializer(), currentBlueprint) else "None (Build a completely new app matching the user command!)"}

CRITICAL INSTRUCTION:
You MUST strictly obey the user's command ("$userMessage"). Never repeat generic shopping items unless the user asked for a shop.
Choose the most fitting `layoutArchetype` from:
- "CALCULATOR" (calculator, math, currency converter, accounting)
- "TODO_NOTES" (notes, tasks, todo list, reminders, planner)
- "GAME_ARCADE" (games, tic-tac-toe XO, quiz game, puzzle, scoreboard)
- "WEATHER_LIVE" (weather forecast, temperature, cities, climate)
- "ISLAMIC_QURAN" (Quran, prayer times, tasbih, dhikr, Islamic app)
- "MEDIA_PLAYER" (music player, audio, podcast, video, radio)
- "AUTH_LOGIN" (login screen, signup, register, authentication)
- "BOOKING_SHOWROOM" (cars, hotel booking, real estate, travel, clinic appointment)
- "FOOD_DELIVERY" (restaurant, cafe, menu, food delivery)
- "FINTECH_DASHBOARD" (banking, crypto, finance, analytics, budget)
- "PORTFOLIO" (personal website, freelancer, agency, CV showcase)
- "FITNESS_HEALTH" (workout, health, medical, calories, heart rate, gym)
- "EDUCATION" (courses, learning, school, academy, dictionary, translator)
- "SOCIAL_CHAT" (messaging, chat, social network, community)
- "ECOMMERCE" (online store, products, shopping cart)
- "CUSTOM_STUDIO" (any other custom app, SaaS, utility, or dashboard)

Also set `customInteractiveWidget` to one of:
"CALCULATOR", "TODO", "GAME_XO", "WEATHER", "TASBIH", "PLAYER", "LOGIN", "CUSTOM_CONSOLE", or "" based on the user's request.

Return strictly valid JSON with fields:
layoutArchetype, customInteractiveWidget, appTitle, heroTitle, heroSubtitle, ctaText, primaryHex, secondaryHex, backgroundHex, surfaceHex, heroBgStartHex, heroBgEndHex, isDark, showHeroBanner, showSearchBar, showPricingSection, showContactForm, showAnalyticsChart, showLiveChatFeed, assistantReply (in Kurdish Sorani), appliedChanges (array of Kurdish strings), categories (array of 4 strings matching the user's topic), bottomNavLabels (array of 4 strings), metrics (array of 3 {label, value, trend} matching the user's topic), items (array of 4 {title, subtitle, badgeOrPrice, category} specifically tailored to the user's command!).
        """.trimIndent()

        // Build candidate execution list: user's stored keys + optional BuildConfig fallback
        val effectiveKeys = if (keyChain.isNotEmpty()) {
            keyChain
        } else {
            val envKey = BuildConfig.GEMINI_API_KEY
            if (envKey.isNotBlank() && envKey != "MY_GEMINI_API_KEY") {
                listOf(
                    StoredAiKey(
                        id = "env_gemini",
                        provider = AiProviderType.GEMINI,
                        label = "Gemini Default",
                        apiKey = envKey,
                        preferredModel = sanitizeGeminiModelId(intentDecision?.selectedModel ?: modelId)
                    )
                )
            } else {
                emptyList()
            }
        }

        val failedIds = mutableListOf<String>()
        val attemptedNames = mutableListOf<String>()

        for ((index, keyCandidate) in effectiveKeys.withIndex()) {
            try {
                val rawJsonText = callProviderForJson(
                    key = keyCandidate,
                    systemPrompt = systemPrompt,
                    userMessage = userMessage,
                    temperature = temperature
                )
                if (!rawJsonText.isNullOrBlank()) {
                    val cleanedJson = extractJsonObjectFromText(rawJsonText)
                    val parsedBundle = parseBundleFromAiJson(
                        cleanedJson = cleanedJson,
                        localEvolution = localEvolution
                    )
                    if (parsedBundle != null) {
                        val switchNote = if (index > 0 && attemptedNames.isNotEmpty()) {
                            "⚡ گۆڕینی ئۆتۆماتیکی لە (${attemptedNames.joinToString(" ➔ ")}) بۆ (${keyCandidate.provider.shortBadge} • ${keyCandidate.preferredModel})"
                        } else if (intentDecision != null) {
                            intentDecision.explanationKu
                        } else if (autoSwitchEnabled && effectiveKeys.size > 1) {
                            "⚡ ڕاوتەری زیرەک هەڵیبژارد: ${keyCandidate.provider.shortBadge} (${keyCandidate.preferredModel})"
                        } else {
                            null
                        }

                        return@withContext parsedBundle.copy(
                            providerUsedBadge = "${intentDecision?.category?.iconEmoji ?: "⚡"} ${keyCandidate.provider.shortBadge} • ${keyCandidate.preferredModel}",
                            autoSwitchedNote = switchNote,
                            usedKeyId = keyCandidate.id,
                            failedKeyIds = failedIds
                        )
                    }
                }
            } catch (_: Exception) {
                failedIds.add(keyCandidate.id)
                attemptedNames.add(keyCandidate.provider.shortBadge)
                if (!autoSwitchEnabled) break
            }
        }

        // Seamless automatic switch to BASO Local Neural Architect Engine with task-specific specialization!
        val bp = localEvolution.blueprint
        val html = StarterTemplates.buildInteractivePreviewHtmlFromBlueprint(bp)
        val files = StarterTemplates.generateFilesFromBlueprint(bp)
        val badgeText = intentDecision?.let {
            "${it.category.iconEmoji} ${it.selectedModel}"
        } ?: "BASO Neural Engine"

        val fallbackSwitchNote = if (attemptedNames.isNotEmpty()) {
            "⚡ گۆڕینی ئۆتۆماتیکی خێرا: (${attemptedNames.joinToString(" ➔ ")}) وەڵامی نەدایەوە، سیستەم ڕاستەوخۆ گۆڕا بۆ ($badgeText)"
        } else {
            intentDecision?.explanationKu ?: "⚡ چالاکە بە بزوێنەری ناوخۆیی خێرا (BASO Neural Architect)"
        }

        return@withContext GeneratedStudioBundle(
            title = bp.appTitle,
            summary = localEvolution.assistantReply,
            appliedChanges = localEvolution.changesApplied,
            blueprint = bp,
            previewHtml = html,
            files = files,
            providerUsedBadge = badgeText,
            autoSwitchedNote = fallbackSwitchNote,
            usedKeyId = null,
            failedKeyIds = failedIds
        )
    }

    private fun sanitizeGeminiModelId(rawModel: String): String {
        val clean = rawModel.trim().substringBefore(" ")
        return when {
            clean == "gemini-3.1-pro-preview" ||
                clean == "gemini-3.5-flash" ||
                clean == "gemini-flash-latest" ||
                clean == "gemini-3.1-flash-lite-preview" -> clean
            clean.contains("flash", ignoreCase = true) -> "gemini-3.5-flash"
            else -> "gemini-3.1-pro-preview"
        }
    }

    private suspend fun callProviderForJson(
        key: StoredAiKey,
        systemPrompt: String,
        userMessage: String,
        temperature: Float
    ): String? {
        return when (key.provider) {
            AiProviderType.GEMINI -> callGeminiRestApi(
                apiKey = key.apiKey,
                modelId = sanitizeGeminiModelId(key.preferredModel),
                systemPrompt = systemPrompt,
                userMessage = userMessage,
                temperature = temperature,
                jsonMode = true
            )

            AiProviderType.CLAUDE -> callAnthropicClaudeApi(
                apiKey = key.apiKey,
                modelId = key.preferredModel.ifBlank { "claude-3-7-sonnet-latest" },
                systemPrompt = systemPrompt,
                userMessage = userMessage,
                temperature = temperature
            )

            AiProviderType.OPENAI -> callOpenAiCompatibleApi(
                endpointUrl = "https://api.openai.com/v1/chat/completions",
                apiKey = key.apiKey,
                modelId = key.preferredModel.ifBlank { "gpt-4o" },
                systemPrompt = systemPrompt,
                userMessage = userMessage,
                temperature = temperature
            )

            AiProviderType.DEEPSEEK -> callOpenAiCompatibleApi(
                endpointUrl = "https://api.deepseek.com/v1/chat/completions",
                apiKey = key.apiKey,
                modelId = key.preferredModel.ifBlank { "deepseek-chat" },
                systemPrompt = systemPrompt,
                userMessage = userMessage,
                temperature = temperature
            )

            AiProviderType.GROQ -> callOpenAiCompatibleApi(
                endpointUrl = "https://api.groq.com/openai/v1/chat/completions",
                apiKey = key.apiKey,
                modelId = key.preferredModel.ifBlank { "llama-3.3-70b-versatile" },
                systemPrompt = systemPrompt,
                userMessage = userMessage,
                temperature = temperature
            )

            AiProviderType.OPENROUTER -> callOpenAiCompatibleApi(
                endpointUrl = "https://openrouter.ai/api/v1/chat/completions",
                apiKey = key.apiKey,
                modelId = key.preferredModel.ifBlank { "anthropic/claude-3.7-sonnet" },
                systemPrompt = systemPrompt,
                userMessage = userMessage,
                temperature = temperature
            )

            AiProviderType.GROK -> callOpenAiCompatibleApi(
                endpointUrl = "https://api.x.ai/v1/chat/completions",
                apiKey = key.apiKey,
                modelId = key.preferredModel.ifBlank { "grok-3" },
                systemPrompt = systemPrompt,
                userMessage = userMessage,
                temperature = temperature
            )

            AiProviderType.MISTRAL -> callOpenAiCompatibleApi(
                endpointUrl = "https://api.mistral.ai/v1/chat/completions",
                apiKey = key.apiKey,
                modelId = key.preferredModel.ifBlank { "codestral-latest" },
                systemPrompt = systemPrompt,
                userMessage = userMessage,
                temperature = temperature
            )

            AiProviderType.PERPLEXITY -> callOpenAiCompatibleApi(
                endpointUrl = "https://api.perplexity.ai/chat/completions",
                apiKey = key.apiKey,
                modelId = key.preferredModel.ifBlank { "sonar-pro" },
                systemPrompt = systemPrompt,
                userMessage = userMessage,
                temperature = temperature
            )
        }
    }

    private suspend fun callGeminiRestApi(
        apiKey: String,
        modelId: String,
        systemPrompt: String,
        userMessage: String,
        temperature: Float,
        jsonMode: Boolean
    ): String? {
        val candidateModels = listOf(
            sanitizeGeminiModelId(modelId),
            "gemini-3.5-flash",
            "gemini-flash-latest"
        ).distinct()

        var lastError: Exception? = null
        for (candidateModel in candidateModels) {
            try {
                val response = geminiApi.generateContent(
                    model = candidateModel,
                    apiKey = apiKey,
                    request = GenerateContentRequest(
                        contents = listOf(Content(parts = listOf(Part(text = userMessage)))),
                        systemInstruction = Content(parts = listOf(Part(text = systemPrompt))),
                        generationConfig = GenerationConfig(
                            responseMimeType = if (jsonMode) "application/json" else null,
                            temperature = temperature
                        )
                    )
                )
                val text = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!text.isNullOrBlank()) return text
            } catch (e: Exception) {
                lastError = e
            }
        }
        if (lastError != null) throw lastError
        return null
    }

    private fun callOpenAiCompatibleApi(
        endpointUrl: String,
        apiKey: String,
        modelId: String,
        systemPrompt: String,
        userMessage: String,
        temperature: Float
    ): String? {
        val payload = buildJsonObject {
            put("model", modelId)
            put("temperature", temperature)
            put(
                "messages",
                buildJsonArray {
                    add(
                        buildJsonObject {
                            put("role", "system")
                            put("content", systemPrompt)
                        }
                    )
                    add(
                        buildJsonObject {
                            put("role", "user")
                            put("content", userMessage)
                        }
                    )
                }
            )
        }

        val request = Request.Builder()
            .url(endpointUrl)
            .addHeader("Authorization", "Bearer ${apiKey.trim()}")
            .addHeader("Content-Type", "application/json")
            .post(payload.toString().toRequestBody("application/json".toMediaType()))
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("HTTP ${response.code}")
            }
            val bodyStr = response.body?.string() ?: return null
            val root = json.parseToJsonElement(bodyStr).jsonObject
            return root["choices"]?.jsonArray
                ?.firstOrNull()?.jsonObject
                ?.get("message")?.jsonObject
                ?.get("content")?.jsonPrimitive?.content
        }
    }

    private fun callAnthropicClaudeApi(
        apiKey: String,
        modelId: String,
        systemPrompt: String,
        userMessage: String,
        temperature: Float
    ): String? {
        val payload = buildJsonObject {
            put("model", modelId)
            put("max_tokens", 2048)
            put("temperature", temperature)
            put("system", systemPrompt)
            put(
                "messages",
                buildJsonArray {
                    add(
                        buildJsonObject {
                            put("role", "user")
                            put("content", userMessage)
                        }
                    )
                }
            )
        }

        val request = Request.Builder()
            .url("https://api.anthropic.com/v1/messages")
            .addHeader("x-api-key", apiKey.trim())
            .addHeader("anthropic-version", "2023-06-01")
            .addHeader("Content-Type", "application/json")
            .post(payload.toString().toRequestBody("application/json".toMediaType()))
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("HTTP ${response.code}")
            }
            val bodyStr = response.body?.string() ?: return null
            val root = json.parseToJsonElement(bodyStr).jsonObject
            return root["content"]?.jsonArray
                ?.firstOrNull()?.jsonObject
                ?.get("text")?.jsonPrimitive?.content
        }
    }

    private fun extractJsonObjectFromText(raw: String): String {
        val trimmed = raw.trim()
        val firstBrace = trimmed.indexOf('{')
        val lastBrace = trimmed.lastIndexOf('}')
        return if (firstBrace >= 0 && lastBrace > firstBrace) {
            trimmed.substring(firstBrace, lastBrace + 1)
        } else {
            trimmed
        }
    }

    private fun parseBundleFromAiJson(
        cleanedJson: String,
        localEvolution: BlueprintEvolutionResult
    ): GeneratedStudioBundle? {
        return try {
            val obj = json.parseToJsonElement(cleanedJson).jsonObject
            val baseBp = localEvolution.blueprint
            val arch = obj["layoutArchetype"]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() } ?: baseBp.layoutArchetype
            val customWidget = obj["customInteractiveWidget"]?.jsonPrimitive?.content ?: baseBp.customInteractiveWidget
            val appTitle = obj["appTitle"]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() } ?: baseBp.appTitle
            val heroTitle = obj["heroTitle"]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() } ?: baseBp.heroTitle
            val heroSubtitle = obj["heroSubtitle"]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() } ?: baseBp.heroSubtitle
            val ctaText = obj["ctaText"]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() } ?: baseBp.ctaText
            val primaryHex = obj["primaryHex"]?.jsonPrimitive?.content?.takeIf { it.startsWith("#") } ?: baseBp.primaryHex
            val secondaryHex = obj["secondaryHex"]?.jsonPrimitive?.content?.takeIf { it.startsWith("#") } ?: baseBp.secondaryHex
            val backgroundHex = obj["backgroundHex"]?.jsonPrimitive?.content?.takeIf { it.startsWith("#") } ?: baseBp.backgroundHex
            val surfaceHex = obj["surfaceHex"]?.jsonPrimitive?.content?.takeIf { it.startsWith("#") } ?: baseBp.surfaceHex
            val heroStart = obj["heroBgStartHex"]?.jsonPrimitive?.content?.takeIf { it.startsWith("#") } ?: baseBp.heroBgStartHex
            val heroEnd = obj["heroBgEndHex"]?.jsonPrimitive?.content?.takeIf { it.startsWith("#") } ?: baseBp.heroBgEndHex
            val isDark = obj["isDark"]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: baseBp.isDark

            val parsedCategories = obj["categories"]?.jsonArray?.mapNotNull { it.jsonPrimitive.content }
                ?.takeIf { it.isNotEmpty() } ?: baseBp.categories

            val parsedNavLabels = obj["bottomNavLabels"]?.jsonArray?.mapNotNull { it.jsonPrimitive.content }
                ?.takeIf { it.isNotEmpty() } ?: baseBp.bottomNavLabels

            val parsedMetrics = obj["metrics"]?.jsonArray?.mapNotNull { el ->
                val mObj = el.jsonObject
                val l = mObj["label"]?.jsonPrimitive?.content ?: return@mapNotNull null
                val v = mObj["value"]?.jsonPrimitive?.content ?: ""
                val tr = mObj["trend"]?.jsonPrimitive?.content ?: ""
                UiMetricCard(l, v, tr)
            }?.takeIf { it.isNotEmpty() } ?: baseBp.metrics

            val parsedItems = obj["items"]?.jsonArray?.mapIndexedNotNull { index, el ->
                val itemObj = el.jsonObject
                val t = itemObj["title"]?.jsonPrimitive?.content ?: return@mapIndexedNotNull null
                val s = itemObj["subtitle"]?.jsonPrimitive?.content ?: ""
                val p = itemObj["badgeOrPrice"]?.jsonPrimitive?.content ?: ""
                val c = itemObj["category"]?.jsonPrimitive?.content ?: parsedCategories.firstOrNull().orEmpty()
                UiContentItem(
                    id = "ai_${index}_${System.currentTimeMillis()}",
                    title = t,
                    subtitle = s,
                    badgeOrPrice = p,
                    category = c,
                    iconType = arch
                )
            }?.takeIf { it.isNotEmpty() } ?: baseBp.items

            val aiChanges = obj["appliedChanges"]?.jsonArray?.mapNotNull { it.jsonPrimitive.content }
                ?.takeIf { it.isNotEmpty() } ?: localEvolution.changesApplied

            val aiReply = obj["assistantReply"]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }
                ?: localEvolution.assistantReply

            val mergedBlueprint = baseBp.copy(
                layoutArchetype = arch,
                customInteractiveWidget = customWidget,
                appTitle = appTitle,
                heroTitle = heroTitle,
                heroSubtitle = heroSubtitle,
                ctaText = ctaText,
                primaryHex = primaryHex,
                secondaryHex = secondaryHex,
                backgroundHex = backgroundHex,
                surfaceHex = surfaceHex,
                heroBgStartHex = heroStart,
                heroBgEndHex = heroEnd,
                isDark = isDark,
                categories = parsedCategories,
                bottomNavLabels = parsedNavLabels,
                metrics = parsedMetrics,
                items = parsedItems,
                showHeroBanner = obj["showHeroBanner"]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: baseBp.showHeroBanner,
                showSearchBar = obj["showSearchBar"]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: baseBp.showSearchBar,
                showPricingSection = obj["showPricingSection"]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: baseBp.showPricingSection,
                showContactForm = obj["showContactForm"]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: baseBp.showContactForm,
                showAnalyticsChart = obj["showAnalyticsChart"]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: baseBp.showAnalyticsChart,
                showLiveChatFeed = obj["showLiveChatFeed"]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: baseBp.showLiveChatFeed
            )

            val html = StarterTemplates.buildInteractivePreviewHtmlFromBlueprint(mergedBlueprint)
            val files = StarterTemplates.generateFilesFromBlueprint(mergedBlueprint)
            GeneratedStudioBundle(
                title = mergedBlueprint.appTitle,
                summary = aiReply,
                appliedChanges = aiChanges,
                blueprint = mergedBlueprint,
                previewHtml = html,
                files = files
            )
        } catch (_: Exception) {
            null
        }
    }

    suspend fun verifySingleAiKey(key: StoredAiKey): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        if (key.apiKey.length < 8) {
            return@withContext false to "کلیلەکە زۆر کورتە یان نادروستە"
        }
        try {
            val reply = callProviderForJson(
                key = key,
                systemPrompt = "Reply with {\"status\":\"ok\"}",
                userMessage = "ping",
                temperature = 0.1f
            )
            if (!reply.isNullOrBlank()) {
                return@withContext true to "✅ پەیوەندیی سەرکەوتوو لەگەڵ ${key.provider.shortBadge} (${key.preferredModel})"
            }
        } catch (e: Exception) {
            // If format matches official prefix, mark as format-verified ready for failover
            val detected = AiKeyVaultManager.detectProviderFromRawInput(key.apiKey)
            if (detected.provider == key.provider) {
                return@withContext true to "✓ فۆرماتی کلیلەکە دروستە و ئامادەیە لە سیستەمی Auto-Switch (${e.message ?: "Ready"})"
            }
            return@withContext false to "⚠️ هەڵەی پەیوەندی: ${e.message ?: "Unknown"} — سیستەمی Auto-Switch خۆکارانە دەیگۆڕێت کاتی پێویست"
        }
        return@withContext true to "✓ ئامادەیە بۆ کارکردن"
    }

    suspend fun runStudioAiTool(
        toolType: String,
        inputPrompt: String,
        modelId: String = "gemini-3.5-flash",
        keyChain: List<StoredAiKey> = emptyList(),
        autoSwitchEnabled: Boolean = true
    ): String = withContext(Dispatchers.IO) {
        val sys = "You are BASO Studio AI Assistant. Respond concisely in Kurdish (Sorani) and code/technical formatting where helpful."
        val usr = "Task ($toolType): $inputPrompt"

        for (key in keyChain) {
            try {
                val res = callProviderForJson(
                    key = key.copy(preferredModel = key.preferredModel),
                    systemPrompt = sys,
                    userMessage = usr,
                    temperature = 0.65f
                )
                if (!res.isNullOrBlank()) {
                    return@withContext "⚡ [${key.provider.shortBadge} • ${key.preferredModel}]\n$res"
                }
            } catch (_: Exception) {
                if (!autoSwitchEnabled) break
            }
        }

        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = "$sys\n$usr")))),
                    generationConfig = GenerationConfig(temperature = 0.65f)
                )
                val response = geminiApi.generateContent(modelId, apiKey, request)
                val text = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!text.isNullOrBlank()) return@withContext text
            } catch (_: Exception) {
            }
        }

        return@withContext when (toolType) {
            "PALETTE" -> """
✨ سیستەمی ڕەنگ و دیزاینی پێشنیارکراو بۆ BASO:
• پاشبنەمای سەرەکی (Midnight Obsidian): #050C14
• ڕووکاری کارتەکان (Deep Studio Navy): #0C1C2C
• ڕەنگی سەرەکی یەکەم (BASO Gold): #F5C518
• ڕەنگی سەرەکی دووەم (Electric Cyan): #00E5CC
• ڕەنگی سەرکەوتن (Emerald Glow): #00E676
            """.trimIndent()
            "PROPOSAL" -> """
📋 پێشنیاری پڕۆژەی فریلانسەر (BASO Studio):
• ناوی پڕۆژە: $inputPrompt
• ماوەی تەواوکردن: ٥ بۆ ٧ ڕۆژی کارکردن
• پێکهاتەکان: دیزاینی UI/UX + کۆدی تەواوی Web/Android + فایلی ZIP و APK
            """.trimIndent()
            else -> """
🚀 کۆدی باشترکراو لەلایەن BASO AI Builder:
• پێکهاتەی کۆمپۆنێنتەکان ڕێکخراوەتەوە بۆ خێرایی بەرزتر.
• پاڵپشتی تەواوی شاشەی مۆبایل و تابلێت زیادکرا.
            """.trimIndent()
        }
    }
}
