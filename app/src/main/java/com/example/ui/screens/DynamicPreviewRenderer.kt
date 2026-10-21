package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Workspaces
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.DynamicUiBlueprint
import com.example.data.UiContentItem
import com.example.ui.theme.BasoBorderCyan
import com.example.ui.theme.BasoCardSurface
import com.example.ui.theme.BasoCyan
import com.example.ui.theme.BasoGold
import com.example.ui.theme.BasoSuccessGreen
import com.example.ui.theme.BasoTextMuted
import com.example.ui.theme.BasoTextWhite

@Composable
fun DynamicBlueprintPreviewCanvas(
    blueprint: DynamicUiBlueprint,
    reloadKey: Int,
    onBlueprintEdited: (DynamicUiBlueprint, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = remember(blueprint.primaryHex) {
        parseHexSafe(blueprint.primaryHex, BasoGold)
    }
    val secondaryColor = remember(blueprint.secondaryHex) {
        parseHexSafe(blueprint.secondaryHex, BasoCyan)
    }
    val bgColor = remember(blueprint.backgroundHex, blueprint.isDark) {
        parseHexSafe(blueprint.backgroundHex, if (blueprint.isDark) Color(0xFF0A141F) else Color(0xFFF3F5F8))
    }
    val surfaceColor = remember(blueprint.surfaceHex, blueprint.isDark) {
        parseHexSafe(blueprint.surfaceHex, if (blueprint.isDark) Color(0xFF132538) else Color.White)
    }
    val heroStart = remember(blueprint.heroBgStartHex) {
        parseHexSafe(blueprint.heroBgStartHex, Color(0xFF091420))
    }
    val heroEnd = remember(blueprint.heroBgEndHex) {
        parseHexSafe(blueprint.heroBgEndHex, Color(0xFF243E58))
    }

    val textMain = if (blueprint.isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val textSub = if (blueprint.isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    var interactionCount by remember(reloadKey, blueprint.appTitle, blueprint.layoutArchetype) { mutableIntStateOf(1) }
    var selectedCategory by remember(reloadKey, blueprint.categories, blueprint.layoutArchetype) {
        mutableStateOf(blueprint.categories.firstOrNull() ?: "گشتییەکان")
    }
    var selectedBottomNavIdx by remember(reloadKey, blueprint.layoutArchetype) { mutableIntStateOf(0) }
    var searchQuery by remember(reloadKey, blueprint.layoutArchetype) { mutableStateOf("") }
    var toastFeedback by remember(reloadKey) { mutableStateOf<String?>(null) }
    var showQuickEditDialog by remember { mutableStateOf(false) }

    val filteredItems = remember(blueprint.items, selectedCategory, searchQuery) {
        val bySearch = if (searchQuery.isBlank()) {
            blueprint.items
        } else {
            blueprint.items.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                    it.subtitle.contains(searchQuery, ignoreCase = true)
            }
        }
        val byCat = bySearch.filter { it.category.equals(selectedCategory, ignoreCase = true) }
        if (byCat.isNotEmpty() || searchQuery.isNotBlank()) byCat else bySearch
    }

    Box(
        modifier = modifier.background(bgColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Scrollable Main Preview Body
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 16.dp)
            ) {
                // 1. Dynamic App Header Bar (Tap title or edit icon to edit directly!)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF07131E))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showQuickEditDialog = true }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Brush.linearGradient(listOf(primaryColor, secondaryColor)))
                        ) {
                            Icon(
                                imageVector = archetypeIcon(blueprint.layoutArchetype),
                                contentDescription = null,
                                tint = Color(0xFF050C14),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Column {
                            Text(
                                text = blueprint.appTitle,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = archetypeBadgeLabel(blueprint.layoutArchetype),
                                color = secondaryColor,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = Color.White.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.clickable { showQuickEditDialog = true }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit UI",
                                    tint = primaryColor,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "دەستکاری",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(primaryColor.copy(alpha = 0.2f))
                                .border(1.dp, primaryColor, RoundedCornerShape(50))
                                .clickable {
                                    interactionCount++
                                    toastFeedback = "چالاکی نوێ تۆمارکرا ($interactionCount)"
                                }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = archetypeIcon(blueprint.layoutArchetype),
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "$interactionCount",
                                    color = primaryColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }

                // Active User Command Banner (Shows clearly that the preview reflects their latest prompt!)
                if (blueprint.userPromptCommand.isNotBlank()) {
                    Surface(
                        color = primaryColor.copy(alpha = 0.14f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "✨ فەرمانی جێبەجێکراو: «${blueprint.userPromptCommand}»",
                                color = primaryColor,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // 2. Optional Hero Section (can be hidden for Calculator / Utility apps or by user command)
                if (blueprint.showHeroBanner) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(heroStart, heroEnd)
                                )
                            )
                            .clickable { showQuickEditDialog = true }
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    color = secondaryColor.copy(alpha = 0.18f),
                                    shape = RoundedCornerShape(50),
                                    border = BorderStroke(1.dp, secondaryColor.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = archetypeBadgeLabel(blueprint.layoutArchetype),
                                        color = secondaryColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = blueprint.heroTitle,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    lineHeight = 22.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = blueprint.heroSubtitle,
                                    color = Color.White.copy(alpha = 0.82f),
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    color = primaryColor,
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier.clickable {
                                        interactionCount++
                                        toastFeedback = "${blueprint.ctaText} چالاک کرا!"
                                    }
                                ) {
                                    Text(
                                        text = blueprint.ctaText,
                                        color = Color(0xFF050C14),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Surface(
                                color = Color.White.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(blueprint.cardRadiusPx.dp),
                                border = BorderStroke(1.2.dp, primaryColor.copy(alpha = 0.6f)),
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = archetypeIcon(blueprint.layoutArchetype),
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
                }

                // 3. SPECIALIZED INTERACTIVE APP MODULE (Renders real working Calculator, Game, Todo, Weather, Tasbih, Media Player, Login, or Custom Console!)
                val activeWidget = blueprint.customInteractiveWidget.ifBlank {
                    when (blueprint.layoutArchetype) {
                        "CALCULATOR" -> "CALCULATOR"
                        "TODO_NOTES" -> "TODO"
                        "GAME_ARCADE" -> "GAME_XO"
                        "WEATHER_LIVE" -> "WEATHER"
                        "ISLAMIC_QURAN" -> "TASBIH"
                        "MEDIA_PLAYER" -> "PLAYER"
                        "AUTH_LOGIN" -> "LOGIN"
                        "CUSTOM_STUDIO" -> "CUSTOM_CONSOLE"
                        else -> ""
                    }
                }

                when (activeWidget) {
                    "CALCULATOR" -> InteractiveCalculatorWidget(
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        surfaceColor = surfaceColor,
                        textMain = textMain
                    )
                    "TODO" -> InteractiveTodoNotesWidget(
                        blueprint = blueprint,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        surfaceColor = surfaceColor,
                        textMain = textMain,
                        textSub = textSub,
                        onBlueprintEdited = onBlueprintEdited
                    )
                    "GAME_XO" -> InteractiveGameXoWidget(
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        surfaceColor = surfaceColor,
                        textMain = textMain
                    )
                    "WEATHER" -> InteractiveWeatherWidget(
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        surfaceColor = surfaceColor,
                        textMain = textMain,
                        textSub = textSub
                    )
                    "TASBIH" -> InteractiveTasbihPrayerWidget(
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        surfaceColor = surfaceColor,
                        textMain = textMain,
                        textSub = textSub
                    )
                    "PLAYER" -> InteractiveMediaPlayerWidget(
                        blueprint = blueprint,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        surfaceColor = surfaceColor,
                        textMain = textMain,
                        textSub = textSub
                    )
                    "LOGIN" -> InteractiveAuthLoginWidget(
                        appTitle = blueprint.appTitle,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        surfaceColor = surfaceColor,
                        textMain = textMain,
                        onFeedback = { toastFeedback = it }
                    )
                    "CUSTOM_CONSOLE" -> InteractiveCustomConsoleWidget(
                        blueprint = blueprint,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        surfaceColor = surfaceColor,
                        textMain = textMain,
                        textSub = textSub,
                        onBlueprintEdited = onBlueprintEdited,
                        onFeedback = { toastFeedback = it }
                    )
                }

                // 4. Optional Live Search Bar
                if (blueprint.showSearchBar) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = "گەڕان لە ناو ${blueprint.appTitle}...",
                                color = textSub,
                                fontSize = 11.5.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textMain,
                            unfocusedTextColor = textMain,
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = secondaryColor.copy(alpha = 0.35f),
                            focusedContainerColor = surfaceColor,
                            unfocusedContainerColor = surfaceColor
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }

                // 5. Metrics / Telemetry Strip (if present)
                if (blueprint.metrics.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        blueprint.metrics.forEach { metric ->
                            Surface(
                                color = surfaceColor,
                                shape = RoundedCornerShape(blueprint.cardRadiusPx.dp),
                                border = BorderStroke(1.dp, secondaryColor.copy(alpha = 0.25f)),
                                shadowElevation = 2.dp,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp)
                                ) {
                                    Text(
                                        text = metric.label,
                                        color = textSub,
                                        fontSize = 10.sp,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = metric.value,
                                        color = textMain,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = metric.trend,
                                        color = primaryColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                // 6. Optional Live Analytics Chart
                if (blueprint.showAnalyticsChart) {
                    Surface(
                        color = surfaceColor,
                        shape = RoundedCornerShape(blueprint.cardRadiusPx.dp),
                        border = BorderStroke(1.dp, secondaryColor.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.BarChart,
                                        contentDescription = null,
                                        tint = primaryColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "ئامار و چالاکی هەفتانە (Live Chart)",
                                        color = textMain,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "+24.8%",
                                    color = BasoSuccessGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            val bars = listOf(0.42f, 0.65f, 0.52f, 0.85f, 0.74f, 0.95f, 0.88f)
                            val days = listOf("ش", "ی", "د", "س", "چ", "پ", "هـ")
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(72.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                bars.forEachIndexed { idx, ratio ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Bottom,
                                        modifier = Modifier.fillMaxHeight()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(18.dp)
                                                .fillMaxHeight(ratio * 0.78f)
                                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                                .background(
                                                    if (idx == 5) primaryColor else secondaryColor.copy(alpha = 0.65f)
                                                )
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = days[idx],
                                            color = textSub,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 7. Dynamic Category Tabs
                if (blueprint.categories.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        blueprint.categories.forEach { cat ->
                            val active = selectedCategory == cat
                            Surface(
                                color = if (active) primaryColor else surfaceColor,
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (active) primaryColor else secondaryColor.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier.clickable {
                                    selectedCategory = cat
                                    toastFeedback = "فلتەری بەشی: $cat"
                                }
                            ) {
                                Text(
                                    text = cat,
                                    color = if (active) Color(0xFF050C14) else textMain,
                                    fontSize = 11.sp,
                                    fontWeight = if (active) FontWeight.ExtraBold else FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }
                }

                // 8. Main Items Section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = sectionHeadingForArchetype(blueprint.layoutArchetype),
                        color = textMain,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "+ زیادکردنی کارت",
                        color = primaryColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            val updatedItems = blueprint.items.toMutableList()
                            updatedItems.add(
                                0,
                                UiContentItem(
                                    id = "item_${System.currentTimeMillis()}",
                                    title = "${blueprint.appTitle} #${blueprint.items.size + 1}",
                                    subtitle = "زیادکراو لە ناو پێشینەی زیندوو",
                                    badgeOrPrice = "نوێ ✨",
                                    category = selectedCategory,
                                    iconType = blueprint.layoutArchetype
                                )
                            )
                            onBlueprintEdited(
                                blueprint.copy(items = updatedItems),
                                "زیادکردنی کارتی نوێ لە ناو پێشینە"
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (blueprint.layoutArchetype in listOf(
                        "SOCIAL_CHAT",
                        "FINTECH_DASHBOARD",
                        "FITNESS_HEALTH",
                        "TODO_NOTES",
                        "ISLAMIC_QURAN",
                        "MEDIA_PLAYER",
                        "CALCULATOR"
                    )
                ) {
                    // Vertical Rich List Layout
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filteredItems.forEach { item ->
                            Surface(
                                color = surfaceColor,
                                shape = RoundedCornerShape(blueprint.cardRadiusPx.dp),
                                border = BorderStroke(1.dp, secondaryColor.copy(alpha = 0.22f)),
                                shadowElevation = 2.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        interactionCount++
                                        toastFeedback = "${item.title} چالاک کرا"
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(11.dp))
                                                .background(primaryColor.copy(alpha = 0.15f))
                                        ) {
                                            Icon(
                                                imageVector = archetypeIcon(blueprint.layoutArchetype),
                                                contentDescription = null,
                                                tint = primaryColor,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = item.title,
                                                color = textMain,
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = item.subtitle,
                                                color = textSub,
                                                fontSize = 10.5.sp
                                            )
                                        }
                                    }
                                    Surface(
                                        color = primaryColor.copy(alpha = 0.18f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = item.badgeOrPrice,
                                            color = primaryColor,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // 2-Column Visual Grid Layout
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        filteredItems.chunked(2).forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                rowItems.forEach { item ->
                                    Surface(
                                        color = surfaceColor,
                                        shape = RoundedCornerShape(blueprint.cardRadiusPx.dp),
                                        shadowElevation = 3.dp,
                                        border = BorderStroke(1.dp, secondaryColor.copy(alpha = 0.2f)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                interactionCount++
                                                toastFeedback = "${item.title} هەڵبژێردرا ($interactionCount)"
                                            }
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(68.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(
                                                        Brush.linearGradient(
                                                            listOf(
                                                                primaryColor.copy(alpha = 0.16f),
                                                                secondaryColor.copy(alpha = 0.16f)
                                                            )
                                                        )
                                                    )
                                            ) {
                                                Icon(
                                                    imageVector = archetypeIcon(blueprint.layoutArchetype),
                                                    contentDescription = item.title,
                                                    tint = primaryColor,
                                                    modifier = Modifier.size(30.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = item.title,
                                                color = textMain,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = item.subtitle,
                                                color = textSub,
                                                fontSize = 10.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = item.badgeOrPrice,
                                                    color = primaryColor,
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                                Surface(
                                                    color = primaryColor,
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(
                                                            imageVector = Icons.Default.Add,
                                                            contentDescription = null,
                                                            tint = Color(0xFF050C14),
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                if (rowItems.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                // 9. Optional Pricing Plans Section
                if (blueprint.showPricingSection && blueprint.pricingPlans.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "پاکێج و نرخەکان (Pricing Plans)",
                        color = textMain,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        blueprint.pricingPlans.forEach { plan ->
                            Surface(
                                color = surfaceColor,
                                shape = RoundedCornerShape(blueprint.cardRadiusPx.dp),
                                border = BorderStroke(
                                    width = if (plan.isPopular) 1.5.dp else 1.dp,
                                    color = if (plan.isPopular) primaryColor else secondaryColor.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = plan.name,
                                        color = textMain,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = plan.price,
                                        color = primaryColor,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    plan.features.forEach { feat ->
                                        Text(
                                            text = "✓ $feat",
                                            color = textSub,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 10. Optional Interactive Contact Form
                if (blueprint.showContactForm) {
                    var senderName by remember { mutableStateOf("") }
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        color = surfaceColor,
                        shape = RoundedCornerShape(blueprint.cardRadiusPx.dp),
                        border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "پەیوەندیمان پێوە بکە (Contact Form)",
                                color = textMain,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = senderName,
                                onValueChange = { senderName = it },
                                placeholder = { Text("ناو یان نامەکەت بنووسە...", fontSize = 11.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    toastFeedback = "نامەکەت بە سەرکەوتوویی نێردرا!"
                                    senderName = ""
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = primaryColor,
                                    contentColor = Color(0xFF050C14)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("ناردنی نامە", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // 11. Optional Live Chat Feed inside Preview
                if (blueprint.showLiveChatFeed) {
                    var quickMsg by remember { mutableStateOf("") }
                    val chatItems = remember(blueprint.appTitle) {
                        mutableStateListOf(
                            "باسۆ: بەخێربێن بۆ ناو ${blueprint.appTitle} 👋",
                            "سارا: دیزاین و خێراییەکەی زۆر نایابە ✨"
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        color = surfaceColor,
                        shape = RoundedCornerShape(blueprint.cardRadiusPx.dp),
                        border = BorderStroke(1.dp, secondaryColor.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "💬 ژووری گفتوگۆی ڕاستەوخۆ",
                                color = textMain,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            chatItems.forEach { line ->
                                Surface(
                                    color = primaryColor.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp)
                                ) {
                                    Text(
                                        text = line,
                                        color = textMain,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = quickMsg,
                                    onValueChange = { quickMsg = it },
                                    placeholder = { Text("نامەیەک بنووسە...", fontSize = 11.sp) },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = {
                                        if (quickMsg.isNotBlank()) {
                                            chatItems.add("تۆ: $quickMsg")
                                            quickMsg = ""
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Send",
                                        tint = primaryColor
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom App Navigation Bar inside the Preview
            if (blueprint.showBottomNavBar && blueprint.bottomNavLabels.isNotEmpty()) {
                Surface(
                    color = Color(0xFF07131E),
                    border = BorderStroke(1.dp, secondaryColor.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        blueprint.bottomNavLabels.take(4).forEachIndexed { idx, navLabel ->
                            val isSelected = selectedBottomNavIdx == idx
                            Surface(
                                color = if (isSelected) primaryColor.copy(alpha = 0.2f) else Color.Transparent,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.clickable {
                                    selectedBottomNavIdx = idx
                                    val targetCat = blueprint.categories.getOrNull(idx)
                                    if (targetCat != null) selectedCategory = targetCat
                                    toastFeedback = "بەشی «$navLabel» کرایەوە"
                                }
                            ) {
                                Text(
                                    text = navLabel,
                                    color = if (isSelected) primaryColor else Color.White.copy(alpha = 0.7f),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (toastFeedback != null) {
            Surface(
                color = Color(0xFF07131E),
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, secondaryColor),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 46.dp)
                    .clickable { toastFeedback = null }
            ) {
                Text(
                    text = toastFeedback!!,
                    color = secondaryColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }
    }

    // Direct Tap-to-Edit Modal
    if (showQuickEditDialog) {
        var editedTitle by remember(blueprint) { mutableStateOf(blueprint.appTitle) }
        var editedHero by remember(blueprint) { mutableStateOf(blueprint.heroTitle) }
        var editedCta by remember(blueprint) { mutableStateOf(blueprint.ctaText) }

        Dialog(onDismissRequest = { showQuickEditDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = BasoCardSurface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.5.dp, BasoCyan),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "دەستکاریکردنی ڕاستەوخۆی پێکهاتەکان",
                        color = BasoGold,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = editedTitle,
                        onValueChange = { editedTitle = it },
                        label = { Text("ناوی ئەپ / وێبسایت", color = BasoCyan) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = BasoTextWhite,
                            unfocusedTextColor = BasoTextWhite,
                            focusedBorderColor = BasoCyan,
                            unfocusedBorderColor = BasoBorderCyan
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editedHero,
                        onValueChange = { editedHero = it },
                        label = { Text("مانشێتی سەرەکی (Hero Title)", color = BasoCyan) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = BasoTextWhite,
                            unfocusedTextColor = BasoTextWhite,
                            focusedBorderColor = BasoCyan,
                            unfocusedBorderColor = BasoBorderCyan
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editedCta,
                        onValueChange = { editedCta = it },
                        label = { Text("دەقی دوگمەی سەرەکی (CTA)", color = BasoCyan) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = BasoTextWhite,
                            unfocusedTextColor = BasoTextWhite,
                            focusedBorderColor = BasoCyan,
                            unfocusedBorderColor = BasoBorderCyan
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onBlueprintEdited(
                                    blueprint.copy(
                                        appTitle = editedTitle.ifBlank { blueprint.appTitle },
                                        heroTitle = editedHero.ifBlank { blueprint.heroTitle },
                                        ctaText = editedCta.ifBlank { blueprint.ctaText }
                                    ),
                                    "دەستکاریکردنی ڕاستەوخۆی ناونیشان و دوگمەکان"
                                )
                                showQuickEditDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BasoGold,
                                contentColor = Color(0xFF050C14)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("پاشەکەوتکردن", fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================================
// SPECIALIZED INTERACTIVE WIDGETS RENDERED LIVE INSIDE THE PREVIEW
// =========================================================================================

@Composable
private fun InteractiveCalculatorWidget(
    primaryColor: Color,
    secondaryColor: Color,
    surfaceColor: Color,
    textMain: Color
) {
    var display by remember { mutableStateOf("0") }
    var firstOperand by remember { mutableStateOf<Double?>(null) }
    var pendingOp by remember { mutableStateOf<String?>(null) }
    var resetNext by remember { mutableStateOf(false) }
    var usdInput by remember { mutableStateOf("100") }

    Surface(
        color = surfaceColor,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.5.dp, primaryColor.copy(alpha = 0.6f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "🧮 ژمێریاری کارلێککەر (ڕاستەوخۆ کار دەکات)",
                color = secondaryColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = Color(0xFF050C14),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, secondaryColor.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    if (pendingOp != null && firstOperand != null) {
                        Text(
                            text = "${firstOperand} $pendingOp",
                            color = secondaryColor,
                            fontSize = 11.sp
                        )
                    }
                    Text(
                        text = display,
                        color = primaryColor,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            val rows = listOf(
                listOf("AC", "±", "%", "÷"),
                listOf("7", "8", "9", "×"),
                listOf("4", "5", "6", "-"),
                listOf("1", "2", "3", "+"),
                listOf("0", ".", "00", "=")
            )

            rows.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    row.forEach { btn ->
                        val isOp = btn in listOf("÷", "×", "-", "+", "=")
                        Surface(
                            color = when {
                                btn == "=" -> primaryColor
                                isOp -> secondaryColor.copy(alpha = 0.22f)
                                btn in listOf("AC", "±", "%") -> Color.White.copy(alpha = 0.12f)
                                else -> Color(0xFF0A1826)
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clickable {
                                    when (btn) {
                                        "AC" -> {
                                            display = "0"
                                            firstOperand = null
                                            pendingOp = null
                                            resetNext = false
                                        }
                                        "±" -> {
                                            val v = display.toDoubleOrNull() ?: 0.0
                                            display = formatCalcNumber(-v)
                                        }
                                        "%" -> {
                                            val v = display.toDoubleOrNull() ?: 0.0
                                            display = formatCalcNumber(v / 100.0)
                                        }
                                        "÷", "×", "-", "+" -> {
                                            firstOperand = display.toDoubleOrNull() ?: 0.0
                                            pendingOp = btn
                                            resetNext = true
                                        }
                                        "=" -> {
                                            val a = firstOperand
                                            val b = display.toDoubleOrNull() ?: 0.0
                                            if (a != null && pendingOp != null) {
                                                val res = when (pendingOp) {
                                                    "+" -> a + b
                                                    "-" -> a - b
                                                    "×" -> a * b
                                                    "÷" -> if (b != 0.0) a / b else 0.0
                                                    else -> b
                                                }
                                                display = formatCalcNumber(res)
                                                firstOperand = null
                                                pendingOp = null
                                                resetNext = true
                                            }
                                        }
                                        else -> {
                                            if (display == "0" || resetNext) {
                                                display = if (btn == ".") "0." else btn
                                                resetNext = false
                                            } else if (!(btn == "." && display.contains("."))) {
                                                display += btn
                                            }
                                        }
                                    }
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = btn,
                                    color = if (btn == "=") Color(0xFF050C14) else if (isOp) secondaryColor else Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            // Quick USD -> IQD Live Converter
            val usdVal = usdInput.toDoubleOrNull() ?: 0.0
            val iqdVal = (usdVal * 1505).toLong()
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = usdInput,
                    onValueChange = { usdInput = it },
                    label = { Text("بڕی دۆلار ($)", fontSize = 10.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = primaryColor.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1.2f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Text("بەهای دینار (IQD)", color = textMain.copy(alpha = 0.7f), fontSize = 10.sp)
                        Text(
                            text = "%,d IQD".format(iqdVal),
                            color = primaryColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

private fun formatCalcNumber(value: Double): String {
    return if (value % 1.0 == 0.0) {
        value.toLong().toString()
    } else {
        String.format("%.4f", value).trimEnd('0').trimEnd('.')
    }
}

@Composable
private fun InteractiveTodoNotesWidget(
    blueprint: DynamicUiBlueprint,
    primaryColor: Color,
    secondaryColor: Color,
    surfaceColor: Color,
    textMain: Color,
    textSub: Color,
    onBlueprintEdited: (DynamicUiBlueprint, String) -> Unit
) {
    var newTaskTitle by remember { mutableStateOf("") }
    val checkedIds = remember { mutableStateListOf<String>() }

    Surface(
        color = surfaceColor,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.5.dp, primaryColor.copy(alpha = 0.55f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "✅ بەڕێوەبەری ئەرک و تێبینی زیندوو",
                color = primaryColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newTaskTitle,
                    onValueChange = { newTaskTitle = it },
                    placeholder = { Text("ئەرک یان تێبینی نوێ بنووسە...", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        if (newTaskTitle.isNotBlank()) {
                            val updated = blueprint.items.toMutableList()
                            updated.add(
                                0,
                                UiContentItem(
                                    id = "todo_${System.currentTimeMillis()}",
                                    title = newTaskTitle.trim(),
                                    subtitle = "ئەرکی زیادکراو • ئێستا",
                                    badgeOrPrice = "چالاک 📌",
                                    category = blueprint.categories.firstOrNull() ?: "ئەرکەکان",
                                    iconType = "TODO"
                                )
                            )
                            onBlueprintEdited(blueprint.copy(items = updated), "زیادکردنی ئەرکی: $newTaskTitle")
                            newTaskTitle = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor,
                        contentColor = Color(0xFF050C14)
                    )
                ) {
                    Text("+ زیادکردن", fontWeight = FontWeight.ExtraBold, fontSize = 11.5.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            blueprint.items.take(5).forEach { task ->
                val isDone = checkedIds.contains(task.id)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.04f))
                        .clickable {
                            if (isDone) checkedIds.remove(task.id) else checkedIds.add(task.id)
                        }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Checkbox(
                            checked = isDone,
                            onCheckedChange = {
                                if (it) checkedIds.add(task.id) else checkedIds.remove(task.id)
                            },
                            colors = CheckboxDefaults.colors(checkedColor = primaryColor)
                        )
                        Text(
                            text = task.title,
                            color = if (isDone) textSub else textMain,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
                        )
                    }
                    IconButton(
                        onClick = {
                            val updated = blueprint.items.filterNot { it.id == task.id }
                            onBlueprintEdited(blueprint.copy(items = updated), "سڕینەوەی ئەرک")
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = secondaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InteractiveGameXoWidget(
    primaryColor: Color,
    secondaryColor: Color,
    surfaceColor: Color,
    textMain: Color
) {
    val board = remember { mutableStateListOf("", "", "", "", "", "", "", "", "") }
    var xTurn by remember { mutableStateOf(true) }
    var xScore by remember { mutableIntStateOf(0) }
    var oScore by remember { mutableIntStateOf(0) }
    var winner by remember { mutableStateOf<String?>(null) }

    fun checkWin(): String? {
        val lines = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
            listOf(0, 4, 8), listOf(2, 4, 6)
        )
        for (l in lines) {
            val a = board[l[0]]
            if (a.isNotEmpty() && a == board[l[1]] && a == board[l[2]]) return a
        }
        if (board.none { it.isEmpty() }) return "DRAW"
        return null
    }

    Surface(
        color = surfaceColor,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.5.dp, primaryColor.copy(alpha = 0.65f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🎮 یاری X و O (ڕاستەوخۆ یاری بکە)",
                    color = primaryColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "X: $xScore  |  O: $oScore",
                    color = secondaryColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when (winner) {
                    "X" -> "🏆 یاریزانی X بردییەوە!"
                    "O" -> "🏆 یاریزانی O بردییەوە!"
                    "DRAW" -> "🤝 یارییەکە یەکسان بوو!"
                    else -> "نۆرەی یاریزان: ${if (xTurn) "X" else "O"}"
                },
                color = textMain,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            for (r in 0..2) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    for (c in 0..2) {
                        val idx = r * 3 + c
                        val cell = board[idx]
                        Surface(
                            color = Color(0xFF07131E),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                1.5.dp,
                                if (cell == "X") primaryColor else if (cell == "O") secondaryColor else Color.White.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier
                                .size(62.dp)
                                .clickable {
                                    if (board[idx].isEmpty() && winner == null) {
                                        board[idx] = if (xTurn) "X" else "O"
                                        xTurn = !xTurn
                                        val w = checkWin()
                                        winner = w
                                        if (w == "X") xScore++
                                        if (w == "O") oScore++
                                    }
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = cell,
                                    color = if (cell == "X") primaryColor else secondaryColor,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    for (i in 0..8) board[i] = ""
                    winner = null
                    xTurn = true
                },
                border = BorderStroke(1.dp, primaryColor)
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = primaryColor, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("یاری نوێ / دووبارەکردنەوە", color = primaryColor, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun InteractiveWeatherWidget(
    primaryColor: Color,
    secondaryColor: Color,
    surfaceColor: Color,
    textMain: Color,
    textSub: Color
) {
    val cities = listOf(
        Triple("هەولێر", "24°C ☀️", "ساماڵ • شێ 38% • با 12 km/h"),
        Triple("سلێمانی", "20°C ⛅", "نیمچە هەور • شێ 46% • با 18 km/h"),
        Triple("دهۆک", "21°C 🌤️", "فێنک • شێ 44% • با 14 km/h"),
        Triple("کەرکووک", "27°C ☀️", "خۆرەتاو • شێ 30% • با 10 km/h"),
        Triple("هەڵەبجە", "19°C 🌧️", "ئەگەری باران • شێ 55% • با 16 km/h")
    )
    var selectedCityIdx by remember { mutableIntStateOf(0) }
    val activeCity = cities[selectedCityIdx]

    Surface(
        color = surfaceColor,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.5.dp, primaryColor.copy(alpha = 0.6f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                cities.forEachIndexed { idx, city ->
                    Surface(
                        color = if (selectedCityIdx == idx) primaryColor else Color(0xFF07131E),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.clickable { selectedCityIdx = idx }
                    ) {
                        Text(
                            text = city.first,
                            color = if (selectedCityIdx == idx) Color(0xFF050C14) else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(activeCity.first, color = textMain, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    Text(activeCity.third, color = textSub, fontSize = 11.sp)
                }
                Text(
                    text = activeCity.second,
                    color = primaryColor,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun InteractiveTasbihPrayerWidget(
    primaryColor: Color,
    secondaryColor: Color,
    surfaceColor: Color,
    textMain: Color,
    textSub: Color
) {
    var count by remember { mutableIntStateOf(0) }
    val dhikrList = listOf("سُبْحَانَ اللَّهِ", "الْحَمْدُ لِلَّهِ", "اللَّهُ أَكْبَرُ", "لَا إِلَٰهَ إِلَّا اللَّهُ")
    var dhikrIdx by remember { mutableIntStateOf(0) }

    Surface(
        color = surfaceColor,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.5.dp, primaryColor.copy(alpha = 0.6f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = "📿 تەسبیحی ئەلیکترۆنی و زیکر (کرتە بکە بۆ ژماردن)",
                color = secondaryColor,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = dhikrList[dhikrIdx],
                color = primaryColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.clickable {
                    dhikrIdx = (dhikrIdx + 1) % dhikrList.size
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(86.dp)
                    .clip(CircleShape)
                    .background(primaryColor.copy(alpha = 0.18f))
                    .border(2.dp, primaryColor, CircleShape)
                    .clickable { count++ }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$count",
                        color = textMain,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text("کرتە بکە", color = textSub, fontSize = 9.5.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { count = 0 }) {
                    Text("سفرکردنەوە (0)", color = primaryColor, fontSize = 11.sp)
                }
                OutlinedButton(onClick = { dhikrIdx = (dhikrIdx + 1) % dhikrList.size }) {
                    Text("گۆڕینی زیکر", color = secondaryColor, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun InteractiveMediaPlayerWidget(
    blueprint: DynamicUiBlueprint,
    primaryColor: Color,
    secondaryColor: Color,
    surfaceColor: Color,
    textMain: Color,
    textSub: Color
) {
    var isPlaying by remember { mutableStateOf(true) }
    var trackIdx by remember { mutableIntStateOf(0) }
    var progress by remember { mutableFloatStateOf(0.38f) }
    val currentTrack = blueprint.items.getOrNull(trackIdx % blueprint.items.size.coerceAtLeast(1))

    Surface(
        color = surfaceColor,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.5.dp, primaryColor.copy(alpha = 0.6f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.linearGradient(listOf(primaryColor, secondaryColor)))
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = Color(0xFF050C14),
                        modifier = Modifier.size(28.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentTrack?.title ?: blueprint.appTitle,
                        color = textMain,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isPlaying) "🟢 ئێستا لێدەدرێت • Hi-Fi Audio" else "⏸️ وەستێنراوە",
                        color = textSub,
                        fontSize = 11.sp
                    )
                }
            }
            Slider(
                value = progress,
                onValueChange = { progress = it },
                colors = SliderDefaults.colors(thumbColor = primaryColor, activeTrackColor = primaryColor)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { if (trackIdx > 0) trackIdx-- }) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = textMain)
                }
                Surface(
                    color = primaryColor,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(46.dp)
                        .clickable { isPlaying = !isPlaying }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color(0xFF050C14)
                        )
                    }
                }
                IconButton(onClick = { trackIdx++ }) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = textMain)
                }
            }
        }
    }
}

@Composable
private fun InteractiveAuthLoginWidget(
    appTitle: String,
    primaryColor: Color,
    secondaryColor: Color,
    surfaceColor: Color,
    textMain: Color,
    onFeedback: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(true) }

    Surface(
        color = surfaceColor,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.5.dp, primaryColor.copy(alpha = 0.6f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🔐 چوونەژوورەوە بۆ $appTitle",
                color = textMain,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("ئیمەیڵ یان ژمارەی مۆبایل", fontSize = 11.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("وشەی نهێنی (Password)", fontSize = 11.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("بیرم بهێنەرەوە", color = textMain, fontSize = 11.5.sp)
                Switch(
                    checked = rememberMe,
                    onCheckedChange = { rememberMe = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = primaryColor)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    onFeedback("✅ بە سەرکەوتوویی چوویتە ژوورەوە بۆ $appTitle!")
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = Color(0xFF050C14)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("چوونەژوورەوە 🚀", fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun InteractiveCustomConsoleWidget(
    blueprint: DynamicUiBlueprint,
    primaryColor: Color,
    secondaryColor: Color,
    surfaceColor: Color,
    textMain: Color,
    textSub: Color,
    onBlueprintEdited: (DynamicUiBlueprint, String) -> Unit,
    onFeedback: (String) -> Unit
) {
    var inputField1 by remember { mutableStateOf("") }
    var inputField2 by remember { mutableStateOf("") }

    Surface(
        color = surfaceColor,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.5.dp, primaryColor.copy(alpha = 0.6f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "⚡ پانێڵی کارکردنی زیندووی (${blueprint.appTitle})",
                color = primaryColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "ئەم بەشە بەپێی فەرمانەکەی تۆ دروست کراوە و ڕاستەوخۆ داتا وەردەگرێت:",
                color = textSub,
                fontSize = 10.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = inputField1,
                onValueChange = { inputField1 = it },
                placeholder = {
                    Text(
                        text = blueprint.customInputFields.getOrNull(0) ?: "ناونیشان یان داتای نوێ بنووسە...",
                        fontSize = 11.sp
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = inputField2,
                onValueChange = { inputField2 = it },
                placeholder = {
                    Text(
                        text = blueprint.customInputFields.getOrNull(1) ?: "وردەکاری، نرخ یان تێبینی...",
                        fontSize = 11.sp
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (inputField1.isNotBlank()) {
                            val updated = blueprint.items.toMutableList()
                            updated.add(
                                0,
                                UiContentItem(
                                    id = "custom_${System.currentTimeMillis()}",
                                    title = inputField1.trim(),
                                    subtitle = inputField2.ifBlank { "تۆمارکراو لە ناو ${blueprint.appTitle}" },
                                    badgeOrPrice = "نوێ ✨",
                                    category = blueprint.categories.firstOrNull() ?: "سەرەکی",
                                    iconType = blueprint.layoutArchetype
                                )
                            )
                            onBlueprintEdited(blueprint.copy(items = updated), "تۆمارکردنی: $inputField1")
                            onFeedback("✅ «$inputField1» زیادکرا بۆ ناو لیستەکە!")
                            inputField1 = ""
                            inputField2 = ""
                        } else {
                            onFeedback("⚡ کردارەکە لە ناو ${blueprint.appTitle} جێبەجێ کرا!")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor,
                        contentColor = Color(0xFF050C14)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = blueprint.customActionButtons.firstOrNull() ?: "➕ تۆمارکردن و زیادکردن",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

private fun parseHexSafe(hex: String, fallback: Color): Color {
    return try {
        val clean = hex.trim().removePrefix("#")
        val full = if (clean.length == 6) "FF$clean" else clean
        Color(full.toLong(16))
    } catch (_: Exception) {
        fallback
    }
}

private fun archetypeIcon(archetype: String): ImageVector {
    return when (archetype) {
        "CALCULATOR" -> Icons.Default.Calculate
        "TODO_NOTES" -> Icons.Default.Checklist
        "GAME_ARCADE" -> Icons.Default.SportsEsports
        "WEATHER_LIVE" -> Icons.Default.Cloud
        "ISLAMIC_QURAN" -> Icons.Default.MenuBook
        "MEDIA_PLAYER" -> Icons.Default.MusicNote
        "AUTH_LOGIN" -> Icons.Default.Lock
        "BOOKING_SHOWROOM" -> Icons.Default.DirectionsCar
        "FOOD_DELIVERY" -> Icons.Default.Restaurant
        "FINTECH_DASHBOARD" -> Icons.Default.AccountBalanceWallet
        "PORTFOLIO" -> Icons.Default.Workspaces
        "FITNESS_HEALTH" -> Icons.Default.FitnessCenter
        "EDUCATION" -> Icons.Default.School
        "SOCIAL_CHAT" -> Icons.Default.ChatBubble
        "CUSTOM_STUDIO" -> Icons.Default.AutoAwesome
        else -> Icons.Default.ShoppingCart
    }
}

private fun archetypeBadgeLabel(archetype: String): String {
    return when (archetype) {
        "CALCULATOR" -> "🧮 ژمێریار و دراو"
        "TODO_NOTES" -> "✅ ئەرک و تێبینییەکان"
        "GAME_ARCADE" -> "🎮 یاری و پێشبڕکێ"
        "WEATHER_LIVE" -> "☀️ کەشوهەوای زیندوو"
        "ISLAMIC_QURAN" -> "🕌 قورئان و زیکر"
        "MEDIA_PLAYER" -> "🎧 میوزیک و میدیا"
        "AUTH_LOGIN" -> "🔐 چوونەژوورەوە و هەژمار"
        "BOOKING_SHOWROOM" -> "🚗 پێشانگا و حیجزکردن"
        "FOOD_DELIVERY" -> "🍔 گەیاندنی خێرای خواردن"
        "FINTECH_DASHBOARD" -> "📊 سیستەمی دارایی و کریپتۆ"
        "PORTFOLIO" -> "✨ پۆرتفۆلیۆی پڕۆفیشناڵ"
        "FITNESS_HEALTH" -> "💪 تەندروستی و وەرزش"
        "EDUCATION" -> "🎓 ئەکادیمیای فێربوون"
        "SOCIAL_CHAT" -> "💬 پلاتفۆرمی چات و پەیوەندی"
        "CUSTOM_STUDIO" -> "🚀 سیستەمی تایبەتی BASO"
        else -> "🛍️ فرۆشگای ئۆنلاین"
    }
}

private fun sectionHeadingForArchetype(archetype: String): String {
    return when (archetype) {
        "CALCULATOR" -> "کردار و گۆڕینە خێراکان"
        "TODO_NOTES" -> "لیستی هەموو ئەرک و تێبینییەکان"
        "GAME_ARCADE" -> "یارییەکان و پاڵەوانێتی"
        "WEATHER_LIVE" -> "کەشوهەوای شارەکان"
        "ISLAMIC_QURAN" -> "سوورەتەکان و کاتەکانی بانگ"
        "MEDIA_PLAYER" -> "لیستی تراک و پۆدکاستەکان"
        "AUTH_LOGIN" -> "ڕێگاکانی چوونەژوورەوە و ئاسایش"
        "BOOKING_SHOWROOM" -> "ئۆفەرە بەردەستەکان بۆ حیجزکردن"
        "FOOD_DELIVERY" -> "مێنۆی خواردنە تایبەتەکان"
        "FINTECH_DASHBOARD" -> "دوایین مامەڵە و دراوەکان"
        "PORTFOLIO" -> "پڕۆژە و کارە دیارەکان"
        "FITNESS_HEALTH" -> "بەرنامەی ڕاهێنان و خۆراک"
        "EDUCATION" -> "کۆرس و وانە فێرکارییەکان"
        "SOCIAL_CHAT" -> "گفتوگۆ و نامە چالاکەکان"
        "CUSTOM_STUDIO" -> "بەش و داتا تۆمارکراوەکان"
        else -> "بەرهەمە هەڵبژێردراوەکان"
    }
}
