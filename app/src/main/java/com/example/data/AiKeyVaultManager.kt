package com.example.data

import android.content.Context
import com.example.BuildConfig
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class DetectedKeyCandidate(
    val provider: AiProviderType,
    val cleanKey: String,
    val suggestedLabel: String,
    val confidenceNoteKu: String
)

enum class PromptTaskCategory(
    val titleKu: String,
    val titleEn: String,
    val iconEmoji: String,
    val idealProvidersOrder: List<AiProviderType>,
    val idealModelHint: String,
    val reasonKu: String
) {
    UI_DESIGN_AND_STYLE(
        titleKu = "دیزاینی ڕووکار و ڕەنگ (UI/UX)",
        titleEn = "UI/UX & Visual Styling",
        iconEmoji = "🎨",
        idealProvidersOrder = listOf(
            AiProviderType.CLAUDE,
            AiProviderType.GEMINI,
            AiProviderType.OPENROUTER,
            AiProviderType.OPENAI
        ),
        idealModelHint = "Claude 3.7 Sonnet / Gemini 3.1 Pro",
        reasonKu = "باشترینە بۆ ڕێکخستنی ڕەنگ، فۆنت، و دیزاینی مۆدێرنی شاشەکان"
    ),
    COMPLEX_CODE_AND_ARCH(
        titleKu = "ئەندازیاریی کۆد و ئەپلیکەیشن",
        titleEn = "Code & App Architecture",
        iconEmoji = "💻",
        idealProvidersOrder = listOf(
            AiProviderType.GEMINI,
            AiProviderType.CLAUDE,
            AiProviderType.MISTRAL,
            AiProviderType.DEEPSEEK,
            AiProviderType.OPENAI
        ),
        idealModelHint = "Gemini 3.1 Pro / Claude 3.7 Sonnet",
        reasonKu = "باشترینە بۆ نووسینی کۆدی پاکی Android (Kotlin) و پێکهاتەی وێبسایت"
    ),
    DATA_ANALYTICS_AND_FINTECH(
        titleKu = "ئامار، چارت و لۆجیکی دارایی",
        titleEn = "Analytics, Charts & Fintech Logic",
        iconEmoji = "📊",
        idealProvidersOrder = listOf(
            AiProviderType.DEEPSEEK,
            AiProviderType.OPENAI,
            AiProviderType.GEMINI,
            AiProviderType.CLAUDE
        ),
        idealModelHint = "DeepSeek R1 / OpenAI o3-mini / Gemini 3.1 Pro",
        reasonKu = "باشترینە بۆ ژمێریاری، خشتەی نرخ، چارت و داشبۆردی داتا"
    ),
    INSTANT_LIVE_CHAT_EDIT(
        titleKu = "دەستکاری خێرای ڕاستەوخۆ",
        titleEn = "Instant Live UI Tweak",
        iconEmoji = "⚡",
        idealProvidersOrder = listOf(
            AiProviderType.GROQ,
            AiProviderType.GEMINI,
            AiProviderType.OPENAI,
            AiProviderType.CLAUDE
        ),
        idealModelHint = "Groq Llama 3.3 70B / Gemini 3.5 Flash",
        reasonKu = "خێراترین مۆدێلە بۆ جێبەجێکردنی گۆڕانکارییە خێراکانی ناو چات"
    ),
    CREATIVE_KURDISH_COPY(
        titleKu = "داڕشتنی ناوەڕۆک و دەقی کوردی",
        titleEn = "Kurdish Copywriting & Branding",
        iconEmoji = "✍️",
        idealProvidersOrder = listOf(
            AiProviderType.OPENAI,
            AiProviderType.GEMINI,
            AiProviderType.CLAUDE,
            AiProviderType.GROK
        ),
        idealModelHint = "GPT-4o / Gemini 3.1 Pro",
        reasonKu = "باشترینە بۆ نووسینی سەردێڕ، مێنوی خواردن و دەقی ڕێکلامی بە کوردی"
    ),
    LIVE_WEB_AND_COMMERCE(
        titleKu = "فرۆشگا و بازرگانی ئۆنلاین",
        titleEn = "E-Commerce & Market Structure",
        iconEmoji = "🛍️",
        idealProvidersOrder = listOf(
            AiProviderType.PERPLEXITY,
            AiProviderType.OPENAI,
            AiProviderType.GEMINI,
            AiProviderType.CLAUDE
        ),
        idealModelHint = "Gemini 3.1 Pro / GPT-4o / Perplexity Sonar",
        reasonKu = "باشترینە بۆ دروستکردنی فرۆشگا، لیستی کاڵاکان و سیستەمی کڕین"
    ),
    FULL_PROJECT_GENERATION(
        titleKu = "دروستکردنی پڕۆژەی تەواو",
        titleEn = "Full Project Synthesis",
        iconEmoji = "🚀",
        idealProvidersOrder = listOf(
            AiProviderType.GEMINI,
            AiProviderType.CLAUDE,
            AiProviderType.OPENAI,
            AiProviderType.DEEPSEEK,
            AiProviderType.OPENROUTER
        ),
        idealModelHint = "Gemini 3.1 Pro Architect",
        reasonKu = "باشترینە بۆ دروستکردنی هەموو بەشەکانی پڕۆژەکە (Preview + Code + ZIP)"
    )
}

