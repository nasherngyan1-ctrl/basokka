package com.example.data

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class BlueprintEvolutionResult(
    val blueprint: DynamicUiBlueprint,
    val assistantReply: String,
    val changesApplied: List<String>
)

object StarterTemplates {
    val json = Json { prettyPrint = true; ignoreUnknownKeys = true; encodeDefaults = true }

    fun evolveBlueprintFromChat(
        userMessage: String,
        current: DynamicUiBlueprint?,
        isIterativeEdit: Boolean
    ): BlueprintEvolutionResult {
        val msg = userMessage.trim()
        val lower = msg.lowercase()
        val changes = mutableListOf<String>()

        val detectedArchetype = detectArchetype(lower)
        val isPureStyleOrSectionTweak = isPureModificationCommand(lower)

        // Switch archetype whenever:
        // 1. current is null
        // 2. !isIterativeEdit (first prompt in a new project)
        // 3. detectedArchetype is different from current.layoutArchetype
        // 4. The user isn't just asking for a pure color/dark-mode tweak (they typed a new subject/app command!)
        val shouldRebuildBase = current == null ||
            !isIterativeEdit ||
            isExplicitNewProjectRequest(lower) ||
            (detectedArchetype != null && detectedArchetype != current.layoutArchetype) ||
            (!isPureStyleOrSectionTweak && detectedArchetype == null)

        var working = if (shouldRebuildBase) {
            val arch = detectedArchetype ?: "CUSTOM_STUDIO"
            changes.add("دروستکردنی پێکهاتە و ڕووکاری تایبەت بە داواکارییەکەت ($arch)")
            buildBaseBlueprintForArchetype(arch, msg)
        } else {
            current!!.copy(userPromptCommand = msg)
        }

        // Always record the latest user command in the blueprint
        working = working.copy(userPromptCommand = msg)

        // Apply color & theme modifications from chat message (Kurdish & English)
        when {
            lower.contains("سوور") || lower.contains("سور") || lower.contains("red") || lower.contains("crimson") -> {
                working = working.copy(
                    primaryHex = "#EF4444",
                    secondaryHex = "#F97316",
                    heroBgStartHex = "#2A080C",
                    heroBgEndHex = "#5C131B"
                )
                changes.add("گۆڕینی پاڵێتی ڕەنگ بۆ سوور و پرتەقاڵی (#EF4444)")
            }
            lower.contains("مۆر") || lower.contains("پەمەیی") || lower.contains("purple") || lower.contains("violet") || lower.contains("pink") -> {
                working = working.copy(
                    primaryHex = "#A855F7",
                    secondaryHex = "#EC4899",
                    heroBgStartHex = "#1A0B2E",
                    heroBgEndHex = "#3B1564"
                )
                changes.add("گۆڕینی پاڵێتی ڕەنگ بۆ مۆری نیۆن و پەمەیی (#A855F7)")
            }
            lower.contains("سەوز") || lower.contains("green") || lower.contains("emerald") -> {
                working = working.copy(
                    primaryHex = "#10B981",
                    secondaryHex = "#06B6D4",
                    heroBgStartHex = "#06241C",
                    heroBgEndHex = "#0E4A3A"
                )
                changes.add("گۆڕینی پاڵێتی ڕەنگ بۆ سەوزی زمڕووتی (#10B981)")
            }
            lower.contains("شین") || lower.contains("blue") || lower.contains("فیرۆزە") || lower.contains("cyan") -> {
                working = working.copy(
                    primaryHex = "#00E5CC",
                    secondaryHex = "#3B82F6",
                    heroBgStartHex = "#07182B",
                    heroBgEndHex = "#123A63"
                )
                changes.add("گۆڕینی پاڵێتی ڕەنگ بۆ شینی فیرۆزەیی و ئاسمانی (#00E5CC)")
            }
            lower.contains("زەرد") || lower.contains("زێڕ") || lower.contains("gold") || lower.contains("yellow") -> {
                working = working.copy(
                    primaryHex = "#F5C518",
                    secondaryHex = "#00E5CC",
                    heroBgStartHex = "#1F1705",
                    heroBgEndHex = "#3D2E0A"
                )
                changes.add("گۆڕینی پاڵێتی ڕەنگ بۆ زێڕینی BASO (#F5C518)")
            }
        }

        // Dark / Light mode switching via chat
        if (lower.contains("تاریک") || lower.contains("ڕەش") || lower.contains("dark")) {
            working = working.copy(
                isDark = true,
                backgroundHex = "#08121C",
                surfaceHex = "#112233"
            )
            changes.add("چالاککردنی دۆخی تاریک (Dark Mode UI)")
        } else if (lower.contains("ڕووناک") || lower.contains("سپی") || lower.contains("light")) {
            working = working.copy(
                isDark = false,
                backgroundHex = "#F4F6F9",
                surfaceHex = "#FFFFFF"
            )
            changes.add("چالاککردنی دۆخی ڕووناک (Light Mode UI)")
        }

        // Hero Banner show/hide
        if (lower.contains("بەشی سەرەوە لابە") || lower.contains("بێ هێرۆ") || lower.contains("hide hero") || lower.contains("لابردنی سەرەوە")) {
            working = working.copy(showHeroBanner = false)
            changes.add("لابردنی بانەری سەرەوە (Hero Banner Hidden)")
        } else if (lower.contains("بەشی سەرەوە دابنێ") || lower.contains("show hero")) {
            working = working.copy(showHeroBanner = true)
            changes.add("پیشاندانی بانەری سەرەوە (Hero Banner Visible)")
        }

        // Search bar addition
        if (lower.contains("گەڕان") || lower.contains("سێرچ") || lower.contains("search")) {
            working = working.copy(showSearchBar = true)
            changes.add("زیادکردنی خانەی گەڕانی زیندوو (Live Search Bar)")
        }

        // Interactive widget additions requested via chat
        when {
            lower.contains("حاسیبە") || lower.contains("ژمێریار") || lower.contains("calculator") -> {
                working = working.copy(customInteractiveWidget = "CALCULATOR")
                changes.add("چالاککردنی ژمێریاری کارلێککەری ڕاستەوخۆ (Live Calculator)")
            }
            lower.contains("تێبینی") || lower.contains("ئەرک") || lower.contains("todo") || lower.contains("task") -> {
                working = working.copy(customInteractiveWidget = "TODO")
                changes.add("چالاککردنی لیستی ئەرک و تێبینییەکان (Interactive Todo & Notes)")
            }
            lower.contains("یاری") || lower.contains("xo") || lower.contains("game") -> {
                working = working.copy(customInteractiveWidget = "GAME_XO")
                changes.add("چالاککردنی یاری کارلێککەر لە ناو پێشینە (Interactive Game)")
            }
            lower.contains("کەشوهەوا") || lower.contains("کەش و هەوا") || lower.contains("weather") -> {
                working = working.copy(customInteractiveWidget = "WEATHER")
                changes.add("چالاککردنی پانێڵی زیندووی کەشوهەوا (Live Weather Forecast)")
            }
            lower.contains("تەسبیح") || lower.contains("زیکر") || lower.contains("قورئان") || lower.contains("بانگ") -> {
                working = working.copy(customInteractiveWidget = "TASBIH")
                changes.add("چالاککردنی ژمێرەری زیکر و کاتەکانی بانگ (Interactive Tasbih & Prayer)")
            }
            lower.contains("میوزیک") || lower.contains("گۆرانی") || lower.contains("پلەیەر") || lower.contains("music") || lower.contains("player") -> {
                working = working.copy(customInteractiveWidget = "PLAYER")
                changes.add("چالاککردنی میوزیک و میدیا پلەیەری زیندوو (Interactive Media Player)")
            }
            lower.contains("چوونەژوورەوە") || lower.contains("لۆگین") || lower.contains("پاسۆرد") || lower.contains("login") || lower.contains("signup") -> {
                working = working.copy(customInteractiveWidget = "LOGIN")
                changes.add("زیادکردنی شاشەی چوونەژوورەوە و هەژمار (Interactive Auth Screen)")
            }
        }

        // Section additions / edits via chat
        if (lower.contains("نرخ") || lower.contains("پاکێج") || lower.contains("پلان") || lower.contains("pricing") || lower.contains("plan")) {
            working = working.copy(
                showPricingSection = true,
                pricingPlans = listOf(
                    UiPricingPlan("سەرەتایی / Basic", "$19/mo", listOf("٣ پڕۆژەی چالاک", "پاڵپشتی خێرا", "دابەزاندنی کۆد")),
                    UiPricingPlan("پڕۆفیشناڵ / Pro", "$49/mo", listOf("پڕۆژەی بێسنوور", "BASO AI Pro", "فایلی ZIP و APK"), isPopular = true)
                )
            )
            changes.add("زیادکردنی بەشی خشتەی نرخ و پاکێجەکان (Pricing Table)")
        }

        if (lower.contains("پەیوەندی") || lower.contains("فۆڕم") || lower.contains("contact") || lower.contains("form")) {
            working = working.copy(showContactForm = true)
            changes.add("زیادکردنی فۆڕمی پەیوەندیکردن و ناردنی نامە (Contact Form)")
        }

        if (lower.contains("ئامار") || lower.contains("چارت") || lower.contains("گراف") || lower.contains("chart") || lower.contains("analytics") || lower.contains("stat")) {
            working = working.copy(
                showAnalyticsChart = true,
                metrics = if (working.metrics.isEmpty()) {
                    listOf(
                        UiMetricCard("کۆی سەردان", "28.4K", "+18%"),
                        UiMetricCard("داهاتی مانگانە", "$14,850", "+24%"),
                        UiMetricCard("بەکارهێنەری چالاک", "3,920", "+12%")
                    )
                } else working.metrics
            )
            changes.add("زیادکردنی بەشی گراف و ئاماری زیندوو (Live Analytics Chart)")
        }

        if (lower.contains("چات") || lower.contains("نامە") || lower.contains("chat") || lower.contains("message")) {
            working = working.copy(showLiveChatFeed = true)
            changes.add("زیادکردنی بەشی گفتوگۆ و چاتی ڕاستەوخۆ (Interactive Chat Box)")
        }

        // Title or custom item extraction if the user asked to add or rename something
        if (lower.contains("ناونیشان") || lower.contains("ناوی") || lower.contains("title") || lower.contains("rename")) {
            val cleaned = msg
                .replace("ناونیشانەکەی بگۆڕە بۆ", "")
                .replace("ناوی بکە بە", "")
                .replace("ناوی بگۆڕە بۆ", "")
                .replace("ناونیشان", "")
                .replace("change title to", "", ignoreCase = true)
                .trim()
                .take(32)
            if (cleaned.isNotBlank()) {
                working = working.copy(appTitle = cleaned, heroTitle = cleaned)
                changes.add("گۆڕینی ناونیشانی سەرەکی بۆ «$cleaned»")
            }
        }

        if (lower.contains("زیاد بکە") || lower.contains("زیادبکە") || lower.contains("add ")) {
            val newItemTitle = msg
                .replace("زیاد بکە", "")
                .replace("زیادبکە", "")
                .replace("بۆ ناو لیستەکە", "")
                .replace("add ", "", ignoreCase = true)
                .trim()
                .take(30)
                .ifBlank { "بەشی نوێی تایبەت" }

            if (!lower.contains("نرخ") && !lower.contains("فۆڕم") && !lower.contains("چارت") && !lower.contains("گەڕان")) {
                val updatedItems = working.items.toMutableList()
                updatedItems.add(
                    0,
                    UiContentItem(
                        id = "custom_${System.currentTimeMillis()}",
                        title = newItemTitle,
                        subtitle = "زیادکراو بەپێی فەرمانی چات: «$msg»",
                        badgeOrPrice = "نوێ ✨",
                        category = working.categories.firstOrNull() ?: "گشتییەکان",
                        iconType = working.layoutArchetype
                    )
                )
                working = working.copy(items = updatedItems)
                changes.add("زیادکردنی بەش/کارتی نوێ: «$newItemTitle»")
            }
        }

        // Customize titles & items with the user's specific prompt topic if they wrote custom subject words
        val topicLabel = extractCleanTopicTitle(msg)
        if (topicLabel.isNotBlank() && !isPureStyleOrSectionTweak) {
            working = working.copy(
                appTitle = topicLabel.take(26),
                heroTitle = msg.take(64)
            )
            if (!changes.any { it.contains(topicLabel) }) {
                changes.add("داڕشتنەوەی ناوەڕۆک و بەشەکان بۆ: «$topicLabel»")
            }
        }

        val replyText = buildString {
            append("✅ فەرمانەکەت جێبەجێ کرا و پێشینە (Preview) بە تەواوی نوێکرایەوە:\n")
            changes.forEach { c -> append("• $c\n") }
            append("ئێستا لە تابی «پێشینە» هەموو بەشە کارلێککەرەکان ئامادەن و کار دەکەن!")
        }

        return BlueprintEvolutionResult(
            blueprint = working,
            assistantReply = replyText.trim(),
            changesApplied = changes
        )
    }

