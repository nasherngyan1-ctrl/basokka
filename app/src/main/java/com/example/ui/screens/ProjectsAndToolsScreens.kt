package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Workspaces
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AiAutoRoutingStrategy
import com.example.data.AiKeyVaultManager
import com.example.data.AiProviderType
import com.example.data.AppLanguage
import com.example.data.BasoProjectEntity
import com.example.data.StoredAiKey
import com.example.ui.theme.BasoBgDark
import com.example.ui.theme.BasoBorderCyan
import com.example.ui.theme.BasoCardElevated
import com.example.ui.theme.BasoCardSurface
import com.example.ui.theme.BasoCyan
import com.example.ui.theme.BasoDarkOnAccent
import com.example.ui.theme.BasoGold
import com.example.ui.theme.BasoSuccessGreen
import com.example.ui.theme.BasoTextMuted
import com.example.ui.theme.BasoTextWhite

@Composable
fun ProjectsLibraryScreen(
    isKurdish: Boolean,
    projects: List<BasoProjectEntity>,
    selectedFilter: String,
    searchQuery: String,
    onFilterChange: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onCreateNewProject: () -> Unit,
    onOpenProject: (BasoProjectEntity) -> Unit
) {
    var showSearchBar by remember { mutableStateOf(false) }

    val filteredProjects = projects.filter { proj ->
        val matchesCategory = when (selectedFilter) {
            "هەموو", "All" -> true
            else -> proj.platform.equals(selectedFilter, ignoreCase = true)
        }
        val matchesSearch = searchQuery.isBlank() ||
            proj.title.contains(searchQuery, ignoreCase = true) ||
            proj.prompt.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BasoBgDark)
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search Projects",
                tint = BasoTextWhite,
                modifier = Modifier
                    .size(26.dp)
                    .clickable { showSearchBar = !showSearchBar }
                    .testTag("projects_search_toggle")
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (isKurdish) "پڕۆژەکان" else "Projects",
                    color = BasoTextWhite,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Surface(
                    color = BasoGold,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .size(36.dp)
                        .clickable { onCreateNewProject() }
                        .testTag("projects_add_new_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Project",
                            tint = BasoDarkOnAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        if (showSearchBar) {
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Text(
                        text = if (isKurdish) "گەڕان لە پڕۆژەکانی باسۆ..." else "Search Baso's projects...",
                        color = BasoTextMuted
                    )
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = BasoTextWhite,
                    unfocusedTextColor = BasoTextWhite,
                    focusedBorderColor = BasoCyan,
                    unfocusedBorderColor = BasoBorderCyan
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        val filters = listOf("هەموو", "Android", "Web", "Web App")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filters.forEach { filter ->
                val isSelected = selectedFilter == filter
                Surface(
                    color = if (isSelected) BasoCardElevated else BasoCardSurface,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) BasoCyan else BasoBorderCyan
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onFilterChange(filter) }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(vertical = 9.dp)
                    ) {
                        Text(
                            text = if (!isKurdish && filter == "هەموو") "All" else filter,
                            color = if (isSelected) BasoTextWhite else BasoTextMuted,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredProjects, key = { it.id }) { project ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = BasoCardSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, BasoBorderCyan),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenProject(project) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                color = BasoCardElevated,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, BasoCyan.copy(alpha = 0.4f)),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (project.platform == "Android") {
                                            Icons.Default.PhoneAndroid
                                        } else {
                                            Icons.Default.Language
                                        },
                                        contentDescription = null,
                                        tint = BasoCyan,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = project.title,
                                    color = BasoTextWhite,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = project.updatedLabelEn,
                                    color = BasoTextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = BasoCyan
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AiToolsStudioScreen(
    isKurdish: Boolean,
    isRunningTool: Boolean,
    aiToolResult: String?,
    onRunTool: (String, String) -> Unit,
    onJumpToBuilderWithPrompt: (String) -> Unit
) {
    var customToolInput by remember {
        mutableStateOf("دیزاینی فرۆشگایەکی مۆدێرن بە ڕەنگی زێڕین و فیرۆزەیی")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BasoBgDark)
            .padding(horizontal = 18.dp, vertical = 14.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = if (isKurdish) "ئامێرەکانی زیرەکی دەستکرد (BASO AI Tools)" else "BASO AI Studio Tools",
            color = BasoTextWhite,
            fontSize = 21.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = if (isKurdish) {
                "ئامرازە تایبەتەکانی ستۆدیۆی باسۆ بۆ دیزاین، کۆدنووسین، و کاری فریلانسەری"
            } else {
                "Personal AI utilities for UI design systems, code optimization, and freelancer workflows"
            },
            color = BasoTextMuted,
            fontSize = 13.sp
        )

        OutlinedTextField(
            value = customToolInput,
            onValueChange = { customToolInput = it },
            label = {
                Text(
                    text = if (isKurdish) "بیرۆکە یان ناوی پڕۆژەکەت" else "Project Idea or Topic",
                    color = BasoCyan
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = BasoTextWhite,
                unfocusedTextColor = BasoTextWhite,
                focusedBorderColor = BasoCyan,
                unfocusedBorderColor = BasoBorderCyan
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        val tools = listOf(
            Triple(
                "PALETTE",
                if (isKurdish) "دروستکەری سیستەمی ڕەنگ و دیزاین" else "UI Color Palette & Design System",
                Icons.Default.Palette
            ),
            Triple(
                "PROPOSAL",
                if (isKurdish) "ئامادەکردنی پێشنیار و نرخی پڕۆژەی فریلانسەر" else "Freelancer Project Proposal & Scope",
                Icons.Default.Workspaces
            ),
            Triple(
                "OPTIMIZE",
                if (isKurdish) "پشکنەر و باشترکردنی تەلارسازی کۆد" else "Code Architecture & Performance Advisor",
                Icons.Default.Code
            )
        )

        tools.forEach { (type, title, icon) ->
            Card(
                colors = CardDefaults.cardColors(containerColor = BasoCardSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BasoCyan.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onRunTool(type, customToolInput) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = BasoGold,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = title,
                            color = BasoTextWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = BasoCyan
                    )
                }
            }
        }

        if (isRunningTool) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CircularProgressIndicator(
                    color = BasoCyan,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = if (isKurdish) "BASO AI خەریکی شیکردنەوەیە..." else "BASO AI is analyzing...",
                    color = BasoCyan,
                    fontSize = 13.sp
                )
            }
        }

        if (!aiToolResult.isNullOrBlank()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = BasoCardElevated),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BasoGold),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = aiToolResult,
                        color = BasoTextWhite,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { onJumpToBuilderWithPrompt(customToolInput) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BasoCyan,
                            contentColor = BasoDarkOnAccent
                        ),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = if (isKurdish) "بیکە بە پڕۆژەی زیندوو لە Builder" else "Open in BASO AI Builder",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PersonalSettingsScreen(
    language: AppLanguage,
    selectedModel: String,
    storedAiKeys: List<StoredAiKey>,
    autoSwitchEnabled: Boolean,
    routingStrategy: AiAutoRoutingStrategy,
    lastAutoSwitchEvent: String?,
    onSelectLanguage: (AppLanguage) -> Unit,
    onSelectModel: (String) -> Unit,
    onAddOrImportAiKeys: (String, String, AiProviderType?, String?) -> Int,
    onToggleAiKeyEnabled: (String) -> Unit,
    onUpdateAiKeyModel: (String, String) -> Unit,
    onUpdateAiKeyProvider: (String, AiProviderType) -> Unit,
    onPromoteAiKeyPriority: (String) -> Unit,
    onTestAiKey: (String) -> Unit,
    onDeleteAiKey: (String) -> Unit,
    onSetAutoSwitchEnabled: (Boolean) -> Unit,
    onSetRoutingStrategy: (AiAutoRoutingStrategy) -> Unit
) {
    val isKurdish = language == AppLanguage.KURDISH

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BasoBgDark)
            .padding(horizontal = 18.dp, vertical = 14.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Personal Baso Profile Header Card with Typographic Monogram Badge
        Card(
            colors = CardDefaults.cardColors(containerColor = BasoCardSurface),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.2.dp, BasoGold),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                BasoLetterMonogramBadge(
                    size = 58.dp,
                    primaryLetter = "B",
                    subLetter = "ب"
                )
                Column {
                    Text(
                        text = "BASO — باسۆ",
                        color = BasoGold,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = if (isKurdish) {
                            "ستۆدیۆی تایبەت بە خۆت • پاڵپشتی ٩ کۆمپانیای AI"
                        } else {
                            "Personal AI Studio • 9 AI Providers & Auto-Failover"
                        },
                        color = BasoTextWhite,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Multi-Provider AI Key Vault & Auto-Failover Router Section
        AiKeyVaultAndAutoRouterSection(
            isKurdish = isKurdish,
            storedAiKeys = storedAiKeys,
            autoSwitchEnabled = autoSwitchEnabled,
            routingStrategy = routingStrategy,
            lastAutoSwitchEvent = lastAutoSwitchEvent,
            onAddOrImportAiKeys = onAddOrImportAiKeys,
            onToggleAiKeyEnabled = onToggleAiKeyEnabled,
            onUpdateAiKeyModel = onUpdateAiKeyModel,
            onUpdateAiKeyProvider = onUpdateAiKeyProvider,
            onPromoteAiKeyPriority = onPromoteAiKeyPriority,
            onTestAiKey = onTestAiKey,
            onDeleteAiKey = onDeleteAiKey,
            onSetAutoSwitchEnabled = onSetAutoSwitchEnabled,
            onSetRoutingStrategy = onSetRoutingStrategy
        )

        // Language Selection Card
        Card(
            colors = CardDefaults.cardColors(containerColor = BasoCardSurface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, BasoBorderCyan),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isKurdish) "زمانی ڕووکاری ئەپەکە" else "Studio Interface Language",
                    color = BasoTextWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AppLanguage.entries.forEach { lang ->
                        val active = language == lang
                        Surface(
                            color = if (active) BasoCardElevated else BasoBgDark,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.5.dp, if (active) BasoCyan else BasoBorderCyan),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onSelectLanguage(lang) }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 12.dp)
                            ) {
                                Text(
                                    text = lang.displayName,
                                    color = if (active) BasoCyan else BasoTextMuted,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // GitHub Actions Standalone APK Status Card
        Card(
            colors = CardDefaults.cardColors(containerColor = BasoCardSurface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, BasoCyan.copy(alpha = 0.45f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = BasoSuccessGreen
                    )
                    Text(
                        text = if (isKurdish) "ئامادەکراو بۆ GitHub Actions APK" else "GitHub Actions APK Ready",
                        color = BasoTextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isKurdish) {
                        "• بە تەواوی بێ Firebase ڕێکخراوە تاوەکو هیچ کێشەیەکی کۆمپایڵ نەبێت.\n" +
                            "• فایلی (.github/workflows/build-apk.yml) لە ناو پڕۆژەکەدا دانراوە.\n" +
                            "• هەر کاتێک کۆدەکە بخەیتە سەر GitHub، لە بەشی Actions ڕاستەوخۆ فایلی BASO-Studio-APK دروست دەبێت."
                    } else {
                        "• 100% Firebase-free with local Room SQLite persistence.\n" +
                            "• Includes .github/workflows/build-apk.yml for automatic APK builds.\n" +
                            "• Push to GitHub and download BASO-Studio-APK directly from the Actions tab."
                    },
                    color = BasoTextMuted,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun AiKeyVaultAndAutoRouterSection(
    isKurdish: Boolean,
    storedAiKeys: List<StoredAiKey>,
    autoSwitchEnabled: Boolean,
    routingStrategy: AiAutoRoutingStrategy,
    lastAutoSwitchEvent: String?,
    onAddOrImportAiKeys: (String, String, AiProviderType?, String?) -> Int,
    onToggleAiKeyEnabled: (String) -> Unit,
    onUpdateAiKeyModel: (String, String) -> Unit,
    onUpdateAiKeyProvider: (String, AiProviderType) -> Unit,
    onPromoteAiKeyPriority: (String) -> Unit,
    onTestAiKey: (String) -> Unit,
    onDeleteAiKey: (String) -> Unit,
    onSetAutoSwitchEnabled: (Boolean) -> Unit,
    onSetRoutingStrategy: (AiAutoRoutingStrategy) -> Unit
) {
    var rawKeyInput by remember { mutableStateOf("") }
    var customKeyLabel by remember { mutableStateOf("") }
    var manualProvider by remember { mutableStateOf<AiProviderType?>(null) }
    var selectedModelOverride by remember { mutableStateOf<String?>(null) }
    var showRawText by remember { mutableStateOf(false) }

    val liveDetected = remember(rawKeyInput, manualProvider) {
        if (rawKeyInput.isNotBlank()) {
            AiKeyVaultManager.detectProviderFromRawInput(rawKeyInput, manualProvider)
        } else {
            null
        }
    }

    // 1. Automatic AI Switching & Smart Failover Card
    Card(
        colors = CardDefaults.cardColors(containerColor = BasoCardSurface),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.4.dp, if (autoSwitchEnabled) BasoCyan else BasoGold),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = BasoCardElevated,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BasoCyan),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.SyncAlt,
                                contentDescription = null,
                                tint = BasoCyan,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = if (isKurdish) {
                                "گۆڕینی ئۆتۆماتیکی ڕاستەوخۆی AI"
                            } else {
                                "Automatic Real-Time AI Switcher"
                            },
                            color = BasoTextWhite,
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = if (isKurdish) {
                                "کاتێک کلیلێک پەستانی لەسەر بێت یان بوەستێت، ڕاستەوخۆ دەچێتە سەر AIـی دواتر"
                            } else {
                                "Automatically switches to the next healthy AI provider when needed"
                            },
                            color = BasoTextMuted,
                            fontSize = 11.5.sp
                        )
                    }
                }

                Switch(
                    checked = autoSwitchEnabled,
                    onCheckedChange = onSetAutoSwitchEnabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = BasoGold,
                        checkedTrackColor = BasoCyan.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier.testTag("auto_switch_ai_toggle")
                )
            }

            if (!lastAutoSwitchEvent.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = Color(0xFF071827),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, BasoGold.copy(alpha = 0.55f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = lastAutoSwitchEvent,
                        color = BasoGold,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (isKurdish) "شێوازی گۆڕینی ئۆتۆماتیکی هەڵبژێرە:" else "Select Auto-Routing Mode:",
                color = BasoCyan,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            AiAutoRoutingStrategy.entries.forEach { strategy ->
                val isSelected = routingStrategy == strategy
                Surface(
                    color = if (isSelected) BasoCardElevated else BasoBgDark,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) BasoCyan else BasoBorderCyan
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clickable { onSetRoutingStrategy(strategy) }
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = if (isSelected) BasoGold else BasoTextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isKurdish) strategy.labelKu else strategy.labelEn,
                                color = if (isSelected) BasoTextWhite else BasoTextMuted,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = strategy.descKu,
                            color = BasoTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }

    // 2. Smart Universal Key Scanner & Multi-Provider Input Card
    Card(
        colors = CardDefaults.cardColors(containerColor = BasoCardSurface),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.2.dp, BasoGold.copy(alpha = 0.7f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = null,
                    tint = BasoGold
                )
                Column {
                    Text(
                        text = if (isKurdish) {
                            "دانان و خوێندنەوەی زیرەکی کلیلەکانی AI"
                        } else {
                            "Smart Universal AI Key Reader & Vault"
                        },
                        color = BasoTextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = if (isKurdish) {
                            "هەر کلیلێک دابنێیت (Gemini, OpenAI, Claude, DeepSeek, Groq, OpenRouter, Grok...) خۆکارانە دەیخوێنێتەوە!"
                        } else {
                            "Paste any key (or multiple .env lines) and it auto-detects the AI provider!"
                        },
                        color = BasoTextMuted,
                        fontSize = 11.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Supported Providers Chip Row (Auto-Detect or Manual Override)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    color = if (manualProvider == null) BasoGold else BasoBgDark,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, BasoGold),
                    modifier = Modifier.clickable {
                        manualProvider = null
                        selectedModelOverride = null
                    }
                ) {
                    Text(
                        text = if (isKurdish) "✨ خوێندنەوەی خۆکار (Auto)" else "✨ Auto-Detect",
                        color = if (manualProvider == null) BasoDarkOnAccent else BasoGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                AiProviderType.entries.forEach { provider ->
                    val isSelected = manualProvider == provider
                    Surface(
                        color = if (isSelected) BasoCardElevated else BasoBgDark,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) BasoCyan else BasoBorderCyan
                        ),
                        modifier = Modifier.clickable {
                            manualProvider = provider
                            selectedModelOverride = provider.defaultModel
                        }
                    ) {
                        Text(
                            text = "${provider.shortBadge} (${provider.keyHintPrefix})",
                            color = if (isSelected) BasoCyan else BasoTextWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = rawKeyInput,
                onValueChange = { rawKeyInput = it },
                label = {
                    Text(
                        text = if (isKurdish) {
                            "کلیلی API لێرە دابنێ (AIza... | sk-proj-... | sk-ant-... | gsk_...)"
                        } else {
                            "Paste API Key(s) here (AIza... | sk-proj-... | sk-ant-... | gsk_...)"
                        },
                        color = BasoCyan,
                        fontSize = 12.sp
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { showRawText = !showRawText }) {
                        Icon(
                            imageVector = if (showRawText) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle visibility",
                            tint = BasoTextMuted
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = BasoTextWhite,
                    unfocusedTextColor = BasoTextWhite,
                    focusedBorderColor = BasoCyan,
                    unfocusedBorderColor = BasoBorderCyan
                ),
                shape = RoundedCornerShape(14.dp),
                minLines = 2,
                maxLines = 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_key_vault_input")
            )

            // Live Detection Feedback Banner
            if (liveDetected != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Color(0xFF081B2B),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BasoCyan),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = liveDetected.confidenceNoteKu,
                            color = BasoCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "${liveDetected.provider.displayName} • ${liveDetected.provider.specialtyKu}",
                            color = BasoTextWhite,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val currentModel = selectedModelOverride ?: liveDetected.provider.defaultModel
                            liveDetected.provider.availableModels.forEach { m ->
                                val activeModel = currentModel == m
                                Surface(
                                    color = if (activeModel) BasoGold else BasoBgDark,
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier.clickable { selectedModelOverride = m }
                                ) {
                                    Text(
                                        text = m,
                                        color = if (activeModel) BasoDarkOnAccent else BasoTextMuted,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = customKeyLabel,
                onValueChange = { customKeyLabel = it },
                placeholder = {
                    Text(
                        text = if (isKurdish) "ناونیشانی کلیل (ئارەزوومەندانە - وەک: کلیلی تایبەتی کڵاود)" else "Optional label (e.g. My Pro Claude Key)",
                        color = BasoTextMuted,
                        fontSize = 11.5.sp
                    )
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = BasoTextWhite,
                    unfocusedTextColor = BasoTextWhite,
                    focusedBorderColor = BasoCyan,
                    unfocusedBorderColor = BasoBorderCyan
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    if (rawKeyInput.isNotBlank()) {
                        onAddOrImportAiKeys(
                            rawKeyInput,
                            customKeyLabel,
                            manualProvider,
                            selectedModelOverride
                        )
                        rawKeyInput = ""
                        customKeyLabel = ""
                    }
                },
                enabled = rawKeyInput.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BasoGold,
                    contentColor = BasoDarkOnAccent
                ),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("add_ai_key_button")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isKurdish) {
                        "خوێندنەوە و زیادکردنی کلیل بۆ ناو سیستەم"
                    } else {
                        "Detect & Save Key to Auto-Router"
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }

    // 3. Stored Keys List & Priority / Failover Controls
    Card(
        colors = CardDefaults.cardColors(containerColor = BasoCardSurface),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, BasoBorderCyan),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isKurdish) {
                        "کلیلە پاشەکەوتکراوەکانی ناو ستۆدیۆ (${storedAiKeys.size})"
                    } else {
                        "Saved Studio AI Keys (${storedAiKeys.size})"
                    },
                    color = BasoTextWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = if (isKurdish) "ڕیزبەندیی پێشینە" else "Priority Chain",
                    color = BasoCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (storedAiKeys.isEmpty()) {
                Surface(
                    color = BasoBgDark,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, BasoBorderCyan),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isKurdish) {
                                "هێشتا هیچ کلیلێکی دەرەکیت زیاد نەکردووە؛ لە ئێستادا بزوێنەری ناوخۆیی (BASO Neural Engine) چالاکە."
                            } else {
                                "No external API keys added yet. BASO Neural Engine is currently active."
                            },
                            color = BasoTextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    storedAiKeys.forEachIndexed { idx, keyItem ->
                        Surface(
                            color = if (keyItem.isEnabled) BasoCardElevated else BasoBgDark,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(
                                width = if (idx == 0 && keyItem.isEnabled) 1.5.dp else 1.dp,
                                color = if (idx == 0 && keyItem.isEnabled) BasoGold else BasoBorderCyan
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Surface(
                                            color = BasoGold.copy(alpha = 0.18f),
                                            shape = CircleShape,
                                            border = BorderStroke(1.dp, BasoGold)
                                        ) {
                                            Text(
                                                text = "#${idx + 1}",
                                                color = BasoGold,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                            )
                                        }

                                        Column {
                                            Text(
                                                text = "${keyItem.provider.displayName} • ${keyItem.label}",
                                                color = BasoTextWhite,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                            Text(
                                                text = "${AiKeyVaultManager.maskKey(keyItem.apiKey)} • Model: ${keyItem.preferredModel}",
                                                color = BasoCyan,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    Switch(
                                        checked = keyItem.isEnabled,
                                        onCheckedChange = { onToggleAiKeyEnabled(keyItem.id) },
                                        colors = SwitchDefaults.colors(checkedThumbColor = BasoCyan)
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Model Switcher Chips for this Key
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    keyItem.provider.availableModels.forEach { modelName ->
                                        val isCurrentModel = keyItem.preferredModel == modelName
                                        Surface(
                                            color = if (isCurrentModel) BasoCyan else BasoBgDark,
                                            shape = RoundedCornerShape(50),
                                            border = BorderStroke(1.dp, BasoCyan.copy(alpha = 0.5f)),
                                            modifier = Modifier.clickable {
                                                onUpdateAiKeyModel(keyItem.id, modelName)
                                            }
                                        ) {
                                            Text(
                                                text = modelName,
                                                color = if (isCurrentModel) BasoDarkOnAccent else BasoTextMuted,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "${keyItem.lastStatusNote} • سەرکەوتوو: ${keyItem.successCount} | گۆڕینی خۆکار: ${keyItem.failoverCount}",
                                    color = BasoTextMuted,
                                    fontSize = 10.5.sp
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        OutlinedButton(
                                            onClick = { onTestAiKey(keyItem.id) },
                                            border = BorderStroke(1.dp, BasoCyan),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.VerifiedUser,
                                                contentDescription = null,
                                                tint = BasoCyan,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (isKurdish) "پشکنین" else "Verify",
                                                color = BasoCyan,
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        if (idx > 0) {
                                            OutlinedButton(
                                                onClick = { onPromoteAiKeyPriority(keyItem.id) },
                                                border = BorderStroke(1.dp, BasoGold),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.height(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = null,
                                                    tint = BasoGold,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (isKurdish) "پێشینە (#1)" else "Make Primary",
                                                    color = BasoGold,
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    IconButton(
                                        onClick = { onDeleteAiKey(keyItem.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Key",
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