data class PromptIntentDecision(
    val category: PromptTaskCategory,
    val selectedProvider: AiProviderType,
    val selectedModel: String,
    val badgeTitleKu: String,
    val explanationKu: String,
    val isUsingStoredKey: Boolean
)

object AiKeyVaultManager {
    private const val PREFS_NAME = "baso_ai_key_vault_prefs"
    private const val KEY_STORED_LIST = "stored_ai_keys_json"
    private const val KEY_AUTO_SWITCH = "auto_switch_enabled"
    private const val KEY_ROUTING_STRATEGY = "routing_strategy_name"
    private const val KEY_ROUND_ROBIN_INDEX = "round_robin_counter"

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /**
     * Analyzes the user's chat message in real time (while typing or when sending)
     * and determines the exact task category + best AI model for what the user is asking!
     */
    fun analyzePromptIntent(
        rawPrompt: String,
        isIterativeEdit: Boolean,
        storedKeys: List<StoredAiKey>
    ): PromptIntentDecision {
        val text = rawPrompt.trim().lowercase()
        val enabledKeys = storedKeys.filter { it.isEnabled && it.apiKey.isNotBlank() }

        val category = when {
            text.isEmpty() -> if (isIterativeEdit) {
                PromptTaskCategory.INSTANT_LIVE_CHAT_EDIT
            } else {
                PromptTaskCategory.FULL_PROJECT_GENERATION
            }

            // 1. Analytics, Charts, Banking, Crypto, Pricing math
            text.contains("چارت") || text.contains("ئامار") || text.contains("بانک") ||
                text.contains("کریپتۆ") || text.contains("دارایی") || text.contains("حساب") ||
                text.contains("chart") || text.contains("analytics") || text.contains("crypto") ||
                text.contains("fintech") || text.contains("dashboard") || text.contains("داشبۆرد") ->
                PromptTaskCategory.DATA_ANALYTICS_AND_FINTECH

            // 2. UI Design, Colors, Theme, Dark/Light, Fonts, Cards
            text.contains("ڕەنگ") || text.contains("رەنگ") || text.contains("دیزاین") ||
                text.contains("تاریک") || text.contains("ڕووناک") || text.contains("جوان") ||
                text.contains("مۆر") || text.contains("سوور") || text.contains("سەوز") ||
                text.contains("شێواز") || text.contains("ستایل") || text.contains("color") ||
                text.contains("theme") || text.contains("style") || text.contains("ui") ||
                text.contains("dark") || text.contains("light") ->
                PromptTaskCategory.UI_DESIGN_AND_STYLE

            // 3. Code, Kotlin, HTML/CSS/JS, Architecture, API, Database
            text.contains("کۆد") || text.contains("ئەندرۆید") || text.contains("کۆتلین") ||
                text.contains("فایل") || text.contains("داتابەیس") || text.contains("ئەپڵیکەیشن") ||
                text.contains("code") || text.contains("kotlin") || text.contains("html") ||
                text.contains("react") || text.contains("api") || text.contains("apk") ->
                PromptTaskCategory.COMPLEX_CODE_AND_ARCH

            // 4. E-Commerce, Shop, Store, Products, Pricing Plans
            text.contains("فرۆشگا") || text.contains("دوکان") || text.contains("کاڵا") ||
                text.contains("نرخ") || text.contains("پاکێج") || text.contains("کڕین") ||
                text.contains("shop") || text.contains("store") || text.contains("price") ||
                text.contains("pricing") || text.contains("ecommerce") ->
                PromptTaskCategory.LIVE_WEB_AND_COMMERCE

            // 5. Copywriting, Names, Titles, Restaurant Menu, Portfolio Bio, Contact
            text.contains("ناو") || text.contains("سەردێڕ") || text.contains("چێشتخانە") ||
                text.contains("خواردن") || text.contains("پۆرتفۆلیۆ") || text.contains("دەربارە") ||
                text.contains("پەیوەندی") || text.contains("کۆرس") || text.contains("چات") ||
                text.contains("menu") || text.contains("title") || text.contains("portfolio") ->
                PromptTaskCategory.CREATIVE_KURDISH_COPY

            // 6. Short live chat tweak (< 45 chars in iterative mode)
            isIterativeEdit && text.length < 45 ->
                PromptTaskCategory.INSTANT_LIVE_CHAT_EDIT

            // 7. Full New Project Generation
            else -> PromptTaskCategory.FULL_PROJECT_GENERATION
        }

        // Find the best available key matching the ideal provider order for this task category
        val matchedStoredKey = category.idealProvidersOrder.firstNotNullOfOrNull { idealProvider ->
            enabledKeys.firstOrNull { it.provider == idealProvider }
        } ?: enabledKeys.firstOrNull()

        return if (matchedStoredKey != null) {
            // Adapt model inside Gemini if the user has a Gemini key and task is speed vs pro
            val chosenModel = when {
                matchedStoredKey.provider == AiProviderType.GEMINI &&
                    category == PromptTaskCategory.INSTANT_LIVE_CHAT_EDIT -> "gemini-3.5-flash"
                matchedStoredKey.provider == AiProviderType.GEMINI &&
                    (category == PromptTaskCategory.COMPLEX_CODE_AND_ARCH ||
                        category == PromptTaskCategory.FULL_PROJECT_GENERATION ||
                        category == PromptTaskCategory.UI_DESIGN_AND_STYLE) -> "gemini-3.1-pro-preview"
                matchedStoredKey.provider == AiProviderType.DEEPSEEK &&
                    category == PromptTaskCategory.DATA_ANALYTICS_AND_FINTECH -> "deepseek-reasoner"
                else -> matchedStoredKey.preferredModel
            }

            PromptIntentDecision(
                category = category,
                selectedProvider = matchedStoredKey.provider,
                selectedModel = chosenModel,
                badgeTitleKu = "${category.iconEmoji} ${category.titleKu} ⬅ ${matchedStoredKey.provider.shortBadge} ($chosenModel)",
                explanationKu = "بۆ ئەم داواکارییە خۆکارانە (${matchedStoredKey.provider.shortBadge} • $chosenModel) هەڵبژێردرا چونکە ${category.reasonKu}",
                isUsingStoredKey = true
            )
        } else {
            // Even when using default/system engine, keep selectedModel as a valid API model ID
            // while displaying the specialized virtual architect mode in badgeTitleKu!
            val idealProvider = category.idealProvidersOrder.first()
            val validApiModelId = when (category) {
                PromptTaskCategory.INSTANT_LIVE_CHAT_EDIT -> "gemini-3.5-flash"
                else -> "gemini-3.1-pro-preview"
            }
            val displayBadgeLabel = when (category) {
                PromptTaskCategory.INSTANT_LIVE_CHAT_EDIT -> "gemini-3.5-flash (Instant Mode)"
                PromptTaskCategory.UI_DESIGN_AND_STYLE -> "BASO UI/UX Studio (${idealProvider.shortBadge} Style)"
                PromptTaskCategory.DATA_ANALYTICS_AND_FINTECH -> "BASO Data & Chart Engine (${idealProvider.shortBadge} Logic)"
                PromptTaskCategory.COMPLEX_CODE_AND_ARCH -> "gemini-3.1-pro-preview (Code Architect)"
                else -> category.idealModelHint
            }

            PromptIntentDecision(
                category = category,
                selectedProvider = idealProvider,
                selectedModel = validApiModelId,
                badgeTitleKu = "${category.iconEmoji} ${category.titleKu} ⬅ $displayBadgeLabel",
                explanationKu = "بەپێی داواکارییەکەت خۆکارانە گۆڕدرا بۆ مۆدی (${category.titleKu}) — ${category.reasonKu}",
                isUsingStoredKey = false
            )
        }
    }

