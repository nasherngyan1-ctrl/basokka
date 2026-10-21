package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppLanguage
import com.example.data.BottomNavTab
import com.example.data.BuilderTab
import com.example.data.PlatformType
import com.example.data.StudioScreen
import com.example.ui.StudioViewModel
import com.example.ui.screens.AiBuilderWorkspaceScreen
import com.example.ui.screens.AiToolsStudioScreen
import com.example.ui.screens.BuildSuccessZipScreen
import com.example.ui.screens.HomeDashboardScreen
import com.example.ui.screens.NewCreationSelectorScreen
import com.example.ui.screens.PersonalSettingsScreen
import com.example.ui.screens.ProjectsLibraryScreen
import com.example.ui.screens.SplashWelcomeScreen
import com.example.ui.theme.BasoBgDark
import com.example.ui.theme.BasoBorderCyan
import com.example.ui.theme.BasoCyan
import com.example.ui.theme.BasoTextMuted
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                BasoStudioApp()
            }
        }
    }
}

@Composable
fun BasoStudioApp(
    viewModel: StudioViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val bottomNavTab by viewModel.bottomNavTab.collectAsStateWithLifecycle()
    val builderTab by viewModel.builderTab.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()
    val projects by viewModel.projects.collectAsStateWithLifecycle()
    val activeProject by viewModel.activeProject.collectAsStateWithLifecycle()
    val uiBlueprint by viewModel.uiBlueprint.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val promptText by viewModel.promptText.collectAsStateWithLifecycle()
    val selectedPlatform by viewModel.selectedPlatform.collectAsStateWithLifecycle()
    val selectedModel by viewModel.selectedModel.collectAsStateWithLifecycle()
    val temperature by viewModel.temperature.collectAsStateWithLifecycle()
    val stylePreset by viewModel.stylePreset.collectAsStateWithLifecycle()
    val designTokens by viewModel.designTokens.collectAsStateWithLifecycle()
    val projectFiles by viewModel.projectFiles.collectAsStateWithLifecycle()
    val selectedFile by viewModel.selectedFile.collectAsStateWithLifecycle()
    val codeFilterTab by viewModel.codeFilterTab.collectAsStateWithLifecycle()
    val deviceViewport by viewModel.deviceViewport.collectAsStateWithLifecycle()
    val isFullScreenPreview by viewModel.isFullScreenPreview.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val statusBanner by viewModel.statusBanner.collectAsStateWithLifecycle()
    val projectsFilter by viewModel.projectsFilter.collectAsStateWithLifecycle()
    val projectsSearchQuery by viewModel.projectsSearchQuery.collectAsStateWithLifecycle()
    val lastZipFileName by viewModel.lastZipFileName.collectAsStateWithLifecycle()
    val lastZipReadableSize by viewModel.lastZipReadableSize.collectAsStateWithLifecycle()
    val aiToolResult by viewModel.aiToolResult.collectAsStateWithLifecycle()
    val isRunningTool by viewModel.isRunningTool.collectAsStateWithLifecycle()

    // Multi-Provider AI Key Vault & Auto-Router States
    val storedAiKeys by viewModel.storedAiKeys.collectAsStateWithLifecycle()
    val autoSwitchEnabled by viewModel.autoSwitchEnabled.collectAsStateWithLifecycle()
    val routingStrategy by viewModel.routingStrategy.collectAsStateWithLifecycle()
    val activeProviderBadge by viewModel.activeProviderBadge.collectAsStateWithLifecycle()
    val lastAutoSwitchEvent by viewModel.lastAutoSwitchEvent.collectAsStateWithLifecycle()

    // GitHub Direct Connect & Cloud APK Builder States
    val gitHubAccountState by viewModel.gitHubAccountState.collectAsStateWithLifecycle()
    val gitHubDeployResult by viewModel.gitHubDeployResult.collectAsStateWithLifecycle()
    val isDeployingToGitHub by viewModel.isDeployingToGitHub.collectAsStateWithLifecycle()
    val gitHubDeployProgressText by viewModel.gitHubDeployProgressText.collectAsStateWithLifecycle()

    val isKurdish = language == AppLanguage.KURDISH
    val layoutDirection = if (language.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    BackHandler(enabled = currentScreen != StudioScreen.SPLASH) {
        viewModel.navigateBack()
    }

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Scaffold(
            containerColor = BasoBgDark,
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                if (currentScreen == StudioScreen.MAIN_HUB) {
                    BasoBottomNavigationBar(
                        isKurdish = isKurdish,
                        selectedTab = bottomNavTab,
                        onSelectTab = { viewModel.selectBottomNavTab(it) }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentScreen) {
                    StudioScreen.SPLASH -> {
                        SplashWelcomeScreen(
                            isKurdish = isKurdish,
                            onGetStarted = { viewModel.enterStudioFromSplash() }
                        )
                    }

                    StudioScreen.MAIN_HUB -> {
                        when (bottomNavTab) {
                            BottomNavTab.HOME -> {
                                HomeDashboardScreen(
                                    isKurdish = isKurdish,
                                    projects = projects,
                                    activeProviderBadge = activeProviderBadge,
                                    configuredKeyCount = storedAiKeys.count { it.isEnabled },
                                    autoSwitchEnabled = autoSwitchEnabled,
                                    onOpenNewCreation = { viewModel.openNewCreationSelector() },
                                    onSelectWebBuilder = {
                                        viewModel.startNewProjectWithCategory(
                                            platform = PlatformType.WEB,
                                            defaultPrompt = ""
                                        )
                                    },
                                    onSelectAppBuilder = {
                                        viewModel.startNewProjectWithCategory(
                                            platform = PlatformType.ANDROID,
                                            defaultPrompt = ""
                                        )
                                    },
                                    onSeeAllProjects = {
                                        viewModel.selectBottomNavTab(BottomNavTab.PROJECTS)
                                    },
                                    onOpenProject = { proj ->
                                        viewModel.loadProjectIntoWorkspace(proj, navigateToBuilder = true)
                                    },
                                    onOpenAiKeyVault = {
                                        viewModel.openAiKeysVaultScreen()
                                    },
                                    onToggleLanguage = { viewModel.toggleLanguage() }
                                )
                            }

                            BottomNavTab.PROJECTS -> {
                                ProjectsLibraryScreen(
                                    isKurdish = isKurdish,
                                    projects = projects,
                                    selectedFilter = projectsFilter,
                                    searchQuery = projectsSearchQuery,
                                    onFilterChange = { viewModel.setProjectsFilter(it) },
                                    onSearchQueryChange = { viewModel.setProjectsSearchQuery(it) },
                                    onCreateNewProject = { viewModel.openNewCreationSelector() },
                                    onOpenProject = { proj ->
                                        viewModel.loadProjectIntoWorkspace(proj, navigateToBuilder = true)
                                    }
                                )
                            }

                            BottomNavTab.TOOLS -> {
                                AiToolsStudioScreen(
                                    isKurdish = isKurdish,
                                    isRunningTool = isRunningTool,
                                    aiToolResult = aiToolResult,
                                    onRunTool = { toolType, input ->
                                        viewModel.executeStudioAiTool(toolType, input)
                                    },
                                    onJumpToBuilderWithPrompt = { idea ->
                                        viewModel.startNewProjectWithCategory(
                                            platform = PlatformType.WEB,
                                            defaultPrompt = idea
                                        )
                                    }
                                )
                            }

                            BottomNavTab.SETTINGS -> {
                                PersonalSettingsScreen(
                                    language = language,
                                    selectedModel = selectedModel,
                                    storedAiKeys = storedAiKeys,
                                    autoSwitchEnabled = autoSwitchEnabled,
                                    routingStrategy = routingStrategy,
                                    lastAutoSwitchEvent = lastAutoSwitchEvent,
                                    onSelectLanguage = { viewModel.setLanguage(it) },
                                    onSelectModel = { viewModel.selectModel(it) },
                                    onAddOrImportAiKeys = { rawInput, label, manualProvider, modelOverride ->
                                        viewModel.addOrImportAiKeys(
                                            rawKeyInput = rawInput,
                                            customLabel = label,
                                            manualProviderOverride = manualProvider,
                                            preferredModelOverride = modelOverride
                                        )
                                    },
                                    onToggleAiKeyEnabled = { viewModel.toggleAiKeyEnabled(it) },
                                    onUpdateAiKeyModel = { keyId, modelName ->
                                        viewModel.updateAiKeyModel(keyId, modelName)
                                    },
                                    onUpdateAiKeyProvider = { keyId, provider ->
                                        viewModel.updateAiKeyProvider(keyId, provider)
                                    },
                                    onPromoteAiKeyPriority = { viewModel.promoteAiKeyPriority(it) },
                                    onTestAiKey = { viewModel.testAndVerifyAiKey(it) },
                                    onDeleteAiKey = { viewModel.deleteAiKey(it) },
                                    onSetAutoSwitchEnabled = { viewModel.setAutoSwitchEnabled(it) },
                                    onSetRoutingStrategy = { viewModel.setRoutingStrategy(it) }
                                )
                            }
                        }
                    }

                    StudioScreen.NEW_CREATION_SELECTOR -> {
                        NewCreationSelectorScreen(
                            isKurdish = isKurdish,
                            onBack = { viewModel.navigateBack() },
                            onSelectMobileApp = {
                                viewModel.startNewProjectWithCategory(
                                    platform = PlatformType.ANDROID,
                                    defaultPrompt = ""
                                )
                            },
                            onSelectWebsite = {
                                viewModel.startNewProjectWithCategory(
                                    platform = PlatformType.WEB,
                                    defaultPrompt = ""
                                )
                            }
                        )
                    }

                    StudioScreen.AI_BUILDER -> {
                        AiBuilderWorkspaceScreen(
                            isKurdish = isKurdish,
                            builderTab = builderTab,
                            promptText = promptText,
                            chatMessages = chatMessages,
                            uiBlueprint = uiBlueprint,
                            selectedPlatform = selectedPlatform,
                            selectedModel = selectedModel,
                            temperature = temperature,
                            stylePreset = stylePreset,
                            activeProject = activeProject,
                            projectFiles = projectFiles,
                            selectedFile = selectedFile,
                            codeFilterTab = codeFilterTab,
                            deviceViewport = deviceViewport,
                            designTokens = designTokens,
                            isGenerating = isGenerating,
                            statusBanner = statusBanner,
                            isFullScreenPreview = isFullScreenPreview,
                            activeProviderBadge = activeProviderBadge,
                            autoSwitchEnabled = autoSwitchEnabled,
                            onToggleAutoSwitch = { viewModel.setAutoSwitchEnabled(it) },
                            onOpenKeyVault = { viewModel.openAiKeysVaultScreen() },
                            onBack = { viewModel.navigateBack() },
                            onSelectBuilderTab = { viewModel.selectBuilderTab(it) },
                            onPromptChange = { viewModel.updatePromptText(it) },
                            onSelectPlatform = { viewModel.selectPlatform(it) },
                            onSelectModel = { viewModel.selectModel(it) },
                            onTemperatureChange = { viewModel.updateTemperature(it) },
                            onStylePresetChange = { viewModel.selectStylePreset(it) },
                            onSendChatMessage = { msgOverride, goPreview ->
                                viewModel.sendChatMessage(
                                    context = context,
                                    messageOverride = msgOverride,
                                    switchToPreviewOnFinish = goPreview
                                )
                            },
                            onRestoreVersion = { bp, ver ->
                                viewModel.restoreBlueprintVersion(bp, ver)
                            },
                            onBlueprintEditedDirectly = { updatedBp, note ->
                                viewModel.updateBlueprintDirectly(updatedBp, note)
                            },
                            onSelectCodeFilterTab = { viewModel.selectCodeFilterTab(it) },
                            onSelectFile = { viewModel.selectProjectFile(it) },
                            onViewportChange = { viewModel.setDeviceViewport(it) },
                            onUpdateDesignTokens = { viewModel.updateDesignTokens(it) },
                            onToggleFullScreenPreview = { viewModel.toggleFullScreenPreview(it) },
                            onOpenZipBuildScreen = { viewModel.openBuildSuccessScreen(context) }
                        )
                    }

                    StudioScreen.BUILD_SUCCESS_ZIP -> {
                        BuildSuccessZipScreen(
                            isKurdish = isKurdish,
                            zipFileName = lastZipFileName,
                            zipSizeLabel = lastZipReadableSize,
                            selectedPlatform = selectedPlatform,
                            projectTitle = uiBlueprint.appTitle,
                            gitHubAccountState = gitHubAccountState,
                            gitHubDeployResult = gitHubDeployResult,
                            isDeployingToGitHub = isDeployingToGitHub,
                            gitHubDeployProgressText = gitHubDeployProgressText,
                            onConnectGitHubToken = { viewModel.connectGitHubToken(it) },
                            onDisconnectGitHub = { viewModel.disconnectGitHubAccount() },
                            onDeployToGitHub = { viewModel.deployActiveProjectToGitHub(context) },
                            onDownloadLatestApk = { viewModel.downloadLatestApkOrOpenActions(context) },
                            onDownloadStandaloneApp = { viewModel.downloadStandaloneAppHtml(context) },
                            onBack = { viewModel.navigateBack() },
                            onDownloadZip = { viewModel.downloadOrShareCurrentZip(context) },
                            onViewFiles = {
                                viewModel.openAiBuilderTab(BuilderTab.CODE)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BasoBottomNavigationBar(
    isKurdish: Boolean,
    selectedTab: BottomNavTab,
    onSelectTab: (BottomNavTab) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF07131E))
            .navigationBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(BasoBorderCyan)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavTab.entries.forEach { tab ->
                val isSelected = selectedTab == tab
                val icon = when (tab) {
                    BottomNavTab.HOME -> if (isSelected) Icons.Filled.Home else Icons.Outlined.Home
                    BottomNavTab.PROJECTS -> if (isSelected) Icons.Filled.Folder else Icons.Outlined.Folder
                    BottomNavTab.TOOLS -> if (isSelected) Icons.Filled.AutoAwesome else Icons.Outlined.AutoAwesome
                    BottomNavTab.SETTINGS -> if (isSelected) Icons.Filled.Key else Icons.Outlined.Key
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectTab(tab) }
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .testTag("bottom_nav_${tab.name.lowercase()}")
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = tab.labelEn,
                        tint = if (isSelected) BasoCyan else BasoTextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = if (isKurdish) tab.labelKu else tab.labelEn,
                        color = if (isSelected) BasoCyan else BasoTextMuted,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(2.5.dp)
                            .clip(RoundedCornerShape(50))
                            .background(if (isSelected) BasoCyan else Color.Transparent)
                    )
                }
            }
        }
    }
}
