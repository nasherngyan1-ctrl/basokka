package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AiAutoRoutingStrategy
import com.example.data.AiKeyVaultManager
import com.example.data.AiProviderType
import com.example.data.AppLanguage
import com.example.data.BasoDatabase
import com.example.data.BasoProjectEntity
import com.example.data.BasoRepository
import com.example.data.BottomNavTab
import com.example.data.BuilderTab
import com.example.data.ChatMessage
import com.example.data.DesignTokens
import com.example.data.DeviceViewport
import com.example.data.DynamicUiBlueprint
import com.example.data.GeminiStudioService
import com.example.data.GitHubAccountState
import com.example.data.GitHubDeployResult
import com.example.data.GitHubDeployService
import com.example.data.PlatformType
import com.example.data.ProjectFile
import com.example.data.StarterTemplates
import com.example.data.StoredAiKey
import com.example.data.StudioScreen
import com.example.data.ZipExporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class StudioViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val repository = BasoRepository(BasoDatabase.getInstance(application).projectDao())
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true; prettyPrint = true }

    val projects: StateFlow<List<BasoProjectEntity>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentScreen = MutableStateFlow(StudioScreen.SPLASH)
    val currentScreen: StateFlow<StudioScreen> = _currentScreen.asStateFlow()

    private val _bottomNavTab = MutableStateFlow(BottomNavTab.HOME)
    val bottomNavTab: StateFlow<BottomNavTab> = _bottomNavTab.asStateFlow()

    private val _builderTab = MutableStateFlow(BuilderTab.PROMPT)
    val builderTab: StateFlow<BuilderTab> = _builderTab.asStateFlow()

    private val _language = MutableStateFlow(AppLanguage.KURDISH)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _activeProject = MutableStateFlow<BasoProjectEntity?>(null)
    val activeProject: StateFlow<BasoProjectEntity?> = _activeProject.asStateFlow()

    private val _uiBlueprint = MutableStateFlow(
        StarterTemplates.buildBaseBlueprintForArchetype("ECOMMERCE", "MyShop")
    )
    val uiBlueprint: StateFlow<DynamicUiBlueprint> = _uiBlueprint.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _promptText = MutableStateFlow("")
    val promptText: StateFlow<String> = _promptText.asStateFlow()

    private val _selectedPlatform = MutableStateFlow(PlatformType.ANDROID)
    val selectedPlatform: StateFlow<PlatformType> = _selectedPlatform.asStateFlow()

    private val _selectedModel = MutableStateFlow("gemini-3.1-pro-preview")
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    private val _temperature = MutableStateFlow(0.7f)
    val temperature: StateFlow<Float> = _temperature.asStateFlow()

    private val _stylePreset = MutableStateFlow("BASO Signature Gold & Cyan")
    val stylePreset: StateFlow<String> = _stylePreset.asStateFlow()

    private val _designTokens = MutableStateFlow(DesignTokens())
    val designTokens: StateFlow<DesignTokens> = _designTokens.asStateFlow()

    private val _projectFiles = MutableStateFlow<List<ProjectFile>>(StarterTemplates.defaultProjectFiles("MyShop"))
    val projectFiles: StateFlow<List<ProjectFile>> = _projectFiles.asStateFlow()

    private val _selectedFile = MutableStateFlow<ProjectFile?>(null)
    val selectedFile: StateFlow<ProjectFile?> = _selectedFile.asStateFlow()

    private val _codeFilterTab = MutableStateFlow("Web")
    val codeFilterTab: StateFlow<String> = _codeFilterTab.asStateFlow()

    private val _deviceViewport = MutableStateFlow(DeviceViewport.PHONE)
    val deviceViewport: StateFlow<DeviceViewport> = _deviceViewport.asStateFlow()

    private val _isFullScreenPreview = MutableStateFlow(false)
    val isFullScreenPreview: StateFlow<Boolean> = _isFullScreenPreview.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    private val _projectsFilter = MutableStateFlow("هەموو")
    val projectsFilter: StateFlow<String> = _projectsFilter.asStateFlow()

    private val _projectsSearchQuery = MutableStateFlow("")
    val projectsSearchQuery: StateFlow<String> = _projectsSearchQuery.asStateFlow()

    private val _lastExportedZipFile = MutableStateFlow<File?>(null)
    private val _lastZipFileName = MutableStateFlow("my-shop-project.zip")
    val lastZipFileName: StateFlow<String> = _lastZipFileName.asStateFlow()

    private val _lastZipReadableSize = MutableStateFlow("24.8 KB • Ready to download")
    val lastZipReadableSize: StateFlow<String> = _lastZipReadableSize.asStateFlow()

    private val _aiToolResult = MutableStateFlow<String?>(null)
    val aiToolResult: StateFlow<String?> = _aiToolResult.asStateFlow()

    private val _isRunningTool = MutableStateFlow(false)
    val isRunningTool: StateFlow<Boolean> = _isRunningTool.asStateFlow()

    // Multi-Provider AI Key Vault & Auto-Switch Router State
    private val _storedAiKeys = MutableStateFlow<List<StoredAiKey>>(emptyList())
    val storedAiKeys: StateFlow<List<StoredAiKey>> = _storedAiKeys.asStateFlow()

    private val _autoSwitchEnabled = MutableStateFlow(true)
    val autoSwitchEnabled: StateFlow<Boolean> = _autoSwitchEnabled.asStateFlow()

    private val _routingStrategy = MutableStateFlow(AiAutoRoutingStrategy.SMART_TASK_AND_FAILOVER)
    val routingStrategy: StateFlow<AiAutoRoutingStrategy> = _routingStrategy.asStateFlow()

    private val _activeProviderBadge = MutableStateFlow("Auto-Router ⚡")
    val activeProviderBadge: StateFlow<String> = _activeProviderBadge.asStateFlow()

    private val _lastAutoSwitchEvent = MutableStateFlow<String?>(null)
    val lastAutoSwitchEvent: StateFlow<String?> = _lastAutoSwitchEvent.asStateFlow()

    // GitHub Direct Connect (Website GitHub Pages + Cloud APK Builder) State
    private val _gitHubAccountState = MutableStateFlow(GitHubAccountState())
    val gitHubAccountState: StateFlow<GitHubAccountState> = _gitHubAccountState.asStateFlow()

    private val _gitHubDeployResult = MutableStateFlow<GitHubDeployResult?>(null)
    val gitHubDeployResult: StateFlow<GitHubDeployResult?> = _gitHubDeployResult.asStateFlow()

    private val _isDeployingToGitHub = MutableStateFlow(false)
    val isDeployingToGitHub: StateFlow<Boolean> = _isDeployingToGitHub.asStateFlow()

    private val _gitHubDeployProgressText = MutableStateFlow<String?>(null)
    val gitHubDeployProgressText: StateFlow<String?> = _gitHubDeployProgressText.asStateFlow()

    private var hasLoadedInitialProject = false

    init {
        _storedAiKeys.value = AiKeyVaultManager.loadStoredKeys(appContext)
        _autoSwitchEnabled.value = AiKeyVaultManager.isAutoSwitchEnabled(appContext)
        _routingStrategy.value = AiKeyVaultManager.getRoutingStrategy(appContext)
        _gitHubAccountState.value = GitHubDeployService.getAccountState(appContext)
        refreshActiveProviderBadge()

        viewModelScope.launch {
            repository.ensureSeeded()
            repository.allProjects.collect { list ->
                if (!hasLoadedInitialProject && _activeProject.value == null && list.isNotEmpty()) {
                    hasLoadedInitialProject = true
                    loadProjectIntoWorkspace(list.first(), navigateToBuilder = false)
                }
            }
        }
    }

    fun connectGitHubToken(rawTokenInput: String) {
        if (rawTokenInput.isBlank()) return
        viewModelScope.launch {
            _gitHubDeployProgressText.value = "⏳ خەریکی پشکنینی تۆکنی GitHub و کۆنێکتکردنی هەژمارەکەتە..."
            val newState = GitHubDeployService.verifyAndSaveToken(appContext, rawTokenInput)
            _gitHubAccountState.value = newState
            _gitHubDeployProgressText.value = newState.statusMessageKu
        }
    }

    fun disconnectGitHubAccount() {
        _gitHubAccountState.value = GitHubDeployService.disconnectGitHub(appContext)
        _gitHubDeployResult.value = null
        _gitHubDeployProgressText.value = null
    }

    fun deployActiveProjectToGitHub(context: Context) {
        if (_isDeployingToGitHub.value) return
        viewModelScope.launch {
            _isDeployingToGitHub.value = true
            _gitHubDeployProgressText.value = "⏳ خەریکی ئامادەکردنی پڕۆژە و ناردنی ڕاستەوخۆیە بۆ GitHub..."
            try {
                val active = _activeProject.value
                val bp = _uiBlueprint.value
                val slug = active?.slug ?: bp.appTitle.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-').ifBlank { "baso-project" }
                val html = StarterTemplates.buildInteractivePreviewHtmlFromBlueprint(bp)
                val files = _projectFiles.value
                val result = GitHubDeployService.deployProjectToGitHub(
                    context = context,
                    projectSlug = slug,
                    blueprint = bp,
                    platform = _selectedPlatform.value,
                    previewHtml = html,
                    projectFiles = files,
                    onProgress = { stepMsg ->
                        _gitHubDeployProgressText.value = stepMsg
                    }
                )
                _gitHubDeployResult.value = result
                _gitHubDeployProgressText.value = result.messageKu
            } finally {
                _isDeployingToGitHub.value = false
            }
        }
    }

    fun downloadLatestApkOrOpenActions(context: Context) {
        val currentDeploy = _gitHubDeployResult.value
        if (currentDeploy == null || !currentDeploy.success) {
            deployActiveProjectToGitHub(context)
            return
        }
        viewModelScope.launch {
            _gitHubDeployProgressText.value = "⏳ پشکنینی لینکی ڕاستەوخۆی APK لە GitHub Releases..."
            val (ready, targetUrl) = GitHubDeployService.resolveLatestApkDownloadUrl(
                context = context,
                repoUrl = currentDeploy.repoUrl,
                fallbackDirectApkUrl = currentDeploy.directApkReleaseUrl,
                actionsUrl = currentDeploy.actionsBuildUrl
            )
            _gitHubDeployProgressText.value = if (ready) {
                "✅ فایلی APK ئامادەیە! خەریکی دابەزاندنی ڕاستەوخۆیە..."
            } else {
                "⚙️ GitHub Actions خەریکی دروستکردنی فایلی APKـە — پەڕەی دابەزاندن کرایەوە!"
            }
            GitHubDeployService.openUrlInBrowser(context, targetUrl)
        }
    }

    fun downloadStandaloneAppHtml(context: Context) {
        viewModelScope.launch {
            val active = _activeProject.value
            val bp = _uiBlueprint.value
            val slug = active?.slug ?: bp.appTitle.lowercase().replace(" ", "-")
            val html = StarterTemplates.buildInteractivePreviewHtmlFromBlueprint(bp)
            val file = ZipExporter.exportStandaloneAppHtml(context, slug, html)
            ZipExporter.shareStandaloneAppHtml(context, file)
        }
    }

    private fun refreshActiveProviderBadge() {
        val activeKeys = _storedAiKeys.value.filter { it.isEnabled }
        _activeProviderBadge.value = when {
            activeKeys.isEmpty() -> "BASO Neural ⚡"
            _autoSwitchEnabled.value && activeKeys.size > 1 -> "Auto (${activeKeys.size} AI) ⚡"
            else -> "${activeKeys.first().provider.shortBadge} ⚡"
        }
    }

    fun addOrImportAiKeys(
        rawKeyInput: String,
        customLabel: String = "",
        manualProviderOverride: AiProviderType? = null,
        preferredModelOverride: String? = null
    ): Int {
        val candidates = AiKeyVaultManager.parseBulkKeysFromText(rawKeyInput, manualProviderOverride)
        if (candidates.isEmpty()) return 0

        val currentList = _storedAiKeys.value.toMutableList()
        var addedCount = 0

        candidates.forEachIndexed { idx, candidate ->
            if (candidate.cleanKey.isNotBlank()) {
                val existingIdx = currentList.indexOfFirst { it.apiKey == candidate.cleanKey }
                val labelToUse = if (customLabel.isNotBlank() && candidates.size == 1) {
                    customLabel
                } else {
                    candidate.suggestedLabel
                }
                val modelToUse = preferredModelOverride?.takeIf { it.isNotBlank() }
                    ?: candidate.provider.defaultModel

                val newEntry = StoredAiKey(
                    id = "key_${System.currentTimeMillis()}_$idx",
                    provider = candidate.provider,
                    label = labelToUse,
                    apiKey = candidate.cleanKey,
                    preferredModel = modelToUse,
                    isEnabled = true,
                    priorityOrder = currentList.size + 1,
                    lastStatusNote = candidate.confidenceNoteKu
                )

                if (existingIdx >= 0) {
                    currentList[existingIdx] = newEntry.copy(id = currentList[existingIdx].id)
                } else {
                    currentList.add(0, newEntry)
                }
                addedCount++
            }
        }

        _storedAiKeys.value = currentList
        AiKeyVaultManager.saveStoredKeys(appContext, currentList)
        refreshActiveProviderBadge()
        _lastAutoSwitchEvent.value = "✅ $addedCount کلیلی نوێ خوێندرایەوە و خرایە ناو سیستەمی Auto-Switch"
        return addedCount
    }

    fun toggleAiKeyEnabled(keyId: String) {
        val updated = _storedAiKeys.value.map {
            if (it.id == keyId) it.copy(isEnabled = !it.isEnabled) else it
        }
        _storedAiKeys.value = updated
        AiKeyVaultManager.saveStoredKeys(appContext, updated)
        refreshActiveProviderBadge()
    }

    fun updateAiKeyModel(keyId: String, newModel: String) {
        val updated = _storedAiKeys.value.map {
            if (it.id == keyId) it.copy(preferredModel = newModel) else it
        }
        _storedAiKeys.value = updated
        AiKeyVaultManager.saveStoredKeys(appContext, updated)
        refreshActiveProviderBadge()
    }

    fun updateAiKeyProvider(keyId: String, newProvider: AiProviderType) {
        val updated = _storedAiKeys.value.map {
            if (it.id == keyId) {
                it.copy(
                    provider = newProvider,
                    preferredModel = newProvider.defaultModel,
                    label = "${newProvider.shortBadge} (${AiKeyVaultManager.maskKey(it.apiKey)})"
                )
            } else {
                it
            }
        }
        _storedAiKeys.value = updated
        AiKeyVaultManager.saveStoredKeys(appContext, updated)
        refreshActiveProviderBadge()
    }

    fun deleteAiKey(keyId: String) {
        val updated = _storedAiKeys.value.filterNot { it.id == keyId }
        _storedAiKeys.value = updated
        AiKeyVaultManager.saveStoredKeys(appContext, updated)
        refreshActiveProviderBadge()
    }

    fun promoteAiKeyPriority(keyId: String) {
        val current = _storedAiKeys.value.toMutableList()
        val index = current.indexOfFirst { it.id == keyId }
        if (index > 0) {
            val item = current.removeAt(index)
            current.add(0, item)
            _storedAiKeys.value = current
            AiKeyVaultManager.saveStoredKeys(appContext, current)
            refreshActiveProviderBadge()
            _lastAutoSwitchEvent.value = "⭐ پێشینەی کلیل درا بە ${item.provider.shortBadge} (${item.preferredModel})"
        }
    }

    fun testAndVerifyAiKey(keyId: String) {
        val target = _storedAiKeys.value.firstOrNull { it.id == keyId } ?: return
        viewModelScope.launch {
            val updating = _storedAiKeys.value.map {
                if (it.id == keyId) it.copy(lastStatusNote = "⏳ خەریکی پشکنینی پەیوەندییە...") else it
            }
            _storedAiKeys.value = updating
            val (_, statusText) = GeminiStudioService.verifySingleAiKey(target)
            val finished = _storedAiKeys.value.map {
                if (it.id == keyId) it.copy(lastStatusNote = statusText) else it
            }
            _storedAiKeys.value = finished
            AiKeyVaultManager.saveStoredKeys(appContext, finished)
        }
    }

    fun setAutoSwitchEnabled(enabled: Boolean) {
        _autoSwitchEnabled.value = enabled
        AiKeyVaultManager.setAutoSwitchEnabled(appContext, enabled)
        refreshActiveProviderBadge()
    }

    fun setRoutingStrategy(strategy: AiAutoRoutingStrategy) {
        _routingStrategy.value = strategy
        AiKeyVaultManager.setRoutingStrategy(appContext, strategy)
        refreshActiveProviderBadge()
    }

    fun openAiKeysVaultScreen() {
        _bottomNavTab.value = BottomNavTab.SETTINGS
        _currentScreen.value = StudioScreen.MAIN_HUB
    }

    fun enterStudioFromSplash() {
        _currentScreen.value = StudioScreen.MAIN_HUB
        _bottomNavTab.value = BottomNavTab.HOME
    }

    fun selectBottomNavTab(tab: BottomNavTab) {
        _bottomNavTab.value = tab
        _currentScreen.value = StudioScreen.MAIN_HUB
    }

    fun openNewCreationSelector() {
        _currentScreen.value = StudioScreen.NEW_CREATION_SELECTOR
    }

    fun startNewProjectWithCategory(platform: PlatformType, defaultPrompt: String? = null) {
        hasLoadedInitialProject = true
        _selectedPlatform.value = platform
        _activeProject.value = null
        _gitHubDeployResult.value = null
        val starterBp = if (!defaultPrompt.isNullOrBlank()) {
            StarterTemplates.evolveBlueprintFromChat(defaultPrompt, null, false).blueprint
        } else {
            StarterTemplates.buildBaseBlueprintForArchetype(
                "CUSTOM_STUDIO",
                "پڕۆژەی نوێ"
            )
        }
        _uiBlueprint.value = starterBp
        syncDesignTokensFromBlueprint(starterBp)
        val generatedFiles = StarterTemplates.generateFilesFromBlueprint(starterBp)
        _projectFiles.value = generatedFiles
        _selectedFile.value = generatedFiles.firstOrNull()
        _chatMessages.value = listOf(
            ChatMessage(
                id = "welcome_${System.currentTimeMillis()}",
                isUser = false,
                text = "سڵاو باسۆ! 👋 من ئامادەم بۆ دروستکردن و دیزاینکردنی پڕۆژەی نوێ بۆ (${platform.labelEn}).\n" +
                    "هەر فەرمان یان بیرۆکەیەکت هەیە بینووسە (بۆ نموونە: بژمێر و دراو، یاری X-O، ئەرک و تێبینی، کەشوهەوا، قورئان و تەسبیح، میوزیک پلەیەر، پێشانگای ئۆتۆمبێل، چێشتخانە، داشبۆردی بانک، فرۆشگا، یان هەر سیستەمێکی تایبەت) و ڕاستەوخۆ لە پێشینە تاقیدەکەیتەوە!",
                versionNumber = 1,
                appliedChanges = listOf("ئامادەکردنی ژینگەی ستۆدیۆ بۆ فەرمانی نوێ"),
                blueprintSnapshot = starterBp,
                providerUsedBadge = _activeProviderBadge.value
            )
        )
        _promptText.value = defaultPrompt ?: ""
        _builderTab.value = if (!defaultPrompt.isNullOrBlank()) BuilderTab.PREVIEW else BuilderTab.PROMPT
        _currentScreen.value = StudioScreen.AI_BUILDER
    }

    fun openAiBuilderTab(tab: BuilderTab) {
        _builderTab.value = tab
        _currentScreen.value = StudioScreen.AI_BUILDER
    }

    fun openBuildSuccessScreen(context: Context) {
        viewModelScope.launch {
            val active = _activeProject.value
            val bp = _uiBlueprint.value
            val slug = active?.slug ?: bp.appTitle.lowercase().replace(" ", "-")
            val html = StarterTemplates.buildInteractivePreviewHtmlFromBlueprint(bp)
            val files = _projectFiles.value
            val result = ZipExporter.createProjectZip(context, slug, html, files)
            _lastExportedZipFile.value = result.zipFile
            _lastZipFileName.value = result.fileName
            _lastZipReadableSize.value = result.readableSize
            _currentScreen.value = StudioScreen.BUILD_SUCCESS_ZIP
        }
    }

    fun downloadOrShareCurrentZip(context: Context) {
        viewModelScope.launch {
            val existing = _lastExportedZipFile.value
            if (existing != null && existing.exists()) {
                ZipExporter.shareOrSaveZipFile(context, existing)
            } else {
                val bp = _uiBlueprint.value
                val slug = _activeProject.value?.slug ?: bp.appTitle.lowercase().replace(" ", "-")
                val html = StarterTemplates.buildInteractivePreviewHtmlFromBlueprint(bp)
                val result = ZipExporter.createProjectZip(context, slug, html, _projectFiles.value)
                _lastExportedZipFile.value = result.zipFile
                _lastZipFileName.value = result.fileName
                _lastZipReadableSize.value = result.readableSize
                ZipExporter.shareOrSaveZipFile(context, result.zipFile)
            }
        }
    }

    fun navigateBack(): Boolean {
        return when (_currentScreen.value) {
            StudioScreen.BUILD_SUCCESS_ZIP -> {
                _currentScreen.value = StudioScreen.AI_BUILDER
                true
            }
            StudioScreen.AI_BUILDER -> {
                _currentScreen.value = StudioScreen.MAIN_HUB
                true
            }
            StudioScreen.NEW_CREATION_SELECTOR -> {
                _currentScreen.value = StudioScreen.MAIN_HUB
                true
            }
            StudioScreen.MAIN_HUB -> {
                if (_bottomNavTab.value != BottomNavTab.HOME) {
                    _bottomNavTab.value = BottomNavTab.HOME
                    true
                } else {
                    false
                }
            }
            StudioScreen.SPLASH -> false
        }
    }

    fun updatePromptText(value: String) {
        _promptText.value = value
    }

    fun selectPlatform(platform: PlatformType) {
        _selectedPlatform.value = platform
    }

    fun selectModel(modelId: String) {
        _selectedModel.value = modelId
    }

    fun updateTemperature(value: Float) {
        _temperature.value = value
    }

    fun selectStylePreset(preset: String) {
        _stylePreset.value = preset
    }

    fun selectBuilderTab(tab: BuilderTab) {
        _builderTab.value = tab
    }

    fun selectCodeFilterTab(tab: String) {
        _codeFilterTab.value = tab
    }

    fun selectProjectFile(file: ProjectFile) {
        _selectedFile.value = file
    }

    fun setDeviceViewport(viewport: DeviceViewport) {
        _deviceViewport.value = viewport
    }

    fun toggleFullScreenPreview(enabled: Boolean) {
        _isFullScreenPreview.value = enabled
    }

    fun setProjectsFilter(filter: String) {
        _projectsFilter.value = filter
    }

    fun setProjectsSearchQuery(query: String) {
        _projectsSearchQuery.value = query
    }

    fun toggleLanguage() {
        _language.value = if (_language.value == AppLanguage.KURDISH) {
            AppLanguage.ENGLISH
        } else {
            AppLanguage.KURDISH
        }
    }

    fun setLanguage(lang: AppLanguage) {
        _language.value = lang
    }

    fun loadProjectIntoWorkspace(project: BasoProjectEntity, navigateToBuilder: Boolean = true) {
        _activeProject.value = project
        _promptText.value = ""
        _selectedModel.value = project.modelId
        _temperature.value = project.temperature
        _stylePreset.value = project.stylePreset
        _selectedPlatform.value = when (project.platform) {
            "Android" -> PlatformType.ANDROID
            "Web App" -> PlatformType.WEB_APP
            else -> PlatformType.WEB
        }

        val parsedBp = try {
            json.decodeFromString<DynamicUiBlueprint>(project.designTokensJson)
        } catch (_: Exception) {
            StarterTemplates.evolveBlueprintFromChat(project.prompt, null, false).blueprint
        }
        _uiBlueprint.value = parsedBp
        syncDesignTokensFromBlueprint(parsedBp)

        val parsedFiles = try {
            json.decodeFromString<List<ProjectFile>>(project.filesJson)
        } catch (_: Exception) {
            StarterTemplates.generateFilesFromBlueprint(parsedBp)
        }
        _projectFiles.value = parsedFiles
        _selectedFile.value = parsedFiles.firstOrNull()

        val parsedChat = try {
            json.decodeFromString<List<ChatMessage>>(project.historyJson)
        } catch (_: Exception) {
            emptyList()
        }.ifEmpty {
            listOf(
                ChatMessage(
                    id = "seed_user_${project.id}",
                    isUser = true,
                    text = project.prompt,
                    versionNumber = 1
                ),
                ChatMessage(
                    id = "seed_ai_${project.id}",
                    isUser = false,
                    text = "✅ پڕۆژەی «${project.title}» ئامادەیە. دەتوانیت لێرە لە چاتەکەدا داوای هەر گۆڕانکارییەک بکەیت!",
                    versionNumber = 1,
                    appliedChanges = listOf("ئامادەکردنی دیزاینی ${parsedBp.layoutArchetype}"),
                    blueprintSnapshot = parsedBp,
                    providerUsedBadge = _activeProviderBadge.value
                )
            )
        }
        _chatMessages.value = parsedChat

        _lastZipFileName.value = "${project.slug}.zip"
        _lastZipReadableSize.value = project.zipSizeLabel

        if (navigateToBuilder) {
            _builderTab.value = BuilderTab.PREVIEW
            _currentScreen.value = StudioScreen.AI_BUILDER
        }
    }

    private fun syncDesignTokensFromBlueprint(bp: DynamicUiBlueprint) {
        _designTokens.value = DesignTokens(
            primaryHex = bp.primaryHex,
            accentHex = bp.secondaryHex,
            backgroundHex = bp.backgroundHex,
            cardRadiusPx = bp.cardRadiusPx,
            darkModePreview = bp.isDark,
            previewTitle = bp.appTitle
        )
    }

    fun updateBlueprintDirectly(newBp: DynamicUiBlueprint, changeNote: String = "دەستکاریکردنی ڕاستەوخۆی دیزاین") {
        _uiBlueprint.value = newBp
        syncDesignTokensFromBlueprint(newBp)
        val newFiles = StarterTemplates.generateFilesFromBlueprint(newBp)
        val newHtml = StarterTemplates.buildInteractivePreviewHtmlFromBlueprint(newBp)
        _projectFiles.value = newFiles
        _selectedFile.value = newFiles.firstOrNull()

        val nextVersion = (_chatMessages.value.maxOfOrNull { it.versionNumber } ?: 1) + 1
        val updatedChat = _chatMessages.value + ChatMessage(
            id = "direct_${System.currentTimeMillis()}",
            isUser = false,
            text = "🎨 $changeNote (v$nextVersion)",
            versionNumber = nextVersion,
            appliedChanges = listOf(changeNote),
            blueprintSnapshot = newBp,
            providerUsedBadge = "Direct Live Edit"
        )
        _chatMessages.value = updatedChat

        val current = _activeProject.value ?: return
        val updatedProject = current.copy(
            title = newBp.appTitle,
            previewHtml = newHtml,
            filesJson = json.encodeToString(newFiles),
            designTokensJson = json.encodeToString(newBp),
            historyJson = json.encodeToString(updatedChat),
            updatedAt = System.currentTimeMillis()
        )
        _activeProject.value = updatedProject
        viewModelScope.launch {
            repository.updateProject(updatedProject)
        }
    }

    fun updateDesignTokens(newTokens: DesignTokens) {
        val currentBp = _uiBlueprint.value
        val updatedBp = currentBp.copy(
            appTitle = newTokens.previewTitle.ifBlank { currentBp.appTitle },
            primaryHex = newTokens.primaryHex,
            secondaryHex = newTokens.accentHex,
            isDark = newTokens.darkModePreview,
            backgroundHex = if (newTokens.darkModePreview) "#08121C" else "#F4F6F9",
            surfaceHex = if (newTokens.darkModePreview) "#112233" else "#FFFFFF",
            cardRadiusPx = newTokens.cardRadiusPx
        )
        updateBlueprintDirectly(updatedBp, "گۆڕینی ڕەنگ و شێوازی دیزاین")
    }

    fun restoreBlueprintVersion(snapshot: DynamicUiBlueprint, versionNumber: Int) {
        _uiBlueprint.value = snapshot
        syncDesignTokensFromBlueprint(snapshot)
        val files = StarterTemplates.generateFilesFromBlueprint(snapshot)
        _projectFiles.value = files
        _selectedFile.value = files.firstOrNull()
        _statusBanner.value = "گەڕایەوە بۆ وەشانی v$versionNumber (${snapshot.appTitle})"
        _builderTab.value = BuilderTab.PREVIEW
    }

    fun sendChatMessage(
        context: Context,
        messageOverride: String? = null,
        switchToPreviewOnFinish: Boolean = false
    ) {
        val msg = (messageOverride ?: _promptText.value).trim()
        if (msg.isEmpty() || _isGenerating.value) return

        viewModelScope.launch {
            _isGenerating.value = true
            if (messageOverride == null) {
                _promptText.value = ""
            }

            val currentVersion = (_chatMessages.value.maxOfOrNull { it.versionNumber } ?: 0) + 1
            val userChatMsg = ChatMessage(
                id = "u_${System.currentTimeMillis()}",
                isUser = true,
                text = msg,
                versionNumber = currentVersion
            )
            _chatMessages.value = _chatMessages.value + userChatMsg
            _statusBanner.value = "ڕاوتەری زیرەکی BASO خەریکی هەڵبژاردنی باشترین AI و جێبەجێکردنە..."

            try {
                val platformStr = _selectedPlatform.value.labelEn
                val isIterative = _activeProject.value != null || _chatMessages.value.size > 2

                val intentDecision = AiKeyVaultManager.analyzePromptIntent(
                    rawPrompt = msg,
                    isIterativeEdit = isIterative,
                    storedKeys = _storedAiKeys.value
                )
                _activeProviderBadge.value = intentDecision.badgeTitleKu
                _statusBanner.value = intentDecision.explanationKu

                val orderedChain = AiKeyVaultManager.buildOrderedKeyChain(
                    context = appContext,
                    allKeys = _storedAiKeys.value,
                    autoSwitch = _autoSwitchEnabled.value,
                    strategy = _routingStrategy.value,
                    userMessage = msg,
                    isIterativeQuickEdit = isIterative
                )

                val bundle = GeminiStudioService.generateOrEditViaChat(
                    userMessage = msg,
                    platform = platformStr,
                    currentBlueprint = _uiBlueprint.value,
                    isIterativeEdit = isIterative,
                    modelId = intentDecision.selectedModel,
                    temperature = _temperature.value,
                    stylePreset = _stylePreset.value,
                    keyChain = orderedChain,
                    autoSwitchEnabled = _autoSwitchEnabled.value,
                    intentDecision = intentDecision
                )

                // Update key usage statistics & failover counts
                if (bundle.usedKeyId != null || bundle.failedKeyIds.isNotEmpty()) {
                    val updatedKeys = _storedAiKeys.value.map { key ->
                        when {
                            key.id == bundle.usedKeyId -> key.copy(
                                successCount = key.successCount + 1,
                                lastStatusNote = "✅ دوایین وەڵام سەرکەوتوو بوو"
                            )
                            bundle.failedKeyIds.contains(key.id) -> key.copy(
                                failoverCount = key.failoverCount + 1,
                                lastStatusNote = "⚡ سیستەم خۆکارانە لەم کلیلەوە گۆڕی بۆ کلیلێکی تر"
                            )
                            else -> key
                        }
                    }
                    _storedAiKeys.value = updatedKeys
                    AiKeyVaultManager.saveStoredKeys(appContext, updatedKeys)
                }

                _activeProviderBadge.value = bundle.providerUsedBadge
                if (bundle.autoSwitchedNote != null) {
                    _lastAutoSwitchEvent.value = bundle.autoSwitchedNote
                }

                _uiBlueprint.value = bundle.blueprint
                syncDesignTokensFromBlueprint(bundle.blueprint)
                _projectFiles.value = bundle.files
                _selectedFile.value = bundle.files.firstOrNull()

                val aiChatMsg = ChatMessage(
                    id = "ai_${System.currentTimeMillis()}",
                    isUser = false,
                    text = bundle.summary,
                    versionNumber = currentVersion,
                    appliedChanges = bundle.appliedChanges,
                    blueprintSnapshot = bundle.blueprint,
                    providerUsedBadge = bundle.providerUsedBadge,
                    autoSwitchedNote = bundle.autoSwitchedNote
                )
                val updatedHistory = _chatMessages.value + aiChatMsg
                _chatMessages.value = updatedHistory

                val slug = bundle.title.lowercase()
                    .replace(Regex("[^a-z0-9]+"), "-")
                    .trim('-')
                    .ifBlank { "baso-project" } + "-project"

                val zipResult = ZipExporter.createProjectZip(
                    context = context,
                    projectSlug = slug,
                    previewHtml = bundle.previewHtml,
                    files = bundle.files
                )
                _lastExportedZipFile.value = zipResult.zipFile
                _lastZipFileName.value = zipResult.fileName
                _lastZipReadableSize.value = zipResult.readableSize

                val existingProject = _activeProject.value
                val projectEntity = if (existingProject != null) {
                    existingProject.copy(
                        title = bundle.blueprint.appTitle,
                        slug = slug,
                        platform = platformStr,
                        prompt = msg,
                        modelId = bundle.providerUsedBadge,
                        temperature = _temperature.value,
                        stylePreset = _stylePreset.value,
                        previewUrl = "https://preview.baso.app/$slug",
                        previewHtml = bundle.previewHtml,
                        filesJson = json.encodeToString(bundle.files),
                        designTokensJson = json.encodeToString(bundle.blueprint),
                        historyJson = json.encodeToString(updatedHistory),
                        zipSizeLabel = zipResult.readableSize,
                        updatedLabelKu = "$platformStr • v$currentVersion • ${bundle.providerUsedBadge}",
                        updatedLabelEn = "$platformStr • v$currentVersion • ${bundle.providerUsedBadge}",
                        updatedAt = System.currentTimeMillis()
                    )
                } else {
                    BasoProjectEntity(
                        title = bundle.blueprint.appTitle,
                        slug = slug,
                        platform = platformStr,
                        prompt = msg,
                        modelId = bundle.providerUsedBadge,
                        temperature = _temperature.value,
                        stylePreset = _stylePreset.value,
                        previewUrl = "https://preview.baso.app/$slug",
                        previewHtml = bundle.previewHtml,
                        filesJson = json.encodeToString(bundle.files),
                        designTokensJson = json.encodeToString(bundle.blueprint),
                        historyJson = json.encodeToString(updatedHistory),
                        zipSizeLabel = zipResult.readableSize,
                        updatedLabelKu = "$platformStr • v$currentVersion • ${bundle.providerUsedBadge}",
                        updatedLabelEn = "$platformStr • v$currentVersion • ${bundle.providerUsedBadge}",
                        updatedAt = System.currentTimeMillis()
                    )
                }

                val savedId = repository.saveProject(projectEntity)
                _activeProject.value = projectEntity.copy(id = savedId)
                _statusBanner.value = "وەشانی v$currentVersion ئامادەیە (${bundle.providerUsedBadge})"

                if (switchToPreviewOnFinish) {
                    _builderTab.value = BuilderTab.PREVIEW
                }
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun generateProjectWithAi(context: Context) {
        sendChatMessage(context = context, messageOverride = null, switchToPreviewOnFinish = true)
    }

    fun executeStudioAiTool(toolType: String, prompt: String) {
        if (_isRunningTool.value) return
        viewModelScope.launch {
            _isRunningTool.value = true
            try {
                val orderedChain = AiKeyVaultManager.buildOrderedKeyChain(
                    context = appContext,
                    allKeys = _storedAiKeys.value,
                    autoSwitch = _autoSwitchEnabled.value,
                    strategy = _routingStrategy.value,
                    userMessage = prompt,
                    isIterativeQuickEdit = false
                )
                val res = GeminiStudioService.runStudioAiTool(
                    toolType = toolType,
                    inputPrompt = prompt,
                    modelId = _selectedModel.value,
                    keyChain = orderedChain,
                    autoSwitchEnabled = _autoSwitchEnabled.value
                )
                _aiToolResult.value = res
            } finally {
                _isRunningTool.value = false
            }
        }
    }
}
