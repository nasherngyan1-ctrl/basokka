package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SimCardDownload
import androidx.compose.material.icons.filled.TabletMac
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.AiKeyVaultManager
import com.example.data.BasoProjectEntity
import com.example.data.BuilderTab
import com.example.data.ChatMessage
import com.example.data.DesignTokens
import com.example.data.DeviceViewport
import com.example.data.DynamicUiBlueprint
import com.example.data.PlatformType
import com.example.data.ProjectFile
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
fun AiBuilderWorkspaceScreen(
    isKurdish: Boolean,
    builderTab: BuilderTab,
    promptText: String,
    chatMessages: List<ChatMessage>,
    uiBlueprint: DynamicUiBlueprint,
    selectedPlatform: PlatformType,
    selectedModel: String,
    temperature: Float,
    stylePreset: String,
    activeProject: BasoProjectEntity?,
    projectFiles: List<ProjectFile>,
    selectedFile: ProjectFile?,
    codeFilterTab: String,
    deviceViewport: DeviceViewport,
    designTokens: DesignTokens,
    isGenerating: Boolean,
    statusBanner: String?,
    isFullScreenPreview: Boolean,
    activeProviderBadge: String = "Auto-Router ⚡",
    autoSwitchEnabled: Boolean = true,
    onToggleAutoSwitch: (Boolean) -> Unit = {},
    onOpenKeyVault: () -> Unit = {},
    onBack: () -> Unit,
    onSelectBuilderTab: (BuilderTab) -> Unit,
    onPromptChange: (String) -> Unit,
    onSelectPlatform: (PlatformType) -> Unit,
    onSelectModel: (String) -> Unit,
    onTemperatureChange: (Float) -> Unit,
    onStylePresetChange: (String) -> Unit,
    onSendChatMessage: (String?, Boolean) -> Unit,
    onRestoreVersion: (DynamicUiBlueprint, Int) -> Unit,
    onBlueprintEditedDirectly: (DynamicUiBlueprint, String) -> Unit,
    onSelectCodeFilterTab: (String) -> Unit,
    onSelectFile: (ProjectFile) -> Unit,
    onViewportChange: (DeviceViewport) -> Unit,
    onUpdateDesignTokens: (DesignTokens) -> Unit,
    onToggleFullScreenPreview: (Boolean) -> Unit,
    onOpenZipBuildScreen: () -> Unit
) {
    BackHandler {
        if (isFullScreenPreview) {
            onToggleFullScreenPreview(false)
        } else {
            onBack()
        }
    }

    var showRunSettingsModal by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BasoBgDark)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        // Top Header: BASO 3D Logo + "BASO AI Builder" + AI Studio Run Settings + ZIP Export
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("builder_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = BasoTextWhite
                    )
                }

                BasoLetterMonogramBadge(
                    size = 36.dp,
                    primaryLetter = "B",
                    subLetter = null,
                    onClick = onOpenKeyVault
                )

                Column {
                    Text(
                        text = "BASO AI Builder",
                        color = BasoTextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "${uiBlueprint.appTitle} • $activeProviderBadge",
                        color = BasoCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = BasoCardSurface,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(
                        1.dp,
                        if (autoSwitchEnabled) BasoCyan else BasoGold
                    ),
                    modifier = Modifier
                        .clickable { showRunSettingsModal = true }
                        .padding(end = 4.dp)
                ) {
                    Text(
                        text = if (autoSwitchEnabled) "⚡ Auto-AI" else "Manual AI",
                        color = if (autoSwitchEnabled) BasoCyan else BasoGold,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }

                IconButton(
                    onClick = { showRunSettingsModal = true },
                    modifier = Modifier.testTag("ai_studio_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "AI Studio Model Settings",
                        tint = BasoCyan
                    )
                }

                Surface(
                    color = BasoGold,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .clickable { onOpenZipBuildScreen() }
                        .testTag("header_zip_export_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SimCardDownload,
                            contentDescription = "APK & GitHub Deploy",
                            tint = BasoDarkOnAccent,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (selectedPlatform == PlatformType.ANDROID) "📲 APK" else "🌐 GitHub",
                            color = BasoDarkOnAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 4 Studio Workspace Tabs: پڕۆمپت (چات) | پێشینە | کۆد | فایلەکان
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BuilderTab.entries.forEach { tab ->
                val isSelected = builderTab == tab
                Surface(
                    color = if (isSelected) BasoCardElevated else BasoCardSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) BasoCyan else BasoBorderCyan
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectBuilderTab(tab) }
                        .testTag("builder_tab_${tab.name.lowercase()}")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(vertical = 9.dp)
                    ) {
                        Text(
                            text = if (isKurdish) tab.labelKu else tab.labelEn,
                            color = if (isSelected) BasoTextWhite else BasoTextMuted,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(modifier = Modifier.weight(1f)) {
            when (builderTab) {
                BuilderTab.PROMPT -> ConversationalPromptBuilderTab(
                    isKurdish = isKurdish,
                    promptText = promptText,
                    chatMessages = chatMessages,
                    selectedPlatform = selectedPlatform,
                    isGenerating = isGenerating,
                    onPromptChange = onPromptChange,
                    onSelectPlatform = onSelectPlatform,
                    onSendMessage = { msg, goPreview -> onSendChatMessage(msg, goPreview) },
                    onRestoreVersion = onRestoreVersion,
                    onJumpToPreview = { onSelectBuilderTab(BuilderTab.PREVIEW) }
                )

                BuilderTab.PREVIEW -> LivePreviewTabView(
                    isKurdish = isKurdish,
                    selectedPlatform = selectedPlatform,
                    activeProject = activeProject,
                    uiBlueprint = uiBlueprint,
                    deviceViewport = deviceViewport,
                    designTokens = designTokens,
                    isGenerating = isGenerating,
                    onSelectPlatform = onSelectPlatform,
                    onViewportChange = onViewportChange,
                    onUpdateDesignTokens = onUpdateDesignTokens,
                    onBlueprintEditedDirectly = onBlueprintEditedDirectly,
                    onSendLiveEditChat = { editMsg -> onSendChatMessage(editMsg, false) },
                    onOpenFullScreen = { onToggleFullScreenPreview(true) }
                )

                BuilderTab.CODE -> CodeExplorerSection(
                    isKurdish = isKurdish,
                    codeFilterTab = codeFilterTab,
                    projectFiles = projectFiles,
                    selectedFile = selectedFile,
                    onSelectCodeFilterTab = onSelectCodeFilterTab,
                    onSelectFile = onSelectFile,
                    onOpenZipBuildScreen = onOpenZipBuildScreen
                )

                BuilderTab.FILES -> ProjectFilesOverviewSection(
                    isKurdish = isKurdish,
                    projectFiles = projectFiles,
                    onSelectFileToInspect = { file ->
                        onSelectFile(file)
                        onSelectBuilderTab(BuilderTab.CODE)
                    },
                    onOpenZipBuildScreen = onOpenZipBuildScreen
                )
            }
        }
    }

    if (showRunSettingsModal) {
        AiStudioRunSettingsDialog(
            isKurdish = isKurdish,
            selectedModel = selectedModel,
            temperature = temperature,
            stylePreset = stylePreset,
            autoSwitchEnabled = autoSwitchEnabled,
            activeProviderBadge = activeProviderBadge,
            onToggleAutoSwitch = onToggleAutoSwitch,
            onOpenKeyVault = {
                showRunSettingsModal = false
                onOpenKeyVault()
            },
            onSelectModel = onSelectModel,
            onTemperatureChange = onTemperatureChange,
            onStylePresetChange = onStylePresetChange,
            onDismiss = { showRunSettingsModal = false }
        )
    }

    if (isFullScreenPreview) {
        Dialog(
            onDismissRequest = { onToggleFullScreenPreview(false) },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BasoBgDark)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BasoCardSurface)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = activeProject?.previewUrl ?: "https://preview.baso.app",
                        color = BasoCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { onToggleFullScreenPreview(false) }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Fullscreen",
                            tint = BasoTextWhite
                        )
                    }
                }
                DynamicBlueprintPreviewCanvas(
                    blueprint = uiBlueprint,
                    reloadKey = 0,
                    onBlueprintEdited = onBlueprintEditedDirectly,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun ConversationalPromptBuilderTab(
    isKurdish: Boolean,
    promptText: String,
    chatMessages: List<ChatMessage>,
    selectedPlatform: PlatformType,
    isGenerating: Boolean,
    onPromptChange: (String) -> Unit,
    onSelectPlatform: (PlatformType) -> Unit,
    onSendMessage: (String?, Boolean) -> Unit,
    onRestoreVersion: (DynamicUiBlueprint, Int) -> Unit,
    onJumpToPreview: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Platform Selector Row at top: Android | Web | Web App
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isKurdish) "پلاتفۆرم:" else "Platform:",
                color = BasoTextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            PlatformType.entries.forEach { platform ->
                val isSelected = selectedPlatform == platform
                Surface(
                    color = if (isSelected) BasoCardElevated else BasoCardSurface,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) BasoCyan else BasoBorderCyan
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectPlatform(platform) }
                        .testTag("platform_chip_${platform.name.lowercase()}")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(vertical = 7.dp)
                    ) {
                        Text(
                            text = platform.labelEn,
                            color = if (isSelected) BasoTextWhite else BasoTextMuted,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Multi-Turn Chat Thread (User & BASO AI Architect messages with version snapshots)
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(chatMessages, key = { it.id }) { msg ->
                if (msg.isUser) {
                    // User Message Bubble (aligned to start/right in RTL)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF12283D)),
                            shape = RoundedCornerShape(
                                topStart = 18.dp,
                                topEnd = 4.dp,
                                bottomStart = 18.dp,
                                bottomEnd = 18.dp
                            ),
                            border = BorderStroke(1.dp, BasoCyan.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    BasoLetterMonogramBadge(
                                        size = 24.dp,
                                        primaryLetter = "B",
                                        subLetter = null
                                    )
                                    Text(
                                        text = "باسۆ (تۆ)",
                                        color = BasoGold,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = msg.text,
                                    color = BasoTextWhite,
                                    fontSize = 13.5.sp,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                } else {
                    // BASO AI Architect Response Bubble with Version Snapshot & Instant Preview Button
                    Card(
                        colors = CardDefaults.cardColors(containerColor = BasoCardSurface),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, BasoGold.copy(alpha = 0.45f)),
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
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    BasoLetterMonogramBadge(
                                        size = 24.dp,
                                        primaryLetter = "B",
                                        subLetter = null
                                    )
                                    Column {
                                        Text(
                                            text = "BASO AI Architect",
                                            color = BasoCyan,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Text(
                                            text = msg.providerUsedBadge,
                                            color = BasoGold,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Surface(
                                    color = BasoGold.copy(alpha = 0.18f),
                                    shape = RoundedCornerShape(50),
                                    border = BorderStroke(1.dp, BasoGold)
                                ) {
                                    Text(
                                        text = "v${msg.versionNumber}",
                                        color = BasoGold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            if (!msg.autoSwitchedNote.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    color = Color(0xFF071929),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, BasoCyan.copy(alpha = 0.45f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = msg.autoSwitchedNote,
                                        color = BasoCyan,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = msg.text,
                                color = BasoTextWhite,
                                fontSize = 13.sp,
                                lineHeight = 19.sp
                            )

                            if (msg.blueprintSnapshot != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = onJumpToPreview,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = BasoCyan,
                                            contentColor = BasoDarkOnAccent
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Visibility,
                                            contentDescription = null,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = "بینینی پێشینە",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            onRestoreVersion(msg.blueprintSnapshot, msg.versionNumber)
                                        },
                                        border = BorderStroke(1.dp, BasoGold),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.History,
                                            contentDescription = null,
                                            tint = BasoGold,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "گەڕانەوە بۆ v${msg.versionNumber}",
                                            color = BasoGold,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Quick Style & Live Edit Suggestion Chips (Creates completely different styles or edits current!)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val suggestions = listOf(
                "🧮 ئەپی ژمێریار و دراو" to "ئەپێکی ژمێریاری زیرەک و گۆڕینی دراو (دۆلار بۆ دینار) دروست بکە",
                "🎮 یاری X و O" to "یارییەکی کارلێکەری X و O (Tic-Tac-Toe) دروست بکە بە دیزاینی نیۆن",
                "✅ ئەپی ئەرک و تێبینی" to "ئەپێکی بەڕێوەبردنی ئەرکەکانی ڕۆژانە و تێبینی (Todo List) دروست بکە",
                "☀️ ئەپی کەشوهەوا" to "ئەپێکی کەشوهەوای زیندووی شارەکانی کوردستان (هەولێر، سلێمانی، دهۆک) دروست بکە",
                "🕌 قورئان و تەسبیح" to "ئەپێکی قورئانی پیرۆز و تەسبیحی ئەلیکترۆنی و کاتەکانی بانگ دروست بکە",
                "🎧 میوزیک پلەیەر" to "ئەپێکی میوزیک پلەیەر و پۆدکاست بە ئیکۆلایزەر دروست بکە",
                "🚗 پێشانگای ئۆتۆمبێل" to "ئەپێکی پێشانگای کڕین و فرۆشتنی ئۆتۆمبێل و حجزکردن دروست بکە",
                "🍔 ئەپی چێشتخانە و خواردن" to "ئەپێکی داواکردنی خواردن و چێشتخانەی کوردی دروست بکە بە ڕەنگی سوور و زەرد",
                "📊 داشبۆردی بانک و کریپتۆ" to "داشبۆردێکی دارایی و کریپتۆ و ئاماری فرۆشتن بە چارت و گراف دروست بکە",
                "✨ پۆرتفۆلیۆی تایبەت بە باسۆ" to "وێبسایتێکی پۆرتفۆلیۆی پڕۆفیشناڵ بۆ باسۆ دروست بکە لەگەڵ بەشی نرخەکان و فۆڕمی پەیوەندی",
                "🎨 ڕەنگەکەی بکە بە مۆر" to "ڕەنگی دیزاینەکە بگۆڕە بۆ مۆر و پەمەیی",
                "💰 بەشی نرخەکان زیاد بکە" to "بەشی پاکێج و نرخەکان (Pricing) زیاد بکە بۆ خوارەوەی پەڕەکە"
            )
            suggestions.forEach { (chipLabel, fullCommand) ->
                Surface(
                    color = BasoCardSurface,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, BasoCyan.copy(alpha = 0.4f)),
                    modifier = Modifier.clickable {
                        onSendMessage(fullCommand, true)
                    }
                ) {
                    Text(
                        text = chipLabel,
                        color = BasoCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Chat Input Composer + Live Intent-Based AI Auto-Selector + Generate/Edit Buttons
        val liveIntentDecision = remember(promptText, chatMessages.size) {
            AiKeyVaultManager.analyzePromptIntent(
                rawPrompt = promptText,
                isIterativeEdit = chatMessages.size > 1,
                storedKeys = emptyList()
            )
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF102235)),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.2.dp, BasoCyan.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                // Real-time Task-Aware AI Auto-Selector Indicator Bar
                Surface(
                    color = Color(0xFF071522),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(
                        1.dp,
                        if (promptText.isNotBlank()) BasoGold.copy(alpha = 0.75f) else BasoCyan.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isKurdish) {
                                    "🤖 هەڵبژێرەری خۆکاری باشترین AI:"
                                } else {
                                    "🤖 Live Task-Aware AI Selector:"
                                },
                                color = BasoCyan,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = liveIntentDecision.badgeTitleKu,
                                color = BasoGold,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        if (promptText.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = liveIntentDecision.explanationKu,
                                color = BasoTextMuted,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = promptText,
                    onValueChange = onPromptChange,
                    placeholder = {
                        Text(
                            text = if (isKurdish) {
                                "هەرچییەک بنووسیت (دیزاین، چارت، کۆد، فرۆشگا...) سیستەمەکە خۆکارانە باشترین AI هەڵدەبژێرێت..."
                            } else {
                                "Type anything (design, chart, code, store...) and the best AI is auto-selected..."
                            },
                            color = BasoTextMuted,
                            fontSize = 12.sp
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = BasoTextWhite,
                        unfocusedTextColor = BasoTextWhite,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = BasoCyan
                    ),
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("prompt_input_field")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Send in Chat & Stay
                    OutlinedButton(
                        onClick = { onSendMessage(null, false) },
                        enabled = !isGenerating && promptText.isNotBlank(),
                        border = BorderStroke(1.dp, BasoCyan),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            tint = BasoCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isKurdish) "ناردن لە چات" else "Send in Chat",
                            color = BasoCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Generate & Jump to Live Preview (matching Screen 4 CTA)
                    Button(
                        onClick = { onSendMessage(null, true) },
                        enabled = !isGenerating && promptText.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BasoCyan,
                            contentColor = BasoDarkOnAccent
                        ),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("generate_project_ai_button")
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                color = BasoDarkOnAccent,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "چاوەڕوانبە...",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isKurdish) "پەیوەندی پڕۆژە دروست بکە" else "Build & Preview",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LivePreviewTabView(
    isKurdish: Boolean,
    selectedPlatform: PlatformType,
    activeProject: BasoProjectEntity?,
    uiBlueprint: DynamicUiBlueprint,
    deviceViewport: DeviceViewport,
    designTokens: DesignTokens,
    isGenerating: Boolean,
    onSelectPlatform: (PlatformType) -> Unit,
    onViewportChange: (DeviceViewport) -> Unit,
    onUpdateDesignTokens: (DesignTokens) -> Unit,
    onBlueprintEditedDirectly: (DynamicUiBlueprint, String) -> Unit,
    onSendLiveEditChat: (String) -> Unit,
    onOpenFullScreen: () -> Unit
) {
    var reloadCounter by remember { mutableIntStateOf(0) }
    var showDesignCustomizer by remember { mutableStateOf(false) }
    var liveChatEditInput by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Expandable Live Design Token Customizer Bar
        AnimatedVisibility(visible = showDesignCustomizer) {
            Surface(
                color = BasoCardSurface,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, BasoCyan.copy(alpha = 0.45f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isKurdish) "گۆڕینی ڕاستەوخۆی ستایل و ڕەنگ" else "Live Visual Theme Inspector",
                            color = BasoGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isKurdish) "تاریک" else "Dark",
                                color = BasoTextMuted,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Switch(
                                checked = uiBlueprint.isDark,
                                onCheckedChange = {
                                    onUpdateDesignTokens(designTokens.copy(darkModePreview = it))
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = BasoCyan)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val swatches = listOf(
                            "#F5C518" to Color(0xFFF5C518),
                            "#00E5CC" to Color(0xFF00E5CC),
                            "#EF4444" to Color(0xFFEF4444),
                            "#A855F7" to Color(0xFFA855F7),
                            "#10B981" to Color(0xFF10B981),
                            "#3B82F6" to Color(0xFF3B82F6)
                        )
                        swatches.forEach { (hex, composeColor) ->
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(composeColor)
                                    .border(
                                        width = if (uiBlueprint.primaryHex == hex) 2.5.dp else 1.dp,
                                        color = if (uiBlueprint.primaryHex == hex) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        onUpdateDesignTokens(designTokens.copy(primaryHex = hex))
                                    }
                            )
                        }
                    }
                }
            }
        }

        // Browser / Device Frame matching Screen 5
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = BasoCardSurface),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.5.dp, BasoCyan.copy(alpha = 0.45f)),
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(deviceViewport.widthFraction)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Browser URL Bar: ↻ 🔒 https://preview.baso.app + Palette toggle
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF07131E))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Reload Preview",
                                    tint = BasoTextMuted,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable { reloadCounter++ }
                                )
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = BasoCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = activeProject?.previewUrl ?: "https://preview.baso.app",
                                    color = BasoTextWhite,
                                    fontSize = 11.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Customize Live Theme",
                                tint = if (showDesignCustomizer) BasoGold else BasoCyan,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable { showDesignCustomizer = !showDesignCustomizer }
                                    .testTag("toggle_design_customizer_button")
                            )
                        }
                    }

                    // Dynamic Multi-Archetype Preview Renderer!
                    DynamicBlueprintPreviewCanvas(
                        blueprint = uiBlueprint,
                        reloadKey = reloadCounter,
                        onBlueprintEdited = onBlueprintEditedDirectly,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // INLINE LIVE AI CHAT EDIT BAR RIGHT INSIDE PREVIEW (WITH REAL-TIME BEST AI SELECTOR)!
        val livePreviewIntent = remember(liveChatEditInput) {
            AiKeyVaultManager.analyzePromptIntent(
                rawPrompt = liveChatEditInput,
                isIterativeEdit = true,
                storedKeys = emptyList()
            )
        }

        Surface(
            color = Color(0xFF102235),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, BasoCyan.copy(alpha = 0.55f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isKurdish) "⚡ باشترین AI بۆ ئەم دەستکارییە:" else "⚡ Best AI for this edit:",
                        color = BasoCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = livePreviewIntent.badgeTitleKu,
                        color = BasoGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = liveChatEditInput,
                        onValueChange = { liveChatEditInput = it },
                        placeholder = {
                            Text(
                                text = if (isKurdish) {
                                    "بە چات ڕاستەوخۆ دەستکاری بکە (بۆ نموونە: بیکە بە چێشتخانە، ڕەنگەکەی بکە مۆر)..."
                                } else {
                                    "Chat to edit live (e.g. make it a restaurant, change color to purple)..."
                                },
                                color = BasoTextMuted,
                                fontSize = 11.sp
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = BasoTextWhite,
                            unfocusedTextColor = BasoTextWhite,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = BasoCyan
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("live_preview_chat_edit_input")
                    )

                    Button(
                        onClick = {
                            if (liveChatEditInput.isNotBlank()) {
                                onSendLiveEditChat(liveChatEditInput)
                                liveChatEditInput = ""
                            }
                        },
                        enabled = !isGenerating && liveChatEditInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BasoCyan,
                            contentColor = BasoDarkOnAccent
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                color = BasoDarkOnAccent,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(14.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Apply Live Edit",
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Bottom Bar matching Screen 5: Device Frame Icons (Tablet | Phone | Desktop) + 👁 پیشینەی ڕاستەقینە
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = BasoCardSurface,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BasoBorderCyan)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onViewportChange(DeviceViewport.TABLET) },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TabletMac,
                            contentDescription = "Tablet View",
                            tint = if (deviceViewport == DeviceViewport.TABLET) BasoCyan else BasoTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = { onViewportChange(DeviceViewport.PHONE) },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = "Mobile View",
                            tint = if (deviceViewport == DeviceViewport.PHONE) BasoCyan else BasoTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = { onViewportChange(DeviceViewport.DESKTOP) },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DesktopWindows,
                            contentDescription = "Full Width View",
                            tint = if (deviceViewport == DeviceViewport.DESKTOP) BasoCyan else BasoTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = onOpenFullScreen,
                border = BorderStroke(1.2.dp, BasoCyan),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = BasoCardSurface,
                    contentColor = BasoCyan
                ),
                modifier = Modifier.testTag("full_screen_preview_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isKurdish) "پیشینەی ڕاستەقینە" else "Real Preview",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun AiStudioRunSettingsDialog(
    isKurdish: Boolean,
    selectedModel: String,
    temperature: Float,
    stylePreset: String,
    autoSwitchEnabled: Boolean,
    activeProviderBadge: String,
    onToggleAutoSwitch: (Boolean) -> Unit,
    onOpenKeyVault: () -> Unit,
    onSelectModel: (String) -> Unit,
    onTemperatureChange: (Float) -> Unit,
    onStylePresetChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = BasoCardSurface),
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.5.dp, BasoCyan),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = if (isKurdish) "ڕێکخستنی مۆدێل و کلیلەکانی AI" else "AI Router & Model Settings",
                    color = BasoGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Auto-Switch AI Toggle Row
                Surface(
                    color = BasoBgDark,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, BasoCyan.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isKurdish) "گۆڕینی ئۆتۆماتیکی AI (Auto-Switch)" else "Auto-Switch AI Failover",
                                color = BasoTextWhite,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = if (isKurdish) {
                                    "چالاک: $activeProviderBadge"
                                } else {
                                    "Active: $activeProviderBadge"
                                },
                                color = BasoCyan,
                                fontSize = 11.sp
                            )
                        }

                        Switch(
                            checked = autoSwitchEnabled,
                            onCheckedChange = onToggleAutoSwitch,
                            colors = SwitchDefaults.colors(checkedThumbColor = BasoGold)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onOpenKeyVault,
                    border = BorderStroke(1.2.dp, BasoGold),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isKurdish) {
                            "🔑 بەڕێوەبردن و دانانی کلیلەکانی (OpenAI / Claude / Gemini...)"
                        } else {
                            "🔑 Open Multi-Provider AI Key Vault"
                        },
                        color = BasoGold,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (isKurdish) "مۆدێلی بنەڕەتی Gemini:" else "Default Gemini Model:",
                    color = BasoTextWhite,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                val models = listOf(
                    "gemini-3.1-pro-preview" to "Gemini 3.1 Pro (باشترین بۆ کۆد و دیزاین)",
                    "gemini-3.5-flash" to "Gemini 3.5 Flash (خێرا بۆ دەستکاری)"
                )
                models.forEach { (id, label) ->
                    val active = selectedModel == id
                    Surface(
                        color = if (active) BasoCardElevated else BasoBgDark,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, if (active) BasoCyan else BasoBorderCyan),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onSelectModel(id) }
                    ) {
                        Text(
                            text = label,
                            color = if (active) BasoCyan else BasoTextWhite,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(11.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Temperature (${String.format("%.1f", temperature)})",
                    color = BasoTextWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Slider(
                    value = temperature,
                    onValueChange = onTemperatureChange,
                    valueRange = 0.1f..1.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = BasoGold,
                        activeTrackColor = BasoCyan
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BasoGold,
                        contentColor = BasoDarkOnAccent
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isKurdish) "داخستن و پاشەکەوتکردن" else "Save & Close",
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}