    /**
     * Automatically detects which AI provider a key belongs to by inspecting its prefix,
     * character set, and length, or optional variable name (e.g. OPENAI_API_KEY=...).
     */
    fun detectProviderFromRawInput(
        rawInput: String,
        manualOverride: AiProviderType? = null
    ): DetectedKeyCandidate {
        val trimmed = rawInput.trim()
        val envParts = if (trimmed.contains("=")) {
            val idx = trimmed.indexOf("=")
            val envName = trimmed.substring(0, idx).trim().uppercase()
            val envVal = trimmed.substring(idx + 1).trim().trim('"', '\'')
            envName to envVal
        } else {
            "" to trimmed.trim('"', '\'')
        }

        val envVarName = envParts.first
        val cleanKey = envParts.second

        if (manualOverride != null) {
            return DetectedKeyCandidate(
                provider = manualOverride,
                cleanKey = cleanKey,
                suggestedLabel = "${manualOverride.shortBadge} Key (${maskKey(cleanKey)})",
                confidenceNoteKu = "دیاریکراو: ${manualOverride.displayName}"
            )
        }

        when {
            envVarName.contains("GEMINI") || envVarName.contains("GOOGLE") ->
                return candidateFor(AiProviderType.GEMINI, cleanKey, "ناسرایەوە بە ناوی گۆڕاو (Google Gemini)")
            envVarName.contains("ANTHROPIC") || envVarName.contains("CLAUDE") ->
                return candidateFor(AiProviderType.CLAUDE, cleanKey, "ناسرایەوە بە ناوی گۆڕاو (Anthropic Claude)")
            envVarName.contains("DEEPSEEK") ->
                return candidateFor(AiProviderType.DEEPSEEK, cleanKey, "ناسرایەوە بە ناوی گۆڕاو (DeepSeek AI)")
            envVarName.contains("GROQ") ->
                return candidateFor(AiProviderType.GROQ, cleanKey, "ناسرایەوە بە ناوی گۆڕاو (Groq LPU)")
            envVarName.contains("OPENROUTER") ->
                return candidateFor(AiProviderType.OPENROUTER, cleanKey, "ناسرایەوە بە ناوی گۆڕاو (OpenRouter)")
            envVarName.contains("GROK") || envVarName.contains("XAI") ->
                return candidateFor(AiProviderType.GROK, cleanKey, "ناسرایەوە بە ناوی گۆڕاو (xAI Grok)")
            envVarName.contains("MISTRAL") || envVarName.contains("CODESTRAL") ->
                return candidateFor(AiProviderType.MISTRAL, cleanKey, "ناسرایەوە بە ناوی گۆڕاو (Mistral AI)")
            envVarName.contains("PERPLEXITY") || envVarName.contains("PPLX") ->
                return candidateFor(AiProviderType.PERPLEXITY, cleanKey, "ناسرایەوە بە ناوی گۆڕاو (Perplexity)")
            envVarName.contains("OPENAI") ->
                return candidateFor(AiProviderType.OPENAI, cleanKey, "ناسرایەوە بە ناوی گۆڕاو (OpenAI)")
        }

        return when {
            cleanKey.startsWith("AIza", ignoreCase = false) ->
                candidateFor(AiProviderType.GEMINI, cleanKey, "✨ خۆکار ناسرایەوە: کلیلی فەرمی Google Gemini (AIza...)")

            cleanKey.startsWith("sk-ant-", ignoreCase = true) ->
                candidateFor(AiProviderType.CLAUDE, cleanKey, "✨ خۆکار ناسرایەوە: کلیلی فەرمی Anthropic Claude (sk-ant-...)")

            cleanKey.startsWith("gsk_", ignoreCase = true) ->
                candidateFor(AiProviderType.GROQ, cleanKey, "✨ خۆکار ناسرایەوە: کلیلی خێرای Groq LPU (gsk_...)")

            cleanKey.startsWith("sk-or-", ignoreCase = true) ->
                candidateFor(AiProviderType.OPENROUTER, cleanKey, "✨ خۆکار ناسرایەوە: کلیلی گشتگیر OpenRouter (sk-or-...)")

            cleanKey.startsWith("xai-", ignoreCase = true) ->
                candidateFor(AiProviderType.GROK, cleanKey, "✨ خۆکار ناسرایەوە: کلیلی xAI Grok (xai-...)")

            cleanKey.startsWith("pplx-", ignoreCase = true) ->
                candidateFor(AiProviderType.PERPLEXITY, cleanKey, "✨ خۆکار ناسرایەوە: کلیلی Perplexity Sonar (pplx-...)")

            cleanKey.startsWith("sk-proj-", ignoreCase = true) ||
                cleanKey.startsWith("sk-svcacct-", ignoreCase = true) ->
                candidateFor(AiProviderType.OPENAI, cleanKey, "✨ خۆکار ناسرایەوە: کلیلی پڕۆژەی OpenAI (sk-proj-...)")

            cleanKey.startsWith("ds-", ignoreCase = true) ->
                candidateFor(AiProviderType.DEEPSEEK, cleanKey, "✨ خۆکار ناسرایەوە: کلیلی DeepSeek AI")

            cleanKey.startsWith("sk-", ignoreCase = true) -> {
                val bodyAfterSk = cleanKey.removePrefix("sk-").removePrefix("SK-")
                val isHex32 = bodyAfterSk.length in 30..36 && bodyAfterSk.all { it.isDigit() || it.lowercaseChar() in 'a'..'f' }
                if (isHex32) {
                    candidateFor(AiProviderType.DEEPSEEK, cleanKey, "✨ خۆکار ناسرایەوە: کلیلی DeepSeek AI (فۆرماتی sk-hex)")
                } else {
                    candidateFor(AiProviderType.OPENAI, cleanKey, "✨ خۆکار ناسرایەوە: کلیلی ستانداردی OpenAI (sk-...)")
                }
            }

            cleanKey.startsWith("mis-", ignoreCase = true) || cleanKey.length == 32 ->
                candidateFor(AiProviderType.MISTRAL, cleanKey, "✨ خۆکار ناسرایەوە: کلیلی Mistral / Codestral")

            else ->
                candidateFor(AiProviderType.GEMINI, cleanKey, "کلیلی گشتی (دەتوانیت جۆری کۆمپانیاکە بگۆڕیت)")
        }
    }