    private fun isPureModificationCommand(lower: String): Boolean {
        val styleWords = listOf(
            "ڕەنگ", "رەنگ", "سوور", "سور", "مۆر", "سەوز", "شین", "زەرد", "زێڕ",
            "تاریک", "ڕەش", "ڕووناک", "سپی", "نرخ", "پاکێج", "فۆڕم", "چارت", "ئامار",
            "زیاد بکە", "زیادبکە", "لابە", "بگۆڕە", "ناونیشان", "ناوی",
            "color", "dark", "light", "red", "purple", "green", "blue", "gold", "add ", "rename", "hide"
        )
        val newAppWords = listOf(
            "ئەپێک", "ئەپی", "وێبسایت", "پڕۆژە", "بەرنامە", "سیستەم", "پلاتفۆرم",
            "حاسیبە", "ژمێریار", "تێبینی", "یاری", "کەشوهەوا", "قورئان", "میوزیک", "چێشتخانە", "فرۆشگا",
            "app", "website", "create", "build", "make", "calculator", "game", "weather", "todo"
        )
        if (newAppWords.any { lower.contains(it) }) return false
        return styleWords.any { lower.contains(it) }
    }

    private fun isExplicitNewProjectRequest(lower: String): Boolean {
        return lower.contains("دروست بکە") || lower.contains("دروستبکە") ||
            lower.contains("ئەپێک") || lower.contains("وێبسایتێک") ||
            lower.contains("بەرنامەیەک") || lower.contains("create ") ||
            lower.contains("build ") || lower.contains("make ")
    }

    private fun extractCleanTopicTitle(rawPrompt: String): String {
        val cleaned = rawPrompt
            .replace("ئەپێکی", "")
            .replace("ئەپێک بۆ", "")
            .replace("ئەپێک", "")
            .replace("وێبسایتێکی", "")
            .replace("وێبسایتێک بۆ", "")
            .replace("وێبسایتێک", "")
            .replace("بەرنامەیەکی", "")
            .replace("دروست بکە", "")
            .replace("دروستبکە", "")
            .replace("بۆ دروست بکە", "")
            .replace("بۆم دروست بکە", "")
            .replace(Regex("(?i)\\b(create|build|make|an|a|app|website|for|me)\\b"), "")
            .trim()
        return cleaned.take(30).ifBlank { rawPrompt.take(25) }
    }

    fun detectArchetype(lower: String): String? {
        return when {
            lower.contains("حاسیبە") || lower.contains("ژمێریار") || lower.contains("حاسبة") ||
                lower.contains("بیرکاری") || lower.contains("کۆکردنەوە") || lower.contains("گۆڕینی دراو") ||
                lower.contains("calc") || lower.contains("calculator") || lower.contains("math") ||
                lower.contains("converter") -> "CALCULATOR"

            lower.contains("تێبینی") || lower.contains("ئەرک") || lower.contains("یاداشت") ||
                lower.contains("کارەکان") || lower.contains("لیستی کار") || lower.contains("بیرخەرەوە") ||
                lower.contains("todo") || lower.contains("task") || lower.contains("note") ||
                lower.contains("checklist") || lower.contains("reminder") -> "TODO_NOTES"

            lower.contains("یاری") || lower.contains("شەترەنج") || lower.contains("xo") ||
                lower.contains("پێشبڕکێ") || lower.contains("مەتەڵ") || lower.contains("تۆپ") ||
                lower.contains("game") || lower.contains("arcade") || lower.contains("tic tac") ||
                lower.contains("puzzle") -> "GAME_ARCADE"

            lower.contains("کەشوهەوا") || lower.contains("کەش و هەوا") || lower.contains("پلەی گەرمی") ||
                lower.contains("باران") || lower.contains("بەفر") || lower.contains("weather") ||
                lower.contains("forecast") || lower.contains("climate") -> "WEATHER_LIVE"

            lower.contains("قورئان") || lower.contains("زیکر") || lower.contains("بانگ") ||
                lower.contains("نوێژ") || lower.contains("تەسبیح") || lower.contains("ئیسلام") ||
                lower.contains("فەرموودە") || lower.contains("ڕەمەزان") || lower.contains("quran") ||
                lower.contains("prayer") || lower.contains("tasbih") || lower.contains("islamic") -> "ISLAMIC_QURAN"

            lower.contains("میوزیک") || lower.contains("گۆرانی") || lower.contains("ڤیدیۆ") ||
                lower.contains("دەنگ") || lower.contains("پۆدکاست") || lower.contains("پلەیەر") ||
                lower.contains("ڕادیۆ") || lower.contains("music") || lower.contains("player") ||
                lower.contains("audio") || lower.contains("video") || lower.contains("podcast") -> "MEDIA_PLAYER"

            lower.contains("چوونەژوورەوە") || lower.contains("تۆمارکردن") || lower.contains("لۆگین") ||
                lower.contains("پاسۆرد") || lower.contains("ئەکاونت") || lower.contains("login") ||
                lower.contains("signup") || lower.contains("register") || lower.contains("auth") -> "AUTH_LOGIN"

            lower.contains("هۆتێل") || lower.contains("ئۆتۆمبێل") || lower.contains("سەیارە") ||
                lower.contains("خانوو") || lower.contains("موڵک") || lower.contains("نۆرە") ||
                lower.contains("گەشت") || lower.contains("بلیت") || lower.contains("hotel") ||
                lower.contains("car") || lower.contains("booking") || lower.contains("real estate") -> "BOOKING_SHOWROOM"

            lower.contains("خواردن") || lower.contains("چێشتخانە") || lower.contains("ڕێستۆرانت") ||
                lower.contains("پیتزا") || lower.contains("کەباب") || lower.contains("بەرگر") ||
                lower.contains("کافێ") || lower.contains("قاوە") || lower.contains("شیرینی") ||
                lower.contains("food") || lower.contains("restaurant") || lower.contains("cafe") ||
                lower.contains("menu") -> "FOOD_DELIVERY"

            lower.contains("بانک") || lower.contains("دراو") || lower.contains("کریپتۆ") ||
                lower.contains("بودجە") || lower.contains("خەرجی") || lower.contains("قازانج") ||
                lower.contains("داشبۆرد") || lower.contains("ئامار") || lower.contains("حسابات") ||
                lower.contains("crypto") || lower.contains("wallet") || lower.contains("dashboard") ||
                lower.contains("finance") -> "FINTECH_DASHBOARD"

            lower.contains("پۆرتفۆلیۆ") || lower.contains("فریلانسەر") || lower.contains("سیڤی") ||
                lower.contains("کەسی") || lower.contains("portfolio") || lower.contains("agency") ||
                lower.contains("freelance") || lower.contains("resume") -> "PORTFOLIO"

            lower.contains("تەندروستی") || lower.contains("وەرزش") || lower.contains("جیم") ||
                lower.contains("پزیشک") || lower.contains("نەخۆشخانە") || lower.contains("دەرمان") ||
                lower.contains("کالۆری") || lower.contains("fitness") || lower.contains("health") ||
                lower.contains("workout") || lower.contains("medical") -> "FITNESS_HEALTH"

            lower.contains("فێربوون") || lower.contains("کۆرس") || lower.contains("قوتابخانە") ||
                lower.contains("زانکۆ") || lower.contains("کتێب") || lower.contains("کویز") ||
                lower.contains("فەرهەنگ") || lower.contains("وەرگێڕ") || lower.contains("زمان") ||
                lower.contains("course") || lower.contains("education") || lower.contains("learn") ||
                lower.contains("translate") -> "EDUCATION"

            lower.contains("چات") || lower.contains("نامە") || lower.contains("سۆشیاڵ") ||
                lower.contains("پۆست") || lower.contains("هاوڕێ") || lower.contains("social") ||
                lower.contains("messenger") -> "SOCIAL_CHAT"

            lower.contains("شۆپینگ") || lower.contains("فرۆشگا") || lower.contains("بازاڕ") ||
                lower.contains("کڕین") || lower.contains("کاڵا") || lower.contains("دوکان") ||
                lower.contains("جلوبەرگ") || lower.contains("shop") || lower.contains("store") ||
                lower.contains("ecommerce") -> "ECOMMERCE"

            else -> null
        }
    }

