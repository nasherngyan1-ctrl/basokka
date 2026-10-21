package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

enum class PlatformType(val labelKu: String, val labelEn: String) {
    ANDROID("Android", "Android"),
    WEB("Web", "Web"),
    WEB_APP("Web App", "Web App")
}

enum class BuilderTab(val labelKu: String, val labelEn: String) {
    PROMPT("پڕۆمپت (چات)", "AI Chat"),
    PREVIEW("پێشینە", "Preview"),
    CODE("کۆد", "Code"),
    FILES("فایلەکان", "Files")
}

enum class BottomNavTab(val labelKu: String, val labelEn: String) {
    HOME("ماڵەوە", "Home"),
    PROJECTS("پڕۆژەکان", "Projects"),
    TOOLS("ئامێرەکان", "AI Tools"),
    SETTINGS("کلیل و ڕێکخستن", "Keys & Settings")
}

enum class StudioScreen {
    SPLASH,
    MAIN_HUB,
    NEW_CREATION_SELECTOR,
    AI_BUILDER,
    BUILD_SUCCESS_ZIP
}

enum class DeviceViewport(val labelKu: String, val labelEn: String, val widthFraction: Float) {
    TABLET("تەبلێت", "Tablet", 0.92f),
    PHONE("مۆبایل", "Mobile", 0.82f),
    DESKTOP("کۆمپیوتەر", "Desktop", 1.0f)
}

enum class AppLanguage(val code: String, val displayName: String, val isRtl: Boolean) {
    KURDISH("ku", "کوردی (سۆرانی)", true),
    ENGLISH("en", "English", false)
}