    fun parseBulkKeysFromText(rawText: String, manualOverride: AiProviderType? = null): List<DetectedKeyCandidate> {
        val lines = rawText.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") && !it.startsWith("//") }

        if (lines.size <= 1) {
            val single = rawText.trim()
            return if (single.isNotEmpty()) {
                listOf(detectProviderFromRawInput(single, manualOverride))
            } else {
                emptyList()
            }
        }

        return lines.mapNotNull { line ->
            val candidate = detectProviderFromRawInput(line, manualOverride)
            if (candidate.cleanKey.length >= 6) candidate else null
        }
    }

    private fun candidateFor(
        provider: AiProviderType,
        cleanKey: String,
        noteKu: String
    ): DetectedKeyCandidate {
        return DetectedKeyCandidate(
            provider = provider,
            cleanKey = cleanKey,
            suggestedLabel = "${provider.shortBadge} (${maskKey(cleanKey)})",
            confidenceNoteKu = noteKu
        )
    }

    fun maskKey(rawKey: String): String {
        val clean = rawKey.trim()
        if (clean.length <= 8) return "••••••••"
        return "${clean.take(6)}••••${clean.takeLast(4)}"
    }

    fun loadStoredKeys(context: Context): List<StoredAiKey> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_STORED_LIST, null)
        val parsed = if (!raw.isNullOrBlank()) {
            try {
                json.decodeFromString<List<StoredAiKey>>(raw)
            } catch (_: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }

        val envGemini = BuildConfig.GEMINI_API_KEY
        if (envGemini.isNotBlank() && envGemini != "MY_GEMINI_API_KEY" && parsed.none { it.apiKey == envGemini }) {
            val seeded = listOf(
                StoredAiKey(
                    id = "env_gemini_default",
                    provider = AiProviderType.GEMINI,
                    label = "Gemini BuildConfig Key",
                    apiKey = envGemini,
                    preferredModel = AiProviderType.GEMINI.defaultModel,
                    isEnabled = true,
                    priorityOrder = 1,
                    lastStatusNote = "چالاکە لە سیستەمەوە"
                )
            ) + parsed
            saveStoredKeys(context, seeded)
            return seeded
        }

        return parsed
    }

    fun saveStoredKeys(context: Context, keys: List<StoredAiKey>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_STORED_LIST, json.encodeToString(keys)).apply()
    }

    fun isAutoSwitchEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_AUTO_SWITCH, true)
    }

    fun setAutoSwitchEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_AUTO_SWITCH, enabled).apply()
    }

    fun getRoutingStrategy(context: Context): AiAutoRoutingStrategy {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val name = prefs.getString(KEY_ROUTING_STRATEGY, AiAutoRoutingStrategy.SMART_TASK_AND_FAILOVER.name)
        return AiAutoRoutingStrategy.entries.firstOrNull { it.name == name }
            ?: AiAutoRoutingStrategy.SMART_TASK_AND_FAILOVER
    }

    fun setRoutingStrategy(context: Context, strategy: AiAutoRoutingStrategy) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_ROUTING_STRATEGY, strategy.name).apply()
    }

    fun nextRoundRobinIndex(context: Context, modulo: Int): Int {
        if (modulo <= 0) return 0
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getInt(KEY_ROUND_ROBIN_INDEX, 0)
        val next = (current + 1) % modulo
        prefs.edit().putInt(KEY_ROUND_ROBIN_INDEX, next).apply()
        return current % modulo
    }

    /**
     * Builds the ordered execution chain of AI keys based on:
     * - The analyzed PromptIntentDecision (matching the best AI to the user's exact request!)
     * - Whether Auto-Switch is ON
     * - The selected Routing Strategy
     */
    fun buildOrderedKeyChain(
        context: Context,
        allKeys: List<StoredAiKey>,
        autoSwitch: Boolean,
        strategy: AiAutoRoutingStrategy,
        userMessage: String,
        isIterativeQuickEdit: Boolean,
        forcedProviderOverride: AiProviderType? = null
    ): List<StoredAiKey> {
        val enabledKeys = allKeys.filter { it.isEnabled && it.apiKey.isNotBlank() }
        if (enabledKeys.isEmpty()) return emptyList()

        if (forcedProviderOverride != null) {
            val matching = enabledKeys.filter { it.provider == forcedProviderOverride }
            val others = enabledKeys.filter { it.provider != forcedProviderOverride }
            return if (autoSwitch) matching + others else matching.ifEmpty { listOf(enabledKeys.first()) }
        }

        if (!autoSwitch) {
            return listOf(enabledKeys.first())
        }

        val intentDecision = analyzePromptIntent(userMessage, isIterativeQuickEdit, enabledKeys)

        return when (strategy) {
            AiAutoRoutingStrategy.SPEED_FIRST -> {
                enabledKeys.sortedBy { key ->
                    when (key.provider) {
                        AiProviderType.GROQ -> 1
                        AiProviderType.GEMINI -> if (key.preferredModel.contains("flash")) 2 else 4
                        AiProviderType.OPENAI -> 3
                        AiProviderType.DEEPSEEK -> 5
                        AiProviderType.CLAUDE -> 6
                        else -> 7
                    }
                }
            }

            AiAutoRoutingStrategy.QUALITY_FIRST -> {
                enabledKeys.sortedBy { key ->
                    when (key.provider) {
                        AiProviderType.CLAUDE -> 1
                        AiProviderType.GEMINI -> 2
                        AiProviderType.OPENAI -> 3
                        AiProviderType.DEEPSEEK -> 4
                        AiProviderType.OPENROUTER -> 5
                        else -> 6
                    }
                }
            }

            AiAutoRoutingStrategy.ROUND_ROBIN_LOAD_BALANCE -> {
                val startIdx = nextRoundRobinIndex(context, enabledKeys.size)
                enabledKeys.drop(startIdx) + enabledKeys.take(startIdx)
            }

            AiAutoRoutingStrategy.SMART_TASK_AND_FAILOVER -> {
                val idealOrder = intentDecision.category.idealProvidersOrder
                enabledKeys.sortedBy { key ->
                    val idx = idealOrder.indexOf(key.provider)
                    if (idx >= 0) idx else 99
                }.map { key ->
                    // Also dynamically tune the model for the #1 provider if it's Gemini or DeepSeek
                    if (key.provider == intentDecision.selectedProvider) {
                        key.copy(preferredModel = intentDecision.selectedModel)
                    } else {
                        key
                    }
                }
            }
        }
    }
}