    fun buildBaseBlueprintForArchetype(archetype: String, rawPrompt: String): DynamicUiBlueprint {
        val topicTitle = extractCleanTopicTitle(rawPrompt)
        return when (archetype) {
            "CALCULATOR" -> DynamicUiBlueprint(
                layoutArchetype = "CALCULATOR",
                appTitle = topicTitle.ifBlank { "BASO Smart Calc" },
                heroTitle = "ژمێریاری زیرەک و گۆڕەری دراو",
                heroSubtitle = "ئەنجامدانی کردارە بیرکارییەکان، ڕێژەی سەدی و گۆڕینی دۆلار و دینار بە خێرایی",
                ctaText = "پاککردنەوەی ژمێریار AC",
                primaryHex = "#F5C518",
                secondaryHex = "#00E5CC",
                backgroundHex = "#070E17",
                surfaceHex = "#111F30",
                heroBgStartHex = "#0C1D31",
                heroBgEndHex = "#1B3B5C",
                isDark = true,
                cardRadiusPx = 20,
                showHeroBanner = false,
                customInteractiveWidget = "CALCULATOR",
                categories = listOf("ژمێریاری ستاندارد", "گۆڕینی دراو ($/IQD)", "قازانج و باج", "مێژووی ژماردن"),
                metrics = listOf(
                    UiMetricCard("نرخی ١٠٠ دۆلار", "150,500 IQD", "زیندوو 💱"),
                    UiMetricCard("زێڕ (مسقاڵ)", "585,000 IQD", "+1.2%"),
                    UiMetricCard("وردبینی", "100%", "⚡ خێرا")
                ),
                items = listOf(
                    UiContentItem("calc1", "گۆڕینی دۆلار بۆ دیناری عێراقی", "$100 = 150,500 IQD", "چالاک 💱", "گۆڕینی دراو ($/IQD)", "CALCULATOR"),
                    UiContentItem("calc2", "هەژمارکردنی داشکاندن (Discount)", "داشکاندنی ٢٠٪ لەسەر کۆی کاڵاکان", "ئامادەیە", "قازانج و باج", "CALCULATOR"),
                    UiContentItem("calc3", "هەژمارکردنی قازانجی مانگانە", "داهات - خەرجی = قازانجی سافی", "دارایی", "قازانج و باج", "CALCULATOR"),
                    UiContentItem("calc4", "گۆڕینی یەکەکانی پێوانە", "کیلۆمەتر، گرام، پلەی گەرمی", "یەکەکان", "ژمێریاری ستاندارد", "CALCULATOR")
                ),
                bottomNavLabels = listOf("ژمێریار", "دراوەکان", "یەکەکان", "مێژوو"),
                userPromptCommand = rawPrompt
            )

            "TODO_NOTES" -> DynamicUiBlueprint(
                layoutArchetype = "TODO_NOTES",
                appTitle = topicTitle.ifBlank { "BASO Tasks & Notes" },
                heroTitle = "پلانی ڕۆژانە، ئەرکەکان و تێبینییە گرنگەکان",
                heroSubtitle = "زیادکردن و نیشانکردنی ئەرکەکان، دابەشکردن بەپێی گرنگی و بیرخەرەوەی کات",
                ctaText = "+ زیادکردنی ئەرکی نوێ",
                primaryHex = "#00E5CC",
                secondaryHex = "#A855F7",
                backgroundHex = "#08121E",
                surfaceHex = "#122438",
                heroBgStartHex = "#0A2338",
                heroBgEndHex = "#1E4468",
                isDark = true,
                cardRadiusPx = 18,
                showSearchBar = true,
                customInteractiveWidget = "TODO",
                categories = listOf("ئەرکەکانی ئەمڕۆ", "تێبینییەکان", "تەواوکراو ✅", "گرنگ ⭐"),
                metrics = listOf(
                    UiMetricCard("ئەرکی ئەمڕۆ", "6 ئەرک", "4 تەواوکراو"),
                    UiMetricCard("تێبینییەکان", "14 تێبینی", "پاشەکەوتکراو"),
                    UiMetricCard("ئاستی ئەنجامدان", "82%", "🔥 نایاب")
                ),
                items = listOf(
                    UiContentItem("t1", "تەواوکردنی دیزاینی پڕۆژەی کڕیار", "پێداچوونەوە بە ڕەنگ و فۆنتەکان", "گرنگ ⭐", "ئەرکەکانی ئەمڕۆ", "TODO"),
                    UiContentItem("t2", "کۆبوونەوە لەگەڵ تیمی گەشەپێدەران", "کاتژمێر ٤:٠٠ی دوانیوەڕۆ — ئۆنلاین", "ئەمڕۆ ⏰", "ئەرکەکانی ئەمڕۆ", "TODO"),
                    UiContentItem("t3", "بیرۆکەکانی ئەپی نوێی زیرەکی دەستکرد", "بەکارهێنانی مۆدێلی Gemini 3.1 Pro", "تێبینی 📝", "تێبینییەکان", "TODO"),
                    UiContentItem("t4", "ناردنی وەشانی APK بۆ تاقیکردنەوە", "بە سەرکەوتوویی نێردرا بۆ GitHub", "تەواوکراو ✅", "تەواوکراو ✅", "TODO")
                ),
                bottomNavLabels = listOf("ئەرکەکان", "تێبینییەکان", "ڕۆژژمێر", "ئامار"),
                userPromptCommand = rawPrompt
            )

            "GAME_ARCADE" -> DynamicUiBlueprint(
                layoutArchetype = "GAME_ARCADE",
                appTitle = topicTitle.ifBlank { "BASO Game Arena" },
                heroTitle = "مەیدانی یاری و پێشبڕکێی زیرەکی 🎮",
                heroSubtitle = "ڕاستەوخۆ لێرە یاری X و O بکە، خاڵ کۆبکەرەوە و ڕیکۆردی نوێ تۆمار بکە!",
                ctaText = "دەستپێکردنی یاری نوێ 🚀",
                primaryHex = "#A855F7",
                secondaryHex = "#00E5CC",
                backgroundHex = "#0B0719",
                surfaceHex = "#181134",
                heroBgStartHex = "#1E0E42",
                heroBgEndHex = "#421B8A",
                isDark = true,
                cardRadiusPx = 22,
                customInteractiveWidget = "GAME_XO",
                categories = listOf("یاری ڕاستەوخۆ (X-O)", "پێشبڕکێی زانیاری", "ڕیزبەندی یاریزانان", "خەڵاتەکان"),
                metrics = listOf(
                    UiMetricCard("خاڵی تۆ (XP)", "2,850", "ئاستی ١٢ 🏆"),
                    UiMetricCard("بردنەوەکان", "18 بردنەوە", "🔥 زنجیرەیی"),
                    UiMetricCard("پلەی ڕیزبەندی", "#1 VIP", "کوردستان")
                ),
                items = listOf(
                    UiContentItem("g1", "یاری کلاسیکی X و O (Tic-Tac-Toe)", "دژی زیرەکی دەستکرد یان هاوڕێکەت", "یاری بکە 🎮", "یاری ڕاستەوخۆ (X-O)", "GAME"),
                    UiContentItem("g2", "پێشبڕکێی پرسیار و وەڵامی گشتی", "٢٠ پرسیاری زانستی و مێژوویی", "+500 XP", "پێشبڕکێی زانیاری", "GAME"),
                    UiContentItem("g3", "مەتەڵی ژمارە و لۆجیک", "چالاککردنی مێشک و بیرکردنەوەی خێرا", "ئاستی قورس", "پێشبڕکێی زانیاری", "GAME"),
                    UiContentItem("g4", "جامی پاڵەوانێتی هەفتانە", "خەڵاتی تایبەت بۆ ٣ یەکەمەکان", "زیندوو 🏆", "ڕیزبەندی یاریزانان", "GAME")
                ),
                bottomNavLabels = listOf("یاریگە", "پێشبڕکێ", "ڕیزبەندی", "پڕۆفایل"),
                userPromptCommand = rawPrompt
            )

            "WEATHER_LIVE" -> DynamicUiBlueprint(
                layoutArchetype = "WEATHER_LIVE",
                appTitle = topicTitle.ifBlank { "Kurdistan Weather Live" },
                heroTitle = "هەولێر • ٢٤°C — ئاسمانی ساماڵ و فێنک ☀️",
                heroSubtitle = "پێشبینی وردی کەشوهەوای شارەکانی کوردستان و جیهان بۆ ٧ ڕۆژی داهاتوو",
                ctaText = "نوێکردنەوەی کەشوهەوا 🔄",
                primaryHex = "#38BDF8",
                secondaryHex = "#F5C518",
                backgroundHex = "#061322",
                surfaceHex = "#0F243C",
                heroBgStartHex = "#0A284B",
                heroBgEndHex = "#1D5B9E",
                isDark = true,
                cardRadiusPx = 22,
                customInteractiveWidget = "WEATHER",
                categories = listOf("هەولێر", "سلێمانی", "دهۆک", "کەرکووک", "هەڵەبجە"),
                metrics = listOf(
                    UiMetricCard("ڕێژەی شێ", "42%", "💧 مامناوەند"),
                    UiMetricCard("خێرایی با", "14 km/h", "🌬️ باکوور"),
                    UiMetricCard("ئەگەری باران", "10%", "☀️ ساماڵ")
                ),
                items = listOf(
                    UiContentItem("w1", "هەولێر (Erbil)", "ساماڵ و خۆرەتاو • بەرزترین ٢٦° / نزمترین ١٥°", "24°C ☀️", "هەولێر", "WEATHER"),
                    UiContentItem("w2", "سلێمانی (Sulaymaniyah)", "نیمچە هەور و شنەبا • بەرزترین ٢٢° / نزمترین ١٢°", "20°C ⛅", "سلێمانی", "WEATHER"),
                    UiContentItem("w3", "دهۆک (Duhok)", "ئاسمانی پاک و فێنک • بەرزترین ٢٣° / نزمترین ١٣°", "21°C 🌤️", "دهۆک", "WEATHER"),
                    UiContentItem("w4", "کەرکووک (Kirkuk)", "خۆرەتاو و گەرم • بەرزترین ٢٨° / نزمترین ١٧°", "27°C ☀️", "کەرکووک", "WEATHER")
                ),
                bottomNavLabels = listOf("ئەمڕۆ", "٧ ڕۆژە", "شارەکان", "ڕادار"),
                userPromptCommand = rawPrompt
            )

            "ISLAMIC_QURAN" -> DynamicUiBlueprint(
                layoutArchetype = "ISLAMIC_QURAN",
                appTitle = topicTitle.ifBlank { "نووری قورئان و زیکر" },
                heroTitle = "قورئانی پیرۆز، کاتەکانی بانگ و زیکری ڕۆژانە 🕌",
                heroSubtitle = "خوێندنەوە و گوێگرتن لە قورئانی پیرۆز، تەسبیحی ئەلیکترۆنی و کاتەکانی نوێژ بەپێی شارەکەت",
                ctaText = "خوێندنەوەی قورئان 📖",
                primaryHex = "#10B981",
                secondaryHex = "#F5C518",
                backgroundHex = "#051611",
                surfaceHex = "#0D2B22",
                heroBgStartHex = "#083024",
                heroBgEndHex = "#145943",
                isDark = true,
                cardRadiusPx = 20,
                customInteractiveWidget = "TASBIH",
                categories = listOf("کاتەکانی بانگ", "تەسبیح و زیکر", "سوورەتەکان", "قیبلەنما"),
                metrics = listOf(
                    UiMetricCard("بانگی داهاتوو", "عەسر 03:45", "🕌 هەولێر"),
                    UiMetricCard("زیکری ئەمڕۆ", "100 جار", "📿 تەسبیح"),
                    UiMetricCard("خەتمی قورئان", "جوزئی ١٤", "📖 بەردەوام")
                ),
                items = listOf(
                    UiContentItem("q1", "سوورەتی الفاتحة (١)", "مەککی • ٧ ئایەت • خوێندنەوە و تەفسیر", "ئایەتی ٧", "سوورەتەکان", "ISLAMIC"),
                    UiContentItem("q2", "سوورەتی الكهف (١٨)", "مەککی • ١١٠ ئایەت • نووری نێوان دوو هەینی", "ئایەتی ١١٠", "سوورەتەکان", "ISLAMIC"),
                    UiContentItem("q3", "زیکری بەیانیان و ئێواران", "قەڵای موسڵمان بە دەنگ و نووسین", "تەواو ✨", "تەسبیح و زیکر", "ISLAMIC"),
                    UiContentItem("q4", "کاتەکانی بانگ بۆ هەرێمی کوردستان", "بەیانی 04:52 • نیوەڕۆ 12:15 • ئێوارە 06:08", "ورد 🕌", "کاتەکانی بانگ", "ISLAMIC")
                ),
                bottomNavLabels = listOf("قورئان", "بانگەکان", "تەسبیح", "زیکرەکان"),
                userPromptCommand = rawPrompt
            )

            "MEDIA_PLAYER" -> DynamicUiBlueprint(
                layoutArchetype = "MEDIA_PLAYER",
                appTitle = topicTitle.ifBlank { "BASO Wave Player" },
                heroTitle = "میوزیک و پۆدکاست بە کوالێتی ستۆدیۆ (Hi-Fi) 🎧",
                heroSubtitle = "پلەیەری زیرەک بە ئیکۆلایزەری دەنگ، لیستی دڵخوازەکان و کارکردن بەبێ ئینتەرنێت",
                ctaText = "لێدانی هەموو تراکەکان ▶️",
                primaryHex = "#EC4899",
                secondaryHex = "#00E5CC",
                backgroundHex = "#120718",
                surfaceHex = "#241030",
                heroBgStartHex = "#2E0C38",
                heroBgEndHex = "#631878",
                isDark = true,
                cardRadiusPx = 20,
                customInteractiveWidget = "PLAYER",
                categories = listOf("هەموو تراکەکان", "پۆدکاست", "ئاوازی کوردی", "دڵخوازەکان ❤️"),
                metrics = listOf(
                    UiMetricCard("کوالێتی دەنگ", "Lossless", "320 kbps"),
                    UiMetricCard("تراکەکان", "48 تراک", "🎧 ئامادە"),
                    UiMetricCard("ئیکۆلایزەر", "Bass Boost", "چالاک ⚡")
                ),
                items = listOf(
                    UiContentItem("m1", "ئاوازی چیاکانی کوردستان (Instrumental)", "ئۆرکێسترای ستۆدیۆ • ٤:١٥ خولەک", "04:15 ▶️", "ئاوازی کوردی", "MEDIA"),
                    UiContentItem("m2", "پۆدکاستی داهاتووی زیرەکی دەستکرد", "ئەڵقەی ١٢ • گفتوگۆی تەکنەلۆژی", "28:40 🎙️", "پۆدکاست", "MEDIA"),
                    UiContentItem("m3", "شەوانی هەولێر — Chill Synthwave", "میوزیکی ئارامبەخش بۆ کاتی کارکردن", "03:50 ▶️", "هەموو تراکەکان", "MEDIA"),
                    UiContentItem("m4", "مەقامی ڕەسەن و عود", "تۆمارکراوی تایبەت • کوالێتی بەرز", "06:10 ❤️", "دڵخوازەکان ❤️", "MEDIA")
                ),
                bottomNavLabels = listOf("لێدەر", "کتێبخانە", "پۆدکاست", "ڕێکخستن"),
                userPromptCommand = rawPrompt
            )

            "AUTH_LOGIN" -> DynamicUiBlueprint(
                layoutArchetype = "AUTH_LOGIN",
                appTitle = topicTitle.ifBlank { "BASO Secure Portal" },
                heroTitle = "بەخێربێیتەوە — چوونەژوورەوە بۆ هەژمارەکەت 🔐",
                heroSubtitle = "سیستەمی پارێزراوی چوونەژوورەوە، دروستکردنی هەژمار و پشتڕاستکردنەوەی دوو هەنگاوی",
                ctaText = "چوونەژوورەوە 🚀",
                primaryHex = "#00E5CC",
                secondaryHex = "#F5C518",
                backgroundHex = "#060F1A",
                surfaceHex = "#102134",
                heroBgStartHex = "#091E32",
                heroBgEndHex = "#163F66",
                isDark = true,
                cardRadiusPx = 20,
                customInteractiveWidget = "LOGIN",
                categories = listOf("چوونەژوورەوە", "هەژماری نوێ", "گەڕاندنەوەی وشەی نهێنی"),
                metrics = listOf(
                    UiMetricCard("ئاسایش", "256-Bit SSL", "🔒 پارێزراو"),
                    UiMetricCard("چوونەژوورەوە", "1-Click", "Google / Apple"),
                    UiMetricCard("دۆخ", "چالاک", "✅ ئۆنلاین")
                ),
                items = listOf(
                    UiContentItem("a1", "چوونەژوورەوەی خێرا بە پەنجەمۆر (Biometric)", "پشتگیری تەواوی پاسکی و پەنجەمۆر", "چالاک 🔐", "چوونەژوورەوە", "AUTH"),
                    UiContentItem("a2", "بەستنەوە بە هەژماری Google و GitHub", "تۆمارکردنی دەستبەجێ بە یەک کرتە", "OAuth 2.0", "هەژماری نوێ", "AUTH")
                ),
                bottomNavLabels = listOf("چوونەژوورەوە", "تۆماربوون", "ئاسایش", "یارمەتی"),
                userPromptCommand = rawPrompt
            )

            "BOOKING_SHOWROOM" -> DynamicUiBlueprint(
                layoutArchetype = "BOOKING_SHOWROOM",
                appTitle = topicTitle.ifBlank { "BASO VIP Booking & Motors" },
                heroTitle = rawPrompt.take(55).ifBlank { "پێشانگا و حیجزکردنی VIP بە نرخی تایبەت" },
                heroSubtitle = "گەڕان و هەڵبژاردنی باشترین ئۆفەرەکان لەگەڵ حیجزکردنی ڕاستەوخۆ و پشکنینی زانیاری تەواو",
                ctaText = "ئێستا حیجزی بکە 📅",
                primaryHex = "#F5C518",
                secondaryHex = "#00E5CC",
                backgroundHex = "#081018",
                surfaceHex = "#122130",
                heroBgStartHex = "#142234",
                heroBgEndHex = "#2A4768",
                isDark = true,
                cardRadiusPx = 20,
                showSearchBar = true,
                categories = listOf("هەموو ئۆفەرەکان", "VIP / لوکس", "نوێترین", "گونجاوترین نرخ"),
                metrics = listOf(
                    UiMetricCard("بەردەست", "+65 ئۆفەر", "🟢 ئامادە"),
                    UiMetricCard("پشتڕاستکراوە", "100%", "⭐ فەرمی"),
                    UiMetricCard("پشتیوانی", "24/7", "ڕاستەوخۆ")
                ),
                items = listOf(
                    UiContentItem("b1", "پاکێجی تایبەتی VIP #1 — ${topicTitle.take(16)}", "تایبەتمەندی تەواو و گەرەنتی فەرمی", "$120 / ڕۆژ", "VIP / لوکس", "BOOKING"),
                    UiContentItem("b2", "پاکێجی پڕۆفیشناڵ #2 — مۆدێلی ٢٠٢٥", "پشکنینی تەواو و خزمەتگوزاری خێرا", "$85 / ڕۆژ", "نوێترین", "BOOKING"),
                    UiContentItem("b3", "ئۆفەری خێزانی و تایبەت #3", "داشکاندنی ٢٠٪ بۆ حیجزکردنی پێشوەختە", "$65 / ڕۆژ", "گونجاوترین نرخ", "BOOKING"),
                    UiContentItem("b4", "بژاردەی ستانداردی باوەڕپێکراو #4", "پەسەندکراو لەلایەن بەکارهێنەرانەوە", "4.9 ★", "هەموو ئۆفەرەکان", "BOOKING")
                ),
                showContactForm = true,
                bottomNavLabels = listOf("پێشانگا", "گەڕان", "حیجزەکانم", "پەیوەندی"),
                userPromptCommand = rawPrompt
            )

            "FOOD_DELIVERY" -> DynamicUiBlueprint(
                layoutArchetype = "FOOD_DELIVERY",
                appTitle = topicTitle.ifBlank { "BasoFood Express" },
                heroTitle = "تامە ڕەسەنەکان لە ١٥ خولەکدا دەگاتە دەستت",
                heroSubtitle = "داواکردنی خێرای خواردن و شیرینی بە گەیاندنی بێبەرامبەر",
                ctaText = "ئێستا داوا بکە 🍔",
                primaryHex = "#FF6B35",
                secondaryHex = "#F5C518",
                backgroundHex = "#120B08",
                surfaceHex = "#221510",
                heroBgStartHex = "#3A1208",
                heroBgEndHex = "#7A2610",
                isDark = true,
                cardRadiusPx = 20,
                showSearchBar = true,
                categories = listOf("کەباب و برژاو", "پیتزا و بەرگر", "شیرینی", "خواردنەوە"),
                metrics = listOf(
                    UiMetricCard("کاتی گەیاندن", "15 خولەک", "⚡ خێرا"),
                    UiMetricCard("هەڵسەنگاندن", "4.9 ★", "+2.4K"),
                    UiMetricCard("داشکاندن", "25% OFF", "ئەمڕۆ")
                ),
                items = listOf(
                    UiContentItem("f1", "پیتزای شاش و پەنیر", "پەنیری مۆزارێلا و سۆسی تایبەت", "12,000 IQD", "پیتزا و بەرگر", "FOOD"),
                    UiContentItem("f2", "دەوری کەبابی سلێمانی", "لەگەڵ نانی گەرم و سەوزەوات", "15,000 IQD", "کەباب و برژاو", "FOOD"),
                    UiContentItem("f3", "بەرگری دەبڵ چێدار", "گۆشتی تازە و پەتاتەی برژاو", "9,500 IQD", "پیتزا و بەرگر", "FOOD"),
                    UiContentItem("f4", "قاوەی ئێسپرێسۆ و کێک", "شیرینی تازەی ڕۆژانە", "6,000 IQD", "شیرینی", "FOOD")
                ),
                bottomNavLabels = listOf("مێنۆ", "ئۆفەرەکان", "سەبەتە 🛒", "داواکارییەکان"),
                userPromptCommand = rawPrompt
            )

            "FINTECH_DASHBOARD" -> DynamicUiBlueprint(
                layoutArchetype = "FINTECH_DASHBOARD",
                appTitle = topicTitle.ifBlank { "BASO Pay & Analytics" },
                heroTitle = "کۆی گشتی باڵانس: $48,920.50",
                heroSubtitle = "داشبۆردی زیرەکی بەڕێوەبردنی دارایی، کریپتۆ و ئاماری فرۆشتن",
                ctaText = "ناردن و وەرگرتنی پارە 💸",
                primaryHex = "#00E5CC",
                secondaryHex = "#6366F1",
                backgroundHex = "#060D16",
                surfaceHex = "#0F1E30",
                heroBgStartHex = "#091D34",
                heroBgEndHex = "#183B66",
                isDark = true,
                cardRadiusPx = 18,
                categories = listOf("گشتییەکان", "کریپتۆ", "کارتەکان", "خەرجی"),
                metrics = listOf(
                    UiMetricCard("داهاتی مانگ", "$18,420", "+22.4%"),
                    UiMetricCard("خەرجییەکان", "$3,150", "-4.1%"),
                    UiMetricCard("قازانجی سافی", "$15,270", "+31.0%")
                ),
                items = listOf(
                    UiContentItem("c1", "Bitcoin (BTC)", "0.64 BTC • گەشەی ڕۆژانە", "$42,180", "کریپتۆ", "CRYPTO"),
                    UiContentItem("c2", "Ethereum (ETH)", "4.20 ETH • Staking Active", "$13,400", "کریپتۆ", "CRYPTO"),
                    UiContentItem("c3", "پارەدانی پڕۆژەی دیزاین", "وەرگیراو لە کڕیار", "+$1,250", "کارتەکان", "CRYPTO"),
                    UiContentItem("c4", "بەشداری سێرڤەر و کڵاود", "پارەدانی خۆکار", "-$49.00", "خەرجی", "CRYPTO")
                ),
                showAnalyticsChart = true,
                bottomNavLabels = listOf("جزدان", "ئامار 📊", "کارتەکان", "ناردن"),
                userPromptCommand = rawPrompt
            )

            "PORTFOLIO" -> DynamicUiBlueprint(
                layoutArchetype = "PORTFOLIO",
                appTitle = topicTitle.ifBlank { "BASO Creative Studio" },
                heroTitle = "باسۆ — گەشەپێدەری ئەپ و دیزاینەری UI/UX",
                heroSubtitle = "دروستکردنی پڕۆژەی دیجیتاڵی، وێبسایتی مۆدێرن، و ئەپی مۆبایل بە ستانداردی جیهانی",
                ctaText = "داواکردنی پڕۆژە ✨",
                primaryHex = "#F5C518",
                secondaryHex = "#00E5CC",
                backgroundHex = "#050C14",
                surfaceHex = "#0D1E2E",
                heroBgStartHex = "#091826",
                heroBgEndHex = "#1C354D",
                isDark = true,
                cardRadiusPx = 18,
                categories = listOf("هەموو کارەکان", "ئەپی مۆبایل", "وێبسایت", "دیزاینی UI"),
                metrics = listOf(
                    UiMetricCard("پڕۆژەی تەواوکراو", "+85", "100% سەرکەوتوو"),
                    UiMetricCard("ئەزموونی کار", "6 ساڵ", "VIP Studio"),
                    UiMetricCard("ڕەزامەندی", "5.0 ★", "کڕیاران")
                ),
                items = listOf(
                    UiContentItem("p1", "سیستەمی بازاڕکردنی KurdMall", "ئەپی Android و iOS + داشبۆرد", "ئەپی مۆبایل", "ئەپی مۆبایل", "PORTFOLIO"),
                    UiContentItem("p2", "پلاتفۆرمی فێرکاری زیرەک", "وێب ئەپی React + زیرەکی دەستکرد", "وێبسایت", "وێبسایت", "PORTFOLIO"),
                    UiContentItem("p3", "ناسنامەی بینراوی براندی BASO", "Design System & 3D Branding", "دیزاینی UI", "دیزاینی UI", "PORTFOLIO"),
                    UiContentItem("p4", "ئەپی گەیاندن و نەخشەی زیندوو", "Kotlin Jetpack Compose", "ئەپی مۆبایل", "ئەپی مۆبایل", "PORTFOLIO")
                ),
                showPricingSection = true,
                pricingPlans = listOf(
                    UiPricingPlan("دیزاینی UI/UX", "$250", listOf("دیزاینی تەواوی شاشەکان", "فایلی فیگما و پرۆتۆتایپ", "پاڵپشتی ٢ زمانی کوردی/ئینگلیزی")),
                    UiPricingPlan("پڕۆژەی تەواو (App + Web)", "$650", listOf("کۆدی تەواوی ئەپ و وێب", "فایلی APK و ZIP ئامادە", "گرێنتی و پاڵپشتی تەکنیکی"), isPopular = true)
                ),
                showContactForm = true,
                bottomNavLabels = listOf("سەرەکی", "کارەکانم", "نرخەکان", "پەیوەندی"),
                userPromptCommand = rawPrompt
            )

            "FITNESS_HEALTH" -> DynamicUiBlueprint(
                layoutArchetype = "FITNESS_HEALTH",
                appTitle = topicTitle.ifBlank { "BasoFit Pro" },
                heroTitle = "ئامانجی ئەمڕۆت: 8,450 هەنگاو و 620 کالۆری",
                heroSubtitle = "بەرنامەی ڕاهێنانی ڕۆژانە، خۆراکی تەندروست و چاودێری لێدانی دڵ",
                ctaText = "دەستپێکردنی ڕاهێنان 💪",
                primaryHex = "#10B981",
                secondaryHex = "#38BDF8",
                backgroundHex = "#061512",
                surfaceHex = "#0F2922",
                heroBgStartHex = "#082E24",
                heroBgEndHex = "#135E4B",
                isDark = true,
                cardRadiusPx = 22,
                categories = listOf("ڕاهێنانەکان", "خشتەی خۆراک", "لێدانی دڵ", "ئامار"),
                metrics = listOf(
                    UiMetricCard("کالۆری سووتاو", "620 kcal", "85% ئامانج"),
                    UiMetricCard("لێدانی دڵ", "76 BPM", "ئاسایی ❤️"),
                    UiMetricCard("کاتی وەرزش", "48 خولەک", "+12 خولەک")
                ),
                items = listOf(
                    UiContentItem("h1", "ڕاهێنانی هێز و ماسولکە (Full Body)", "٤٥ خولەک • ئاستی مامناوەند", "320 kcal", "ڕاهێنانەکان", "FITNESS"),
                    UiContentItem("h2", "ڕاکردن و کاردیۆی بەیانیان", "٢٥ خولەک • سووتاندنی چەوری", "240 kcal", "ڕاهێنانەکان", "FITNESS"),
                    UiContentItem("h3", "ژەمی پڕۆتینی تەندروست", "سنگمریشک + برنجی قاوەیی + زەڵاتە", "480 kcal", "خشتەی خۆراک", "FITNESS"),
                    UiContentItem("h4", "یۆگا و ئارامکردنەوەی مێشک", "١٥ خولەک پێش خەوتن", "90 kcal", "ڕاهێنانەکان", "FITNESS")
                ),
                showAnalyticsChart = true,
                bottomNavLabels = listOf("چالاکی", "ڕاهێنان", "خۆراک", "تەندروستی"),
                userPromptCommand = rawPrompt
            )

            "EDUCATION" -> DynamicUiBlueprint(
                layoutArchetype = "EDUCATION",
                appTitle = topicTitle.ifBlank { "BASO Academy" },
                heroTitle = "فێربوونی پرۆگرامینگ، زیرەکی دەستکرد و دیزاین",
                heroSubtitle = "کۆرسی ڤیدیۆیی بە زمانی کوردی لەگەڵ تاقیکردنەوەی زیندوو و بڕوانامە",
                ctaText = "دەستپێکردنی خوێندن 🎓",
                primaryHex = "#8B5CF6",
                secondaryHex = "#00E5CC",
                backgroundHex = "#0B091B",
                surfaceHex = "#171432",
                heroBgStartHex = "#1E164B",
                heroBgEndHex = "#372982",
                isDark = true,
                cardRadiusPx = 18,
                showSearchBar = true,
                categories = listOf("کۆرسەکان", "پرۆگرامینگ", "دیزاینی UI", "زیرەکی دەستکرد"),
                metrics = listOf(
                    UiMetricCard("وانەی تەواوکراو", "24 وانە", "78% پێشکەوتن"),
                    UiMetricCard("خاڵی بەدەستهاتوو", "1,450 XP", "ئاستی زێڕین"),
                    UiMetricCard("بڕوانامەکان", "4 بڕوانامە", "پەسەندکراو")
                ),
                items = listOf(
                    UiContentItem("e1", "دروستکردنی ئەپی Android بە Kotlin", "٣٢ وانەی ڤیدیۆیی • پڕۆژەی کرداری", "بێبەرامبەر", "پرۆگرامینگ", "COURSE"),
                    UiContentItem("e2", "فێربوونی React & Tailwind CSS", "٢٤ وانە • دروستکردنی وێب ئەپ", "VIP Course", "پرۆگرامینگ", "COURSE"),
                    UiContentItem("e3", "ئەندازیاری پڕۆمپت و Gemini AI", "١٨ وانە • بەکارهێنانی API", "نوێ ✨", "زیرەکی دەستکرد", "COURSE"),
                    UiContentItem("e4", "ماستەرکڵاسی دیزاینی UI/UX", "٢٠ وانە • لە سفرەوە بۆ پڕۆفیشناڵ", "4.9 ★", "دیزاینی UI", "COURSE")
                ),
                bottomNavLabels = listOf("کۆرسەکان", "فێربوون", "تاقیکردنەوە", "بڕوانامە"),
                userPromptCommand = rawPrompt
            )

            "SOCIAL_CHAT" -> DynamicUiBlueprint(
                layoutArchetype = "SOCIAL_CHAT",
                appTitle = topicTitle.ifBlank { "BasoConnect" },
                heroTitle = "تۆڕی پەیوەندی و چاتی ڕاستەوخۆ",
                heroSubtitle = "نامەگۆڕینەوەی خێرا، ژووری گفتوگۆی گەشەپێدەران و هاوبەشیکردنی بیرۆکەکان",
                ctaText = "نامەی نوێ بنێرە 💬",
                primaryHex = "#00E5CC",
                secondaryHex = "#EC4899",
                backgroundHex = "#070F19",
                surfaceHex = "#102033",
                heroBgStartHex = "#0A2239",
                heroBgEndHex = "#1E3F66",
                isDark = true,
                cardRadiusPx = 20,
                categories = listOf("نامەکان", "گرووپەکان", "چالاکییەکان", "هاوڕێیان"),
                metrics = listOf(
                    UiMetricCard("ئۆنلاین", "142 کەس", "🟢 چالاک"),
                    UiMetricCard("نامەی نوێ", "8 نامە", "ئێستا"),
                    UiMetricCard("گرووپەکان", "12 گرووپ", "کوردستان")
                ),
                items = listOf(
                    UiContentItem("s1", "گرووپی گەشەپێدەرانی کوردستان", "باسۆ: وەشانی نوێی ئەپەکە تەواو بوو 🚀", "ئێستا", "گرووپەکان", "CHAT"),
                    UiContentItem("s2", "ئارام محەمەد (UI Designer)", "فایلی دیزاینەکەم نارد بۆ پێداچوونەوە", "٢ خولەک پێش", "نامەکان", "CHAT"),
                    UiContentItem("s3", "ژووری زیرەکی دەستکرد (AI Hub)", "گفتوگۆ لەسەر مۆدێلی Gemini 3.1", "١٠ خولەک پێش", "گرووپەکان", "CHAT"),
                    UiContentItem("s4", "سارا کەریم (Web Developer)", "پیرۆزە! دیزاینەکە زۆر ناوازەیە ✨", "١ کاتژمێر پێش", "نامەکان", "CHAT")
                ),
                showLiveChatFeed = true,
                bottomNavLabels = listOf("چاتەکان", "گرووپ", "پۆستەکان", "پڕۆفایل"),
                userPromptCommand = rawPrompt
            )

            "CUSTOM_STUDIO" -> {
                val customTitle = topicTitle.ifBlank { "BASO Custom App" }
                val words = rawPrompt.split(" ", "،", ",").map { it.trim() }.filter { it.length > 2 }
                val primarySubject = words.firstOrNull() ?: customTitle
                DynamicUiBlueprint(
                    layoutArchetype = "CUSTOM_STUDIO",
                    appTitle = customTitle,
                    heroTitle = rawPrompt.take(65).ifBlank { "پڕۆژەی تایبەت دروستکراو بە BASO AI" },
                    heroSubtitle = "سیستەمی تەواو کارلێککەر بۆ «$customTitle» — هەموو فۆڕم، دوگمە و بەشەکان کار دەکەن",
                    ctaText = "جێبەجێکردن و تاقیکردنەوە ✨",
                    primaryHex = "#00E5CC",
                    secondaryHex = "#F5C518",
                    backgroundHex = "#07121E",
                    surfaceHex = "#102336",
                    heroBgStartHex = "#0B2138",
                    heroBgEndHex = "#1D436B",
                    isDark = true,
                    cardRadiusPx = 18,
                    showSearchBar = true,
                    customInteractiveWidget = "CUSTOM_CONSOLE",
                    customInputFields = listOf(
                        "ناونیشان یان داتای نوێ بۆ ($customTitle)",
                        "تێبینی یان بڕی دیاریکراو"
                    ),
                    customActionButtons = listOf(
                        "➕ تۆمارکردنی داتا",
                        "⚡ پشکنین و ژماردن",
                        "📊 هەناردەکردنی ڕاپۆرت"
                    ),
                    categories = listOf("بەشی سەرەکی", "تۆمارکراوەکان", "ئامار و ڕاپۆرت", "ڕێکخستن"),
                    metrics = listOf(
                        UiMetricCard("دۆخی سیستەم", "چالاک 🟢", "100%"),
                        UiMetricCard("تۆمارەکان", "+24 داتا", "نوێکراوە"),
                        UiMetricCard("وەشان", "v2.0 Pro", "BASO AI")
                    ),
                    items = listOf(
                        UiContentItem("u1", "بەشی ١: بەڕێوەبردنی $customTitle", "زیادکردن، گەڕان و دەستکاریکردنی ڕاستەوخۆ", "چالاک ✅", "بەشی سەرەکی", "TECH"),
                        UiContentItem("u2", "بەشی ٢: فۆڕمی زانیاری و تۆمارکردن", "پاشەکەوتکردنی داتاکان بە شێوازی خۆکار", "سیستەم", "تۆمارکراوەکان", "TECH"),
                        UiContentItem("u3", "بەشی ٣: پانێڵی کۆنترۆڵی $primarySubject", "ڕووکاری تایبەتمەند بەپێی فەرمانی بەکارهێنەر", "مۆدێرن ✨", "بەشی سەرەکی", "TECH"),
                        UiContentItem("u4", "بەشی ٤: ڕاپۆرت و ئاماری زیندوو", "پشکنینی وردی چالاکییەکان و ئەنجامەکان", "زیندوو 📊", "ئامار و ڕاپۆرت", "TECH")
                    ),
                    showAnalyticsChart = true,
                    showContactForm = true,
                    bottomNavLabels = listOf("داشبۆرد", "تۆمارکردن", "ڕاپۆرت", "ڕێکخستن"),
                    userPromptCommand = rawPrompt
                )
            }

            else -> DynamicUiBlueprint(
                layoutArchetype = "ECOMMERCE",
                appTitle = topicTitle.ifBlank { "MyShop" },
                heroTitle = if (topicTitle.isNotBlank() && topicTitle != "MyShop") {
                    "فرۆشگای تایبەتی $topicTitle — باشترین کاڵاکان"
                } else {
                    "Best Products For You"
                },
                heroSubtitle = "باشترین بەرهەمە ئەلیکترۆنی و مۆدێرنەکان بە نرخی تایبەت و گەیاندنی خێرا",
                ctaText = "ئێستا بکڕە 🛍️",
                primaryHex = "#F5C518",
                secondaryHex = "#00E5CC",
                backgroundHex = "#08121C",
                surfaceHex = "#122334",
                heroBgStartHex = "#091420",
                heroBgEndHex = "#243E58",
                isDark = true,
                cardRadiusPx = 16,
                showSearchBar = true,
                categories = listOf("هەموو کاڵاکان", "ئەلیکترۆنی", "جلوبەرگ", "داشکاندن 🔥"),
                metrics = listOf(
                    UiMetricCard("داشکاندنی وەرز", "40% OFF", "🔥 تایبەت"),
                    UiMetricCard("بەرهەمی بەردەست", "+240", "ئۆرجیناڵ"),
                    UiMetricCard("گەیاندن", "خۆڕایی", "٢٤ کاتژمێر")
                ),
                items = listOf(
                    UiContentItem("1", "Studio Headphones Pro", "Noise Cancelling • Wireless", "$49.99", "ئەلیکترۆنی", "SHOP"),
                    UiContentItem("2", "BASO Smart Watch Ultra", "AMOLED • 7 Days Battery", "$79.99", "ئەلیکترۆنی", "SHOP"),
                    UiContentItem("3", "4K Creator Camera", "Ultra HD • Stabilized", "$129.00", "داشکاندن 🔥", "SHOP"),
                    UiContentItem("4", "Smart Home Speaker", "Hi-Fi Surround Audio", "$64.50", "هەموو کاڵاکان", "SHOP")
                ),
                bottomNavLabels = listOf("فرۆشگا", "گەڕان", "سەبەتە 🛒", "هەژمار"),
                userPromptCommand = rawPrompt
            )
        }
    }