enum class AiProviderType(
    val displayName: String,
    val shortBadge: String,
    val keyHintPrefix: String,
    val badgeColorHex: String,
    val defaultModel: String,
    val availableModels: List<String>,
    val specialtyKu: String,
    val specialtyEn: String
) {
    GEMINI(
        displayName = "Google Gemini",
        shortBadge = "Gemini",
        keyHintPrefix = "AIzaSy...",
        badgeColorHex = "#00E5CC",
        defaultModel = "gemini-3.1-pro-preview",
        availableModels = listOf("gemini-3.1-pro-preview", "gemini-3.5-flash", "gemini-flash-latest"),
        specialtyKu = "نایاب بۆ تەلارسازی ئەپ و دیزاینی تەواو",
        specialtyEn = "Full-stack UI & Android/Web architecture"
    ),
    OPENAI(
        displayName = "OpenAI (ChatGPT)",
        shortBadge = "OpenAI",
        keyHintPrefix = "sk-proj-...",
        badgeColorHex = "#10B981",
        defaultModel = "gpt-4o",
        availableModels = listOf("gpt-4o", "gpt-4.1", "o3-mini"),
        specialtyKu = "بەهێز لە لۆجیک و پێکهاتەی وێبسایت",
        specialtyEn = "Strong logic & structured UI generation"
    ),
    CLAUDE(
        displayName = "Anthropic Claude",
        shortBadge = "Claude",
        keyHintPrefix = "sk-ant-...",
        badgeColorHex = "#F59E0B",
        defaultModel = "claude-3-7-sonnet-latest",
        availableModels = listOf("claude-3-7-sonnet-latest", "claude-3-5-haiku-latest"),
        specialtyKu = "پێشەنگ لە کۆدنووسینی پاک و دیزاینی مۆدێرن",
        specialtyEn = "Premier clean code & modern frontend styling"
    ),
    DEEPSEEK(
        displayName = "DeepSeek AI",
        shortBadge = "DeepSeek",
        keyHintPrefix = "sk-...",
        badgeColorHex = "#3B82F6",
        defaultModel = "deepseek-chat",
        availableModels = listOf("deepseek-chat", "deepseek-reasoner"),
        specialtyKu = "زیرەکی قووڵ بۆ چارەسەرکردنی کۆد و ئەلگۆریزم",
        specialtyEn = "Deep reasoning & algorithmic code generation"
    ),
    GROQ(
        displayName = "Groq LPU (Ultra-Fast)",
        shortBadge = "Groq",
        keyHintPrefix = "gsk_...",
        badgeColorHex = "#EF4444",
        defaultModel = "llama-3.3-70b-versatile",
        availableModels = listOf("llama-3.3-70b-versatile", "mixtral-8x7b-32768"),
        specialtyKu = "خێراییەکی خەیاڵی بۆ دەستکاری ڕاستەوخۆی چات",
        specialtyEn = "Instant real-time live chat UI edits"
    ),
    OPENROUTER(
        displayName = "OpenRouter (Multi-Model)",
        shortBadge = "OpenRouter",
        keyHintPrefix = "sk-or-v1-...",
        badgeColorHex = "#A855F7",
        defaultModel = "anthropic/claude-3.7-sonnet",
        availableModels = listOf("anthropic/claude-3.7-sonnet", "google/gemini-2.5-pro", "openai/gpt-4o"),
        specialtyKu = "دەروازەی یەکگرتوو بۆ هەموو مۆدێلە جیهانییەکان",
        specialtyEn = "Unified gateway to all top frontier models"
    ),
    GROK(
        displayName = "xAI Grok",
        shortBadge = "Grok",
        keyHintPrefix = "xai-...",
        badgeColorHex = "#EC4899",
        defaultModel = "grok-3",
        availableModels = listOf("grok-3", "grok-2-latest"),
        specialtyKu = "بیرۆکەی داهێنەرانە و وەڵامی خێرا",
        specialtyEn = "Creative ideation & fast code synthesis"
    ),
    MISTRAL(
        displayName = "Mistral / Codestral",
        shortBadge = "Mistral",
        keyHintPrefix = "mis-...",
        badgeColorHex = "#F97316",
        defaultModel = "codestral-latest",
        availableModels = listOf("codestral-latest", "mistral-large-latest"),
        specialtyKu = "تایبەتمەند بە کۆدنووسینی خێرا (Codestral)",
        specialtyEn = "Specialized in fast code completion"
    ),
    PERPLEXITY(
        displayName = "Perplexity Sonar",
        shortBadge = "Perplexity",
        keyHintPrefix = "pplx-...",
        badgeColorHex = "#06B6D4",
        defaultModel = "sonar-pro",
        availableModels = listOf("sonar-pro", "sonar-reasoning"),
        specialtyKu = "گەڕانی زیندوو و زانیاری نوێی وێب",
        specialtyEn = "Live web-grounded research & architecture"
    )
}

enum class AiAutoRoutingStrategy(val labelKu: String, val labelEn: String, val descKu: String) {
    SMART_TASK_AND_FAILOVER(
        "زیرەک + گۆڕینی خۆکار (پێشنیارکراو)",
        "Smart Task + Auto-Failover",
        "بەپێی جۆری داواکارییەکەت باشترین AI هەڵدەبژێرێت و ئەگەر کلیلێک وەستا یەکسەر دەچێتە سەر دانەیەکی تر"
    ),
    SPEED_FIRST(
        "خێراترین وەڵام (Speed First)",
        "Fastest Response First",
        "پێشینە دەدات بە خێراترین مۆدێلەکان (Groq / Gemini Flash) بۆ دەستکاری دەستبەجێ"
    ),
    QUALITY_FIRST(
        "بەرزترین کوالێتی کۆد (Pro First)",
        "Highest Code Quality First",
        "پێشینە دەدات بە بەهێزترین مۆدێلەکان (Claude Sonnet / Gemini Pro / GPT-4o)"
    ),
    ROUND_ROBIN_LOAD_BALANCE(
        "دابەشکردنی یەکسان لە نێوان کلیلەکان",
        "Round-Robin Key Rotation",
        "بە نۆرە داواکارییەکان بەسەر هەموو کلیلە چالاکەکانتدا دابەش دەکات تا هیچ کامیان پەستانی نەکەوێتە سەر"
    )
}

