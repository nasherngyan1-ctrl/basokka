package com.example.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
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
import java.util.concurrent.TimeUnit

@Serializable
data class GitHubAccountState(
    val isConnected: Boolean = false,
    val username: String = "",
    val displayName: String = "",
    val profileUrl: String = "",
    val tokenMasked: String = "",
    val statusMessageKu: String = "کۆنێکت نەکراوە بە GitHub"
)

@Serializable
data class GitHubDeployResult(
    val success: Boolean,
    val repoName: String = "",
    val repoUrl: String = "",
    val liveWebsiteUrl: String = "",
    val actionsBuildUrl: String = "",
    val directApkReleaseUrl: String = "",
    val isApkBuildTriggered: Boolean = false,
    val isPagesEnabled: Boolean = false,
    val messageKu: String = "",
    val uploadedFilesCount: Int = 0
)

object GitHubDeployService {

    private const val PREFS_NAME = "baso_github_prefs"
    private const val KEY_GITHUB_TOKEN = "github_pat_token"
    private const val KEY_GITHUB_USERNAME = "github_username"
    private const val KEY_GITHUB_DISPLAY_NAME = "github_display_name"
    private const val KEY_LAST_REPO_URL = "last_repo_url"
    private const val KEY_LAST_PAGES_URL = "last_pages_url"
    private const val KEY_LAST_APK_URL = "last_apk_url"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(40, TimeUnit.SECONDS)
        .writeTimeout(40, TimeUnit.SECONDS)
        .build()