    fun buildInteractivePreviewHtmlFromBlueprint(bp: DynamicUiBlueprint): String {
        val itemsHtml = bp.items.joinToString("\n") { item ->
            """
            <div class="card">
              <div class="card-title">${item.title}</div>
              <div class="card-sub">${item.subtitle}</div>
              <div class="card-footer">
                <span class="badge">${item.badgeOrPrice}</span>
                <button class="action-btn" onclick="alert('${item.title} چالاک کرا!')">+</button>
              </div>
            </div>
            """.trimIndent()
        }

        val metricsHtml = if (bp.metrics.isNotEmpty()) {
            """
            <section class="metrics">
              ${bp.metrics.joinToString("\n") { m -> "<div class='metric-box'><small>${m.label}</small><strong>${m.value}</strong><span>${m.trend}</span></div>" }}
            </section>
            """.trimIndent()
        } else ""

        val interactiveWidgetHtml = when (bp.customInteractiveWidget) {
            "CALCULATOR" -> """
            <section style="padding:14px;">
              <div style="background:var(--surface);padding:16px;border-radius:var(--radius);border:1px solid var(--secondary);">
                <input id="calc-display" type="text" value="0" readonly style="width:100%;font-size:26px;padding:12px;margin-bottom:12px;border-radius:10px;border:none;background:#050C14;color:#00E5CC;text-align:left;box-sizing:border-box;" />
                <div style="display:grid;grid-template-columns:repeat(4,1fr);gap:8px;">
                  <button class="cta" onclick="document.getElementById('calc-display').value='0'">AC</button>
                  <button class="cta" onclick="document.getElementById('calc-display').value+='/'">÷</button>
                  <button class="cta" onclick="document.getElementById('calc-display').value+='*'">×</button>
                  <button class="cta" onclick="document.getElementById('calc-display').value+='-'">-</button>
                  <button class="cta" onclick="let d=document.getElementById('calc-display');d.value=d.value==='0'?'7':d.value+'7'">7</button>
                  <button class="cta" onclick="let d=document.getElementById('calc-display');d.value=d.value==='0'?'8':d.value+'8'">8</button>
                  <button class="cta" onclick="let d=document.getElementById('calc-display');d.value=d.value==='0'?'9':d.value+'9'">9</button>
                  <button class="cta" onclick="document.getElementById('calc-display').value+='+'">+</button>
                  <button class="cta" onclick="let d=document.getElementById('calc-display');d.value=d.value==='0'?'4':d.value+'4'">4</button>
                  <button class="cta" onclick="let d=document.getElementById('calc-display');d.value=d.value==='0'?'5':d.value+'5'">5</button>
                  <button class="cta" onclick="let d=document.getElementById('calc-display');d.value=d.value==='0'?'6':d.value+'6'">6</button>
                  <button class="cta" onclick="try{let d=document.getElementById('calc-display');d.value=eval(d.value)}catch(e){}">=</button>
                </div>
              </div>
            </section>
            """.trimIndent()
            else -> ""
        }

        return """
<!DOCTYPE html>
<html lang="ku" dir="rtl">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>${bp.appTitle} — BASO Studio</title>
  <style>
    :root {
      --primary: ${bp.primaryHex};
      --secondary: ${bp.secondaryHex};
      --bg: ${bp.backgroundHex};
      --surface: ${bp.surfaceHex};
      --radius: ${bp.cardRadiusPx}px;
    }
    body { margin: 0; font-family: system-ui, sans-serif; background: var(--bg); color: ${if (bp.isDark) "#F8FAFC" else "#0F172A"}; padding-bottom: 64px; }
    header { background: #07131E; color: #fff; padding: 14px 18px; display: flex; justify-content: space-between; align-items: center; }
    .hero { background: linear-gradient(135deg, ${bp.heroBgStartHex}, ${bp.heroBgEndHex}); color: #fff; padding: 26px 20px; }
    .hero h1 { margin: 0 0 8px; font-size: 22px; }
    .hero p { margin: 0 0 14px; opacity: 0.85; font-size: 13px; }
    .cta { background: var(--primary); color: #050C14; border: none; padding: 10px 20px; border-radius: 999px; font-weight: 800; cursor: pointer; }
    .metrics { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; padding: 14px; }
    .metric-box { background: var(--surface); border-radius: var(--radius); padding: 10px; text-align: center; }
    .grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; padding: 14px; }
    .card { background: var(--surface); border-radius: var(--radius); padding: 14px; box-shadow: 0 4px 12px rgba(0,0,0,0.08); }
    .card-title { font-weight: 700; margin-bottom: 4px; }
    .card-sub { font-size: 12px; opacity: 0.7; margin-bottom: 10px; }
    .card-footer { display: flex; justify-content: space-between; align-items: center; }
    .badge { font-weight: 800; color: var(--primary); }
    .action-btn { background: var(--primary); color: #050C14; border: none; border-radius: 8px; width: 28px; height: 28px; font-weight: 900; cursor: pointer; }
  </style>
</head>
<body>
  <header>
    <strong>${bp.appTitle}</strong>
    <span>${bp.layoutArchetype}</span>
  </header>
  ${if (bp.showHeroBanner) """
  <section class="hero">
    <h1>${bp.heroTitle}</h1>
    <p>${bp.heroSubtitle}</p>
    <button class="cta">${bp.ctaText}</button>
  </section>
  """ else ""}
  $interactiveWidgetHtml
  $metricsHtml
  <section class="grid">
    $itemsHtml
  </section>
</body>
</html>
        """.trimIndent()
    }