@Serializable
data class StoredAiKey(
    val id: String,
    val provider: AiProviderType,
    val label: String,
    val apiKey: String,
    val preferredModel: String = provider.defaultModel,
    val isEnabled: Boolean = true,
    val priorityOrder: Int = 1,
    val successCount: Int = 0,
    val failoverCount: Int = 0,
    val lastStatusNote: String = "ئامادەیە (Ready)",
    val addedAt: Long = System.currentTimeMillis()
)

@Serializable
data class ProjectFile(
    val path: String,
    val name: String,
    val folder: String,
    val platformCategory: String, // "Web", "Android", "Files"
    val language: String,
    val content: String
)

@Serializable
data class UiMetricCard(
    val label: String,
    val value: String,
    val trend: String
)

@Serializable
data class UiContentItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val badgeOrPrice: String,
    val category: String,
    val iconType: String
)

@Serializable
data class UiPricingPlan(
    val name: String,
    val price: String,
    val features: List<String>,
    val isPopular: Boolean = false
)

@Serializable
data class DynamicUiBlueprint(
    val layoutArchetype: String = "ECOMMERCE",
    val appTitle: String = "MyShop",
    val heroTitle: String = "Best Products For You",
    val heroSubtitle: String = "دیزاین و بەرهەمی ناوازە بە کوالێتی بەرز",
    val ctaText: String = "Shop Now",
    val primaryHex: String = "#F5C518",
    val secondaryHex: String = "#00E5CC",
    val backgroundHex: String = "#F3F5F8",
    val surfaceHex: String = "#FFFFFF",
    val heroBgStartHex: String = "#091420",
    val heroBgEndHex: String = "#243E58",
    val isDark: Boolean = false,
    val cardRadiusPx: Int = 16,
    val categories: List<String> = listOf("Electronics", "Clothes", "Home", "More"),
    val metrics: List<UiMetricCard> = emptyList(),
    val items: List<UiContentItem> = emptyList(),
    val showHeroBanner: Boolean = true,
    val showSearchBar: Boolean = false,
    val showBottomNavBar: Boolean = true,
    val bottomNavLabels: List<String> = listOf("سەرەکی", "گەڕان", "بەشەکان", "پڕۆفایل"),
    val customInteractiveWidget: String = "",
    val customInputFields: List<String> = emptyList(),
    val customActionButtons: List<String> = emptyList(),
    val showPricingSection: Boolean = false,
    val pricingPlans: List<UiPricingPlan> = emptyList(),
    val showContactForm: Boolean = false,
    val showAnalyticsChart: Boolean = false,
    val showLiveChatFeed: Boolean = false,
    val userPromptCommand: String = "",
    val customFooterText: String = "Built with BASO AI Studio"
)

@Serializable
data class DesignTokens(
    val primaryHex: String = "#F5C518",
    val accentHex: String = "#00E5CC",
    val backgroundHex: String = "#F8FAFC",
    val cardRadiusPx: Int = 16,
    val darkModePreview: Boolean = false,
    val previewTitle: String = "MyShop"
)

@Serializable
data class ChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val versionNumber: Int = 1,
    val appliedChanges: List<String> = emptyList(),
    val blueprintSnapshot: DynamicUiBlueprint? = null,
    val providerUsedBadge: String = "BASO AI",
    val autoSwitchedNote: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class AiGenerationTurn(
    val prompt: String,
    val modelUsed: String,
    val summary: String,
    val timestamp: Long
)

@Entity(tableName = "baso_projects")
data class BasoProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val slug: String,
    val platform: String,
    val prompt: String,
    val modelId: String,
    val temperature: Float,
    val stylePreset: String,
    val previewUrl: String,
    val previewHtml: String,
    val filesJson: String,
    val designTokensJson: String,
    val historyJson: String,
    val zipSizeLabel: String,
    val updatedLabelKu: String,
    val updatedLabelEn: String,
    val updatedAt: Long = System.currentTimeMillis()
)