    fun getSavedToken(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_GITHUB_TOKEN, "")?.trim() ?: ""
    }

    fun getAccountState(context: Context): GitHubAccountState {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val token = prefs.getString(KEY_GITHUB_TOKEN, "")?.trim() ?: ""
        val username = prefs.getString(KEY_GITHUB_USERNAME, "")?.trim() ?: ""
        val displayName = prefs.getString(KEY_GITHUB_DISPLAY_NAME, "")?.trim() ?: ""
        if (token.isBlank()) {
            return GitHubAccountState(
                isConnected = false,
                statusMessageKu = "تۆکنی GitHub دانەنراوە — تکایە کۆنێکتی بکە بۆ بڵاوکردنەوەی ڕاستەوخۆ و دروستکردنی APK لە بەشی Actions"
            )
        }
        val masked = if (token.length > 10) {
            "${token.take(6)}••••${token.takeLast(4)}"
        } else {
            "••••••••"
        }
        return GitHubAccountState(
            isConnected = true,
            username = username.ifBlank { "GitHub User" },
            displayName = displayName.ifBlank { username },
            profileUrl = if (username.isNotBlank()) "https://github.com/$username" else "https://github.com",
            tokenMasked = masked,
            statusMessageKu = "✅ کۆنێکت کراوە بە هەژماری GitHub (@${username.ifBlank { "connected" }})"
        )
    }

    fun disconnectGitHub(context: Context): GitHubAccountState {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_GITHUB_TOKEN)
            .remove(KEY_GITHUB_USERNAME)
            .remove(KEY_GITHUB_DISPLAY_NAME)
            .apply()
        return getAccountState(context)
    }

    suspend fun verifyAndSaveToken(context: Context, rawTokenInput: String): GitHubAccountState =
        withContext(Dispatchers.IO) {
            val cleanToken = rawTokenInput.trim()
                .removePrefix("Bearer ")
                .removePrefix("token ")
                .trim()

            if (cleanToken.length < 10) {
                return@withContext GitHubAccountState(
                    isConnected = false,
                    statusMessageKu = "❌ تۆکنی GitHub دروست نییە (پێویستە بە ghp_ یان github_pat_ دەست پێبکات)"
                )
            }

            try {
                val req = Request.Builder()
                    .url("https://api.github.com/user")
                    .addHeader("Authorization", "Bearer $cleanToken")
                    .addHeader("Accept", "application/vnd.github+json")
                    .addHeader("X-GitHub-Api-Version", "2022-11-28")
                    .get()
                    .build()

                httpClient.newCall(req).execute().use { resp ->
                    val bodyStr = resp.body?.string().orEmpty()
                    if (!resp.isSuccessful) {
                        return@withContext GitHubAccountState(
                            isConnected = false,
                            statusMessageKu = "❌ تۆکنەکە کار ناکات (کۆدی هەڵە: ${resp.code}) — دڵنیابەرەوە لە مۆڵەتی repo و workflow"
                        )
                    }

                    val root = json.parseToJsonElement(bodyStr).jsonObject
                    val login = root["login"]?.jsonPrimitive?.content.orEmpty()
                    val name = root["name"]?.jsonPrimitive?.content ?: login

                    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                        .edit()
                        .putString(KEY_GITHUB_TOKEN, cleanToken)
                        .putString(KEY_GITHUB_USERNAME, login)
                        .putString(KEY_GITHUB_DISPLAY_NAME, name)
                        .apply()

                    val masked = if (cleanToken.length > 10) {
                        "${cleanToken.take(6)}••••${cleanToken.takeLast(4)}"
                    } else {
                        "••••••••"
                    }

                    GitHubAccountState(
                        isConnected = true,
                        username = login,
                        displayName = name,
                        profileUrl = "https://github.com/$login",
                        tokenMasked = masked,
                        statusMessageKu = "✅ بە سەرکەوتوویی کۆنێکت کرا بە @$login لە GitHub!"
                    )
                }
            } catch (e: Exception) {
                GitHubAccountState(
                    isConnected = false,
                    statusMessageKu = "❌ هەڵە لە کاتی پەیوەندیکردن بە GitHub: ${e.localizedMessage ?: "Network Error"}"
                )
            }
        }

    /**
     * Deploys the current BASO Studio project directly to the user's GitHub account:
     * - Creates repository (if not existing)
     * - Uploads complete Android Gradle build files (settings.gradle.kts, build.gradle.kts, app/build.gradle.kts,
     *   AndroidManifest.xml, MainActivity.kt, assets/index.html)
     * - Uploads .github/workflows/build-apk.yml and .github/workflows/deploy-pages.yml
     * - Triggers GitHub Actions workflow_dispatch immediately so the APK starts building in the Actions tab!
     */
    suspend fun deployProjectToGitHub(
        context: Context,
        projectSlug: String,
        blueprint: DynamicUiBlueprint,
        platform: PlatformType,
        previewHtml: String,
        projectFiles: List<ProjectFile>,
        onProgress: (String) -> Unit = {}
    ): GitHubDeployResult = withContext(Dispatchers.IO) {
        val token = getSavedToken(context)
        if (token.isBlank()) {
            return@withContext GitHubDeployResult(
                success = false,
                messageKu = "⚠️ تکایە سەرەتا تۆکنی GitHub (Personal Access Token) دابنێ بۆ کۆنێکتکردنی ڕاستەوخۆ."
            )
        }

        val repoName = projectSlug.lowercase()
            .replace(Regex("[^a-z0-9-_]"), "-")
            .replace(Regex("-+"), "-")
            .trim('-')
            .ifBlank { "baso-studio-project" }

        try {
            onProgress("١/٥ پشکنینی هەژماری GitHub...")
            val owner = ensureOwnerUsername(context, token)
            if (owner.isBlank()) {
                return@withContext GitHubDeployResult(
                    success = false,
                    messageKu = "❌ نەتوانرا زانیاری هەژماری GitHub بخوێنرێتەوە. تکایە تۆکنەکەت نوێ بکەرەوە."
                )
            }

            onProgress("٢/٥ دروستکردن و ئامادەکردنی ڕیپۆزیتۆری ($repoName)...")
            val repoCreatedOrExists = ensureRepositoryExists(
                token = token,
                owner = owner,
                repoName = repoName,
                description = "${blueprint.appTitle} — Built with BASO AI Studio (${platform.labelEn})"
            )
            if (!repoCreatedOrExists) {
                return@withContext GitHubDeployResult(
                    success = false,
                    messageKu = "❌ نەتوانرا ڕیپۆزیتۆری لە GitHub دروست بکرێت (دڵنیابەرەوە تۆکنەکەت مۆڵەتی 'repo' و 'workflow' هەیە)."
                )
            }

            onProgress("٣/٥ ئامادەکردنی پڕۆژەی تەواوی Android Gradle و کۆدەکان...")

            val filesToUpload = LinkedHashMap<String, String>()
            // 1. Root index.html so GitHub Pages works immediately out-of-the-box
            filesToUpload["index.html"] = previewHtml

            // 2. Complete standalone Android Gradle project files so GitHub Actions compiles APK cleanly
            val androidProjectMap = buildCompleteAndroidGradleFilesMap(
                repoSlug = repoName,
                appTitle = blueprint.appTitle,
                previewHtml = previewHtml
            )
            filesToUpload.putAll(androidProjectMap)

            // 3. All generated project files from Code Explorer
            projectFiles.forEach { pf ->
                val cleanPath = pf.path.trimStart('/')
                if (!cleanPath.startsWith(".github/workflows/")) {
                    filesToUpload[cleanPath] = pf.content
                }
            }

            // 4. README with direct links to Live Website & APK Download
            val livePagesUrl = "https://$owner.github.io/$repoName/"
            val directApkReleaseUrl = "https://github.com/$owner/$repoName/releases/latest/download/$repoName-debug.apk"
            val actionsUrl = "https://github.com/$owner/$repoName/actions"
            filesToUpload["README.md"] = buildRichRepoReadme(
                title = blueprint.appTitle,
                subtitle = blueprint.heroSubtitle,
                owner = owner,
                repoName = repoName,
                livePagesUrl = livePagesUrl,
                directApkReleaseUrl = directApkReleaseUrl,
                actionsUrl = actionsUrl
            )

            // 5. Upload GitHub Actions workflows LAST so the push event sees all Android source files ready
            filesToUpload[".github/workflows/deploy-pages.yml"] = buildGitHubPagesWorkflowYaml(blueprint.appTitle)
            filesToUpload[".github/workflows/build-apk.yml"] = buildGitHubApkReleaseWorkflowYaml(repoName, blueprint.appTitle)

            onProgress("٤/٥ ناردنی فایلەکان و فایلی Workflow (build-apk.yml) بۆ GitHub...")
            var uploadedCount = 0
            for ((path, content) in filesToUpload) {
                val ok = putFileToGitHubRepo(
                    token = token,
                    owner = owner,
                    repo = repoName,
                    path = path,
                    content = content,
                    commitMessage = "Deploy $path via BASO AI Builder"
                )
                if (ok) uploadedCount++
            }

            onProgress("٥/٥ خستنەکاری ڕاستەوخۆی GitHub Actions بۆ دروستکردنی APK و GitHub Pages...")
            val pagesEnabled = enableGitHubPages(token, owner, repoName)
            triggerWorkflowDispatch(token, owner, repoName, "build-apk.yml")
            triggerWorkflowDispatch(token, owner, repoName, "deploy-pages.yml")

            val repoUrl = "https://github.com/$owner/$repoName"
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_LAST_REPO_URL, repoUrl)
                .putString(KEY_LAST_PAGES_URL, livePagesUrl)
                .putString(KEY_LAST_APK_URL, directApkReleaseUrl)
                .apply()

            val summaryMsg = "✅ فایلی تەواوی Workflow (.github/workflows/build-apk.yml) و هەموو فایلەکانی پڕۆژەی Android نێردران بۆ GitHub! ئێستا لە بەشی Actions خەریکی دروستکردنی فایلی APKـە."

            GitHubDeployResult(
                success = true,
                repoName = repoName,
                repoUrl = repoUrl,
                liveWebsiteUrl = livePagesUrl,
                actionsBuildUrl = actionsUrl,
                directApkReleaseUrl = directApkReleaseUrl,
                isApkBuildTriggered = true,
                isPagesEnabled = pagesEnabled,
                messageKu = summaryMsg,
                uploadedFilesCount = uploadedCount
            )
        } catch (e: Exception) {
            GitHubDeployResult(
                success = false,
                messageKu = "❌ هەڵە لە کاتی ناردن بۆ GitHub: ${e.localizedMessage ?: "Unknown Error"}"
            )
        }
    }

    /**
     * Checks if the latest GitHub Release APK is ready to download, and returns the direct browser download URL.
     */
    suspend fun resolveLatestApkDownloadUrl(
        context: Context,
        repoUrl: String,
        fallbackDirectApkUrl: String,
        actionsUrl: String
    ): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val token = getSavedToken(context)
        val cleanPath = repoUrl.removePrefix("https://github.com/").trim('/')
        if (cleanPath.count { it == '/' } != 1) {
            return@withContext Pair(false, fallbackDirectApkUrl.ifBlank { actionsUrl })
        }
        try {
            val reqBuilder = Request.Builder()
                .url("https://api.github.com/repos/$cleanPath/releases/latest")
                .addHeader("Accept", "application/vnd.github+json")
            if (token.isNotBlank()) {
                reqBuilder.addHeader("Authorization", "Bearer $token")
            }
            httpClient.newCall(reqBuilder.build()).execute().use { resp ->
                if (resp.isSuccessful) {
                    val body = resp.body?.string().orEmpty()
                    val obj = json.parseToJsonElement(body).jsonObject
                    val assets = obj["assets"]?.jsonArray
                    val firstApkUrl = assets?.firstOrNull()?.jsonObject
                        ?.get("browser_download_url")?.jsonPrimitive?.content
                    if (!firstApkUrl.isNullOrBlank()) {
                        return@withContext Pair(true, firstApkUrl)
                    }
                }
            }
        } catch (_: Exception) {
        }
        Pair(false, actionsUrl.ifBlank { repoUrl })
    }

    fun openUrlInBrowser(context: Context, url: String) {
        if (url.isBlank()) return
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }

    private fun ensureOwnerUsername(context: Context, token: String): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val cached = prefs.getString(KEY_GITHUB_USERNAME, "")?.trim().orEmpty()
        if (cached.isNotBlank()) return cached

        val req = Request.Builder()
            .url("https://api.github.com/user")
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Accept", "application/vnd.github+json")
            .get()
            .build()

        httpClient.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) return ""
            val body = resp.body?.string().orEmpty()
            val login = json.parseToJsonElement(body).jsonObject["login"]?.jsonPrimitive?.content.orEmpty()
            if (login.isNotBlank()) {
                prefs.edit().putString(KEY_GITHUB_USERNAME, login).apply()
            }
            return login
        }
    }

    private fun ensureRepositoryExists(
        token: String,
        owner: String,
        repoName: String,
        description: String
    ): Boolean {
        val checkReq = Request.Builder()
            .url("https://api.github.com/repos/$owner/$repoName")
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Accept", "application/vnd.github+json")
            .get()
            .build()

        httpClient.newCall(checkReq).execute().use { checkResp ->
            if (checkResp.isSuccessful) return true
        }

        val createPayload = buildJsonObject {
            put("name", repoName)
            put("description", description)
            put("private", false)
            put("auto_init", true)
            put("has_issues", true)
            put("has_projects", false)
        }.toString()

        val createReq = Request.Builder()
            .url("https://api.github.com/user/repos")
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Accept", "application/vnd.github+json")
            .post(createPayload.toRequestBody("application/json".toMediaType()))
            .build()

        httpClient.newCall(createReq).execute().use { createResp ->
            return createResp.isSuccessful || createResp.code == 422
        }
    }

    private fun putFileToGitHubRepo(
        token: String,
        owner: String,
        repo: String,
        path: String,
        content: String,
        commitMessage: String
    ): Boolean {
        var existingSha: String? = null
        val getReq = Request.Builder()
            .url("https://api.github.com/repos/$owner/$repo/contents/$path")
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Accept", "application/vnd.github+json")
            .get()
            .build()

        try {
            httpClient.newCall(getReq).execute().use { resp ->
                if (resp.isSuccessful) {
                    val bodyStr = resp.body?.string().orEmpty()
                    existingSha = json.parseToJsonElement(bodyStr).jsonObject["sha"]?.jsonPrimitive?.content
                }
            }
        } catch (_: Exception) {
        }

        val encodedContent = Base64.encodeToString(content.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        val payload = buildJsonObject {
            put("message", commitMessage)
            put("content", encodedContent)
            if (!existingSha.isNullOrBlank()) {
                put("sha", existingSha)
            }
        }.toString()

        val putReq = Request.Builder()
            .url("https://api.github.com/repos/$owner/$repo/contents/$path")
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Accept", "application/vnd.github+json")
            .put(payload.toRequestBody("application/json".toMediaType()))
            .build()

        httpClient.newCall(putReq).execute().use { putResp ->
            return putResp.isSuccessful
        }
    }

    private fun triggerWorkflowDispatch(
        token: String,
        owner: String,
        repo: String,
        workflowFileName: String
    ): Boolean {
        val payload = buildJsonObject {
            put("ref", "main")
        }.toString()

        val req = Request.Builder()
            .url("https://api.github.com/repos/$owner/$repo/actions/workflows/$workflowFileName/dispatches")
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Accept", "application/vnd.github+json")
            .post(payload.toRequestBody("application/json".toMediaType()))
            .build()

        return try {
            httpClient.newCall(req).execute().use { resp ->
                resp.isSuccessful || resp.code == 204
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun enableGitHubPages(token: String, owner: String, repo: String): Boolean {
        val payload = buildJsonObject {
            putJsonObject("source") {
                put("branch", "main")
                put("path", "/")
            }
        }.toString()

        val req = Request.Builder()
            .url("https://api.github.com/repos/$owner/$repo/pages")
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Accept", "application/vnd.github+json")
            .post(payload.toRequestBody("application/json".toMediaType()))
            .build()

        return try {
            httpClient.newCall(req).execute().use { resp ->
                resp.isSuccessful || resp.code == 409
            }
        } catch (_: Exception) {
            false
        }
    }

    fun buildCompleteAndroidGradleFilesMap(
        repoSlug: String,
        appTitle: String,
        previewHtml: String
    ): Map<String, String> {
        val safePkgSuffix = repoSlug.lowercase().replace(Regex("[^a-z0-9]"), "").ifBlank { "basoapp" }
        val escapedTitle = appTitle.replace("\"", "").ifBlank { "BASO App" }
        return linkedMapOf(
            "settings.gradle.kts" to """
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
rootProject.name = "$repoSlug"
include(":app")
            """.trimIndent(),
            "build.gradle.kts" to """
plugins {
    id("com.android.application") version "8.7.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
}
            """.trimIndent(),
            "gradle.properties" to """
org.gradle.jvmargs=-Xmx4g -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
            """.trimIndent(),
            "app/build.gradle.kts" to """
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.baso.generated"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.baso.generated.$safePkgSuffix"
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
            """.trimIndent(),
            "app/src/main/AndroidManifest.xml" to """
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.INTERNET" />
    <application
        android:label="$escapedTitle"
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
            """.trimIndent(),
            "app/src/main/java/com/baso/generated/MainActivity.kt" to """
package com.baso.generated

import android.app.Activity
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val webView = WebView(this)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.webViewClient = WebViewClient()
        webView.loadUrl("file:///android_asset/index.html")
        setContentView(webView)
    }
}
            """.trimIndent(),
            "app/src/main/assets/index.html" to previewHtml
        )
    }

    fun buildGitHubPagesWorkflowYaml(appTitle: String): String = """
name: Deploy Website to GitHub Pages ($appTitle)

on:
  push:
    branches:
      - "**"
    tags-ignore:
      - "v*"
  workflow_dispatch:

permissions:
  contents: read
  pages: write
  id-token: write

concurrency:
  group: "pages"
  cancel-in-progress: false

jobs:
  deploy:
    environment:
      name: github-pages
      url: ${'$'}{{ steps.deployment.outputs.page_url }}
    runs-on: ubuntu-latest
    steps:
      - name: Checkout Repository
        uses: actions/checkout@v4

      - name: Setup GitHub Pages
        uses: actions/configure-pages@v5

      - name: Upload Website Artifact
        uses: actions/upload-pages-artifact@v3
        with:
          path: '.'

      - name: Deploy to GitHub Pages
        id: deployment
        uses: actions/deploy-pages@v4
""".trimIndent()

    /**
     * Returns the complete GitHub Actions workflow (.github/workflows/build-apk.yml)
     * that builds any project or BASO Studio itself into an installable APK in GitHub Actions.
     */
    fun buildGitHubApkReleaseWorkflowYaml(repoSlug: String, appTitle: String): String = """
name: BASO Cloud Android APK Builder ($appTitle)

on:
  push:
    branches:
      - "**"
    tags-ignore:
      - "v*"
  pull_request:
    branches:
      - "main"
      - "master"
  workflow_dispatch:

permissions:
  contents: write
  actions: read

jobs:
  build-android-apk:
    name: Build & Publish Installable Android APK
    runs-on: ubuntu-latest

    steps:
      - name: 1. Checkout Source Code
        uses: actions/checkout@v4

      - name: 2. Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: 3. Setup Android SDK
        uses: android-actions/setup-android@v3

      - name: 4. Accept Android SDK Licenses & Install Build Tools
        run: |
          yes | sdkmanager --licenses > /dev/null 2>&1 || true
          sdkmanager "platform-tools" "build-tools;34.0.0" "platforms;android-34" || true

      - name: 5. Setup Gradle
        uses: gradle/actions/setup-gradle@v4
        with:
          gradle-version: '8.9'

      - name: 6. Ensure Android Assets & Keystore Ready
        run: |
          mkdir -p app/src/main/assets
          if [ -f index.html ] && [ ! -f app/src/main/assets/index.html ]; then
            cp -f index.html app/src/main/assets/index.html
          fi

      - name: 7. Assemble Debug APK
        run: gradle assembleDebug --no-daemon --stacktrace

      - name: 8. Prepare APK Output & Job Summary
        run: |
          APK_FILE=${'$'}(find app/build/outputs/apk/debug -name "*.apk" | head -n 1)
          cp "${'$'}APK_FILE" $repoSlug-debug.apk
          APK_SIZE=${'$'}(du -h $repoSlug-debug.apk | cut -f1)
          echo "### ✅ فایلی APK ئامادەیە ($appTitle)" >> ${'$'}GITHUB_STEP_SUMMARY
          echo "- **فایل**: \`$repoSlug-debug.apk\` (${'$'}APK_SIZE)" >> ${'$'}GITHUB_STEP_SUMMARY
          echo "- لە خوارەوەی ئەم پەڕەیە لە بەشی **Artifacts** یان **Releases** دایبەزێنە." >> ${'$'}GITHUB_STEP_SUMMARY

      - name: 9. Upload Installable APK Artifact (Actions Tab)
        uses: actions/upload-artifact@v4
        with:
          name: $repoSlug-apk
          path: $repoSlug-debug.apk
          retention-days: 90
          if-no-files-found: error

      - name: 10. Publish Direct APK to GitHub Releases
        if: github.event_name != 'pull_request'
        uses: softprops/action-gh-release@v2
        with:
          tag_name: v1.0.${'$'}{{ github.run_number }}
          name: "$appTitle APK (Build #${'$'}{{ github.run_number }})"
          files: $repoSlug-debug.apk
          generate_release_notes: true
""".trimIndent()

    /**
     * Returns the exact root `.github/workflows/build-apk.yml` for the main BASO Studio Android app itself
     * (JDK 21 + Gradle 9.3.1 + Android SDK 36.1 + Keystore + Artifacts + Releases).
     */
    fun buildMainBasoStudioWorkflowYaml(): String = """
name: Build & Release Android APK (BASO Studio)

on:
  push:
    branches:
      - "**"
    tags-ignore:
      - "v*"
  pull_request:
    branches:
      - "main"
      - "master"
  workflow_dispatch:

permissions:
  contents: write
  actions: read

jobs:
  build-apk:
    name: Build Installable Android APK
    runs-on: ubuntu-latest

    steps:
      - name: 1. Checkout Repository
        uses: actions/checkout@v4

      - name: 2. Set up JDK 21 (Required for AGP 9.1.1 & Gradle 9.3.1)
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: 3. Setup Android SDK & Accept All Licenses
        uses: android-actions/setup-android@v3

      - name: 4. Install Android SDK Platform 36.1 & Build Tools
        run: |
          yes | sdkmanager --licenses > /dev/null 2>&1 || true
          sdkmanager "platform-tools" "build-tools;36.0.0" "platforms;android-36.1" || sdkmanager "platform-tools" "platforms;android-36" || true

      - name: 5. Setup Gradle 9.3.1
        uses: gradle/actions/setup-gradle@v4
        with:
          gradle-version: '9.3.1'

      - name: 6. Prepare .env Secrets File
        run: |
          if [ ! -f .env ]; then
            if [ -f .env.example ]; then
              cp .env.example .env
            else
              echo "GEMINI_API_KEY=" > .env
            fi
          fi
          if [ -n "${'$'}{{ secrets.GEMINI_API_KEY }}" ]; then
            echo "GEMINI_API_KEY=${'$'}{{ secrets.GEMINI_API_KEY }}" > .env
          fi

      - name: 7. Prepare Android Signing Keystores (debug.keystore)
        run: |
          if [ ! -f debug.keystore ]; then
            if [ -f debug.keystore.base64 ]; then
              tr -d '\n\r ' < debug.keystore.base64 | base64 -d > debug.keystore
            else
              keytool -genkeypair -v \
                -keystore debug.keystore \
                -alias androiddebugkey \
                -keyalg RSA \
                -keysize 2048 \
                -validity 10000 \
                -storepass android \
                -keypass android \
                -dname "CN=Android Debug,O=Android,C=US"
            fi
          fi

      - name: 8. Compile & Assemble Android APK
        run: gradle :app:assembleDebug --no-daemon --stacktrace

      - name: 9. Package & Verify APK Output
        run: |
          APK_PATH=${'$'}(find app/build/outputs/apk/debug -name "*.apk" | head -n 1)
          cp "${'$'}APK_PATH" BASO-Studio.apk
          APK_SIZE=${'$'}(du -h BASO-Studio.apk | cut -f1)
          echo "### ✅ فایلی APK بە سەرکەوتوویی دروست کرا (BASO Studio APK Ready)" >> ${'$'}GITHUB_STEP_SUMMARY
          echo "- **ناوی فایل**: \`BASO-Studio.apk\` (${'$'}APK_SIZE)" >> ${'$'}GITHUB_STEP_SUMMARY

      - name: 10. Upload APK to GitHub Actions Artifacts
        uses: actions/upload-artifact@v4
        with:
          name: BASO-Studio-APK
          path: BASO-Studio.apk
          retention-days: 90
          if-no-files-found: error

      - name: 11. Publish Direct APK to GitHub Releases
        if: github.event_name != 'pull_request'
        uses: softprops/action-gh-release@v2
        with:
          tag_name: v1.0.${'$'}{{ github.run_number }}
          name: "BASO Studio APK (Build #${'$'}{{ github.run_number }})"
          files: BASO-Studio.apk
          generate_release_notes: true
""".trimIndent()

    private fun buildRichRepoReadme(
        title: String,
        subtitle: String,
        owner: String,
        repoName: String,
        livePagesUrl: String,
        directApkReleaseUrl: String,
        actionsUrl: String
    ): String = """
# $title
> $subtitle

Generated and deployed directly by **BASO AI Builder (ستۆدیۆی فریلانسەر)**.

## 🌐 Live Website (GitHub Pages)
- **Live Website URL**: [$livePagesUrl]($livePagesUrl)

## 📲 Download Android APK (GitHub Actions & Releases)
- **Direct Latest APK Download**: [Download `$repoName-debug.apk`]($directApkReleaseUrl)
- **GitHub Actions Build Status & Artifacts**: [View APK Build Workflow]($actionsUrl)
""".trimIndent()
}