    fun generateFilesFromBlueprint(bp: DynamicUiBlueprint): List<ProjectFile> {
        val htmlContent = buildInteractivePreviewHtmlFromBlueprint(bp)
        val itemsJsx = bp.items.joinToString(",\n  ") {
            "{ id: '${it.id}', title: '${it.title.replace("'", "")}', subtitle: '${it.subtitle.replace("'", "")}', price: '${it.badgeOrPrice}' }"
        }
        return listOf(
            ProjectFile(
                path = "web-app/index.html",
                name = "index.html",
                folder = "web-app",
                platformCategory = "Web",
                language = "html",
                content = htmlContent
            ),
            ProjectFile(
                path = "web-app/src/components/Header.jsx",
                name = "Header.jsx",
                folder = "src/components",
                platformCategory = "Web",
                language = "jsx",
                content = """
import React from 'react';

export default function Header({ title = "${bp.appTitle}" }) {
  return (
    <header className="baso-header" style={{ background: '#07131E', color: '#FFF', padding: '14px 20px' }}>
      <div className="brand-logo">
        <span className="badge-b" style={{ background: '${bp.primaryHex}' }}>B</span>
        <h1>{title}</h1>
      </div>
      <span className="archetype-tag" style={{ color: '${bp.secondaryHex}' }}>${bp.layoutArchetype}</span>
    </header>
  );
}
                """.trimIndent()
            ),
            ProjectFile(
                path = "web-app/src/components/ProductCard.jsx",
                name = "ProductCard.jsx",
                folder = "src/components",
                platformCategory = "Web",
                language = "jsx",
                content = """
import React from 'react';

export default function ItemCard({ title, subtitle, price, onAction }) {
  return (
    <div className="baso-card" style={{ borderRadius: '${bp.cardRadiusPx}px', background: '${bp.surfaceHex}' }}>
      <h3>{title}</h3>
      <p>{subtitle}</p>
      <div className="card-bottom">
        <strong style={{ color: '${bp.primaryHex}' }}>{price}</strong>
        <button onClick={onAction} style={{ background: '${bp.primaryHex}' }}>+</button>
      </div>
    </div>
  );
}
                """.trimIndent()
            ),
            ProjectFile(
                path = "web-app/src/pages/Home.jsx",
                name = "Home.jsx",
                folder = "src/pages",
                platformCategory = "Web",
                language = "jsx",
                content = """
import React from 'react';
import Header from '../components/Header';
import ItemCard from '../components/ProductCard';

const DATA_ITEMS = [
  $itemsJsx
];

export default function Home() {
  return (
    <main style={{ background: '${bp.backgroundHex}', minHeight: '100vh' }}>
      <Header title="${bp.appTitle}" />
      <section className="hero" style={{ background: 'linear-gradient(135deg, ${bp.heroBgStartHex}, ${bp.heroBgEndHex})' }}>
        <h2>${bp.heroTitle}</h2>
        <p>${bp.heroSubtitle}</p>
        <button style={{ background: '${bp.primaryHex}' }}>${bp.ctaText}</button>
      </section>
      <section className="grid">
        {DATA_ITEMS.map(item => (
          <ItemCard key={item.id} {...item} />
        ))}
      </section>
    </main>
  );
}
                """.trimIndent()
            ),
            ProjectFile(
                path = "web-app/src/pages/Shop.jsx",
                name = "Shop.jsx",
                folder = "src/pages",
                platformCategory = "Web",
                language = "jsx",
                content = """
import React, { useState } from 'react';

export default function CategoriesView() {
  const categories = [${bp.categories.joinToString(", ") { "'$it'" }}];
  const [active, setActive] = useState(categories[0]);
  return (
    <div className="categories-bar">
      {categories.map(cat => (
        <button key={cat} onClick={() => setActive(cat)}>{cat}</button>
      ))}
    </div>
  );
}
                """.trimIndent()
            ),
            ProjectFile(
                path = "web-app/assets/style.css",
                name = "style.css",
                folder = "assets",
                platformCategory = "Web",
                language = "css",
                content = """
:root {
  --primary: ${bp.primaryHex};
  --secondary: ${bp.secondaryHex};
  --bg: ${bp.backgroundHex};
  --surface: ${bp.surfaceHex};
  --radius: ${bp.cardRadiusPx}px;
}

body {
  margin: 0;
  background-color: var(--bg);
  font-family: 'Noto Sans Arabic', 'Plus Jakarta Sans', sans-serif;
}
                """.trimIndent()
            ),
            ProjectFile(
                path = "web-app/package.json",
                name = "package.json",
                folder = "web-app",
                platformCategory = "Files",
                language = "json",
                content = """
{
  "name": "${bp.appTitle.lowercase().replace(" ", "-")}-baso",
  "version": "1.0.0",
  "private": true,
  "author": "BASO Freelancer Studio",
  "dependencies": {
    "react": "^19.0.0",
    "react-dom": "^19.0.0"
  }
}
                """.trimIndent()
            ),
            ProjectFile(
                path = "android-app/app/src/main/java/com/baso/app/MainActivity.kt",
                name = "MainActivity.kt",
                folder = "android/ui",
                platformCategory = "Android",
                language = "kotlin",
                content = """
package com.baso.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    GeneratedScreen()
                }
            }
        }
    }
}

@Composable
fun GeneratedScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "${bp.appTitle}", style = MaterialTheme.typography.headlineMedium)
        Text(text = "${bp.heroTitle}", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = { }) {
            Text("${bp.ctaText}")
        }
    }
}
                """.trimIndent()
            ),
            ProjectFile(
                path = ".github/workflows/build-apk.yml",
                name = "build-apk.yml",
                folder = "android/ui",
                platformCategory = "Android",
                language = "yaml",
                content = GitHubDeployService.buildGitHubApkReleaseWorkflowYaml(
                    repoSlug = bp.appTitle.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-').ifBlank { "baso-app" },
                    appTitle = bp.appTitle
                )
            ),
            ProjectFile(
                path = "app/build.gradle.kts",
                name = "build.gradle.kts",
                folder = "android/ui",
                platformCategory = "Android",
                language = "kotlin",
                content = """
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.baso.generated"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.baso.generated.${bp.appTitle.lowercase().replace(Regex("[^a-z0-9]"), "").ifBlank { "basoapp" }}"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.activity:activity-ktx:1.9.2")
}
                """.trimIndent()
            ),
            ProjectFile(
                path = "settings.gradle.kts",
                name = "settings.gradle.kts",
                folder = "android/ui",
                platformCategory = "Android",
                language = "kotlin",
                content = """
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "${bp.appTitle.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-').ifBlank { "baso-app" }}"
include(":app")
                """.trimIndent()
            ),
            ProjectFile(
                path = "app/src/main/AndroidManifest.xml",
                name = "AndroidManifest.xml",
                folder = "android/ui",
                platformCategory = "Android",
                language = "xml",
                content = """
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.INTERNET" />
    <application
        android:label="${bp.appTitle.replace("\"", "")}"
        android:usesCleartextTraffic="true"
        android:theme="@android:style/Theme.DeviceDefault.NoActionBar">
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
                """.trimIndent()
            ),
            ProjectFile(
                path = ".github/workflows/deploy-pages.yml",
                name = "deploy-pages.yml",
                folder = "web-app",
                platformCategory = "Web",
                language = "yaml",
                content = GitHubDeployService.buildGitHubPagesWorkflowYaml(bp.appTitle)
            )
        )
    }

    fun defaultProjectFiles(projectTitle: String = "MyShop"): List<ProjectFile> {
        val bp = buildBaseBlueprintForArchetype("ECOMMERCE", projectTitle).copy(appTitle = projectTitle)
        return generateFilesFromBlueprint(bp)
    }

    fun buildInteractivePreviewHtml(
        shopName: String = "MyShop",
        heroHeadline: String = "Best Products For You",
        ctaText: String = "Shop Now",
        primaryHex: String = "#F5C518",
        accentHex: String = "#00E5CC",
        darkMode: Boolean = false,
        radiusPx: Int = 16
    ): String {
        val bp = buildBaseBlueprintForArchetype("ECOMMERCE", shopName).copy(
            appTitle = shopName,
            heroTitle = heroHeadline,
            ctaText = ctaText,
            primaryHex = primaryHex,
            secondaryHex = accentHex,
            isDark = darkMode,
            cardRadiusPx = radiusPx
        )
        return buildInteractivePreviewHtmlFromBlueprint(bp)
    }

    fun initialSeedProjects(): List<BasoProjectEntity> {
        val shopBp = buildBaseBlueprintForArchetype("ECOMMERCE", "MyShop")
        val foodBp = buildBaseBlueprintForArchetype("FOOD_DELIVERY", "Food Delivery")
        val portfolioBp = buildBaseBlueprintForArchetype("PORTFOLIO", "Portfolio Website")
        val chatBp = buildBaseBlueprintForArchetype("SOCIAL_CHAT", "Chat App")
        val fintechBp = buildBaseBlueprintForArchetype("FINTECH_DASHBOARD", "Business Website")

        return listOf(
            createSeedEntity("My Shop", "my-shop-project", "Web", "من دەمەوێت ئەپێکی سادەی شۆپینگ بۆ Android و Web دروست بکەم.", shopBp, "Web • 2 days ago"),
            createSeedEntity("Shopping App", "shopping-app-android", "Android", "دروستکردنی ئەپێکی مۆبایلی بازاڕکردن بە کۆتلین و Material 3.", shopBp.copy(isDark = true, backgroundHex = "#09131E", surfaceHex = "#122436", appTitle = "Shopping App"), "Android • Last updated 3 days ago"),
            createSeedEntity("Food Delivery", "food-delivery-app", "Android", "ئەپی داواکردنی خواردنی خێرا بە نەخشەی گەیاندن و لیستی چێشتخانەکان.", foodBp, "Android • 4 days ago"),
            createSeedEntity("Portfolio Website", "baso-portfolio-web", "Web", "پۆرتفۆلیۆی تایبەت بە باسۆ بۆ نمایشکردنی پڕۆژەکانی دیزاین و ئەپ.", portfolioBp, "Web • 5 days ago"),
            createSeedEntity("Chat App", "baso-chat-app", "Android", "ئەپڵیکەیشنی چات و نامەگۆڕینەوەی خێرا بە دیزاینی مۆدێرن.", chatBp, "Android • 1 week ago"),
            createSeedEntity("Business Website", "business-website", "Web App", "وێب ئەپی بەڕێوەبردنی کۆمپانیا و فرۆشتن لەگەڵ داشبۆردی ئامارەکان.", fintechBp, "Web • 1 week ago")
        )
    }

    private fun createSeedEntity(
        title: String,
        slug: String,
        platform: String,
        prompt: String,
        bp: DynamicUiBlueprint,
        updatedLabel: String
    ): BasoProjectEntity {
        val initialChat = listOf(
            ChatMessage(
                id = "msg_1",
                isUser = true,
                text = prompt,
                versionNumber = 1
            ),
            ChatMessage(
                id = "msg_2",
                isUser = false,
                text = "✅ پڕۆژەی «$title» بە شێوازی (${bp.layoutArchetype}) دروست کرا. دەتوانیت لێرە لە چاتەکەدا داوای هەر گۆڕانکارییەک بکەیت (بۆ نموونە: «ڕەنگەکەی بکە بە مۆر»، «بەشی نرخەکان زیاد بکە»، «چارت زیاد بکە»).",
                versionNumber = 1,
                appliedChanges = listOf("دروستکردنی دیزاینی سەرەکی (${bp.layoutArchetype})", "ئامادەکردنی کۆد و پێشینەی زیندوو"),
                blueprintSnapshot = bp
            )
        )
        return BasoProjectEntity(
            title = title,
            slug = slug,
            platform = platform,
            prompt = prompt,
            modelId = "gemini-3.1-pro-preview",
            temperature = 0.7f,
            stylePreset = "BASO Signature Gold & Cyan",
            previewUrl = "https://preview.baso.app/$slug",
            previewHtml = buildInteractivePreviewHtmlFromBlueprint(bp),
            filesJson = json.encodeToString(generateFilesFromBlueprint(bp)),
            designTokensJson = json.encodeToString(bp),
            historyJson = json.encodeToString(initialChat),
            zipSizeLabel = "24.8 KB • Ready to download",
            updatedLabelKu = updatedLabel,
            updatedLabelEn = updatedLabel,
            updatedAt = System.currentTimeMillis()
        )
    }
}
